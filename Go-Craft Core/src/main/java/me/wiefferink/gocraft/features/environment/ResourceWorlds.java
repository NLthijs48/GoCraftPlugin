package me.wiefferink.gocraft.features.environment;

import me.wiefferink.bukkitdo.Do;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class ResourceWorlds extends Feature {

	public ResourceWorlds() {
		ConfigurationSection resourceWorldsSection = getConfig().getConfigurationSection("worlds");
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
		if(plugin.isReload()) {
			return;
		}
		// Reset the resource worlds that need it
		ConfigurationSection rWorldsSection = getConfig().getConfigurationSection("worlds");
		if(rWorldsSection != null) {
			for(String worldName : rWorldsSection.getKeys(false)) {
				World world = Bukkit.getWorld(worldName);
				if(world == null) {
					Log.warn("World "+worldName+" does not exist and therefore cannot be used as resource world");
					continue;
				}
				long resetTime = getResetTime(world);
				if(resetTime > 0 && Calendar.getInstance().getTimeInMillis() > (getLastReset(world)+(resetTime*0.9))) {
					resetWorld(world);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void playerJoin(PlayerJoinEvent event) {
		checkWorldSpawn(event.getPlayer());
	}

	@EventHandler(ignoreCancelled = true)
	public void switchWorld(PlayerChangedWorldEvent event) {
		checkWorldSpawn(event.getPlayer());
	}

	public void checkWorldSpawn(Player player) {
		long lastPlayed = player.getLastPlayed();
		long lastReset = getLastReset(player.getWorld());
		//Log.debug("lastReset: "+lastReset+", distance: "+player.getWorld().getSpawnLocation().distance(player.getLocation())+", safe: "+Utils.isSafe(player.getLocation()));
		double distanceFromSpawn = player.getWorld().getSpawnLocation().distance(player.getLocation());
		boolean isSafe = Utils.isSafe(player.getLocation());
		if(lastReset > 0 && (distanceFromSpawn < 10 || !isSafe)) {
			//Log.debug("teleporting "+player.getName()+", lastPlayed: "+lastPlayed+", lastReset: "+lastReset+", distance: "+distanceFromSpawn+", isSafe: "+isSafe);
			Utils.teleportRandomly(player, player.getWorld(), Utils.getWorldRadius(player.getWorld()), teleported -> {
				if(teleported) {
					plugin.message(player, "resetworld-randomtp");
				} else {
					plugin.message(player, "resetworld-randomtpFailed");
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
		ConfigurationSection section = getConfig().getConfigurationSection("worlds");
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
		if(world.getEnvironment() == World.Environment.NETHER) {
			regionPath = "DIM-1";
		} else if(world.getEnvironment() == World.Environment.THE_END) {
			regionPath = "DIM1";
		}
		File regionFolder = new File(plugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile()+File.separator+world.getName()+File.separator+regionPath);
		File dataFolder = new File(plugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile()+File.separator+world.getName()+File.separator+"data");
		//Log.debug("region folder of "+world.getName()+" at "+regionFolder.getAbsolutePath());
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.getWorld().getName().equals(world.getName())) {
				plugin.message(player, "resetworld-tp", world.getName());
				player.teleport(Bukkit.getWorld("world").getSpawnLocation());
			}
		}
		if(!Bukkit.getServer().unloadWorld(world, false)) {
			Log.warn("Could not unload resourceworld "+world.getName()+" for reset");
			return;
		}
		// REGION folder
		File[] files = regionFolder.listFiles();
		if(files != null) {
			for(File file : files) {
				try {
					FileDeleteStrategy.FORCE.delete(file);
				} catch(IOException e) {
					Log.error("Could not delete file of resourceworld "+world.getName()+": "+file.getAbsolutePath());
				}
			}
		}
		try {
			FileDeleteStrategy.FORCE.delete(regionFolder);
		} catch(IOException e) {
			Log.error("Could not reset resourceworld "+world.getName()+": "+regionFolder.getAbsolutePath(), ExceptionUtils.getStackTrace(e));
		}
		// DATA folder
		File[] dataFiles = dataFolder.listFiles();
		if(dataFiles != null) {
			for(File file : dataFiles) {
				try {
					FileDeleteStrategy.FORCE.delete(file);
				} catch(IOException e) {
					Log.error("Could not delete file of resourceworld "+world.getName()+": "+file.getAbsolutePath());
				}
			}
		}
		try {
			FileDeleteStrategy.FORCE.delete(dataFolder);
		} catch(IOException e) {
			Log.error("Could not reset resourceworld "+world.getName()+": "+dataFolder.getAbsolutePath(), ExceptionUtils.getStackTrace(e));
		}

		// Wrapup reset
		updateResetTime(world);
		Log.info("World "+world.getName()+" has been reset");
		plugin.increaseStatistic("resourceWorldReset."+world.getName());
		final String worldName = world.getName();
		try {
			Do.sync(() -> Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mv load " + worldName));
		} catch(Exception ignored) {
		}
	}

}
