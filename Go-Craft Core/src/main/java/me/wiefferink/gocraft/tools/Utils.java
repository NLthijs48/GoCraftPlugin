package me.wiefferink.gocraft.tools;

import com.sk89q.worldguard.protection.flags.DefaultFlag;
import me.wiefferink.gocraft.GoCraft;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;

public class Utils {

	private static ArrayList<Material> canSpawnIn = new ArrayList<>(Arrays.asList(Material.WOOD_DOOR, Material.WOODEN_DOOR, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE));
	private static ArrayList<Material> cannotSpawnOn = new ArrayList<>(Arrays.asList(Material.PISTON_EXTENSION, Material.PISTON_MOVING_PIECE, Material.SIGN_POST, Material.WALL_SIGN, Material.STONE_PLATE, Material.IRON_DOOR_BLOCK, Material.WOOD_PLATE, Material.TRAP_DOOR, Material.REDSTONE_LAMP_OFF, Material.REDSTONE_LAMP_ON, Material.CACTUS, Material.IRON_FENCE, Material.FENCE_GATE, Material.THIN_GLASS, Material.NETHER_FENCE, Material.DRAGON_EGG, Material.GOLD_PLATE, Material.IRON_PLATE, Material.STAINED_GLASS_PANE));
	private static ArrayList<Material> cannotSpawnBeside = new ArrayList<>(Arrays.asList(Material.LAVA, Material.STATIONARY_LAVA, Material.CACTUS));

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
		String result = GoCraft.getInstance().getLanguageManager().getLang("staffbroadcast-template", GoCraft.getInstance().getServerName(), type, message);
		// Display in console
		Bukkit.getConsoleSender().sendMessage(ChatColor.stripColor(fixColors(result)));
		// Send to other servers
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "sync console all displaystaffmessage " + result);
	}

	/**
	 * Display a message to all staff in this server
	 * @param type The type of message to indicate what the message is about
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
		message = fixColors(message);
		// Display to all staff members
		for (Player player : Bukkit.getOnlinePlayers()) {
			if (player.hasPermission("gocraft.staff")) {
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
	 * Check if a player is in PVP area
	 * @param player The player to check
	 * @return true if the player is in a PVP area, otherwise false
	 */
	public static boolean isInPvpArea(Player player) {
		return GoCraft.getInstance().getWorldGuardLink() != null && GoCraft.getInstance().getWorldGuardLink().get()
				.getRegionManager(Bukkit.getWorld("world"))
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
		before = before.replace(GoCraft.currencyEuro, "\u20ac");
		String after = GoCraft.getInstance().getConfig().getString("moneyCharacterAfter");
		after = after.replace(GoCraft.currencyEuro, "\u20ac");
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
	 * Translate color codes to the internal color values
	 * @param input The input string
	 * @return The string with Minecraft color codes
	 */
	public static String fixColors(String input) {
		String result = null;
		if (input != null) {
			result = input.replaceAll("(&([a-f0-9]))", "§$2");
			result = result.replace("&k", ChatColor.MAGIC.toString());
			result = result.replace("&l", ChatColor.BOLD.toString());
			result = result.replace("&m", ChatColor.STRIKETHROUGH.toString());
			result = result.replace("&n", ChatColor.UNDERLINE.toString());
			result = result.replace("&o", ChatColor.ITALIC.toString());
			result = result.replace("&r", ChatColor.RESET.toString());
		}
		return result;
	}

	// Helper function
	private static String getTargetGroup(String groups, boolean highest) {
		if (groups == null) {
			return null;
		}
		String result = null;
		String[] splits = groups.split(",( )?");
		ConfigurationSection ranksSection = GoCraft.getInstance().getGeneralConfig().getConfigurationSection("ranks");
		if (ranksSection == null) {
			return null;
		}
		int currentOrder = highest ? Integer.MAX_VALUE : -1;
		for (String split : splits) {
			int order = Integer.MAX_VALUE;
			int current = 0;
			for (String rank : ranksSection.getKeys(false)) {
				String rankName = ranksSection.getString(rank + ".name");
				if (rank.equalsIgnoreCase(split) || (rankName != null && rankName.equalsIgnoreCase(split))) {
					if ((!highest && current > currentOrder)
							|| (highest && current < currentOrder)) {
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
	 * Get the lowest ranked group from a comma-separated list of groups
	 * @param groups The groups to check
	 * @return The identifier of the lowest group
	 */
	public static String getLowestGroup(String groups) {
		return getTargetGroup(groups, false);
	}

	/**
	 * Get the highest ranked group from a comma-separated list of groups
	 * @param groups The groups to check
	 * @return The identifier of the highest group
	 */
	public static String getHighestGroup(String groups) {
		return getTargetGroup(groups, true);
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
		a, b, c, d, e, f, g, h, i, j, k, l, m, n, o, p, q, r, s, t, u, v, w, x, y, z;
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

}
