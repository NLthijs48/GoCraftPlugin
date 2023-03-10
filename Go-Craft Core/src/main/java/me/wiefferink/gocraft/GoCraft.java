package me.wiefferink.gocraft;

import com.google.common.base.Charsets;
import me.wiefferink.bukkitdo.Do;
import me.wiefferink.gocraft.commands.*;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.features.auracheck.AuraCheck;
import me.wiefferink.gocraft.features.blocks.DisableAnvilBreak;
import me.wiefferink.gocraft.features.blocks.DisableBedrockBreak;
import me.wiefferink.gocraft.features.blocks.DisableBedrockPlace;
import me.wiefferink.gocraft.features.blocks.DisableBlockBreaking;
import me.wiefferink.gocraft.features.blocks.DisableDispensers;
import me.wiefferink.gocraft.features.blocks.DisableTradeSignPlacing;
import me.wiefferink.gocraft.features.blocks.DisableWitherDamage;
import me.wiefferink.gocraft.features.environment.DisableAboveNetherGlitching;
import me.wiefferink.gocraft.features.environment.DisableMobSpawning;
import me.wiefferink.gocraft.features.environment.DisableRain;
import me.wiefferink.gocraft.features.environment.DisableVoidFall;
import me.wiefferink.gocraft.features.environment.ResourceWorlds;
import me.wiefferink.gocraft.features.items.*;
import me.wiefferink.gocraft.features.other.AddDefaultRank;
import me.wiefferink.gocraft.features.other.ClickChatMessages;
import me.wiefferink.gocraft.features.other.NauseaPotions;
import me.wiefferink.gocraft.features.players.*;
import me.wiefferink.gocraft.features.players.timedfly.TimedServerFly;
import me.wiefferink.gocraft.information.InformationManager;
import me.wiefferink.gocraft.inspector.InspectionManager;
import me.wiefferink.gocraft.integration.AuthMeLink;
import me.wiefferink.gocraft.integration.BanManagerLink;
import me.wiefferink.gocraft.integration.EssentialsLink;
import me.wiefferink.gocraft.integration.FactionsLink;
import me.wiefferink.gocraft.integration.GoPVPLink;
import me.wiefferink.gocraft.integration.MapSwitcherLink;
import me.wiefferink.gocraft.integration.ProtocolLibLink;
import me.wiefferink.gocraft.integration.WorldGuardLink;
import me.wiefferink.gocraft.interfaces.SpecificUtilsBase;
import me.wiefferink.gocraft.management.commandsync.SyncCommandsServer;
import me.wiefferink.gocraft.management.distribution.DistributionManager;
import me.wiefferink.gocraft.ranks.RankManager;
import me.wiefferink.gocraft.rewards.RewardClaim;
import me.wiefferink.gocraft.sessions.FixGCPlayer;
import me.wiefferink.gocraft.sessions.SeenCommand;
import me.wiefferink.gocraft.shop.Shop;
import me.wiefferink.gocraft.tools.Constant;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.storage.Cleaner;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.gocraft.tools.storage.UTF8Config;
import me.wiefferink.gocraft.votes.VoteManager;
import me.wiefferink.gocraft.votes.VoteScoreboard;
import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.interactivemessenger.source.LanguageManager;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GoCraft extends JavaPlugin {
	// Constants
	public static final String signLog = "signs";
	public static final SimpleDateFormat shortTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
	public static final SimpleDateFormat longTimeFormat = new SimpleDateFormat("dd MMMMMMMMMMMMMMMMM yyyy HH:mm");
	// Variables
	private Map<Class, Feature> features;
	private LanguageManager languageManager;
	private DistributionManager distributionManager;
	private InspectionManager inspectionManager;
	private InformationManager informationManager;
	private SyncCommandsServer syncCommandsServer;
	private RewardClaim rewardClaim;
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
	private ProtocolLibLink protocolLibLink = null;
	private WorldGuardLink worldGuardLink = null;
	private BanManagerLink banManagerLink = null;
	private AuthMeLink authMeLink = null;
	private FactionsLink factionsLink = null;

	// Version specific classes
	private SpecificUtilsBase specificUtils = null;

	private boolean isReload = false;

	private boolean dynMapInstalled = false;
	public static boolean loadedCorrectly = false;

	private ConfigurationSection serverSettings;
	private String serverId;

	public void onEnable() {
		instance = this;
		Do.init(this);
		Log.setLogger(getLogger());
		reloadConfig();
		saveDefaultConfig();
		chatPrefix = Utils.listOrSingle(getConfig(), "chatPrefix");
		Log.setDebug(getConfig().getBoolean("debug"));
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

		// Check if Protocollib is present
		Plugin pl = getServer().getPluginManager().getPlugin("ProtocolLib");
		if(pl != null && pl.isEnabled()) {
			protocolLibLink = new ProtocolLibLink();
			connnected.add("ProtocolLib");
		}

		// Check if AuthMe is present
		Plugin authme = getServer().getPluginManager().getPlugin("Authme");
		if(authme != null && authme.isEnabled()) {
			authMeLink = new AuthMeLink();
			connnected.add("AuthMe");
		}

		// Check if Factions is present
		Plugin factions = getServer().getPluginManager().getPlugin("Factions");
		if(factions != null && factions.isEnabled()) {
			factionsLink = new FactionsLink();
			connnected.add("Factions");
		}

		// Check if Vault is present
		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
		if(vault != null && vault.isEnabled()) {
			RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
			if (economyProvider != null) {
				economy = economyProvider.getProvider();
				connnected.add("Vault (economy)");
			} else {
				Log.info("Error: Vault or the Economy plugin is not present or has not loaded correctly");
			}
			RegisteredServiceProvider<Permission> permissionProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.permission.Permission.class);
			if (permissionProvider != null) {
				this.permissionProvider = permissionProvider.getProvider();
				connnected.add("Vault (permissions)");
			} else {
				Log.info("Error: Vault or the Permissions plugin is not present or has not loaded correctly");
			}
		}

		// Check if DynMap is present
		Plugin dm = getServer().getPluginManager().getPlugin("dynmap");
		dynMapInstalled = dm != null && dm.isEnabled();
		if (dynMapInstalled) {
			connnected.add("DynMap");
		}

		// Check if GoPVP is present
		Do.sync(() -> {
			Plugin gp = getServer().getPluginManager().getPlugin("GoPVP");
			if(gp != null && gp.isEnabled()) {
				goPVPLink = new GoPVPLink();
				connnected.add("GoPVP");
			}
			Log.info("Connected plugins:", StringUtils.join(connnected, ", "));
		});

		// Setup version specific classes
		String packageName = getServer().getClass().getPackage().getName();
		String version = packageName.substring(packageName.lastIndexOf('.') + 1);
		try {
			final Class<?> clazz = Class.forName("me.wiefferink.gocraft.versions." + version + ".SpecificUtils");
			if (SpecificUtilsBase.class.isAssignableFrom(clazz)) {
				this.specificUtils = (SpecificUtilsBase) clazz.getConstructor().newInstance();
			}
		} catch (final Exception e) {
			Log.error("Could not load version specific classes (tried to load " + version + ").");
		}

		// Assign a default if failed
		if (specificUtils == null) {
			specificUtils = new SpecificUtilsBase();
		} else {
			Log.info("Using " + version + " for version specific classes");
		}

		this.languageManager = new LanguageManager(
				this,
				"lang",
				getConfig().getString("language"),
				"EN",
				getChatPrefix()
		);
		Message.useColorsInConsole(true);
		generalFolder = new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParent() + File.separator + Constant.GENERAL_FOLDER_NAME);
		loadGeneralConfig();

		// Startup database
		Database.setup(
				getGeneralConfig().getString("settings.sessionTracker.database", "wrong"),
				getGeneralConfig().getString("settings.sessionTracker.username", "wrong"),
				getGeneralConfig().getString("settings.sessionTracker.password", "wrong"),
				getGeneralConfig().getBoolean("debug", false)
		);

		distributionManager = new DistributionManager();
		loadLocalStorage();
		informationManager = new InformationManager();

		inspectionManager = new InspectionManager(this);
		addListeners();

		if (getConfig().getConfigurationSection("shop") != null && getConfig().getConfigurationSection("shop").getKeys(false).size() > 0) {
			shop = new Shop(this);
			this.getServer().getPluginManager().registerEvents(shop, this);
		}

		Do.syncTimer(18000, this::saveLocalStorageNow);
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
		features.values().forEach(Feature::stopFeature);
		if (shop != null) {
			shop.handleServerStop();
		}
		saveLocalStorageNow();
		if(!isReload) {
			getDistributionManager().updateNow(Bukkit.getConsoleSender(), getServerName(), null, false);
		}

		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		Database.shutdown();
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
		// Month based
		Calendar now = Calendar.getInstance();
		String path = "statistics." + now.get(Calendar.YEAR) + "." + now.get(Calendar.MONTH) + "." + key;
		getLocalStorage().set(path, getLocalStorage().getLong(path) + 1);

		// Overall
		getLocalStorage().set("statistics."+key, getLocalStorage().getLong("statistics."+key) + 1);

		saveLocalStorage();
	}

	/**
	 * Get the name of this server
	 * @return The display name of this server
	 */
	public String getServerName() {
		return getServerName(getServerId());
	}

	/**
	 * Get the directory the server is in
	 * @return The server directory
	 */
	public String getServerDirectory() {
		String result = getDataFolder().getAbsoluteFile().getParentFile().getParent().replace(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParent(), "");
		if(result != null && (result.startsWith("/") || result.startsWith("\\"))) {
			result = result.substring(1);
		}
		return result;
	}

	public ConfigurationSection getServerSettings() {
		return serverSettings;
	}

	/**
	 * Get the name of this server
	 * @param result The id of the server to get the name for
	 * @return The display name of this server
	 */
	public String getServerName(String result) {
		if (result == null) {
			result = serverSettings.getString("name");
		} else {
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
	 * Get a settting of this server
	 * @param path The path of the setting
	 * @return The value of the setting
	 */
	public String getStringSetting(String path) {
		String result = getGeneralConfig().getString("servers."+getServerId()+"."+path);
		if(result == null) {
			result = getGeneralConfig().getString("servers.DEFAULT."+path);
		}
		return result;
	}

	/**
	 * Get the id of this server for use with the general config
	 * @return The id of this server
	 */
	public String getServerId() {
		return serverId;
	}

	/**
	 * Get the BungeeCord name
	 * @return The BungeeCord id
	 */
	public String getBungeeId() {
		return serverSettings.getString("bungeeId");
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
	 * Get the link to the Factions plugin
	 * @return FactionsLink
	 */
	public FactionsLink getFactionsLink() {
		return factionsLink;
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

	public AuthMeLink getAuthMeLink() {
		return this.authMeLink;
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
	 * Get the commands server to communicate with BungeeCord
	 * @return SyncCommandsServer instance
	 */
	public SyncCommandsServer getSyncCommandsServer() {
		return syncCommandsServer;
	}

	/**
	 * Get the shop
	 * @return The shop
	 */
	public Shop getShop() {
		return shop;
	}

	/**
	 * Get the RewardClaim feature
	 * @return RewardClaim feature
	 */
	public RewardClaim getRewardClaim() {
		return rewardClaim;
	}

	/**
	 * Register all listener and command classes
	 */
	public void addListeners() {
		// TODO replace by dynamic instantiation: https://stackoverflow.com/questions/1429172/how-do-i-list-the-files-inside-a-jar-file

		features = new HashMap<>();

		List<Feature> f = new ArrayList<>();
		// Blocks
		f.add(new DisableBedrockBreak());
		f.add(new DisableBedrockPlace());
		f.add(new DisableDispensers());
		f.add(new DisableTradeSignPlacing());
		f.add(new DisableBlockBreaking());
		f.add(new DisableAnvilBreak());
		f.add(new DisableWitherDamage());
		// General
		f.add(new DisableRain());
		f.add(new DisableMobSpawning());
		f.add(new DisableHungerLoss());
		f.add(new EnablePotionEffectsOnJoin());
		f.add(new SpawnTeleport());
		f.add(new EnableRegionPotionEffects());
		if (getBanManagerLink() != null) {
			f.add(new PunishmentNotifications());
			f.add(new HackBanCommand());
		}
		f.add(new OpenenderLimiter());
		f.add(new DisableSignUseWhileMuted());
		// Items
		f.add(new DisableItemDrops());
		f.add(new DisableItemSpawning());
		f.add(new DisablePotionSplash());
		f.add(new DisablePotionThrow());
		f.add(new DisablePotionInvisibleDrink());
		f.add(new DisableXpBottleThrow());
		f.add(new DisableFirework());
		f.add(new DisableEnderpearl());
		f.add(new DisableEyeOfEnder());
		f.add(new DisableBooks());
		// Logging
		f.add(new LogSigns());
		// PVP
		f.add(new DisablePlayerDamage());
		f.add(new DisableFallDamage());
		// Other
		f.add(new DisableAboveNetherGlitching());
		f.add(new AddDefaultRank());
		f.add(new NauseaPotions());
		f.add(new FixInventories());
		syncCommandsServer = new SyncCommandsServer();
		f.add(syncCommandsServer);
		f.add(new SpawnPoints());
		f.add(new DisableVoidFall());
		f.add(new OldHunger());
		f.add(new JoinLeaveMessages());
		f.add(new ClickChatMessages());
		f.add(new TimedServerFly());
		f.add(new DisableFrostWalker());
		f.add(new RankManager());
		f.add(new VoteManager());
		f.add(new VoteScoreboard());
		f.add(new FixGCPlayer());

		f.add(new TempbanCommand());
		f.add(new PingCommand());
		f.add(new StaffMessagesCommands());
		f.add(new ReloadCommand());
		f.add(new RulesCommand());
		f.add(new HelpCommand());
		if(protocolLibLink != null && worldGuardLink != null) {
			f.add(new AuraCheck());
		}
		f.add(new ResourceWorlds());
		f.add(new RandomtpCommand());
		f.add(new Rewards());
		f.add(new AttackSpeed());
		f.add(new MapCommand());
		f.add(new DiscordCommand());
		f.add(new SafeTeleportCommand());
		f.add(new SeenCommand());
		f.add(new ServerSwitchCommands());
		f.add(new BroadcastCommand());
		f.add(new OnlinePlayersCommand());
		rewardClaim = new RewardClaim();
		f.add(rewardClaim);

		f.forEach(feature -> features.put(feature.getClass(), feature));
		features.values().forEach(Feature::startFeature);
	}

	public <T> T getFeature(Class<T> type) {
		//noinspection unchecked
		return (T)features.get(type);
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
		messages.forEach(message -> message.send(target));
	}

	public void deRegisterEvents() {
		features.values().forEach(HandlerList::unregisterAll);
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


	/**
	 * Get the InformationManager
	 * @return The InformationManager
	 */
	public InformationManager getInformationManager() {
		return informationManager;
	}

	public void logLine(String fileName, String message) {
		File file = new File(getDataFolder() + File.separator + "logs" + File.separator);
		if (!file.exists()) {
			if (!file.mkdirs()) {
				Log.warn("Could not create directories leading to logs folder:", file.getAbsolutePath());
				return;
			}
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(file + File.separator + fileName + ".log", true));
			out.append(message);
			out.newLine();
			out.close();
		} catch (IOException e) {
			Log.warn("Writing to the file failed:", file + File.separator + fileName + ".log");
		}
	}

	/**
	 * Get the language manager
	 * @return The language manager
	 */
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
				Log.error("Failed to save localStorage.yml:", ExceptionUtils.getStackTrace(e));
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
				Log.warn("Could not load localstorage:", localStorageFile.getAbsolutePath());
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
		File commonConfigFile = new File(generalFolder, Constant.GENERAL_CONFIG_NAME);
		try (
				InputStreamReader reader = new InputStreamReader(new FileInputStream(commonConfigFile), Charsets.UTF_8)
		) {
			generalConfig = UTF8Config.loadConfiguration(reader);
		} catch (IOException e) {
			Log.warn("Could not find common config: " + commonConfigFile.getAbsolutePath());
		}
		if (generalConfig == null) {
			generalConfig = new UTF8Config();
		}

		// Get serverSettings section
		String directory = getServerDirectory();
		ConfigurationSection servers = generalConfig.getConfigurationSection("servers");
		if(servers != null) {
			for(String id : servers.getKeys(false)) {
				if(directory.equalsIgnoreCase(id)
						|| directory.equalsIgnoreCase(servers.getString(id+".name"))
						|| directory.equalsIgnoreCase(servers.getString(id+".directory"))) {
					serverId = id;
					serverSettings = servers.getConfigurationSection(serverId);
				}
			}
		}
		if(serverSettings == null) {
			serverSettings = new YamlConfiguration();
		}
		if(serverId == null) {
			serverId = directory;
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
		Log.debug(messages);
	}

}
