package me.wiefferink.gocraft.inspector;

import me.wiefferink.bukkitdo.Do;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.inspector.actions.BanInfoAction;
import me.wiefferink.gocraft.inspector.actions.ChestAction;
import me.wiefferink.gocraft.inspector.actions.CompassAction;
import me.wiefferink.gocraft.inspector.actions.EnderchestAction;
import me.wiefferink.gocraft.inspector.actions.ExitAction;
import me.wiefferink.gocraft.inspector.actions.InventoryAction;
import me.wiefferink.gocraft.inspector.actions.KillAuraCheckAction;
import me.wiefferink.gocraft.inspector.actions.NCPAction;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.storage.Database;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Statistic;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inspection {

	private Player inspector;
	private Player inspected;
	// Things to restore later
	public ItemStack[] inspectorInventory;
	public ItemStack[] inspectorArmor;
	public Collection<PotionEffect> potionEffects;
	public GameMode gamemode;
	public GoCraft plugin;
	public boolean scoreboardType;
	public Map<Integer, InventoryAction> actions;
	public Location location;
	public boolean allowFlight;
	public boolean isFlying;
	public double health;
	public int food;

	public Inspection(GoCraft plugin, Player inspector, Player inspected) {
		this.plugin = plugin;
		this.inspector = inspector;
		this.inspected = inspected;
		this.scoreboardType = true;
	}

	public Inspection(GoCraft plugin, Player inspector) {
		this(plugin, inspector, null);
	}

	/**
	 * Get the player that is inspecting
	 * @return The inspecting player
	 */
	public Player getInspector() {
		return inspector;
	}

	/**
	 * Check if a player is the inspector of this inspection
	 * @param player The player to check
	 * @return true if the player is the inspector of this inspection, otherwise false
	 */
	public boolean isInspector(Player player) {
		return player != null && player.getUniqueId().equals(inspector.getUniqueId());
	}

	/**
	 * Get the inspected player
	 * @return The inspected player
	 */
	public Player getInspected() {
		return inspected;
	}

	/**
	 * Check if this inspection has a target
	 * @return true if this inspection has a target, otherwise false
	 */
	public boolean hasInspected() {
		return inspected != null;
	}

	/**
	 * Check if a player is the inspected of this inspection
	 * @param player The player to check
	 * @return true if the player is the inspected of this inspection, otherwise false
	 */
	public boolean isInspected(Player player) {
		return hasInspected() && player != null && player.getUniqueId().equals(inspected.getUniqueId());
	}

	/**
	 * Start the inspection
	 * @param restore true if this inspection is restored from disk, otherwise false
	 */
	public void startInspection(boolean restore) {
		Log.debug("Inspect:", inspector.getName(), " starts inspecting", inspected == null ? "nobody" : inspected.getName(), "restore:", restore, ", gamemode:", inspector.getGameMode().toString());
		plugin.getInspectionManager().addInspection(this);
		if (!restore && !saveInspectorState()) {
			// Ending inspection, because this is before most things apply we dont have to restore much
			plugin.getInspectionManager().removeInspection(this);
			return;
		}

		// Set health to max to prevent the player dying
		inspector.setHealth(inspector.getMaxHealth());
		inspector.setFoodLevel(20);

		// Turn on vanish and hide on DynMap
		if (plugin.getEssentialsLink() != null) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "essentials:vanish " + getInspector().getName() + " on");
		}
		if (plugin.dynMapInstalled()) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dynmap:dynmap hide " + getInspector().getName());
		}
		// Teleport to 1 block behind the direction the inspected player is looking at (third person like view)
		this.teleportToInspected();
		inspector.setGameMode(GameMode.SPECTATOR);
		inspector.getInventory().clear();
		// Prepare and select actions
		updateAll(true);
		plugin.getInspectionManager().registerUpdater();
		// Fix for glitch in Factions and reloads
		final Player finalPlayer = inspector;
		Do.sync(() -> {
			finalPlayer.setAllowFlight(true);
			finalPlayer.setFlying(true);
			finalPlayer.setGameMode(GameMode.SPECTATOR);
		});
		Do.async(() ->
			Database.run(session -> {
				GCPlayer player = Database.getPlayer(finalPlayer.getUniqueId(), finalPlayer.getName());
				if(player != null) {
					player.setInvisible(true);
					session.update(player);
					plugin.getSyncCommandsServer().runCommand("updatePlayers");
				}
			})
		);
	}

	/**
	 * Start the inspection
	 */
	public void startInspection() {
		startInspection(false);
	}

	/**
	 * Switch to a new inspected player
	 * @param newInspected New player
	 * @param noMessage true if there should not be a message to the player, othwerwise false
	 */
	public void switchToPlayer(Player newInspected, boolean noMessage) {
		String oldInspected = "nobody";
		if (hasInspected()) {
			oldInspected = inspected.getName();
		}
		inspected = newInspected;
		String name = "nobody";
		if (!noMessage) {
			if (inspected != null) {
				name = inspected.getName();
			}
			plugin.message(inspector, "inspect-switched", oldInspected, name);
		}
		// Save new target to disk
		String target = null;
		String targetName = null;
		if (inspected != null) {
			//target = inspected.getUniqueId().toString();
			//targetName = inspected.getName();
			teleportToInspected();
		}
		// Currenty has problems
		//plugin.getInspectionManager().getInspectorStorage().set(inspector.getUniqueId().toString() + ".target", target);
		//plugin.getInspectionManager().getInspectorStorage().set(inspector.getUniqueId().toString() + ".targetName", targetName);
		plugin.getInspectionManager().saveInspectors();
		Do.sync(() -> updateAll(true));
		Log.debug("Inspect: switching target for", inspector.getName(), "from", oldInspected, "to", newInspected==null ? "nobody" : newInspected.getName(), "gamemode:", gamemode);
	}

	/**
	 * Switch to a new inspected player
	 * @param newInspected New player
	 */
	public void switchToPlayer(Player newInspected) {
		switchToPlayer(newInspected, false);
	}

	/**
	 * Setup the inventory actions that will be active for this inspection
	 */
	public void prepareInventoryActions() {
		List<InventoryAction> sourceActions = new ArrayList<>();
		sourceActions.add(new CompassAction(this));
		sourceActions.add(new ChestAction(this));
		sourceActions.add(new EnderchestAction(this));
		sourceActions.add(new KillAuraCheckAction(this));
		sourceActions.add(new NCPAction(this));
		sourceActions.add(new BanInfoAction(this));
		sourceActions.add(new ExitAction(this));
		// Determine layout
		actions = new HashMap<>();
		int currentSlot = 0;
		for (InventoryAction action : sourceActions) {
			if (action.isActive()) {
				int toSlot = action.getItemSlot();
				if (toSlot == -1) {
					toSlot = currentSlot;
					currentSlot++;
				}
				actions.put(toSlot, action);
			}
		}
	}

	/**
	 * Set the armor slots of the inspector to the inspected players armor
	 */
	public void updateArmor() {
		ItemStack[] armor = {null, null, null, null};
		if (hasInspected()) {
			armor = new ItemStack[inspected.getInventory().getArmorContents().length];
			for (int i = 0; i < armor.length; i++) {
				ItemStack armorItem = inspected.getInventory().getArmorContents()[i];
				if (armorItem != null) {
					armor[i] = armorItem.clone();
				}
			}
			for (ItemStack armorItem : armor) {
				if (armorItem != null) {
					ItemMeta meta = armorItem.getItemMeta();
					if (meta != null) {
						List<String> lores = meta.getLore();
						if (lores == null) {
							lores = new ArrayList<>();
						}
						lores.add(0, ChatColor.DARK_GREEN + "Armor from " + inspected.getName());
						meta.setLore(lores);
						armorItem.setItemMeta(meta);
					}
				}
			}
		}
		inspector.getInventory().setArmorContents(armor);
	}

	/**
	 * Set the inventory to the current status
	 * @param forceUpdate Force to update everything
	 */
	public void updateInventory(boolean forceUpdate) {
		PlayerInventory inventory = inspector.getInventory();
		// Clear old items
		if (forceUpdate) {
			for (int i = 0; i <= 35; i++) {
				inventory.clear(i);
			}
		}
		// Setup inventory actions
		int currentSlot = 0;
		for (Integer slot : actions.keySet()) {
			InventoryAction action = actions.get(slot);
			if (action.doUpdates() || inventory.getItem(slot) == null || forceUpdate) {
				inventory.setItem(slot, action.getItem().hideAllAttributes().getItemStack());
			}
		}
		// Update potion effects (for indicators in top right)
		for(PotionEffect potionEffect : inspector.getActivePotionEffects()) {
			if(potionEffect.getDuration() > 300) {
				inspector.removePotionEffect(potionEffect.getType());
			}
		}
		if(inspected != null) {
			inspector.addPotionEffects(inspected.getActivePotionEffects());
		}
	}

	/**
	 * Update the inventory to the current status
	 */
	public void updateInventory() {
		updateInventory(false);
	}

	/**
	 * Update the scoreboard containing all the stats from the inspected player
	 */
	public void updateScoreboard() {
		Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
		Objective objective;
		// Flip to make sure scoreboard does not flicker
		if (scoreboardType) {
			objective = scoreboard.registerNewObjective("inspector", "dummy");
		} else {
			objective = scoreboard.registerNewObjective("inspector2", "dummy");
		}
		scoreboardType = !scoreboardType;
		if (hasInspected()) {
			objective.setDisplayName(ChatColor.GREEN + "?????? " + ChatColor.BOLD + inspected.getName() + ChatColor.RESET + ChatColor.GREEN + " ??????");

			int currentScore = 0;

			// Display food
			StringBuilder food = new StringBuilder(""+ChatColor.YELLOW);
			int foodNumber = inspected.getFoodLevel();
			for (int i = 0; i < 20; i++) {
				if (i == foodNumber) {
					food.append(ChatColor.GRAY);
				}
				food.append('I');
			}
			objective.getScore(food.toString()).setScore(currentScore);
			currentScore++;

			// Display health
			StringBuilder health = new StringBuilder();
			int healthNumber = (int) inspected.getHealth();
			if (healthNumber < 7) {
				health.append(ChatColor.RED);
			} else if (healthNumber < 13) {
				health.append(ChatColor.GOLD);
			} else {
				health.append(ChatColor.GREEN);
			}
			for (int i = 0; i < 20; i++) {
				if (i == healthNumber) {
					health.append(ChatColor.GRAY);
				}
				health.append('I');
			}
			objective.getScore(health.toString()).setScore(currentScore);
			currentScore++;

			// Display ping
			if (getInspected().isOnline()) {
				int ping = Utils.getPing(getInspected());
				String pingString = ping + "";
				if (ping >= 1000) {
					pingString = "999+";
				}
				objective.getScore(ChatColor.GRAY + "Ping: " + ChatColor.WHITE + pingString + " ms").setScore(currentScore);
				currentScore++;
			}

			// Display XP levels
			objective.getScore(ChatColor.GRAY + "XP levels: " + ChatColor.RESET + getInspected().getLevel()).setScore(currentScore);
			currentScore++;

			// Display hours played
			double hours = ((double) getInspected().getStatistic(Statistic.PLAY_ONE_TICK)) / 20 / 3600;
			BigDecimal bigHours = new BigDecimal(hours);
			bigHours = bigHours.setScale(2, RoundingMode.HALF_UP);
			objective.getScore(ChatColor.WHITE + " " + bigHours).setScore(currentScore);
			currentScore++;
			objective.getScore(ChatColor.GRAY + "Hours played:").setScore(currentScore);
			currentScore++;

			// Display money
			double balance = plugin.getEconomy().getBalance(getInspected());
			BigDecimal bigDecimal = new BigDecimal(balance);
			bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_UP);
			balance = bigDecimal.doubleValue();
			objective.getScore(" \u20ac" + balance).setScore(currentScore);
			currentScore++;
			objective.getScore(ChatColor.GRAY + "Money:").setScore(currentScore);
			currentScore++;

			// Display kills/deaths
			if (plugin.getGoPVPLink() != null) {
				// Display K/D ratio
				double kd = ((double) plugin.getGoPVPLink().get().getFileManager().getKills(getInspected().getName())) / ((double) plugin.getGoPVPLink().get().getFileManager().getDeaths(getInspected().getName()));
				if (!((Double) kd).isNaN() && !((Double) kd).isInfinite()) {
					bigDecimal = new BigDecimal(kd);
					bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP);
					kd = bigDecimal.doubleValue();
					objective.getScore(" " + kd).setScore(currentScore);
				} else {
					objective.getScore(" -").setScore(currentScore);
				}
				currentScore++;
				objective.getScore(ChatColor.GRAY + "Kills/Deaths:").setScore(currentScore);
				currentScore++;
				// Display deaths
				objective.getScore(ChatColor.RESET + " " + plugin.getGoPVPLink().get().getFileManager().getDeaths(getInspected().getName())).setScore(currentScore);
				currentScore++;
				objective.getScore(ChatColor.GRAY + "Deaths:").setScore(currentScore);
				currentScore++;
				// Display kills
				objective.getScore(ChatColor.WHITE + " " + plugin.getGoPVPLink().get().getFileManager().getKills(getInspected().getName())).setScore(currentScore);
				currentScore++;
				objective.getScore(ChatColor.GRAY + "Kills:").setScore(currentScore);
				currentScore++;

				// Display last seen
				if (!getInspected().isOnline()) {
					long lastPlayed;
					if (plugin.getEssentialsLink() != null && plugin.getEssentialsLink().get().getUser(getInspected().getUniqueId()) != null) {
						lastPlayed = plugin.getEssentialsLink().get().getUser(getInspected().getUniqueId()).getLastLogout();
					} else {
						lastPlayed = getInspected().getLastPlayed();
					}
					String text = "Never";
					if (lastPlayed >= 0) {
						text = Utils.agoMessage(lastPlayed).getPlain();
					}
					objective.getScore(" " + text).setScore(currentScore);
					currentScore++;
					objective.getScore(ChatColor.GRAY + "Last seen:").setScore(currentScore);
					//currentScore++;
				}
			}
		} else {
			objective.setDisplayName(ChatColor.GREEN + "?????? " + ChatColor.BOLD + "General inspection" + ChatColor.RESET + ChatColor.GREEN + " ??????");
			objective.getScore("Nothing to").setScore(1);
			objective.getScore("see here...").setScore(0);
		}
		getInspector().setScoreboard(scoreboard);
		objective.setDisplaySlot(DisplaySlot.SIDEBAR);
	}

	/**
	 * Update the inspector about the inspected
	 * @param forceUpdate Force refresh everything, bypass lazy updating
	 */
	public void updateAll(boolean forceUpdate) {
		if (forceUpdate) {
			prepareInventoryActions();
		}
		updateArmor();
		updateInventory(forceUpdate);
		updateScoreboard();
		// Workaround for factions where inspectors sometimes are unable to fly (isFlying() is suddenly false)
		if (inspector.getGameMode() == GameMode.SPECTATOR
				&& (!inspector.getAllowFlight() || !inspector.isFlying())) {
			inspector.setAllowFlight(true);
			inspector.setFlying(true);
		}
	}

	/**
	 * Update all information displays
	 */
	public void updateAll() {
		updateAll(false);
	}

	/**
	 * Teleport the inspector to the inspected
	 */
	public void teleportToInspected() {
		if (!hasInspected()) {
			return;
		}
		Location result = getInspected().getLocation().clone();
		// One block in opposite direction of the direction the inspected is looking at
		result.setPitch(0.0f);
		result = result.subtract(result.getDirection().normalize());
		// One block up
		result.add(0, 1, 0);
		result.setPitch(45);
		this.getInspector().teleport(result);
	}

	/**
	 * Teleport the inspector to spawn
	 */
	public void teleportToSpawn() {
		inspector.teleport(inspector.getWorld().getSpawnLocation());
	}

	/**
	 * End this inspection
	 */
	public void endInspection() {
		// revert all actions
		restoreInspectorState();
		plugin.getInspectionManager().removeInspection(this);
		if (plugin.getEssentialsLink() != null) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "essentials:vanish " + getInspector().getName() + " off");
		}
		if (plugin.dynMapInstalled()) {
			Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "dynmap:dynmap show " + getInspector().getName());
		}
		plugin.getInspectionManager().registerUpdater();

		Player finalPlayer = inspector;
		Do.async(() ->
			Database.run(session -> {
				GCPlayer player = Database.getPlayer(finalPlayer.getUniqueId(), finalPlayer.getName());
				if(player != null) {
					player.setInvisible(false);
					session.update(player);
					plugin.getSyncCommandsServer().runCommand("updatePlayers");
				}
			})
		);
	}

	/**
	 * Handle a click of the inspector somewhere in his inventory
	 * @param slot The slot clicked
	 */
	public void handleClick(int slot) {
		InventoryAction action = actions.get(slot);
		if (action != null) {
			action.handleClick();
		}
	}

	/**
	 * Store the inspectors state to memory and disk
	 */
	public boolean saveInspectorState() {
		Log.debug("Inspect: save inventory of", inspector.getName(), "while inspecting", inspected == null ? "nobody" : inspected.getName(), "gamemode:", inspector.getGameMode());
		String baseKey = inspector.getUniqueId().toString() + ".";
		YamlConfiguration storage = plugin.getInspectionManager().getInspectorStorage();

		// Save inspector name for identification
		storage.set(baseKey + "inspectorName", inspector.getName());
		/* Causes problems with offline-read when inspector logs in while player is not online
		// Save target
		if (inspected != null) {
			storage.set(baseKey + "target", inspected.getUniqueId().toString());
			storage.set(baseKey + "targetName", inspected.getName());
		}
		*/
		// Save health and food
		health = inspector.getHealth();
		storage.set(baseKey + "health", health);
		food = inspector.getFoodLevel();
		storage.set(baseKey + "food", food);

		// Save inventory to memory and disk
		ItemStack[] inventory = inspector.getInventory().getContents();
		inspectorInventory = new ItemStack[inventory.length];
		for (int i = 0; i < inspectorInventory.length; i++) {
			inspectorInventory[i] = inventory[i];
			storage.set(baseKey + "inventory." + i, inspectorInventory[i]);
		}
		ItemStack[] armor = inspector.getInventory().getArmorContents();
		inspectorArmor = new ItemStack[armor.length];
		for (int i = 0; i < inspectorArmor.length; i++) {
			inspectorArmor[i] = armor[i];
			storage.set(baseKey + "armor." + i, inspectorArmor[i]);
		}
		// Save active potion effects
		potionEffects = inspector.getActivePotionEffects();
		for (PotionEffect effect : potionEffects) {
			storage.set(baseKey + "potioneffects." + effect.getType().getName(), effect.getDuration() + ":" + effect.getAmplifier() + ":" + effect.isAmbient() + ":" + effect.hasParticles());
		}
		// Save gamemode
		gamemode = inspector.getGameMode();
		storage.set(baseKey + "gamemode", gamemode.toString());
		// Save current location
		location = inspector.getLocation();
		storage.set(baseKey + "location", Utils.locationToConfig(location, true));
		// Save fly state
		allowFlight = inspector.getAllowFlight();
		isFlying = inspector.isFlying();
		storage.set(baseKey + "allowFlight", allowFlight);
		storage.set(baseKey + "isFlying", isFlying);

		// Save config to disk
		if (!plugin.getInspectionManager().saveInspectors()) {
			plugin.message(inspector, "inspect-cancel");
			return false;
		}
		return true;
	}

	/**
	 * Restore saved inspector state from the items in memory
	 */
	public void restoreInspectorState() {
		Log.debug("Inspect: restore inspection from memory for", inspector.getName(), "when ending inspect of", inspected == null ? "nobody" : inspected.getName(), "restoring gamemode", gamemode.toString());
		// Restore inventory
		for(int i = 0; i < inspector.getInventory().getSize(); i++) {
			inspector.getInventory().setItem(i, null);
		}
		inspector.getInventory().setContents(inspectorInventory);
		inspector.getInventory().setArmorContents(inspectorArmor);
		// Restore potion effects
		for (PotionEffect effect : inspector.getActivePotionEffects()) {
			inspector.removePotionEffect(effect.getType());
		}
		inspector.addPotionEffects(potionEffects);
		// Restore location
		inspector.teleport(location);
		// Restore gamemode
		inspector.setGameMode(gamemode);
		// Restore health and food
		inspector.setHealth(health);
		inspector.setFoodLevel(food);
		// Restore fly state
		inspector.setAllowFlight(allowFlight);
		inspector.setFlying(isFlying);

		// Remove from disk storage because everything is restored from memory
		plugin.getInspectionManager().getInspectorStorage().set(inspector.getUniqueId().toString(), null);
		plugin.getInspectionManager().saveInspectors();
		inspector.updateInventory();
	}

}
