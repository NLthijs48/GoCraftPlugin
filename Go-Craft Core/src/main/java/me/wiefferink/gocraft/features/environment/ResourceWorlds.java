package me.wiefferink.gocraft.features.environment;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Callback;
import me.wiefferink.gocraft.tools.Utils;
import org.apache.commons.io.FileDeleteStrategy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class ResourceWorlds extends Feature {

	public ResourceWorlds() {
		ConfigurationSection resourceWorldsSection = plugin.getConfig().getConfigurationSection("worlds");
		if(resourceWorldsSection != null && resourceWorldsSection.getKeys(false).size() > 0) {
			int count = 0;
			for(String world : resourceWorldsSection.getKeys(false)) {
				if(resourceWorldsSection.isSet(world+".resetTime")) {
					count++;
				}
			}
			if(count > 0) {
				listen();
			}
		}
	}

	@Override
	public void stop() {
		// Reset the resource worlds that need it
		ConfigurationSection rWorldsSection = plugin.getConfig().getConfigurationSection("worlds");
		if(rWorldsSection != null) {
			for(String worldName : rWorldsSection.getKeys(false)) {
				World world = Bukkit.getWorld(worldName);
				if(world == null) {
					plugin.getLogger().warning("World "+worldName+" does not exist and therefore cannot be used as resource world");
					continue;
				}
				long resetTime = getResetTime(world);
				if(resetTime > 0 && Calendar.getInstance().getTimeInMillis() > (getLastReset(world)+(resetTime*0.9))) {
					resetWorld(world);
				}
			}
		}
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event) {
		checkWorldSpawn(event.getPlayer());
	}

	@EventHandler
	public void switchWorld(PlayerChangedWorldEvent event) {
		checkWorldSpawn(event.getPlayer());
	}

	public void checkWorldSpawn(Player player) {
		long lastPlayed = player.getLastPlayed();
		long lastReset = getLastReset(player.getWorld());
		//GoCraft.debug("lastReset: "+lastReset+", distance: "+player.getWorld().getSpawnLocation().distance(player.getLocation())+", safe: "+Utils.isSafe(player.getLocation()));
		double distanceFromSpawn = player.getWorld().getSpawnLocation().distance(player.getLocation());
		boolean isSafe = Utils.isSafe(player.getLocation());
		if(lastReset > 0 && (distanceFromSpawn < 10 || !isSafe)) {
			GoCraft.debug("teleporting "+player.getName()+", lastPlayed: "+lastPlayed+", lastReset: "+lastReset+", distance: "+distanceFromSpawn+", isSafe: "+isSafe);
			Utils.teleportRandomly(player, player.getWorld(), Utils.getWorldRadius(player.getWorld()), new Callback<Boolean>() {
				@Override
				public void execute(Boolean teleported) {
					if(teleported) {
						plugin.message(player, "resetworld-randomtp");
					} else {
						plugin.message(player, "resetworld-randomtpFailed");
					}
				}
			});
		}
	}

	/**
	 * Get the time the world should be reset
	 * @param world The world to check for
	 * @return The time after which a world should reset
	 */
	public long getResetTime(World world) {
		long result = -1;
		ConfigurationSection section = plugin.getConfig().getConfigurationSection("worlds");
		if(section != null) {
			result = Utils.durationStringToLong(section.getString(world.getName()+".resetTime"));
		}
		return result;
	}

	/**
	 * Get the last time the world has been reset
	 * @param world The world to check for
	 * @return The last time he world has been reset, or -1 if never
	 */
	public long getLastReset(World world) {
		return plugin.getLocalStorage().getLong("worldReset."+world.getName(), -1);
	}

	/**
	 * Update the last reset time of a world
	 * @param world The world to set it for
	 */
	public void updateResetTime(World world) {
		plugin.getLocalStorage().set("worldReset."+world.getName(), Calendar.getInstance().getTimeInMillis());
		plugin.saveLocalStorage();
	}

	/**
	 * Reset the given world
	 * @param world The world to reset
	 */
	public void resetWorld(World world) {
		String regionPath = "region";
		if(world.getEnvironment() == World.Environment.NETHER || world.getEnvironment() == World.Environment.THE_END) {
			regionPath = "DIM-1"+File.separator+"region";
		}
		File regionFolder = new File(plugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile()+File.separator+world.getName()+File.separator+regionPath);
		//GoCraft.debug("region folder of "+world.getName()+" at "+regionFolder.getAbsolutePath());
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getWorld().getName().equals(world.getName())) {
				plugin.message(player, "resetworld-tp", world.getName());
				player.teleport(Bukkit.getWorld("world").getSpawnLocation());
			}
		}
		if(!Bukkit.getServer().unloadWorld(world, false)) {
			plugin.getLogger().warning("Could not unload resourceworld "+world.getName()+" for reset");
			return;
		}
		// Delete individual files
		File[] files = regionFolder.listFiles();
		if(files != null) {
			for(File file : files) {
				try {
					FileDeleteStrategy.FORCE.delete(file);
				} catch(IOException e) {
					plugin.getLogger().warning("Could not delete file of resourceworld "+world.getName()+": "+file.getAbsolutePath());
				}
			}
		}
		// Delete folder
		try {
			FileDeleteStrategy.FORCE.delete(regionFolder);
		} catch(IOException e) {
			plugin.getLogger().warning("Could not reset resourceworld "+world.getName()+": "+regionFolder.getAbsolutePath());
			e.printStackTrace();
		}
		// Wrapup reset
		updateResetTime(world);
		plugin.getLogger().info("World "+world.getName()+" has been reset");
		plugin.increaseStatistic("resourceWorldReset."+world.getName());
		final String worldName = world.getName();
		try {
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mv load "+worldName);
				}
			}.runTaskLater(plugin, 1L);
		} catch(Exception ignored) {
		}
	}

}
