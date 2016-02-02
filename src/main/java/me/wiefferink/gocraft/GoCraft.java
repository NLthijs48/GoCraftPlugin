package me.wiefferink.gocraft;

import com.google.common.base.Charsets;
import me.wiefferink.gocraft.blocks.*;
import me.wiefferink.gocraft.commands.*;
import me.wiefferink.gocraft.distribution.DistributionManager;
import me.wiefferink.gocraft.general.*;
import me.wiefferink.gocraft.inspector.InspectionManager;
import me.wiefferink.gocraft.integration.*;
import me.wiefferink.gocraft.items.*;
import me.wiefferink.gocraft.logging.LogSigns;
import me.wiefferink.gocraft.other.AboveNetherPrevention;
import me.wiefferink.gocraft.other.ResetExpiredPlots;
import me.wiefferink.gocraft.pvp.DisableFallDamage;
import me.wiefferink.gocraft.pvp.DisablePlayerDamage;
import me.wiefferink.gocraft.storage.Database;
import me.wiefferink.gocraft.storage.MySQLDatabase;
import me.wiefferink.gocraft.storage.UTF8Config;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public final class GoCraft extends JavaPlugin {
	// Constants
	public static final String signLog = "signs";
	public static final String generalFolderName = "GENERAL";
	public static final String generalConfigName = "config.yml";
	public static final String generalPluginDataFoldername = "plugins";
	// Variables
	private ArrayList<Listener> listeners;
	private LanguageManager languageManager;
	private DistributionManager distributionManager;
	private InspectionManager inspectionManager;
	private boolean debug = false;
	private String chatprefix = null;
	private static GoCraft instance = null;
	// Config files
	private UTF8Config generalConfig = null;
	private UTF8Config localStorage = null;
	private File generalFolder = null;
	// Dependencies
	private Economy economy = null;
	private MapSwitcherLink mapSwitcherLink = null;
	private GoPVPLink goPVPLink = null;
	private EssentialsLink essentialsLink = null;
	private WorldGuardLink worldGuardLink = null;
	private BanManagerLink banManagerLink = null;

	private boolean dynMapInstalled = false;

	public void onEnable() {
		reloadConfig();
		instance = this;
		saveDefaultConfig();
		this.chatprefix = getConfig().getString("chatPrefix");
		this.debug = getConfig().getBoolean("debug");

		// Check if WorldGuard is present
		Plugin wg = getServer().getPluginManager().getPlugin("WorldGuard");
		if (wg == null) {
			debug("  No WorldGuard plugin found");
		} else {
			this.worldGuardLink = new WorldGuardLink();
		}

		// Check if BanManager is present
		Plugin bm = getServer().getPluginManager().getPlugin("BanManager");
		if (bm == null) {
			debug("  No BanManager plugin found");
		} else {
			banManagerLink = new BanManagerLink();
		}

		// Check if MapSwitcher is present
		Plugin ms = getServer().getPluginManager().getPlugin("MapSwitcher");
		if (ms == null) {
			debug("  No MapSwitcher plugin found");
		} else {
			mapSwitcherLink = new MapSwitcherLink();
		}

		// Check if Essentials is present
		Plugin es = getServer().getPluginManager().getPlugin("Essentials");
		if (ms == null) {
			debug("  No Essentails plugin found");
		} else {
			essentialsLink = new EssentialsLink();
		}

		// Check if Vault is present
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		} else {
			getLogger().info("Error: Vault or the Economy plugin is not present or has not loaded correctly");
		}

		// Check if DynMap is present
		Plugin dm = getServer().getPluginManager().getPlugin("dynmap");
		dynMapInstalled = dm != null;

		// Check if GoPVP is present
		new BukkitRunnable() {
			public void run() {
				Plugin gp = getServer().getPluginManager().getPlugin("GoPVP");
				if (gp == null) {
					debug("  No GoPVP plugin found");
				} else {
					goPVPLink = new GoPVPLink();
				}
			}
		}.runTask(this);

		this.languageManager = new LanguageManager(this);
		generalFolder = new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParent() + File.separator + generalFolderName);
		loadGeneralConfig();
		distributionManager = new DistributionManager(this);
		loadLocalStorage();

		inspectionManager = new InspectionManager(this);
		addListeners();
	}

	/**
	 * Get the GoCraft instance
	 *
	 * @return The GoCraft instance
	 */
	public static GoCraft getInstance() {
		return instance;
	}
	
	public void onDisable() {
		getDistributionManager().updatePluginDataNow(Bukkit.getConsoleSender(), getServerName(), "pluginJar, pluginConfig, permissions");
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
	}


	/**
	 * Get the name of this server
	 * @return The display name of this server
	 */
	public String getServerName() {
		return getServerName(null);
	}

	/**
	 * Get the name of this server
	 * @param result The id of the server to get the name for
	 * @return The display name of this server
	 */
	public String getServerName(String result) {
		if(result == null) {
			result = getDataFolder().getAbsoluteFile().getParentFile().getParent().replace(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParent(), "");
			if(result != null) {
				result = result.substring(1);
			}
		}
		if(result != null) {
			//GoCraft.debug("generalconfig null="+(getGeneralConfig()==null));
			ConfigurationSection servers = getGeneralConfig().getConfigurationSection("servers");
			if(servers != null) {
				for(String id : servers.getKeys(false)) {
					if(result.equalsIgnoreCase(id)
							|| result.equalsIgnoreCase(servers.getString(id+".name"))
							|| result.equalsIgnoreCase(servers.getString(id+".directory"))) {
						result = servers.getString(id+".name");
					}
				}
			}
		}
		if(result == null || result.length() == 0) {
			result = "UNKNOWN";
		}
		return result;
	}

	/**
	 * Get the id of this server for use with the general config
	 *
	 * @return The id of this server
	 */
	public String getServerId() {
		String result = getDataFolder().getAbsoluteFile().getParentFile().getParent().replace(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParent(), "");
		if (result != null) {
			result = result.substring(1);
		}
		if (result != null) {
			ConfigurationSection servers = getGeneralConfig().getConfigurationSection("servers");
			if (servers != null) {
				for (String id : servers.getKeys(false)) {
					if (result.equalsIgnoreCase(id)
							|| result.equalsIgnoreCase(servers.getString(id + ".name"))
							|| result.equalsIgnoreCase(servers.getString(id + ".directory"))) {
						result = id;
					}
				}
			}
		}
		if (result == null || result.length() == 0) {
			result = "UNKNOWN";
		}
		return result;
	}

	/**
	 * Get the folder where the general files are located
	 * @return The folder where the general files are located
	 */
	public File getGeneralFolder() {
		return generalFolder;
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

	/**
	 * Get the link to the MapSwitcher plugin
	 *
	 * @return MapSwitcherLink
	 */
	public MapSwitcherLink getMapSwitcherLink() {
		return mapSwitcherLink;
	}

	/**
	 * Get the link to the GoPVP plugin
	 *
	 * @return GoPVPLink
	 */
	public GoPVPLink getGoPVPLink() {
		return goPVPLink;
	}

	/**
	 * Get the link to the Essentials plugin
	 *
	 * @return EssentialsLink
	 */
	public EssentialsLink getEssentialsLink() {
		return essentialsLink;
	}

	/**
	 * Get the link to the BanManager plugin
	 *
	 * @return BanManagerLink
	 */
	public BanManagerLink getBanManagerLink() {
		return banManagerLink;
	}


	/**
	 * Get the link to the WorldGuard plugin
	 *
	 * @return WorldGuardLink
	 */
	public WorldGuardLink getWorldGuardLink() {
		return this.worldGuardLink;
	}

	/**
	 * Check if DynMap is installed
	 *
	 * @return true if DynMap is installed, otherwise false
	 */
	public boolean dynMapInstalled() {
		return dynMapInstalled;
	}

	/**
	 * Get the Economy running on the server
	 *
	 * @return The Economy provider
	 */
	public Economy getEconomy() {
		return economy;
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
			db.execute("INSERT INTO combat VALUES (?, ?)", i, 10 - i);
		}
	}

	/**
	 * Register all listener and command classes
	 */
	public void addListeners() {
		this.listeners = new ArrayList<>();
		// Blocks
		this.listeners.add(new DisableBedrockBreak(this));
		this.listeners.add(new DisableBedrockPlace(this));
		this.listeners.add(new DisableDispensers(this));
		this.listeners.add(new DisableTradeSignPlacing(this));
		this.listeners.add(new DisableBlockBreaking(this));
		this.listeners.add(new DisableAnvilBreak(this));
		this.listeners.add(new DisableWitherDamage(this));
		// General
		this.listeners.add(new DisableRain(this));
		this.listeners.add(new DisableMobSpawning(this));
		this.listeners.add(new DisableHungerLoss(this));
		this.listeners.add(new EnablePotionEffectsOnJoin(this));
		this.listeners.add(new SpawnTeleport(this));
		this.listeners.add(new EnableRegionPotionEffects(this));
		if (getBanManagerLink() != null) {
			this.listeners.add(new PunishmentNotifications(this));
		}
		this.listeners.add(new DisableStaffJoinLeaveMessages(this));
		this.listeners.add(new OpenenderLimiter(this));
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
		new UpdateCommand(this);
		new ReloadCommand(this);
		new InspectCommand(this);
		new RulesCommand(this);
		new HelpCommand(this);
		// Other
		this.listeners.add(new ResetExpiredPlots(this));
		this.listeners.add(new AboveNetherPrevention(this));
	}

	/**
	 * Get the InspectionManager
	 *
	 * @return The InspectionManager
	 */
	public InspectionManager getInspectionManager() {
		return inspectionManager;
	}

	/**
	 * Set the inspection manager, used to get startup behavior correct
	 *
	 * @param manager The manager to set
	 */
	public void setInspectionManager(InspectionManager manager) {
		inspectionManager = manager;
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

	/**
	 * Reload the plugin data
	 */
	public void reload() {
		onDisable();
		onEnable();
	}

	/**
	 * Get the DistributionManager
	 * @return The DistributionManager
	 */
	public DistributionManager getDistributionManager() {
		return distributionManager;
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

	public void configurableMessage(String prefix, Object target, String key, Object... params) {
		String langString = fixColors(this.languageManager.getLang(key, params));
		if (langString == null) {
			getLogger().info("Something is wrong with the language file, could not find key: " + key);
		} else if ((target instanceof Player)) {
			String message = langString;
			if (prefix != null) {
				message = prefix + message;
			}
			message = fixColors(message);
			((Player) target).sendMessage(message);
		} else if ((target instanceof CommandSender)) {
			((CommandSender) target).sendMessage(langString);
		} else if ((target instanceof Logger)) {
			((Logger) target).info(ChatColor.stripColor(langString));
		} else if (target instanceof BufferedWriter) {
			try {
				if (prefix != null) {
					langString = prefix + langString;
				}
				((BufferedWriter) target).append(ChatColor.stripColor(langString));
				((BufferedWriter) target).newLine();
			} catch (IOException e) {
				getLogger().warning("Error while printing message to BufferedWriter: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			getLogger().info("Could not send message, target is wrong: " + langString);
		}
	}

	public void message(Object target, String key, Object... params) {
		configurableMessage(this.chatprefix, target, key, params);
	}

	public void messageNoPrefix(Object target, String key, Object... params) {
		configurableMessage(null, target, key, params);
	}

	public String fixColors(String input) {
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
		if(localStorageFile.exists()) {
			try(
					InputStreamReader reader = new InputStreamReader(new FileInputStream(localStorageFile), Charsets.UTF_8)
			) {
				localStorage = UTF8Config.loadConfiguration(reader);
			} catch(IOException e) {
				getLogger().warning("Could not load localstorage: " + localStorageFile.getAbsolutePath());
			}
		}
		if(localStorage == null) {
			localStorage = new UTF8Config();
		}
		return true;
	}

	/**
	 * Load the generalConfig.yml file
	 *
	 * @return true if it has been loaded successfully, otherwise false
	 */
	public boolean loadGeneralConfig() {
		File commonConfigFile = new File(generalFolder, generalConfigName);
		try(
				InputStreamReader reader = new InputStreamReader(new FileInputStream(commonConfigFile), Charsets.UTF_8)
		) {
			generalConfig = UTF8Config.loadConfiguration(reader);
		} catch(IOException e) {
			getLogger().warning("Could not find common config: " + commonConfigFile.getAbsolutePath());
		}
		if(generalConfig == null) {
			generalConfig = new UTF8Config();
		}
		return true;
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
