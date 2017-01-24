package me.wiefferink.gocraft.tools;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

	private static ArrayList<Material> canSpawnIn = new ArrayList<>(Arrays.asList(Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE));
	private static ArrayList<Material> cannotSpawnOn = new ArrayList<>(Arrays.asList(Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.CACTUS, Material.IRON_FENCE, Material.FENCE_GATE, Material.THIN_GLASS, Material.NETHER_FENCE, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STAINED_GLASS_PANE, Material.FIRE));
	private static ArrayList<Material> cannotSpawnBeside = new ArrayList<>(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.CACTUS, Material.FIRE));

	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss SSS");
	public static Random random = new Random();

	private Utils() {
	}

	// Setup all time identifiers
	private static List<String> identifiers;
	private static List<String> seconds = Arrays.asList("s", "sec", "secs", "second", "seconds");
	private static List<String> minutes = Arrays.asList("m", "min", "mins", "minute", "minutes");
	private static List<String> hours = Arrays.asList("h", "hour", "hours");
	private static List<String> days = Arrays.asList("d", "day", "days");
	private static List<String> weeks = Arrays.asList("w", "week", "weeks");
	private static List<String> months = Arrays.asList("M", "month", "months");
	private static List<String> years = Arrays.asList("y", "year", "years");

	static {
		identifiers = new ArrayList<>();
		identifiers.addAll(seconds);
		identifiers.addAll(minutes);
		identifiers.addAll(hours);
		identifiers.addAll(days);
		identifiers.addAll(weeks);
		identifiers.addAll(months);
		identifiers.addAll(years);
	}

	/**
	 * Get the ping of a player
	 * @param player The player to check
	 * @return The ping in ms
	 */
	public static int getPing(Player player) {
		return GoCraft.getInstance().getSpecificUtils().getPing(player);
	}

	/**
	 * Get the number of free slots in the inventory of the player
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
	 * Remove a certain number of items from an inventory
	 * @param inventory The inventory to remove them from
	 * @param item The item to remove (material and data are checked)
	 * @return true if enough items could be removed, otherwise false
	 */
	public static boolean removeItems(Inventory inventory, ItemStack item) {
		int toRemove = item.getAmount();
		ItemStack[] contents = inventory.getContents();
		for (int i = 0; i < contents.length; i++) {
			ItemStack iItem = contents[i];
			if (iItem != null
					&& iItem.getType() == item.getType()
					&& iItem.getDurability() == item.getDurability()) {
				if (iItem.getAmount() <= toRemove) {
					contents[i] = null;
					toRemove -= iItem.getAmount();
				} else {
					iItem.setAmount(iItem.getAmount() - toRemove);
					toRemove = 0;
				}
			}
			if (toRemove <= 0) {
				break;
			}
		}
		inventory.setContents(contents);
		return toRemove == 0;
	}


	/**
	 * Check how many items of a certain type are in the inventory
	 * @param inventory The inventory to check
	 * @param item The item to count
	 * @return The number of times the item is in the inventory
	 */
	public static int hasItems(Inventory inventory, ItemStack item) {
		int result = 0;
		for (ItemStack itemStack : inventory.getContents()) {
			if (itemStack != null
					&& itemStack.getType() == item.getType()
					&& itemStack.getDurability() == item.getDurability()) {
				result += itemStack.getAmount();
			}
		}
		return result;
	}

	/**
	 * Get the current date and time (with milliseconds)
	 * @return The current date time string
	 */
	public static String getCurrentDateTime() {
		Calendar c = Calendar.getInstance();
		return dateFormat.format(c.getTime());
	}

	/**
	 * Create a map from a location, to save it in the config
	 * @param location The location to transform
	 * @return The map with the location values
	 */
	public static ConfigurationSection locationToConfig(Location location, boolean setPitchYaw) {
		if (location == null) {
			return null;
		}
		ConfigurationSection result = new YamlConfiguration();
		result.set("world", location.getWorld().getName());
		result.set("x", location.getX());
		result.set("y", location.getY());
		result.set("z", location.getZ());
		if (setPitchYaw) {
			result.set("yaw", Float.toString(location.getYaw()));
			result.set("pitch", Float.toString(location.getPitch()));
		}
		return result;
	}

	/**
	 * Create a config section from a location, without pitch and yaw
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
		if (config == null
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
		if (config.isString("yaw") && config.isString("pitch")) {
			result.setPitch(Float.parseFloat(config.getString("pitch")));
			result.setYaw(Float.parseFloat(config.getString("yaw")));
		}
		return result;
	}

	/**
	 * Get an identifier string for a location (for example as map key)
	 * @param location The location to get the string for
	 * @return An identifier string for a location
	 */
	public static String locationToString(Location location) {
		return location.getWorld().getName() + ":" + location.getBlockX() + ":" + location.getBlockY() + ":" + location.getBlockZ();
	}

	/**
	 * Display a message to all staff in all servers
	 * @param type The type of message to indicate what the message is about
	 * @param message The message, already prefixed by the type so no need to repeat that
	 */
	public static void sendStaffMessage(String type, String message) {
		Message result = Message.fromKey("staffbroadcast-template").replacements(GoCraft.getInstance().getServerName(), type, message);
		// Display in console
		result.send(Bukkit.getConsoleSender());
		// Send to other servers
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "syncservers displaystaffmessage "+result.getPlain());
	}

	/**
	 * Display a message to all staff in this server
	 * @param type The type of message to indicate what the message is about
	 * @param message The message, already prefixed by the type so no need to repeat that
	 */
	public static void displayStaffMessage(String type, String message) {
		displayStaffMessage(Message.fromKey("staffbroadcast-template").replacements(GoCraft.getInstance().getServerName(), type, message).getPlain());
	}

	/**
	 * Display a raw message to all staff in thes server (used as receiver of messages from other servers)
	 * @param input The raw message
	 */
	public static void displayStaffMessage(String input) {
		Message message = Message.fromString(input);
		// Display to all staff members
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("gocraft.staff")) {
				message.send(player);
			}
		}
	}

	/**
	 * Perform a command in the console
	 * @param command The command to executeBuy
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
		if ((feet.getType().isSolid() && !canSpawnIn.contains(feet.getType())) || feet.isLiquid()) {
			return false;
		} else if ((head.getType().isSolid() && !canSpawnIn.contains(head.getType())) || head.isLiquid()) {
			return false;
		} else if (!below.getType().isSolid() || cannotSpawnOn.contains(below.getType()) || below.isLiquid()) {
			return false;
		} else if (above.isLiquid() || cannotSpawnBeside.contains(above.getType())) {
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
		for (Material material : around) {
			if (cannotSpawnBeside.contains(material)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Try to teleport the player to a random location in the world
	 * @param player The player to teleport
	 * @param world  The world to teleport to
	 */
	public static void teleportRandomly(Player player, World world, int radius, Callback<Boolean> callback) {
		final Location spawn = world.getSpawnLocation();
		final int finalRadius = Math.max(radius, 1);
		new BukkitRunnable() {
			private int current = 100;
			private boolean found = false;

			@Override
			public void run() {
				current--;

				Location attemptBase = spawn.clone();
				double x = Utils.random.nextInt(finalRadius*2)-finalRadius+0.5;
				double z = Utils.random.nextInt(finalRadius*2)-finalRadius+0.5;
				attemptBase.setX(attemptBase.getBlockX()+x);
				attemptBase.setZ(attemptBase.getBlockZ()+z);
				int end = 0;
				int start = 255;
				int count = -1;
				if(world.getEnvironment() == World.Environment.NETHER) {
					start = 1;
					end = 128;
					count = 1;
				}
				for(int i = start; i != end && i <= 255 && i > 0 && !found; i += count) {
					Location attempt = attemptBase.clone();
					attempt.setY(i);
					if(Utils.isSafe(attempt)) {
						//GoCraft.debug("   Utils.teleportRandomly: teleport "+player.getName()+", world: "+world.getName()+", radius: "+radius+", to: "+attempt.toString()+", attempt "+(100-current));
						player.teleport(attempt);
						callback.execute(true);
						this.cancel();
						found = true;
						break;
					}
				}

				if(current <= 0) {
					callback.execute(false);
					this.cancel();
				}
			}
		}.runTaskTimer(GoCraft.getInstance(), 1, 1);
	}

	/**
	 * Teleport a player to a location, or somewhere close to it where it is safe
	 * @param player           Player that should be teleported
	 * @param location The location to teleport to
	 * @return true if the teleport succeeded, otherwise false
	 */
	public static boolean teleportToLocation(Player player, Location location, int maximumAttempts) {
		int checked = 1;

		// Setup startlocation at the center of the block (simplifies safe location check)
		Location startLocation = location.clone();
		startLocation.setX(startLocation.getBlockX()+0.5);
		startLocation.setZ(startLocation.getBlockZ()+0.5);

		// Check locations starting from startLocation and then a cube that increases
		// radius around that (until no block in the region is found at all cube sides)
		Location safeLocation = startLocation;
		int radius = 1;
		boolean done = isSafe(safeLocation);
		while(!done) {
			// North side
			for(int x = -radius+1; x <= radius && !done; x++) {
				for(int y = -radius+1; y < radius && !done; y++) {
					safeLocation = startLocation.clone().add(x, y, -radius);
					if(safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
						continue;
					}
					checked++;
					done = isSafe(safeLocation) || checked > maximumAttempts;
				}
			}

			// East side
			for(int z = -radius+1; z <= radius && !done; z++) {
				for(int y = -radius+1; y < radius && !done; y++) {
					safeLocation = startLocation.clone().add(radius, y, z);
					if(safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
						continue;
					}
					checked++;
					done = isSafe(safeLocation) || checked > maximumAttempts;
				}
			}

			// South side
			for(int x = radius-1; x >= -radius && !done; x--) {
				for(int y = -radius+1; y < radius && !done; y++) {
					safeLocation = startLocation.clone().add(x, y, radius);
					if(safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
						continue;
					}
					checked++;
					done = isSafe(safeLocation) || checked > maximumAttempts;
				}
			}

			// West side
			for(int z = radius-1; z >= -radius && !done; z--) {
				for(int y = -radius+1; y < radius && !done; y++) {
					safeLocation = startLocation.clone().add(-radius, y, z);
					if(safeLocation.getBlockY() > 256 || safeLocation.getBlockY() < 0) {
						continue;
					}
					checked++;
					done = isSafe(safeLocation) || checked > maximumAttempts;
				}
			}

			// Top side
			if(startLocation.getBlockY()+radius < 256 && !done) {
				// Middle block of the top
				safeLocation = startLocation.clone().add(0, radius, 0);
				checked++;
				done = isSafe(safeLocation) || checked > maximumAttempts;
				// Blocks around it
				for(int r = 1; r <= radius && !done; r++) {
					// North
					for(int x = -r+1; x <= r && !done; x++) {
						safeLocation = startLocation.clone().add(x, radius, -r);
						checked++;
						done = isSafe(safeLocation) || checked > maximumAttempts;
					}
					// East
					for(int z = -r+1; z <= r && !done; z++) {
						safeLocation = startLocation.clone().add(r, radius, z);
						checked++;
						done = isSafe(safeLocation) || checked > maximumAttempts;
					}
					// South side
					for(int x = r-1; x >= -r && !done; x--) {
						safeLocation = startLocation.clone().add(x, radius, r);
						checked++;
						done = isSafe(safeLocation) || checked > maximumAttempts;
					}
					// West side
					for(int z = r-1; z >= -r && !done; z--) {
						safeLocation = startLocation.clone().add(-r, radius, z);
						checked++;
						done = isSafe(safeLocation) || checked > maximumAttempts;
					}
				}
			}

			// Bottom side
			if(startLocation.getBlockY()-radius >= 0 && !done) {
				// Middle block of the bottom
				safeLocation = startLocation.clone().add(0, -radius, 0);
				checked++;
				done = isSafe(safeLocation) || checked > maximumAttempts;
				// Blocks around it
				for(int r = 1; r <= radius && !done; r++) {
					// North
					for(int x = -r+1; x <= r && !done; x++) {
						safeLocation = startLocation.clone().add(x, -radius, -r);
						checked++;
						done = isSafe(safeLocation) || checked > maximumAttempts;
					}
					// East
					for(int z = -r+1; z <= r && !done; z++) {
						safeLocation = startLocation.clone().add(r, -radius, z);
						checked++;
						done = isSafe(safeLocation) || checked > maximumAttempts;
					}
					// South side
					for(int x = r-1; x >= -r && !done; x--) {
						safeLocation = startLocation.clone().add(x, -radius, r);
						checked++;
						done = isSafe(safeLocation) || checked > maximumAttempts;
					}
					// West side
					for(int z = r-1; z >= -r && !done; z--) {
						safeLocation = startLocation.clone().add(-r, -radius, z);
						checked++;
						done = isSafe(safeLocation) || checked > maximumAttempts;
					}
				}
			}

			// Increase cube radius
			radius++;
		}

		// Either found safe location or ran out of attempts
		if(isSafe(safeLocation)) {
			player.teleport(safeLocation);
			//GoCraft.debug("Found location: "+safeLocation.toString()+" Tries: "+(checked-1));
			return true;
		} else {
			GoCraft.debug("No location found, checked "+(checked-1)+" spots of max "+maximumAttempts);
			return false;
		}
	}

	/**
	 * Get the radius of the world
	 * @param world The world to get it for
	 * @return The maximum radius from spawn to teleport from
	 */
	public static int getWorldRadius(World world) {
		int result = -1;
		ConfigurationSection section = GoCraft.getInstance().getConfig().getConfigurationSection("worlds");
		if(section != null) {
			result = section.getInt(world.getName()+".radius");
		}
		return result;
	}

	/**
	 * Load player data of a (possibly) offline player by UUID
	 * @param uuid The UUID of the player to load
	 * @return The Player object of the target player
	 */
	public static Player loadPlayer(UUID uuid) {
		return GoCraft.getInstance().getSpecificUtils().loadPlayer(uuid);
	}

	/**
	 * Load player data of a (possibly) offline player by name
	 * @param name The name of the player to get
	 * @return The Player object of the target player
	 */
	public static Player loadPlayer(String name) {
		Player result = null;
		OfflinePlayer player = Bukkit.getOfflinePlayer(name);
		if (player != null) {
			result = loadPlayer(player.getUniqueId());
		}
		return result;
	}

	/**
	 * Convert milliseconds to a human readable format
	 * @param milliseconds The amount of milliseconds to convert
	 * @return A formatted string based on the language file
	 */
	public static String millisToHumanFormat(long milliseconds) {
		// Flip to time ago when negative
		Message ago = Message.empty();
		if(milliseconds < 0) {
			ago = Message.fromKey("timeleft-ago");
			milliseconds *= -1;
		}

		long timeLeft = milliseconds+500;
		// To seconds
		timeLeft = timeLeft/1000;
		if(timeLeft <= 0) {
			return Message.fromKey("timeleft-ended").getPlain();
		} else if(timeLeft == 1) {
			return Message.fromKey("timeleft-second").replacements(timeLeft, ago).getPlain();
		} else if(timeLeft <= 120) {
			return Message.fromKey("timeleft-seconds").replacements(timeLeft, ago).getPlain();
		}
		// To minutes
		timeLeft = timeLeft/60;
		if(timeLeft <= 120) {
			return Message.fromKey("timeleft-minutes").replacements(timeLeft, ago).getPlain();
		}
		// To hours
		timeLeft = timeLeft/60;
		if(timeLeft <= 48) {
			return Message.fromKey("timeleft-hours").replacements(timeLeft, ago).getPlain();
		}
		// To days
		timeLeft = timeLeft/24;
		if(timeLeft <= 60) {
			return Message.fromKey("timeleft-days").replacements(timeLeft, ago).getPlain();
		}
		// To months
		timeLeft = timeLeft/30;
		if(timeLeft <= 24) {
			return Message.fromKey("timeleft-months").replacements(timeLeft, ago).getPlain();
		}
		// To years
		timeLeft = timeLeft/12;
		return Message.fromKey("timeleft-years").replacements(timeLeft, ago).getPlain();
	}

	/**
	 * Checks if the string is a correct time period
	 * @param time String that has to be checked
	 * @return true if format is correct, false if not
	 */
	public static boolean checkDuration(String time) {
		// Check if the string is not empty and check the length
		if (time == null || time.length() <= 1 || time.indexOf(' ') == -1 || time.indexOf(' ') >= (time.length() - 1)) {
			return false;
		}

		// Check if the suffix is one of these values
		String suffix = time.substring(time.indexOf(' ') + 1, time.length());
		if (!identifiers.contains(suffix)) {
			return false;
		}

		// check if the part before the space is a number
		String prefix = time.substring(0, (time.indexOf(' ')));
		return prefix.matches("\\d+");
	}

	/**
	 * Methode to tranlate a duration string to a millisecond value
	 * @param duration The duration string
	 * @return The duration in milliseconds translated from the durationstring, or if it is invalid then 0
	 */
	public static long durationStringToLong(String duration) {
		if (duration == null) {
			return 0;
		} else if (duration.equalsIgnoreCase("disabled") || duration.equalsIgnoreCase("unlimited")) {
			return -1;
		} else if (duration.indexOf(' ') == -1) {
			return 0;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(0);

		String durationString = duration.substring(duration.indexOf(' ') + 1, duration.length());
		int durationInt = 0;
		try {
			durationInt = Integer.parseInt(duration.substring(0, duration.indexOf(' ')));
		} catch (NumberFormatException exception) {
			// No Number found, add zero
		}

		if (seconds.contains(durationString)) {
			calendar.add(Calendar.SECOND, durationInt);
		} else if (minutes.contains(durationString)) {
			calendar.add(Calendar.MINUTE, durationInt);
		} else if (hours.contains(durationString)) {
			calendar.add(Calendar.HOUR, durationInt);
		} else if (days.contains(durationString)) {
			calendar.add(Calendar.DAY_OF_MONTH, durationInt);
		} else if (weeks.contains(durationString)) {
			calendar.add(Calendar.DAY_OF_MONTH, durationInt * 7);
		} else if (months.contains(durationString)) {
			calendar.add(Calendar.MONTH, durationInt);
		} else if (years.contains(durationString)) {
			calendar.add(Calendar.YEAR, durationInt);
		}
		return calendar.getTimeInMillis();
	}

	/**
	 * Get a string showing how long ago a time is
	 * @param timestamp The time to show
	 * @return Indication how long ago this is
	 */
	public static Message agoMessage(long timestamp) {
		return Message.fromKey("ago").replacements(millisToHumanFormat(timestamp-Calendar.getInstance().getTimeInMillis()), Utils.longTimeString(timestamp));
	}

	/**
	 * Get a timestamp in a long readable format
	 * @param timestamp The time to display
	 * @return An absolute time indication
	 */
	public static String longTimeString(long timestamp) {
		return GoCraft.longTimeFormat.format(timestamp);
	}

	/**
	 * Get a timestamp in a short readable format
	 * @param timestamp The time to display
	 * @return An absolute time indication
	 */
	public static String shorTimeString(long timestamp) {
		return GoCraft.shortTimeFormat.format(timestamp);
	}

	/**
	 * Check if a player is in PVP area
	 * @param player The player to check
	 * @return true if the player is in a PVP area, otherwise false
	 */
	public static boolean isInPvpArea(Player player) {
		return GoCraft.getInstance().getWorldGuardLink() != null && GoCraft.getInstance().getWorldGuardLink().get()
				.getRegionManager(player.getWorld())
				.getApplicableRegions(player.getLocation())
				.testState(GoCraft.getInstance().getWorldGuardLink().get().wrapPlayer(player), DefaultFlag.PVP);
	}

	/**
	 * Format the currency amount with the characters before and after
	 * @param amount Amount of money to format
	 * @return Currency character format string
	 */
	public static String formatCurrency(double amount) {
		String before = GoCraft.getInstance().getConfig().getString("moneyCharacter");
		before = before.replace("%euro%", "\u20ac");
		String after = GoCraft.getInstance().getConfig().getString("moneyCharacterAfter");
		after = after.replace("%euro%", "\u20ac");
		String result;
		// Check for infinite and NaN
		if (Double.isInfinite(amount)) {
			result = "\u221E"; // Infinite symbol
		} else if (Double.isNaN(amount)) {
			result = "NaN";
		} else {
			// Add metric
			double metricAbove = GoCraft.getInstance().getConfig().getDouble("metricSuffixesAbove");
			if (metricAbove != -1 && amount >= metricAbove) {
				if (amount >= 1000000000000000000000000.0) {
					amount = amount / 1000000000000000000000000.0;
					after = "Y" + after;
				} else if (amount >= 1000000000000000000000.0) {
					amount = amount / 1000000000000000000000.0;
					after = "Z" + after;
				} else if (amount >= 1000000000000000000.0) {
					amount = amount / 1000000000000000000.0;
					after = "E" + after;
				} else if (amount >= 1000000000000000.0) {
					amount = amount / 1000000000000000.0;
					after = "P" + after;
				} else if (amount >= 1000000000000.0) {
					amount = amount / 1000000000000.0;
					after = "T" + after;
				} else if (amount >= 1000000000.0) {
					amount = amount / 1000000000.0;
					after = "G" + after;
				} else if (amount >= 1000000.0) {
					amount = amount / 1000000.0;
					after = "M" + after;
				} else if (amount >= 1000.0) {
					amount = amount / 1000.0;
					after = "k" + after;
				}
				BigDecimal bigDecimal = new BigDecimal(amount);
				if (bigDecimal.toString().contains(".")) {
					int frontLength = bigDecimal.toString().substring(0, bigDecimal.toString().indexOf('.')).length();
					bigDecimal = bigDecimal.setScale(GoCraft.getInstance().getConfig().getInt("fractionalNumbers") + (3 - frontLength), RoundingMode.HALF_UP);
				}
				result = bigDecimal.toString();
			} else {
				BigDecimal bigDecimal = new BigDecimal(amount);
				bigDecimal = bigDecimal.setScale(GoCraft.getInstance().getConfig().getInt("fractionalNumbers"), RoundingMode.HALF_UP);
				amount = bigDecimal.doubleValue();
				result = bigDecimal.toString();
				if (GoCraft.getInstance().getConfig().getBoolean("hideEmptyFractionalPart") && (amount % 1.0) == 0.0 && result.contains(".")) {
					result = result.substring(0, result.indexOf('.'));
				}
			}
		}
		result = result.replace(".", GoCraft.getInstance().getConfig().getString("decimalMark"));
		return before + result + after;
	}

	/**
	 * Convert color and formatting codes to bukkit values
	 * @param input Start string with color and formatting codes in it
	 * @return String with the color and formatting codes in the bukkit format
	 */
	public static String applyColors(String input) {
		String result = null;
		if(input != null) {
			result = ChatColor.translateAlternateColorCodes('&', input);
		}
		return result;
	}

	/**
	 * Get the lowest ranked group from a comma-separated list of groups
	 * @param groups The groups to check
	 * @return The identifier of the lowest group
	 */
	public static String getLowestGroup(String groups) {
		if(groups == null) {
			return null;
		}
		String[] splits = groups.split(",( )?");
		ConfigurationSection ranksSection = GoCraft.getInstance().getGeneralConfig().getConfigurationSection("ranks");
		if(ranksSection == null) {
			return null;
		}
		String result = null;
		int currentOrder = -1;
		for(String split : splits) {
			int current = 0;
			for(String rank : ranksSection.getKeys(false)) {
				String rankName = ranksSection.getString(rank+".name");
				if(rank.equalsIgnoreCase(split) || (rankName != null && rankName.equalsIgnoreCase(split))) {
					if(current > currentOrder) {
						currentOrder = current;
						result = rank;
					}
					break;
				}
				current++;
			}
		}
		return result;
	}

	/**
	 * Get the highest ranked group from a comma-separated list of groups
	 * @param groups The groups to check
	 * @return The identifier of the highest group
	 */
	public static String getHighestGroup(String groups) {
		if(groups == null) {
			return null;
		}
		String[] splits = groups.split(",( )?");
		ConfigurationSection ranksSection = GoCraft.getInstance().getGeneralConfig().getConfigurationSection("ranks");
		if(ranksSection == null) {
			return null;
		}
		String result = null;
		int currentOrder = -1;
		for(String split : splits) {
			int current = Integer.MAX_VALUE;
			for(String rank : ranksSection.getKeys(false)) {
				String rankName = ranksSection.getString(rank+".name");
				if(rank.equalsIgnoreCase(split) || (rankName != null && rankName.equalsIgnoreCase(split))) {
					if(current > currentOrder) {
						currentOrder = current;
						result = rank;
					}
					break;
				}
				current--;
			}
		}
		return result;
	}

	/**
	 * Play a sound to a player
	 * Old to new sound: https://minecraft.gamepedia.com/Sounds.json
	 * @param player Player to play it to
	 * @param oldSound Name of the old sound
	 * @param newSound Name of the new sound
	 * @param volume Volume of the sound
	 * @param pitch Pitch of the sound
	 */
	public static void playSound(Player player, String oldSound, String newSound, float volume, float pitch) {
		if (Bukkit.getBukkitVersion().startsWith("1.8")) {
			player.playSound(player.getLocation(), Sound.valueOf(oldSound.replace(".", "_").toUpperCase()), volume, pitch);
		} else {
			player.playSound(player.getLocation(), Sound.valueOf(newSound.replace(".", "_").toUpperCase()), volume, pitch);
		}
	}

	/**
	 * Get a random number between two values
	 * @param one The first value (highest or lowest)
	 * @param two The second value (highest or lowest)
	 * @return A random value between the two provided values (boundaries included)
	 */
	public static int getRandomBetween(int one, int two) {
		int min = Math.min(one, two);
		int max = Math.max(one, two);
		return random.nextInt(max - min + 1) + min;
	}


	private static final List<Alphabet> letters = Collections.unmodifiableList(Arrays.asList(Alphabet.values()));
	private enum Alphabet {
		a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z
	}
	private static String getRandomLetter() {
		return (letters.get(Utils.random.nextInt(letters.size()))).name();
	}

	/**
	 * Get a random player name
	 * @return A random playername between 3 and 14 characters
	 */
	public static String randomName() {
		StringBuilder stringBuilder = new StringBuilder();
		for (int i = 3 + Utils.random.nextInt(9); i > 0; i--) {
			stringBuilder.append(getRandomLetter());
		}
		stringBuilder.append(Utils.random.nextInt(99));
		return stringBuilder.toString();
	}

	/**
	 * Get a start of the message with a maximum length
	 * @param message       The message
	 * @param maximumLength The maximum length to return
	 * @return The start of the message with at most maximumLength characters
	 */
	public static String getMessageStart(Message message, int maximumLength) {
		String messageStart = "";
		for(int i = 0; i < message.getRaw().size() && messageStart.length() < maximumLength; i++) {
			messageStart += message.getRaw().get(i).substring(0, Math.min(maximumLength, message.getRaw().get(i).length()));
		}
		return messageStart.substring(0, Math.min(maximumLength, messageStart.length()));
	}

	/**
	 * Get a string list from the config, with fallback to single string as list
	 * @param section The section to look in
	 * @param key     The key in the section to get
	 * @return A list
	 */
	public static List<String> listOrSingle(ConfigurationSection section, String key) {
		if(section.isList(key)) {
			return section.getStringList(key);
		} else if(section.isSet(key)) {
			return new ArrayList<>(Collections.singletonList(section.getString(key)));
		}
		return null;
	}

	/**
	 * Check if an input is numeric
	 * @param input The input to check
	 * @return true if the input is numeric, otherwise false
	 */
	public static boolean isNumeric(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch(NumberFormatException ignored) {
			return false;
		}
	}


	public static Message locationMessage(World world, double x, double y, double z) {
		return Message.fromKey("location").replacements(world.getName(), Math.round(x), Math.round(y), Math.round(z));
	}

	public static Message locationMessage(World world, double x, double y, double z, float yaw, float pitch) {
		return Message.fromKey("locationRotation").replacements(world.getName(), Math.round(x), Math.round(y), Math.round(z), Math.round(yaw), Math.round(pitch));
	}

}
