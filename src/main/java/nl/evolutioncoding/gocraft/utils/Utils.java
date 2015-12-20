package nl.evolutioncoding.gocraft.utils;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import nl.evolutioncoding.gocraft.GoCraft;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;

public class Utils {

	private static ArrayList<Material> canSpawnIn = new ArrayList<>(Arrays.asList(Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE));
	private static ArrayList<Material> cannotSpawnOn = new ArrayList<>(Arrays.asList(Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.CACTUS, Material.IRON_FENCE, Material.FENCE_GATE, Material.THIN_GLASS, Material.NETHER_FENCE, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STAINED_GLASS_PANE));
	private static ArrayList<Material> cannotSpawnBeside = new ArrayList<>(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.CACTUS));

	private Utils() {
	}

	/**
	 * Get the ping of a player
	 * @param player The player to check
	 * @return The ping in ms
	 */
	public static int getPing(Player player) {
		CraftPlayer cp = (CraftPlayer)player;
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

	/**
	 * Checks if a certain location is safe to teleport to
	 * @param location The location to check
	 * @return true if it is safe, otherwise false
	 */
	public static boolean isSafe(Location location) {
		Block feet = location.getBlock();
		Block head = feet.getRelative(BlockFace.UP);
		Block below = feet.getRelative(BlockFace.DOWN);
		Block above = head.getRelative(BlockFace.UP);
		// Check the block at the feet of the player
		if((feet.getType().isSolid() && !canSpawnIn.contains(feet.getType())) || feet.isLiquid()) {
			return false;
		} else if((head.getType().isSolid() && !canSpawnIn.contains(head.getType())) || head.isLiquid()) {
			return false;
		} else if(!below.getType().isSolid() || cannotSpawnOn.contains(below.getType()) || below.isLiquid()) {
			return false;
		} else if(above.isLiquid() || cannotSpawnBeside.contains(above.getType())) {
			return false;
		}
		// Check all blocks around
		ArrayList<Material> around = new ArrayList<>(Arrays.asList(
				feet.getRelative(BlockFace.NORTH).getType(),
				feet.getRelative(BlockFace.NORTH_EAST).getType(),
				feet.getRelative(BlockFace.EAST).getType(),
				feet.getRelative(BlockFace.SOUTH_EAST).getType(),
				feet.getRelative(BlockFace.SOUTH).getType(),
				feet.getRelative(BlockFace.SOUTH_WEST).getType(),
				feet.getRelative(BlockFace.WEST).getType(),
				feet.getRelative(BlockFace.NORTH_WEST).getType(),
				below.getRelative(BlockFace.NORTH).getType(),
				below.getRelative(BlockFace.NORTH_EAST).getType(),
				below.getRelative(BlockFace.EAST).getType(),
				below.getRelative(BlockFace.SOUTH_EAST).getType(),
				below.getRelative(BlockFace.SOUTH).getType(),
				below.getRelative(BlockFace.SOUTH_WEST).getType(),
				below.getRelative(BlockFace.WEST).getType(),
				below.getRelative(BlockFace.NORTH_WEST).getType(),
				head.getRelative(BlockFace.NORTH).getType(),
				head.getRelative(BlockFace.NORTH_EAST).getType(),
				head.getRelative(BlockFace.EAST).getType(),
				head.getRelative(BlockFace.SOUTH_EAST).getType(),
				head.getRelative(BlockFace.SOUTH).getType(),
				head.getRelative(BlockFace.SOUTH_WEST).getType(),
				head.getRelative(BlockFace.WEST).getType(),
				head.getRelative(BlockFace.NORTH_WEST).getType(),
				above.getRelative(BlockFace.NORTH).getType(),
				above.getRelative(BlockFace.NORTH_EAST).getType(),
				above.getRelative(BlockFace.EAST).getType(),
				above.getRelative(BlockFace.SOUTH_EAST).getType(),
				above.getRelative(BlockFace.SOUTH).getType(),
				above.getRelative(BlockFace.SOUTH_WEST).getType(),
				above.getRelative(BlockFace.WEST).getType(),
				above.getRelative(BlockFace.NORTH_WEST).getType()
		));
		for(Material material : around) {
			if(cannotSpawnBeside.contains(material)) {
				return false;
			}
		}
		return true;
	}

}
