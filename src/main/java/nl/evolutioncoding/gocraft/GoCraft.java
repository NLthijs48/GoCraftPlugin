package nl.evolutioncoding.gocraft;

import com.google.common.base.Charsets;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import nl.evolutioncoding.gocraft.blocks.*;
import nl.evolutioncoding.gocraft.commands.PingCommand;
import nl.evolutioncoding.gocraft.commands.SetspawnCommand;
import nl.evolutioncoding.gocraft.commands.StaffMessagesCommands;
import nl.evolutioncoding.gocraft.commands.TempbanCommand;
import nl.evolutioncoding.gocraft.general.*;
import nl.evolutioncoding.gocraft.items.*;
import nl.evolutioncoding.gocraft.logging.LogSigns;
import nl.evolutioncoding.gocraft.other.ResetExpiredPlots;
import nl.evolutioncoding.gocraft.pvp.DisableFallDamage;
import nl.evolutioncoding.gocraft.pvp.DisablePlayerDamage;
import nl.evolutioncoding.gocraft.storage.Database;
import nl.evolutioncoding.gocraft.storage.MySQLDatabase;
import nl.evolutioncoding.gocraft.storage.UTF8Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.*;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class GoCraft extends JavaPlugin {
	public static final String signLog = "signs";
	public static final String generalFolderName = "GENERAL";
	public static final String generalConfigName = "config.yml";
	private ArrayList<Listener> listeners;
	private LanguageManager languageManager;
	private WorldGuardPlugin worldGuard = null;
	private boolean debug = false;
	private String chatprefix = null;
	private static GoCraft instance = null;
	private UTF8Config generalConfig = null;
	private UTF8Config localStorage = null;
	private File generalFolder = null;

	public void onEnable() {
		instance = this;
		saveDefaultConfig();
		this.chatprefix = getConfig().getString("chatPrefix");
		this.debug = getConfig().getBoolean("debug");

		Plugin wg = getServer().getPluginManager().getPlugin("WorldGuard");
		if (wg == null || !(wg instanceof WorldGuardPlugin)) {
			getLogger().warning("Error: WorldGuard plugin is not present or has not loaded correctly");
		} else {
			this.worldGuard = ((WorldGuardPlugin) wg);
		}
		this.languageManager = new LanguageManager(this);

		generalFolder = new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParent() + File.separator + generalFolderName);

		loadGeneralConfig();
		loadLocalStorage();
		addListeners();
	}

	public static GoCraft getInstance() {
		return instance;
	}
	
	public void onDisable() {
	}


	/**
	 * Get the name of this server
	 * @return The display name of this server
	 */
	public String getServerName() {
		String result = getDataFolder().getAbsoluteFile().getParentFile().getParent().replace(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParent(), "");
		if(result != null) {
			result = result.substring(1);
			String realName = getGeneralConfig().getString("server." + result + ".name");
			if(realName != null) {
				result = realName;
			}
		}
		if(result == null || result.length() == 0) {
			result = "UNKNOWN";
		}
		return result;
	}


	/**
	 * Get the generalConfig config file
	 *
	 * @return The UTF8Config generalConfig file
	 */
	public UTF8Config getGeneralConfig() {
		return generalConfig;
	}

	/**
	 * Get the localStorage config file
	 * @return The UTF8Config localStorage file
	 */
	public UTF8Config getLocalStorage() {
		return localStorage;
	}
	
	public void testStorage() {
		String host = "localhost";
		int port = 3306;
		String database = "testdb";
		String username = "root";
		String password = "";
		Database db = new MySQLDatabase(host, port, database, username, password, this);
		debug("is connected: "+db.isConnected());
		PreparedStatement statement = db.prepareStatement("CREATE TABLE IF NOT EXISTS combat (kills int, deaths int)");
		db.execute(statement);
		// ORDER not kept! running as async tasks
		for(int i=0; i<10; i++) {
			debug("Doing: i="+i);
			db.execute("INSERT INTO combat VALUES (?, ?)", i, 10-i);
		}
	}
	
	
	public void addListeners() {
		this.listeners = new ArrayList<>();
		// Blocks
		this.listeners.add(new DisableBedrockBreak(this));
		this.listeners.add(new DisableBedrockPlace(this));
		this.listeners.add(new DisableDispensers(this));
		this.listeners.add(new DisableTradeSignPlacing(this));
		this.listeners.add(new DisableBlockBreaking(this));
		this.listeners.add(new DisableAnvilBreak(this));
		// General
		this.listeners.add(new DisableRain(this));
		this.listeners.add(new DisableMobSpawning(this));
		this.listeners.add(new DisableHungerLoss(this));
		this.listeners.add(new EnablePotionEffectsOnJoin(this));
		this.listeners.add(new SpawnTeleport(this));
		this.listeners.add(new EnableRegionPotionEffects(this));
		// Items
		this.listeners.add(new DisableItemDrops(this));
		this.listeners.add(new DisableItemSpawning(this));
		this.listeners.add(new DisablePotionSplash(this));
		this.listeners.add(new DisablePotionThrow(this));
		this.listeners.add(new DisablePotionInvisibleDrink(this));
		this.listeners.add(new DisableXpBottleThrow(this));
		this.listeners.add(new DisableFirework(this));
		this.listeners.add(new DisableEnderpearl(this));
		this.listeners.add(new DisableEyeOfEnder(this));
		this.listeners.add(new DisableBooks(this));
		// Logging
		this.listeners.add(new LogSigns(this));
		// PVP
		this.listeners.add(new DisablePlayerDamage(this));
		this.listeners.add(new DisableFallDamage(this));
		// Commands
		this.listeners.add(new TempbanCommand(this));		
		new PingCommand(this);
		new SetspawnCommand(this);
		new StaffMessagesCommands(this);
		// Other
		this.listeners.add(new ResetExpiredPlots(this));
	}

	public WorldGuardPlugin getWorldGuard() {
		return this.worldGuard;
	}

	public void showHelp(CommandSender target) {
		List<String> messages = new ArrayList<>();
		messages.add(this.chatprefix + getLanguageManager().getLang("help-header"));
		messages.add(this.chatprefix + getLanguageManager().getLang("help-alias"));
		if (target.hasPermission("gocraft.resetstats")) {
			messages.add(getLanguageManager().getLang("help-resetstats"));
		}
		if (target.hasPermission("gocraft.resetall")) {
			messages.add(getLanguageManager().getLang("help-resetall"));
		}
		messages.add(getLanguageManager().getLang("help-stats"));
		for (String message : messages) {
			target.sendMessage(fixColors(message));
		}
	}

	public void deRegisterEvents() {
		for (Listener listener : this.listeners) {
			HandlerList.unregisterAll(listener);
		}
	}

	public void logLine(String fileName, String message) {
		File file = new File(getDataFolder() + File.separator + "logs" + File.separator);
		if (!file.exists()) {
			if(!file.mkdirs()) {
				getLogger().warning("Could not create directories leading to logs folder: " + file.getAbsolutePath());
				return;
			}
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file + File.separator + fileName + ".log", true));
			out.append(message);
			out.newLine();
			out.close();
		} catch (IOException e) {
			getLogger().info("Writing to the file failed: " + file + File.separator + fileName + ".log");
		}
	}

	public boolean onThisWorld(String configLine, Object world) {
		boolean result = true;
		List<String> worlds = getConfig().getStringList(configLine + "Worlds");
		if (worlds.size() != 0) {
			String worldString = "";
			if ((world instanceof String)) {
				worldString = (String) world;
			} else if ((world instanceof World)) {
				worldString = ((World) world).getName();
			} else if ((world instanceof Block)) {
				worldString = ((Block) world).getWorld().getName();
			} else if ((world instanceof Location)) {
				worldString = ((Location) world).getWorld().getName();
			} else if ((world instanceof Entity)) {
				worldString = ((Entity) world).getWorld().getName();
			} else {
				debug("Cannot get world from object: " + world.toString());
			}
			if (!worlds.contains(worldString)) {
				result = false;
			}
		}
		return result;
	}

	public LanguageManager getLanguageManager() {
		return this.languageManager;
	}

	public void message(Object target, String key, Object... params) {
		String langString = fixColors(this.languageManager.getLang(key, params));
		if (langString == null) {
			getLogger().info("Something is wrong with the language file, could not find key: " + key);
		} else if ((target instanceof Player)) {
			((Player)target).sendMessage(fixColors(this.chatprefix) + langString);
		} else if ((target instanceof CommandSender)) {
			((CommandSender) target).sendMessage(langString);
		} else if ((target instanceof Logger)) {
			((Logger) target).info(langString);
		} else {
			getLogger().info("Could not send message, target is wrong: " + langString);
		}
	}

	public String fixColors(String input) {
		String result = null;
		if (input != null) {
			result = input.replaceAll("(&([a-f0-9]))", "§$2");
			result = result.replaceAll("&k", ChatColor.MAGIC.toString());
			result = result.replaceAll("&l", ChatColor.BOLD.toString());
			result = result.replaceAll("&m", ChatColor.STRIKETHROUGH.toString());
			result = result.replaceAll("&n", ChatColor.UNDERLINE.toString());
			result = result.replaceAll("&o", ChatColor.ITALIC.toString());
			result = result.replaceAll("&r", ChatColor.RESET.toString());
		}
		return result;
	}
	
	/**
	 * Save the localStorage file
	 */
	public void saveLocalStorage() {
		File file = new File(this.getDataFolder(), "localStorage.yml");
		try {
			localStorage.save(file);
		} catch(IOException e) {
			this.getLogger().info("Failed to save localStorage.yml");
			e.printStackTrace();
		}
	}

	/**
	 * Load the localStorage.yml file
	 * @return true if it has been loaded successfully, otherwise false
	 */
	public boolean loadLocalStorage() {
		File localStorageFile = new File(this.getDataFolder(), "localStorage.yml");
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(localStorageFile), Charsets.UTF_8);
		} catch (FileNotFoundException e) {}
		if(reader != null) {
			localStorage = UTF8Config.loadConfiguration(reader);
			try {
				reader.close();
			} catch (IOException e) {}
		}
		if(localStorage == null) {
			localStorage = new UTF8Config();
		}
		return localStorage != null;
	}

	/**
	 * Load the generalConfig.yml file
	 *
	 * @return true if it has been loaded successfully, otherwise false
	 */
	public boolean loadGeneralConfig() {
		File commonConfigFile = new File(generalFolder, generalConfigName);
		InputStreamReader reader = null;
		try {
			reader = new InputStreamReader(new FileInputStream(commonConfigFile), Charsets.UTF_8);
		} catch(FileNotFoundException e) {
		}
		if(reader != null) {
			generalConfig = UTF8Config.loadConfiguration(reader);
			try {
				reader.close();
			} catch(IOException e) {
			}
		}
		if(generalConfig == null) {
			generalConfig = new UTF8Config();
		}
		return generalConfig != null;
	}

	public void _debug(String message) {
		if (this.debug) {
			getLogger().info("Debug: " + message);
		}
	}
	public static void debug(String message) {
		instance._debug(message);
	}

	public static String toName(String uuid) {
		if (uuid == null) {
			return null;
		}
		return toName(UUID.fromString(uuid));
	}

	public static String toName(UUID uuid) {
		if (uuid == null) {
			return null;
		}
		return Bukkit.getOfflinePlayer(uuid).getName();
	}

	@SuppressWarnings("deprecation")
	public static String toUUID(String name) {
		if (name == null) {
			return null;
		}
		return Bukkit.getOfflinePlayer(name).getUniqueId().toString();
	}
}
