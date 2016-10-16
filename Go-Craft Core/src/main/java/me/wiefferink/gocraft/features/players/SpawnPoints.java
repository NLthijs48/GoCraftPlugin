package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.PageDisplay;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SpawnPoints extends Feature {

	public static final String PATH = "spawnPoints";

	public SpawnPoints() {
		if(config.getBoolean("spawnPoints.enable")) {
			permission("spawnpoints.manage", "Manage spawnpoints");
			permission("spawnpoints.spawn", "Spawn at one of the spawnpoints", PermissionDefault.TRUE);
			command("spawnpoints", "Manage spawnpoints", "/spawnpoints <add,remove,list,tp>", "sp");
			command("respawn", "Spawn at a random spawnpoint");
		}
	}

	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		////////// RESPAWN
		if("respawn".equalsIgnoreCase(command)) {
			if(!(sender instanceof Player)) {
				plugin.message(sender, "general-playerOnly");
				return;
			}
			Player player = (Player)sender;

			if(!sender.hasPermission("gocraft.spawnpoints.spawn")) {
				plugin.message(sender, "spawnpoints-noPermissionSpawn");
				return;
			}

			// Whitelist if set
			List<String> regions = config.getStringList("spawnPoints.regions");
			if(regions.size() > 0 && plugin.getWorldGuardLink() != null) {
				boolean inRegion = false;
				for(String region : regions) {
					inRegion |= plugin.getWorldGuardLink().get().getRegionManager(player.getWorld()).getRegion(region) != null && plugin.getWorldGuardLink().get().getRegionManager(player.getWorld()).getRegion(region).contains(player.getLocation().getBlockX(), player.getLocation().getBlockY(), player.getLocation().getBlockZ());
				}
				if(!inRegion) {
					plugin.message(sender, "spawnpoints-notInRegion");
					return;
				}
			}

			ConfigurationSection spawnPointsSection = plugin.getLocalStorage().getConfigurationSection(PATH);
			if(spawnPointsSection != null) {
				ArrayList<String> spawnPoints = new ArrayList<>(spawnPointsSection.getKeys(false));
				while(spawnPoints.size() > 0) {
					int index = Utils.random.nextInt(spawnPoints.size());
					Location location = Utils.configToLocation(spawnPointsSection.getConfigurationSection(spawnPoints.get(index)+".location"));
					if(location != null) {
						if(config.getBoolean("spawnPoints.randomizeDirection")) {
							location.setYaw(Utils.random.nextInt(360));
							location.setPitch(0);
						}
						boolean result = Utils.teleportToLocation(player, location, 3*3*3);
						if(result) {
							plugin.message(sender, "spawnpoints-spawned");
							return;
						}
					}
					spawnPoints.remove(index);
				}
			}
			plugin.message(sender, "spawnpoints-failed");
			return;
		}


		if(!sender.hasPermission("gocraft.spawnpoints.manage")) {
			plugin.message(sender, "spawnpoints-noPermission");
			return;
		}

		if(args.length == 0) {
			plugin.message(sender, "spawnpoints-help");
			return;
		}

		////////// ADD
		if("add".equalsIgnoreCase(args[0])) {

			if(!(sender instanceof Player)) {
				// Might also accept coordinates with arguments later, also better for automation
				plugin.message(sender, "general-playerOnly");
				return;
			}

			// Save location
			Player player = (Player)sender;
			int key = 1;
			while(plugin.getLocalStorage().isSet(PATH+"."+key)) {
				key++;
			}
			Location location = player.getLocation();
			plugin.getLocalStorage().set(PATH+"."+key+".location", Utils.locationToConfig(location, true));
			plugin.saveLocalStorage();
			plugin.message(sender, "spawnpoints-added", key, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), location.getPitch(), location.getYaw());
		}

		////////// REMOVE
		else if("remove".equalsIgnoreCase(args[0])) {
			if(args.length < 2) {
				plugin.message(sender, "spawnpoints-helpRemove");
				return;
			}

			if(!plugin.getLocalStorage().isSet(PATH+"."+args[1])) {
				plugin.message(sender, "spawnpoints-noSpawnpoint", args[1]);
				return;
			}

			Location location = Utils.configToLocation(plugin.getLocalStorage().getConfigurationSection(PATH+"."+args[1]+".location"));
			plugin.getLocalStorage().set(PATH+"."+args[1], null);
			plugin.saveLocalStorage();
			if(location == null) {
				plugin.message(sender, "spawnpoints-removedBroken", args[1]);
			} else {
				plugin.message(sender, "spawnpoints-removed", args[1], location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
			}
		}

		////////// TP
		else if("tp".equalsIgnoreCase(args[0])) {
			if(!(sender instanceof Player)) {
				plugin.message(sender, "general-playerOnly");
				return;
			}
			Player player = (Player)sender;

			if(args.length < 2) {
				plugin.message(sender, "spawnpoints-helpTp");
				return;
			}

			if(!plugin.getLocalStorage().isSet(PATH+"."+args[1])) {
				plugin.message(sender, "spawnpoints-noSpawnpoint", args[1]);
				return;
			}

			Location location = Utils.configToLocation(plugin.getLocalStorage().getConfigurationSection(PATH+"."+args[1]+".location"));
			if(location == null) {
				plugin.message(sender, "spawnpoints-brokenSpawnpoint", args[1]);
				return;
			}

			boolean result = Utils.teleportToLocation(player, location, 9*9*9);
			if(result) {
				plugin.message(sender, "spawnpoints-teleported", args[1], location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockY());
			} else {
				plugin.message(sender, "spawnpoints-noSafeSpot", args[1], location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockY());
			}
		}

		////////// LIST
		else if("list".equalsIgnoreCase(args[0])) {
			ConfigurationSection spawnPoints = plugin.getLocalStorage().getConfigurationSection(PATH);
			if(spawnPoints == null || spawnPoints.getKeys(false).size() == 0) {
				plugin.message(sender, "spawnpoints-listNone");
				return;
			}

			ArrayList<String> spawnPointList = new ArrayList<>(spawnPoints.getKeys(false));
			// Sort numerically
			Collections.sort(spawnPointList, new Comparator<String>() {
				@Override
				public int compare(String one, String two) {
					Integer oneNumber, twoNumber;
					try {
						oneNumber = Integer.parseInt(one);
						twoNumber = Integer.parseInt(two);
						return oneNumber.compareTo(twoNumber);
					} catch(NumberFormatException ignored) {}
					return one.compareTo(two);
				}
			});
			new PageDisplay(sender, spawnPointList.size(), "/spawnpoints list") {
				@Override
				public void renderHeader() {
					plugin.message(target, "spawnpoints-listHeader");
				}

				@Override
				public void renderEmpty() {
					plugin.message(target, "spawnpoints-listNone");
				}

				@Override
				public void renderItem(int itemNumber) {
					String key = spawnPointList.get(itemNumber);
					Location location = Utils.configToLocation(spawnPoints.getConfigurationSection(key+".location"));
					if(location == null) {
						GoCraft.warn("Spawn point '"+key+"' has no proper location");
						plugin.messageNoPrefix(target, "spawnpoints-listImproper", key);
						return;
					}
					plugin.messageNoPrefix(target, "spawnpoints-listItem", key, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockY());
				}
			}.renderPage(args.length>1 ? args[1] : null);
		}

	}

	/**
	 * Teleport the given player to the spawnpoint
	 * @param player The player to teleport
	 * @param key The key of the spawnpoint
	 */
	private void gotoSpawnpoint(Player player, String key) {

	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String command, String[] args) {
		List<String> result = new ArrayList<>();
		if(args.length == 1) {
			result.add("add");
			result.add("remove");
			result.add("list");
			result.add("tp");
		}
		return result;
	}
}
