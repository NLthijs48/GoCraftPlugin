package me.wiefferink.gocraft;

import com.google.common.base.Charsets;
import me.wiefferink.gocraft.commands.*;
import me.wiefferink.gocraft.distribution.DistributionManager;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.features.auracheck.AuraCheck;
import me.wiefferink.gocraft.features.blocks.*;
import me.wiefferink.gocraft.features.environment.DisableMobSpawning;
import me.wiefferink.gocraft.features.environment.DisableRain;
import me.wiefferink.gocraft.features.environment.ResourceWorlds;
import me.wiefferink.gocraft.features.items.*;
import me.wiefferink.gocraft.features.other.AddDefaultRank;
import me.wiefferink.gocraft.features.other.NauseaPotions;
import me.wiefferink.gocraft.features.other.ResetExpiredPlots;
import me.wiefferink.gocraft.features.players.*;
import me.wiefferink.gocraft.inspector.InspectionManager;
import me.wiefferink.gocraft.integration.*;
import me.wiefferink.gocraft.interfaces.SpecificUtilsBase;
import me.wiefferink.gocraft.messages.LanguageManager;
import me.wiefferink.gocraft.messages.Message;
import me.wiefferink.gocraft.shop.Shop;
import me.wiefferink.gocraft.tools.storage.Cleaner;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.gocraft.tools.storage.MySQLDatabase;
import me.wiefferink.gocraft.tools.storage.UTF8Config;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.sql.PreparedStatement;
import java.util.*;

public final class GoCraft extends JavaPlugin {
	// Constants
	public static final String signLog = "signs";
	public static final String generalFolderName = "GENERAL";
	public static final String generalConfigName = "config.yml";
	public static final String generalPluginDataFoldername = "plugins";
	public static final String generalRootDataFoldername = "root";
	public static final String currencyEuro = "%euro%";
	public static final String languageFolder = "lang";
	// Variables
	private ArrayList<Listener> listeners;
	private LanguageManager languageManager;
	private DistributionManager distributionManager;
	private InspectionManager inspectionManager;
	private boolean debug = false;
	private List<String> chatPrefix = null;
	private static GoCraft instance = null;
	private Map<String, Cleaner> localStorageCleaners;
	private boolean localStorageDirty;
	private Shop shop;
	// Config files
	private UTF8Config generalConfig = null;
	private UTF8Config localStorage = null;
	private File generalFolder = null;
	// Dependencies
	private Economy economy = null;
	private Permission permissionProvider = null;
	private MapSwitcherLink mapSwitcherLink = null;

	private GoPVPLink goPVPLink = null;
	private EssentialsLink essentialsLink = null;
	private WorldGuardLink worldGuardLink = null;
	private BanManagerLink banManagerLink = null;
	// Version specific classes
	private SpecificUtilsBase specificUtils = null;

	private boolean isReload = false;

	private boolean dynMapInstalled = false;
	public static boolean loadedCorrectly = false;

	public void onEnable() {
		reloadConfig();
		instance = this;
		saveDefaultConfig();
		if(getConfig().isList("chatPrefix")) {
			chatPrefix = getConfig().getStringList("chatPrefix");
		} else {
			chatPrefix = Collections.singletonList(getConfig().getString("chatPrefix"));
		}
		this.debug = getConfig().getBoolean("debug");
		localStorageCleaners = new HashMap<>();

		final Set<String> connnected = new HashSet<>();

		// Check if WorldGuard is present
		Plugin wg = getServer().getPluginManager().getPlugin("WorldGuard");
		if (wg != null && wg.isEnabled()) {
			this.worldGuardLink = new WorldGuardLink();
			connnected.add("WorldGuard");
		}

		// Check if BanManager is present
		Plugin bm = getServer().getPluginManager().getPlugin("BanManager");
		if (bm != null && bm.isEnabled()) {
			banManagerLink = new BanManagerLink();
			connnected.add("BanManager");
		}

		// Check if MapSwitcher is present
		Plugin ms = getServer().getPluginManager().getPlugin("MapSwitcher");
		if (ms != null && ms.isEnabled()) {
			mapSwitcherLink = new MapSwitcherLink();
			connnected.add("MapSwitcher");
		}

		// Check if Essentials is present
		Plugin es = getServer().getPluginManager().getPlugin("Essentials");
		if (es != null && es.isEnabled()) {
			essentialsLink = new EssentialsLink();
			connnected.add("Essentials");
		}

		// Check if Vault is present
		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
			connnected.add("Vault (economy)");
		} else {
			getLogger().info("Error: Vault or the Economy plugin is not present or has not loaded correctly");
		}
		RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
		if (permissionProvider != null) {
			this.permissionProvider = permissionProvider.getProvider();
			connnected.add("Vault (permissions)");
		} else {
			getLogger().info("Error: Vault or the Permissions plugin is not present or has not loaded correctly");
		}

