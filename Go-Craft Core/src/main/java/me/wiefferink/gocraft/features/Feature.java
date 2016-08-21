package me.wiefferink.gocraft.features;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockEvent;
import org.bukkit.event.entity.EntityEvent;
import org.bukkit.event.hanging.HangingEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.vehicle.VehicleEvent;
import org.bukkit.event.weather.WeatherEvent;
import org.bukkit.event.world.WorldEvent;

import java.util.List;

public class Feature implements Listener, CommandExecutor {
	public static GoCraft plugin = GoCraft.getInstance();
	public static FileConfiguration config = plugin.getConfig();

	protected String configKey = null;

	/**
	 * Stop actions of the feature
	 */
	public void stopFeature() {
		stop();
	}

	public void stop() {
	}

	/**
	 * Startup actions of the feature
	 */
	public void startFeature() {
	}

	/**
	 * Register as listener
	 * @return true if registered, otherwise false
	 */
	protected boolean listen() {
		return listen(null);
	}

	/**
	 * Register as listener
	 * @param configKey The key to check for in the config
	 * @return true if registered, otherwise false
	 */
	protected boolean listen(String configKey) {
		this.configKey = configKey;
		if(configKey == null || (config.isBoolean(configKey) && config.getBoolean(configKey)) || config.isSet(configKey)) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
			return true;
		}
		return false;
	}

	/**
	 * Register for a certain command
	 * @param name The command to register for
	 */
	protected void command(String name) {
		PluginCommand command = plugin.getCommand(name);
		if(command != null) {
			command.setExecutor(this);
		} else {
			GoCraft.warn("Could not register for command: "+name);
		}
	}

	/**
	 * Check if the feature is enabled on the given world
	 * @param world The world to check
	 * @return true if the feature is enabled in the given world, otherwise false
	 */
	protected boolean inWorld(Object world) {
		List<String> worlds = config.getStringList(configKey+"Worlds");
		if(configKey != null && worlds.size() != 0) {
			String worldString = "";
			if((world instanceof String)) {
				worldString = (String)world;
			} else if((world instanceof World)) {
				worldString = ((World)world).getName();
			} else if((world instanceof Block)) {
				worldString = ((Block)world).getWorld().getName();
			} else if((world instanceof Location)) {
				worldString = ((Location)world).getWorld().getName();
			} else if((world instanceof Entity)) {
				worldString = ((Entity)world).getWorld().getName();
			} else if(world instanceof PlayerEvent) {
				worldString = ((PlayerEvent)world).getPlayer().getWorld().getName();
			} else if(world instanceof BlockEvent) {
				worldString = ((BlockEvent)world).getBlock().getWorld().getName();
			} else if(world instanceof EntityEvent) {
				worldString = ((EntityEvent)world).getEntity().getWorld().getName();
			} else if(world instanceof HangingEvent) {
				worldString = ((HangingEvent)world).getEntity().getWorld().getName();
			} else if(world instanceof VehicleEvent) {
				worldString = ((VehicleEvent)world).getVehicle().getLocation().getWorld().getName();
			} else if(world instanceof WeatherEvent) {
				worldString = ((WeatherEvent)world).getWorld().getName();
			} else if(world instanceof WorldEvent) {
				worldString = ((WorldEvent)world).getWorld().getName();
			} else {
				GoCraft.warn("GoCraft.inWorld: Cannot get world from object:", world.toString());
			}
			if(!worlds.contains(worldString)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		return false;
	}
}
