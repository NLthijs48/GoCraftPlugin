package me.wiefferink.gocraft.inspector;

import com.google.common.base.Charsets;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.storage.UTF8Config;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class InspectionManager {

	private Map<UUID, Inspection> currentInspections;
	private final GoCraft plugin;
	private UTF8Config inspectorStorage;
	private File inspectorsFile;
	private boolean updaterRegistered = false;
	private UpdateListener updateListener;
	private BukkitRunnable updateTask;

	public InspectionManager(final GoCraft plugin) {
		this.plugin = plugin;
		plugin.setInspectionManager(this);
		inspectorsFile = new File(plugin.getDataFolder(), "inspectors.yml");
		currentInspections = new HashMap<>();
		setupListeners();
		loadInspectors();

		// Keep inspectors inside the map
		if (plugin.getMapSwitcherLink() != null) {
			new BukkitRunnable() {
				@Override
				public void run() {
					for (Inspection inspection : plugin.getInspectionManager().getCurrentInspections().values()) {
						if (!plugin.getMapSwitcherLink().get().isInsideCurrentMap(inspection.getInspector())) {
							plugin.message(inspection.getInspector(), "inspect-outsideMap");
							if (inspection.hasInspected()) {
								inspection.teleportToInspected();
							} else {
								inspection.getInspector().teleport(plugin.getMapSwitcherLink().get().getCurrentSpawnLocation());
							}
						}
					}
				}
			}.runTaskTimer(plugin, 20, 20);
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
		new BukkitRunnable() {
			@Override
			public void run() {
				for (Inspection inspect : getInspectionsByInspected(player)) {
					plugin.message(inspect.getInspector(), "inspect-inspectedLeft", inspect.getInspected().getName());
					// Swap to offline inspection
					inspect.updateAll(true);
				}
			}
		}.runTaskLater(plugin, 1L);
	}

	/**
	 * Handle a player joining the server
	 * @param player The player that joined the server
	 */
	public void handlePlayerJoined(final Player player, boolean noInspectJoin) {
		// Restore stored inspection state
		final Inspection inspection = restoreInspection(player);
		if (inspection != null) {
			inspection.startInspection(true);
			if (!player.hasPermission("gocraft.staff")) {
				inspection.endInspection();
			} else {
				new BukkitRunnable() {
					@Override
					public void run() {
						if (inspection.hasInspected()) {
							plugin.message(player, "inspect-restore", inspection.getInspected().getName());
						} else {
							plugin.message(player, "inspect-restoreNoTarget");
						}
					}
				}.runTaskLater(plugin, 10L);
			}
			return;
		}
		// Join in inspect
		if (!noInspectJoin
				&& player.hasPermission("gocraft.staff")
				&& plugin.getConfig().getBoolean("staffJoinsInInspect")
				&& player.hasPermission("gocraft.joinInInspect")
				&& !player.isDead()) { // Prevent switching to inspect when killed by combat logging
			final Inspection finalInspection = setupInspection(player);
			final boolean inPVP = Utils.isInPvpArea(player);
			finalInspection.startInspection();
			plugin.increaseStatistic("command.inspect.restoredAtJoin");
			new BukkitRunnable() {
				@Override
				public void run() {
					plugin.message(player, "inspect-joinEnable");
					if (inPVP) {
						plugin.message(player, "inspect-joinEnableInPVP");
					}
				}
			}.runTaskLater(plugin, 10L);
		}
		// Player is target
		for (Inspection insp : getInspectionsByInspected(player)) {
			insp.updateAll(true);
		}
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
		if (gamemode == null) {
			gamemode = GameMode.SURVIVAL;
		}
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
					GoCraft.warn("InspectionManager.restoreInspection: potions of "+player.getName()+" no effect found for "+effectString);
					continue;
				}
				String[] options = optionsString.split(":");
				if (options.length < 4) {
					GoCraft.warn("InspectionManager.restoreInspection: potions of "+player.getName()+" not enough options for "+effectString+", "+optionsString);
					continue;
				}
				int duration, amplifier;
				boolean ambient, particles;
				try {
					duration = Integer.parseInt(options[0]);
					amplifier = Integer.parseInt(options[1]);
				} catch (NumberFormatException e) {
					GoCraft.warn("InspectionManager.restoreInspection: potions of "+player.getName()+" options are not numbers "+effectString+", "+optionsString);
					continue;
				}
				ambient = "true".equalsIgnoreCase(options[2]);
				particles = "true".equalsIgnoreCase(options[3]);
				//GoCraft.debug("duration=" + duration + ", amplifier=" + amplifier + ", ambient=" + ambient + ", particles=" + particles);
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
		if (currentInspections.isEmpty() && updaterRegistered) {
			HandlerList.unregisterAll(updateListener);
			updaterRegistered = false;
			updateListener = null;
			updateTask.cancel();
			updateTask = null;
		} else if (!currentInspections.isEmpty() && !updaterRegistered) {
			updateListener = new UpdateListener(plugin);
			plugin.getServer().getPluginManager().registerEvents(updateListener, plugin);
			updaterRegistered = true;
			updateTask = new BukkitRunnable() {
				@Override
				public void run() {
					for (Inspection inspection : plugin.getInspectionManager().getCurrentInspections().values()) {
						inspection.updateAll();
					}
				}
			};
			updateTask.runTaskTimer(plugin, 40L, 40L);
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
				GoCraft.warn("Loading the inspectory inventories failed: "+inspectorsFile.getAbsolutePath());
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
			GoCraft.warn("Could not save inspector storage file: "+inspectorsFile.toString());
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

}
