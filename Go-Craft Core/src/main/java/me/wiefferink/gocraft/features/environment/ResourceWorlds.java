package me.wiefferink.gocraft.features.environment;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.apache.commons.io.FileDeleteStrategy;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class ResourceWorlds extends Feature {

	public ResourceWorlds() {
		ConfigurationSection resourceWorldsSection = plugin.getConfig().getConfigurationSection("resourceWorlds");
		if(resourceWorldsSection != null && resourceWorldsSection.getKeys(false).size() > 0) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);


		}
	}

	@Override
	public void stop() {
		// Reset the resource worlds that need it
		ConfigurationSection rWorldsSection = plugin.getConfig().getConfigurationSection("resourceWorlds");
		if(rWorldsSection != null) {
			for(String worldName : rWorldsSection.getKeys(false)) {
				World world = Bukkit.getWorld(worldName);
				if(world == null) {
					plugin.getLogger().warning("World "+worldName+" does not exist and therefore cannot be used as resource world");
					continue;
				}
				long resetTime = getResetTime(world);
				if(resetTime > 0 && Calendar.getInstance().getTimeInMillis() > (getLastReset(world)+resetTime)) {
					resetWorld(world);
				}
			}
		}
	}

	/**
	 * Get the time the world should be reset
	 * @param world The world to check for
	 * @return The time after which a world should reset
	 */
	public long getResetTime(World world) {
		long result = -1;
		ConfigurationSection section = plugin.getConfig().getConfigurationSection("resourceWorlds");
		if(section != null) {
			result = Utils.durationStringToLong(section.getString(world.getName()));
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
		File regionFolder = new File(plugin.getDataFolder().getAbsoluteFile().getParentFile().getParentFile()+File.separator+world.getName()+File.separator+"region");
		GoCraft.debug("region folder of "+world.getName()+" at "+regionFolder.getAbsolutePath());
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
		try {
			FileDeleteStrategy.FORCE.delete(regionFolder);
		} catch(IOException e) {
			plugin.getLogger().warning("Could not reset resourceworld "+world.getName()+":");
			e.printStackTrace();
		}
		updateResetTime(world);
		plugin.getLogger().info("World "+world.getName()+" has been reset");
		plugin.increaseStatistic("resourceWorldReset."+world.getName());
		final String worldName = world.getName();
		new BukkitRunnable() {
			@Override
			public void run() {
				Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "mv load "+worldName);
			}
		}.runTaskLater(plugin, 1L);
	}

}
