package me.wiefferink.gocraft.inspector;

import com.google.common.base.Charsets;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.scheduling.Do;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.gocraft.tools.storage.UTF8Config;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class InspectionManager extends Feature {

	private Map<UUID, Inspection> currentInspections;
	private final GoCraft plugin;
	private UTF8Config inspectorStorage;
	private File inspectorsFile;
	private UpdateListener updateListener;
	private BukkitTask updateTask;

	public InspectionManager(final GoCraft plugin) {
		// Does not really make sense here, no better place for it now though
		permission("staff", "Indicates the player is staff", PermissionDefault.OP);
		permission("admin", "Indicates the player is an admin", PermissionDefault.OP);

		this.plugin = plugin;
		if(!getConfig().getBoolean("enableInspecting")) {
			return;
		}
		permission("inspect", "Inspect players to see if they are hacking");
		permission("joinInInspect", "Join the server in inspect mode", PermissionDefault.FALSE);
		command("inspect", "Inspect a player (for detecting hacks)", "/inspect [player]", "i", "in");

		plugin.setInspectionManager(this);
		inspectorsFile = new File(plugin.getDataFolder(), "inspectors.yml");
		currentInspections = new HashMap<>();
		setupListeners();
		loadInspectors();

		// Keep inspectors inside the map
		if (plugin.getMapSwitcherLink() != null) {
			Do.syncTimer(20, () -> {
				for(Inspection inspection : plugin.getInspectionManager().getCurrentInspections().values()) {
					if(!plugin.getMapSwitcherLink().get().isInsideCurrentMap(inspection.getInspector())) {
						plugin.message(inspection.getInspector(), "inspect-outsideMap");
						if(inspection.hasInspected()) {
							inspection.teleportToInspected();
						} else {
							inspection.getInspector().teleport(plugin.getMapSwitcherLink().get().getCurrentSpawnLocation());
						}
					}
				}
			});
		}

		// Deal with players that are already in the server (for example with reloads)
		for (Player player : Bukkit.getOnlinePlayers()) {
			handlePlayerJoined(player, true);
		}
	}

	/**
	 * Setup listeners
	 */
	public void setupListeners() {
		plugin.getServer().getPluginManager().registerEvents(new QuitJoinListener(plugin), plugin);
		plugin.getServer().getPluginManager().registerEvents(new InventoryListener(this), plugin);
	}

	/**
	 * Setup an inspection session
	 * @param inspector The player that is going to be inspecting
	 * @param inspected The player that is going to be inspected
	 * @return The inspection session
	 */
	public Inspection setupInspection(Player inspector, Player inspected) {
		return new Inspection(plugin, inspector, inspected);
	}

	/**
	 * Setup an inspection session without target
	 * @param inspector The player that is going to be inspecting
	 * @return The inspection session
	 */
	public Inspection setupInspection(Player inspector) {
		return new Inspection(plugin, inspector, null);
	}

	/**
	 * Start an inspection, add this inspection to the manager
	 * @param inspection Inspection to start and add
	 */
	public void addInspection(Inspection inspection) {
		currentInspections.put(inspection.getInspector().getUniqueId(), inspection);
	}

	/**
	 * End inspection
	 * @param inspection Inspection to end
	 */
	public void removeInspection(Inspection inspection) {
		currentInspections.remove(inspection.getInspector().getUniqueId());
	}

	/**
	 * Get an inspection session from the inspector
	 * @param inspector The inspector to check
	 * @return The inspection session that this inspector is in or null if none
	 */
	public Inspection getInspectionByInspector(Player inspector) {
		return currentInspections.get(inspector.getUniqueId());
	}

	/**
	 * Get the inspections that are ongoing about a player
	 * @param inspected The inspected player
	 * @return A set of all inspections going on on this player
	 */
	public Set<Inspection> getInspectionsByInspected(Player inspected) {
		Set<Inspection> result = new HashSet<>();
		for (Inspection inspection : currentInspections.values()) {
			if (inspection.isInspected(inspected)) {
				result.add(inspection);
			}
		}
		return result;
	}

	/**
	 * Get the current inspections
	 * @return A map of the inspector UUID to the inspection
	 */
	public Map<UUID, Inspection> getCurrentInspections() {
		return currentInspections;
	}

	/**
	 * Handle a leave/kick/etc. of a player
	 * @param player The player that is gone now
	 */
	public void handlePlayerStopped(final Player player) {
		if (player == null) {
			return;
		}
		// Player leaves next tick
		Do.sync(() -> {
			for(Inspection inspect : getInspectionsByInspected(player)) {
				plugin.message(inspect.getInspector(), "inspect-inspectedLeft", inspect.getInspected().getName());
				// Swap to offline inspection
				inspect.updateAll(true);
			}
		});
	}

	/**
	 * Handle a player joining the server
	 * @param player The player that joined the server
	 */
	public void handlePlayerJoined(final Player player, boolean noInspectJoin) {
		// Restore stored inspection state
		final Inspection inspection = restoreInspection(player);
		if (inspection != null) {
			Log.debug("Inspect: starting restored inspection on join for", player.getName());
			inspection.startInspection(true);
			if (!player.hasPermission("gocraft.staff")) {
				inspection.endInspection();
			} else {
				Do.syncLater(10, () -> {
					if(inspection.hasInspected()) {
						plugin.message(player, "inspect-restore", inspection.getInspected().getName());
					} else {
						plugin.message(player, "inspect-restoreNoTarget");
					}
				});
			}
			return;
		}

		// Join in inspect
		if (!noInspectJoin
				&& player.hasPermission("gocraft.staff")
				&& getConfig().getBoolean("staffJoinsInInspect")
				&& player.hasPermission("gocraft.joinInInspect")
				&& !player.isDead()) { // Prevent switching to inspect when killed by combat logging
			final Inspection finalInspection = setupInspection(player);
			final boolean inPVP = Utils.isInPvpArea(player);
			Log.debug("Inspect: starting join in inspect inspection for", player.getName());
			finalInspection.startInspection();
			plugin.increaseStatistic("command.inspect.restoredAtJoin");
			Do.syncLater(10, () -> {
				plugin.message(player, "inspect-joinEnable");
				if(inPVP) {
					plugin.message(player, "inspect-joinEnableInPVP");
				}
			});
			return;
		}

		// Player is target
		for (Inspection insp : getInspectionsByInspected(player)) {
			insp.updateAll(true);
		}

		async(() ->
			Database.run(session -> {
				GCPlayer gcPlayer = Database.getPlayer(player.getUniqueId(), player.getName());
				if(gcPlayer != null && gcPlayer.isInvisible()) {
					gcPlayer.setInvisible(false);
					session.update(gcPlayer);
					plugin.getSyncCommandsServer().runCommand("updatePlayers");
				}
			})
		);
	}

	/**
	 * Restore saved old inventory if that exists (could happen when server crashed while spectating)
	 * @param player Player to (possibly) restore an old inventory for
	 * @return The Inspection object created from the stored copy if there is one, otherwise null
	 */
	public Inspection restoreInspection(Player player) {
		if (!getInspectorStorage().contains(player.getUniqueId().toString())) {
			return null;
		}
		String baseKey = player.getUniqueId().toString() + ".";

		// Restore target
		String target = getInspectorStorage().getString(baseKey + "target");
		UUID targetUUID = null;
		Player inspected = null;
		if (target != null) {
			try {
				targetUUID = UUID.fromString(target);
			} catch (IllegalArgumentException ignore) {
			}
		}
		if (targetUUID != null) {
			inspected = Utils.loadPlayer(targetUUID);
		}
		Inspection result = new Inspection(plugin, player, inspected);

		// Restore health and food
		double health = getInspectorStorage().getDouble(baseKey + "health");
		if (health == 0) {
			health = player.getMaxHealth();
		}
		result.health = health;
		int food = getInspectorStorage().getInt(baseKey + "food");
		if (food == 0) {
			food = 20;
		}
		result.food = food;

		// Restore gamemode
		String gamemodeString = getInspectorStorage().getString(baseKey + "gamemode");
		GameMode gamemode = GameMode.valueOf(gamemodeString);
		result.gamemode = gamemode;
		// Restore inventory
		ItemStack[] inventory = player.getInventory().getContents();
		for (int i = 0; i < inventory.length; i++) {
			ItemStack item = getInspectorStorage().getItemStack(baseKey + "inventory." + i);
			inventory[i] = item;
		}
		result.inspectorInventory = inventory;
		// Restore armor
		ItemStack[] armor = player.getInventory().getArmorContents();
		for (int i = 0; i < armor.length; i++) {
			ItemStack item = getInspectorStorage().getItemStack(baseKey + "armor." + i);
			armor[i] = item;
		}
		result.inspectorArmor = armor;
		// Restore potion effects
		ConfigurationSection section = getInspectorStorage().getConfigurationSection(baseKey + "potioneffects");
		Collection<PotionEffect> effects = new HashSet<>();
		if (section != null) {
			for (String effectString : section.getKeys(false)) {
				PotionEffectType effect = PotionEffectType.getByName(effectString);
				String optionsString = section.getString(effectString);
				if (optionsString == null || effect == null) {
					Log.warn("InspectionManager.restoreInspection: potions of "+player.getName()+" no effect found for "+effectString);
					continue;
				}
				String[] options = optionsString.split(":");
				if (options.length < 4) {
					Log.warn("InspectionManager.restoreInspection: potions of "+player.getName()+" not enough options for "+effectString+", "+optionsString);
					continue;
				}
				int duration, amplifier;
				boolean ambient, particles;
				try {
					duration = Integer.parseInt(options[0]);
					amplifier = Integer.parseInt(options[1]);
				} catch (NumberFormatException e) {
					Log.warn("InspectionManager.restoreInspection: potions of "+player.getName()+" options are not numbers "+effectString+", "+optionsString);
					continue;
				}
				ambient = "true".equalsIgnoreCase(options[2]);
				particles = "true".equalsIgnoreCase(options[3]);
				//Log.debug("duration=" + duration + ", amplifier=" + amplifier + ", ambient=" + ambient + ", particles=" + particles);
				PotionEffect finalEffect = new PotionEffect(effect, duration, amplifier, ambient, particles);
				effects.add(finalEffect);
			}
		}
		result.potionEffects = effects;
		// Restore fly state
		result.allowFlight = getInspectorStorage().getBoolean(baseKey + "allowFlight");
		result.isFlying = getInspectorStorage().getBoolean(baseKey + "isFlying");
		// Restore location
		result.location = Utils.configToLocation(getInspectorStorage().getConfigurationSection(baseKey + "location"));
		Log.debug("Inspect: found stored inspection on disk for", player.getName(), "gamemode:", gamemode.toString());
		return result;
	}

	/**
	 * Check if a player is inspecting
	 * @param inspector The player to check
	 * @return true if the player is inspecting, otherwise false
	 */
	public boolean isInspecting(Player inspector) {
		return getInspectionByInspector(inspector) != null;
	}

	/**
	 * Register or deregister updater as needed
	 */
	public void registerUpdater() {
		if (currentInspections.isEmpty() && updateTask != null) {
			HandlerList.unregisterAll(updateListener);
			updateListener = null;
			updateTask.cancel();
			updateTask = null;
		} else if (!currentInspections.isEmpty() && updateTask == null) {
			updateListener = new UpdateListener(plugin);
			plugin.getServer().getPluginManager().registerEvents(updateListener, plugin);
			updateTask = Do.syncTimer(40, () -> {
				for(Inspection inspection : plugin.getInspectionManager().getCurrentInspections().values()) {
					inspection.updateAll();
				}
			});
		}
	}


	/**
	 * Load the default.yml file
	 */
	public void loadInspectors() {
		// Safe the file from the jar to disk if it does not exist
		if (inspectorsFile.exists()) {
			// Load inspectorStorage.yml from the plugin folder
			try (
					InputStreamReader reader = new InputStreamReader(new FileInputStream(inspectorsFile), Charsets.UTF_8)
			) {
				inspectorStorage = UTF8Config.loadConfiguration(reader);
			} catch (IOException e) {
				Log.warn("Loading the inspectory inventories failed: "+inspectorsFile.getAbsolutePath());
			}
		}
		if (inspectorStorage == null) {
			inspectorStorage = new UTF8Config();
		}
	}

	/**
	 * Save the inspectors config file
	 * @return true if the file has been saved correctly, otherwise false
	 */
	public boolean saveInspectors() {
		try {
			inspectorStorage.save(inspectorsFile);
		} catch (IOException e) {
			Log.warn("Could not save inspector storage file: "+inspectorsFile.toString());
			return false;
		}
		return true;
	}

	/**
	 * Get the inspectors config file
	 * @return The inspectors config file
	 */
	public YamlConfiguration getInspectorStorage() {
		return inspectorStorage;
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!(sender instanceof Player)) {
			plugin.message(sender, "general-playerOnly");
			return;
		}
		Player inspector = (Player)sender;
		if(!sender.hasPermission("gocraft.staff")) {
			plugin.message(inspector, "inspect-noPermission");
			return;
		}
		Inspection inspection = plugin.getInspectionManager().getInspectionByInspector(inspector);
		if(inspection != null && args.length == 0) {
			// Stop existing inspection
			inspection.endInspection();
			if(inspection.hasInspected()) {
				plugin.message(inspector, "inspect-ended", inspection.getInspected().getName());
			} else {
				plugin.message(inspector, "inspect-endedNoTarget");
			}
			return;
		}

		Player newTarget = null;
		if(args.length > 0) {
			newTarget = Utils.loadPlayer(args[0]);
			// Did not play before
			if(newTarget == null) {
				plugin.message(inspector, "inspect-notAvailable", args[0]);
				return;
			} else if(plugin.getInspectionManager().getInspectionByInspector(newTarget) != null) {
				// Trying to inspect an inspector
				plugin.message(inspector, "inspect-inspection", newTarget.getName());
				return;
			} else if(inspector.getUniqueId().equals(newTarget.getUniqueId())) {
				plugin.message(inspector, "inspect-self");
				return;
			}
		}
		if(inspection != null) {
			// From existing to new target
			inspection.switchToPlayer(newTarget, true);
			plugin.increaseStatistic("command.inspect.switchTarget");
			if(newTarget != null) {
				plugin.message(inspector, "inspect-started", newTarget.getName());
			} else {
				plugin.message(inspector, "inspect-startedNoTarget");
			}
			return;
		}

		// New inspection
		if(Utils.isInPvpArea(inspector) && inspector.getGameMode() == GameMode.SURVIVAL) {
			plugin.message(inspector, "inspect-inNonPVP");
			return;
		}

		// Start inspection
		inspection = plugin.getInspectionManager().setupInspection(inspector, newTarget);
		inspection.startInspection();
		Log.debug("Inspect: starting inspection by command for", inspector.getName());
		if(newTarget != null) {
			plugin.message(inspector, "inspect-started", newTarget.getName());
			plugin.increaseStatistic("command.inspect.withTarget");
		} else {
			plugin.message(inspector, "inspect-startedNoTarget");
			plugin.increaseStatistic("command.inspect.withoutTarget");
		}
	}

}