		// Check if DynMap is present
		Plugin dm = getServer().getPluginManager().getPlugin("dynmap");
		dynMapInstalled = dm != null && dm.isEnabled();
		if (dynMapInstalled) {
			connnected.add("DynMap");
		}

		// Check if GoPVP is present
		new BukkitRunnable() {
			public void run() {
				Plugin gp = getServer().getPluginManager().getPlugin("GoPVP");
				if (gp != null && gp.isEnabled()) {
					goPVPLink = new GoPVPLink();
					connnected.add("GoPVP");
				}

				getLogger().info("Connected plugins: " + StringUtils.join(connnected, ", "));
			}
		}.runTask(this);

		// Setup version specific classes
		String packageName = getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.lastIndexOf('.') + 1);
		try {
			final Class<?> clazz = Class.forName("me.wiefferink.gocraft.versions." + version + ".SpecificUtils");
			if (SpecificUtilsBase.class.isAssignableFrom(clazz)) {
				this.specificUtils = (SpecificUtilsBase) clazz.getConstructor().newInstance();
			}
		} catch (final Exception e) {
			this.getLogger().severe("Could not load version specific classes (tried to load " + version + ").");
		}
		// Assign a default if failed
		if (specificUtils == null) {
			specificUtils = new SpecificUtilsBase();
		} else {
			getLogger().info("Using " + version + " for version specific classes");
		}

		this.languageManager = new LanguageManager();
		generalFolder = new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParent() + File.separator + generalFolderName);
		loadGeneralConfig();
		distributionManager = new DistributionManager(this);
		loadLocalStorage();

		inspectionManager = new InspectionManager(this);
		addListeners();

		if (getConfig().getConfigurationSection("shop") != null && getConfig().getConfigurationSection("shop").getKeys(false).size() > 0) {
			shop = new Shop(this);
			this.getServer().getPluginManager().registerEvents(shop, this);
		}

		// Save local storage timer
		new BukkitRunnable() {
			@Override
			public void run() {
				saveLocalStorageNow();
			}
		}.runTaskTimer(this, 18000L, 18000L);
		loadedCorrectly = true;
	}

	/**
	 * Get the GoCraft instance
	 * @return The GoCraft instance
	 */
	public static GoCraft getInstance() {
		return instance;
	}

	/**
	 * Plugin disable actions
	 */
	public void onDisable() {
		// Call stop methods of the registered features
		for (Listener listener : listeners) {
			if (listener instanceof Feature) {
				((Feature) listener).stopFeature();
			}
		}
		if (shop != null) {
			shop.handleServerStop();
		}
		saveLocalStorageNow();
		if(!isReload) {
			getDistributionManager().updateNow(Bukkit.getConsoleSender(), getServerName(), null);
		}

		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
	}

	/**
	 * Check if this enable/disable is because of a reload or not
	 * @return true if it is a reload, otherwise false
	 */
	public boolean isReload() {
		return isReload;
	}

	/**
	 * Increase a statistic for tracking
	 * @param key The key to track it with
	 */
	public void increaseStatistic(String key) {
		getLocalStorage().set("statistics." + key, getLocalStorage().getLong("statistics." + key) + 1);
		saveLocalStorage();
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
		if (result == null) {
			result = getDataFolder().getAbsoluteFile().getParentFile().getParent().replace(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParent(), "");
			if (result != null) {
				result = result.substring(1);
			}
		}
		if (result != null) {
			ConfigurationSection servers = getGeneralConfig().getConfigurationSection("servers");
			if (servers != null) {
				for (String id : servers.getKeys(false)) {
					if (result.equalsIgnoreCase(id)
							|| result.equalsIgnoreCase(servers.getString(id + ".name"))
							|| result.equalsIgnoreCase(servers.getString(id + ".directory"))) {
						result = servers.getString(id + ".name");
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
	 * Get the id of this server for use with the general config
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
	 * @return The UTF8Config generalConfig file
	 */
	public UTF8Config getGeneralConfig() {
		return generalConfig;
	}

	/**
	 * Get the link to the MapSwitcher plugin
	 * @return MapSwitcherLink
	 */
	public MapSwitcherLink getMapSwitcherLink() {
		return mapSwitcherLink;
	}

	/**
	 * Get the link to the GoPVP plugin
	 * @return GoPVPLink
	 */
	public GoPVPLink getGoPVPLink() {
		return goPVPLink;
	}

	/**
	 * Get the link to the Essentials plugin
	 * @return EssentialsLink
	 */
	public EssentialsLink getEssentialsLink() {
		return essentialsLink;
	}

	/**
	 * Get the link to the BanManager plugin
	 * @return BanManagerLink
	 */
	public BanManagerLink getBanManagerLink() {
		return banManagerLink;
	}


	/**
	 * Get the link to the WorldGuard plugin
	 * @return WorldGuardLink
	 */
	public WorldGuardLink getWorldGuardLink() {
		return this.worldGuardLink;
	}

	/**
	 * Check if DynMap is installed
	 * @return true if DynMap is installed, otherwise false
	 */
	public boolean dynMapInstalled() {
		return dynMapInstalled;
	}

	/**
	 * Get the Economy running on the server
	 * @return The Economy provider
	 */
	public Economy getEconomy() {
		return economy;
	}

	/**
	 * Get the permissions provider of Vault
	 * @return The permissions provider
	 */
	public Permission getPermissionProvider() {
		return permissionProvider;
	}

	/**
	 * Get the specificUtils instance
	 * @return The specificUtils instance
	 */
	public SpecificUtilsBase getSpecificUtils() {
		return specificUtils;
	}

	/**
	 * Get the shop
	 * @return The shop
	 */
	public Shop getShop() {
		return shop;
	}

	public void testStorage() {
		String host = "localhost";
		int port = 3306;
		String database = "testdb";
		String username = "root";
		String password = "";
		Database db = new MySQLDatabase(host, port, database, username, password, this);
		//debug("is connected: " + db.isConnected());
		PreparedStatement statement = db.prepareStatement("CREATE TABLE IF NOT EXISTS combat (kills int, deaths int)");
		db.execute(statement);
		// ORDER not kept! running as async tasks
		for (int i = 0; i < 10; i++) {
			//debug("Doing: i=" + i);
			db.execute("INSERT INTO combat VALUES (?, ?)", i, 10 - i);
		}
	}

	/**
	 * Register all listener and command classes
	 */
	public void addListeners() {
		listeners = new ArrayList<>();
		// Blocks
		listeners.add(new DisableBedrockBreak(this));
		listeners.add(new DisableBedrockPlace(this));
		listeners.add(new DisableDispensers(this));
		listeners.add(new DisableTradeSignPlacing(this));
		listeners.add(new DisableBlockBreaking(this));
		listeners.add(new DisableAnvilBreak(this));
		listeners.add(new DisableWitherDamage(this));
		// General
		listeners.add(new DisableRain(this));
		listeners.add(new DisableMobSpawning(this));
		listeners.add(new DisableHungerLoss(this));
		listeners.add(new EnablePotionEffectsOnJoin(this));
		listeners.add(new SpawnTeleport(this));
		listeners.add(new EnableRegionPotionEffects(this));
		if (getBanManagerLink() != null) {
			listeners.add(new PunishmentNotifications(this));
		}
		listeners.add(new DisableStaffJoinLeaveMessages(this));
		listeners.add(new OpenenderLimiter(this));
		listeners.add(new DisableSignUseWhileMuted(this));
		// Items
		listeners.add(new DisableItemDrops(this));
		listeners.add(new DisableItemSpawning(this));
		listeners.add(new DisablePotionSplash(this));
		listeners.add(new DisablePotionThrow(this));
		listeners.add(new DisablePotionInvisibleDrink(this));
		listeners.add(new DisableXpBottleThrow(this));
		listeners.add(new DisableFirework(this));
		listeners.add(new DisableEnderpearl(this));
		listeners.add(new DisableEyeOfEnder(this));
		listeners.add(new DisableBooks(this));
		// Logging
		listeners.add(new LogSigns(this));
		// PVP
		listeners.add(new DisablePlayerDamage(this));
		listeners.add(new DisableFallDamage(this));
		// Commands
		listeners.add(new TempbanCommand(this));
		new PingCommand(this);
		new SetspawnCommand(this);
		new StaffMessagesCommands(this);
		new UpdateCommand(this);
		new ReloadCommand(this);
		new InspectCommand(this);
		new RulesCommand(this);
		new HelpCommand(this);
		new ShopCommand(this);
		// Other
		listeners.add(new ResetExpiredPlots(this));
		listeners.add(new DisableAboveNetherGlitching(this));
		listeners.add(new AddDefaultRank(this));
		listeners.add(new NauseaPotions(this));
		// Feature based classes
		listeners.add(new AuraCheck());
		listeners.add(new ResourceWorlds());
		listeners.add(new RandomtpCommand());
		listeners.add(new Rewards());
		listeners.add(new AttackSpeed());
		listeners.add(new MapCommand());

		for (Listener listener : listeners) {
			if (listener instanceof Feature) {
				((Feature) listener).startFeature();
			}
		}
	}

	/**
	 * Get the InspectionManager
	 * @return The InspectionManager
	 */
	public InspectionManager getInspectionManager() {
		return inspectionManager;
	}

	/**
	 * Set the inspection manager, used to get startup behavior correct
	 * @param manager The manager to set
	 */
	public void setInspectionManager(InspectionManager manager) {
		inspectionManager = manager;
	}

	/**
	 * Show the help page with all commands to the target
	 * @param target The person to send it to
	 */
	public void showHelp(CommandSender target) {
		List<Message> messages = new ArrayList<>();
		messages.add(Message.fromKey("help-header").prefix());
		messages.add(Message.fromKey("help-alias").prefix());
		if (target.hasPermission("gocraft.resetstats")) {
			messages.add(Message.fromKey("help-resetstats"));
		}
		if (target.hasPermission("gocraft.resetall")) {
			messages.add(Message.fromKey("help-resetall"));
		}
		messages.add(Message.fromKey("help-stats"));
		for(Message message : messages) {
			message.send(target);
		}
	}

	public void deRegisterEvents() {
		for(Listener listener : listeners) {
			HandlerList.unregisterAll(listener);
		}
	}

	/**
	 * Reload the plugin data
	 */
	public void reload() {
		isReload = true;
		onDisable();
		onEnable();
		isReload = false;
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
			if (!file.mkdirs()) {
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
				GoCraft.warn("GoCraft.onThisWorld: Cannot get world from object: "+world.toString());
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

	/**
	 * Send a message to a target without a prefix
	 * @param target       The target to send the message to
	 * @param key          The key of the language string
	 * @param replacements The replacements to insert in the message
	 */
	public void messageNoPrefix(Object target, String key, Object... replacements) {
		Message.fromKey(key).replacements(replacements).send(target);
	}

	/**
	 * Send a message to a target, prefixed by the default chat prefix
	 * @param target       The target to send the message to
	 * @param key          The key of the language string
	 * @param replacements The replacements to insert in the message
	 */
	public void message(Object target, String key, Object... replacements) {
		Message.fromKey(key).prefix().replacements(replacements).send(target);
	}

	/**
	 * Get the localStorage config file
	 * @return The UTF8Config localStorage file
	 */
	public UTF8Config getLocalStorage() {
		return localStorage;
	}

	/**
	 * Indicate that the local storage should be saved (will be done in 15 minutes or at shutdown)
	 */
	public void saveLocalStorage() {
		localStorageDirty = true;
	}

	/**
	 * Save the local storage file, only do this if saving later is not fine!
	 */
	public void saveLocalStorageNow() {
		if (localStorageDirty) {
			runLocalStorageCleaners();
			// Save file
			File file = new File(this.getDataFolder(), "localStorage.yml");
			try {
				localStorage.save(file);
			} catch (IOException e) {
				this.getLogger().info("Failed to save localStorage.yml");
				e.printStackTrace();
			}
			localStorageDirty = false;
		}
	}

	/**
	 * Load the localStorage.yml file
	 * @return true if it has been loaded successfully, otherwise false
	 */
	public boolean loadLocalStorage() {
		File localStorageFile = new File(this.getDataFolder(), "localStorage.yml");
		if (localStorageFile.exists()) {
			try (
					InputStreamReader reader = new InputStreamReader(new FileInputStream(localStorageFile), Charsets.UTF_8)
			) {
				localStorage = UTF8Config.loadConfiguration(reader);
			} catch (IOException e) {
				getLogger().warning("Could not load localstorage: " + localStorageFile.getAbsolutePath());
			}
		}
		if (localStorage == null) {
			localStorage = new UTF8Config();
		}
		runLocalStorageCleaners();
		return true;
	}

	/**
	 * Add a localstorage cleaner, used before saving the localstorage and at register time
	 * @param identifier Identifier for the cleaner, replaces existing cleaner with the same identifier
	 * @param cleaner The cleaner to register
	 */
	public void registerLocalStorageCleaner(String identifier, Cleaner cleaner) {
		localStorageCleaners.put(identifier, cleaner);
		if (cleaner.clean(localStorage)) {
			saveLocalStorage();
		}
	}

	/**
	 * Run the localstorage cleaners
	 */
	public void runLocalStorageCleaners() {
		boolean save = false;
		for (Cleaner cleaner : localStorageCleaners.values()) {
			save |= cleaner.clean(localStorage);
		}
		if (save) {
			saveLocalStorage();
		}
	}

	/**
	 * Load the generalConfig.yml file
	 * @return true if it has been loaded successfully, otherwise false
	 */
	public boolean loadGeneralConfig() {
		File commonConfigFile = new File(generalFolder, generalConfigName);
		try (
				InputStreamReader reader = new InputStreamReader(new FileInputStream(commonConfigFile), Charsets.UTF_8)
		) {
			generalConfig = UTF8Config.loadConfiguration(reader);
		} catch (IOException e) {
			getLogger().warning("Could not find common config: " + commonConfigFile.getAbsolutePath());
		}
		if (generalConfig == null) {
			generalConfig = new UTF8Config();
		}
		return true;
	}

	/**
	 * Get the current chatPrefix
	 * @return The current chatPrefix
	 */
	public List<String> getChatPrefix() {
		return chatPrefix;
	}

	/**
	 * Instance method of the debug function that can specified in an interface
	 * @param messages The message to send to the debug output
	 */
	public void _debug(Object... messages) {
		if (this.debug) {
			getLogger().info("Debug: "+StringUtils.join(messages, " "));
		}
	}

	/**
	 * Sends an debug message to the console
	 * @param message The message that should be printed to the console
	 */
	public static void debug(Object... message) {
		if(GoCraft.getInstance().debug) {
			GoCraft.getInstance().getLogger().info("Debug: "+StringUtils.join(message, " "));
		}
	}

	/**
	 * Print an information message to the console
	 * @param message The message to print
	 */
	public static void info(Object... message) {
		GoCraft.getInstance().getLogger().info(StringUtils.join(message, " "));
	}

	/**
	 * Print a warning to the console
	 * @param message The message to print
	 */
	public static void warn(Object... message) {
		GoCraft.getInstance().getLogger().warning(StringUtils.join(message, " "));
	}

	/**
	 * Print an error to the console
	 * @param message The message to print
	 */
	public static void error(Object... message) {
		GoCraft.getInstance().getLogger().severe(StringUtils.join(message, " "));
	}
}
