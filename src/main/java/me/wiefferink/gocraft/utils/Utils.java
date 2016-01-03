package me.wiefferink.gocraft.utils;

import com.mojang.authlib.GameProfile;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import me.wiefferink.gocraft.GoCraft;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;

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
	 * Get the number of free slots in the inventory of the player
	 *
	 * @param player The player to check
	 * @return The number of free slots in the inventory
	 */
	public static int inventoryRoom(Player player) {
		int result = 0;
		for (ItemStack stack : player.getInventory().getContents()) {
			if (stack == null) {
				result++;
			}
		}
		return result;
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

	/**
	 * Create a config section from a location, without pitch and yaw
	 *
	 * @param location The location to save
	 * @return ConfigurationSection containing all details about a location
	 */
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

	/**
	 * Display a message to all staff in all servers
	 * @param type    The type of message to indicate what the message is about
	 * @param message The message, already prefixed by the type so no need to repeat that
	 */
	public static void sendStaffMessage(String type, String message) {
		String result = GoCraft.getInstance().getLanguageManager().getLang("staffbroadcast-template", GoCraft.getInstance().getServerName(), type, message);
		// Display in console
		Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(GoCraft.getInstance().fixColors(result)));
		// Send to other servers
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sync console all displaystaffmessage "+result);
	}

	/**
	 * Display a message to all staff in this server
	 * @param type    The type of message to indicate what the message is about
	 * @param message The message, already prefixed by the type so no need to repeat that
	 */
	public static void displayStaffMessage(String type, String message) {
		String result = GoCraft.getInstance().getLanguageManager().getLang("staffbroadcast-template", GoCraft.getInstance().getServerName(), type, message);
		displayStaffMessage(result);
	}

	/**
	 * Display a raw message to all staff in thes server (used as receiver of messages from other servers)
	 * @param message The raw message
	 */
	public static void displayStaffMessage(String message) {
		message = GoCraft.getInstance().fixColors(message);
		// Display to all staff members
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(player.hasPermission("gocraft.staff")) {
				player.sendMessage(message);
			}
		}
	}

	/**
	 * Perform a command in the console
	 * @param command The command to execute
	 */
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

	public static boolean giveItems(Player player, Collection<ItemStack> items) {
		boolean result = true;
		Inventory inventory = player.getInventory();
		for (ItemStack item : items) {
			// TODO: Make 1.9 compatible?
			for (int i = 9; i < 44; i++) {
				ItemStack current = inventory.getItem(i);
				if (current == null) {
					inventory.setItem(i, item);
					break;
				} else if (current.getType() == item.getType() && current.getAmount() < current.getMaxStackSize()) {
					int left = current.getMaxStackSize() - current.getAmount();
					if (item.getAmount() <= left) {

					}
				}
			}
		}
		return result;
	}


	/**
	 * Load player data of a (possibly) offline player by UUID
	 *
	 * @param uuid The UUID of the player to load
	 * @return The Player object of the target player
	 */
	public static Player loadPlayer(UUID uuid) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
		if (player == null || !player.hasPlayedBefore()) {
			return null;
		}
		// Check if the player is online
		if (player.getPlayer() != null) {
			return player.getPlayer();
		}
		// Load offline player data
		GameProfile profile = new GameProfile(uuid, player.getName());
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile, new PlayerInteractManager(server.getWorldServer(0)));

		// Get the bukkit entity
		Player target = entity.getBukkitEntity();
		if (target != null) {
			target.loadData();
		}
		return target;
	}

	/**
	 * Load player data of a (possibly) offline player by name
	 *
	 * @param name The name of the player to get
	 * @return The Player object of the target player
	 */
	public static Player loadPlayer(String name) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(name);
		Player result = null;
		if (player != null) {
			result = loadPlayer(player.getUniqueId());
		}
		return result;
	}

	/**
	 * Add action to the lores of an item
	 *
	 * @param lores  Current lores
	 * @param action The action string to add
	 * @return The list with the action added
	 */
	public static List<String> addItemAction(List<String> lores, String action) {
		if (lores == null) {
			lores = new ArrayList<>();
		}
		lores.add(lores.size(), ChatColor.BLUE + "" + ChatColor.BOLD + "<" + action + ">");
		return lores;
	}

	/**
	 * Convert milliseconds to a human readable format
	 *
	 * @param milliseconds The amount of milliseconds to convert
	 * @return A formatted string based on the language file
	 */
	public static String millisToHumanFormat(long milliseconds) {
		long timeLeft = milliseconds + 500;
		// To seconds
		timeLeft = timeLeft / 1000;
		if (timeLeft <= 0) {
			return GoCraft.getInstance().getLanguageManager().getLang("timeleft-ended");
		} else if (timeLeft == 1) {
			return GoCraft.getInstance().getLanguageManager().getLang("timeleft-second", timeLeft);
		} else if (timeLeft <= 120) {
			return GoCraft.getInstance().getLanguageManager().getLang("timeleft-seconds", timeLeft);
		}
		// To minutes
		timeLeft = timeLeft / 60;
		if (timeLeft <= 120) {
			return GoCraft.getInstance().getLanguageManager().getLang("timeleft-minutes", timeLeft);
		}
		// To hours
		timeLeft = timeLeft / 60;
		if (timeLeft <= 48) {
			return GoCraft.getInstance().getLanguageManager().getLang("timeleft-hours", timeLeft);
		}
		// To days
		timeLeft = timeLeft / 24;
		if (timeLeft <= 60) {
			return GoCraft.getInstance().getLanguageManager().getLang("timeleft-days", timeLeft);
		}
		// To months
		timeLeft = timeLeft / 30;
		if (timeLeft <= 24) {
			return GoCraft.getInstance().getLanguageManager().getLang("timeleft-months", timeLeft);
		}
		// To years
		timeLeft = timeLeft / 12;
		return GoCraft.getInstance().getLanguageManager().getLang("timeleft-years", timeLeft);
	}

	/**
	 * Check if a player is in PVP area
	 *
	 * @param player The player to check
	 * @return true if the player is in a PVP area, otherwise false
	 */
	public static boolean isInPvpArea(Player player) {
		RegionManager manager = GoCraft.getInstance().getWorldGuard().getRegionManager(Bukkit.getWorld("world"));
		ApplicableRegionSet regions = manager.getApplicableRegions(player.getLocation());
		return regions.testState(GoCraft.getInstance().getWorldGuard().wrapPlayer(player), DefaultFlag.PVP);
	}

	/**
	 * @param item
	 * @param name
	 */
	public static void setName(ItemStack item, String name) {

	}
}
