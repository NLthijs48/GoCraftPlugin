package nl.evolutioncoding.gocraft.utils;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import nl.evolutioncoding.gocraft.GoCraft;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Utils {

	private Utils() {}

	/**
	 * Get the ping of a player
	 * @param player The player to check
	 * @return The ping in ms
	 */
	public static int getPing(Player player) { 
		CraftPlayer cp = (CraftPlayer) player; 
		EntityPlayer ep = cp.getHandle(); 
		return ep.ping; 
	}
	
	/**
	 * Create a map from a location, to save it in the config
	 * @param location The location to transform
	 * @return The map with the location values
	 */
	public static ConfigurationSection locationToConfig(Location location, boolean setPitchYaw) {
		if(location == null) {
			return null;
		}
		ConfigurationSection result = new YamlConfiguration();
		result.set("world", location.getWorld().getName());
		result.set("x", location.getX());
		result.set("y", location.getY());
		result.set("z", location.getZ());
		if(setPitchYaw) {
			result.set("yaw", Float.toString(location.getYaw()));
			result.set("pitch", Float.toString(location.getPitch()));		
		}
		return result;
	}
	public static ConfigurationSection locationToConfig(Location location) {
		return locationToConfig(location, false);
	}
	
	/**
	 * Create a location from a map, reconstruction from the config values
	 * @param config The config section to reconstruct from
	 * @return The location
	 */
	public static Location configToLocation(ConfigurationSection config) {
		if(config == null
				|| !config.isString("world")
				|| !config.isDouble("x")
				|| !config.isDouble("y")
				|| !config.isDouble("z")
				|| Bukkit.getWorld(config.getString("world")) == null) {
			return null;
		}
		Location result = new Location(
				Bukkit.getWorld(config.getString("world")),
				config.getDouble("x"),
				config.getDouble("y"),
				config.getDouble("z"));
		if(config.isString("yaw") && config.isString("pitch")) {
			result.setPitch(Float.parseFloat(config.getString("pitch")));
			result.setYaw(Float.parseFloat(config.getString("yaw")));
		}
		return result;
	}

	public static void sendStaffMessage(String type, String message) {
		String result = GoCraft.getInstance().getLanguageManager().getLang("staffbroadcast-template", GoCraft.getInstance().getServerName(), type, message);
		// Display in console
		Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(GoCraft.getInstance().fixColors(result)));
		// Send to other servers
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sync console all displaystaffmessage " + result);
	}

	public static void displayStaffMessage(String message) {
		message = GoCraft.getInstance().fixColors(message);
		// Display to all staff members
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.hasPermission("gocraft.staff")) {
				player.sendMessage(message);
			}
		}
	}

	public static void consoleCommand(String command) {
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}

}
