package me.wiefferink.gocraft;

import me.wiefferink.gocraft.api.Api;
import me.wiefferink.gocraft.features.players.SwitchJoinLeaveMessages;
import me.wiefferink.gocraft.management.commandsync.SyncCommandsBungee;
import me.wiefferink.gocraft.sessions.SessionTracker;
import me.wiefferink.gocraft.tools.Constant;
import me.wiefferink.gocraft.tools.storage.Database;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.IOException;

public class GoCraftBungee extends net.md_5.bungee.api.plugin.Plugin implements Listener {

	private static GoCraftBungee instance;
	private SyncCommandsBungee syncCommandsBungee;
	private Configuration generalConfig;
	private Api api;

	public static GoCraftBungee getInstance() {
		return instance;
	}

	@Override
	public void onEnable() {
		instance = this;
		Log.setLogger(getLogger());

		// Setup general config file
		File generalFolder = new File(getDataFolder().getAbsoluteFile().getParentFile().getParentFile().getParent()+File.separator+ Constant.GENERAL_FOLDER_NAME);
		ConfigurationProvider configurationProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
		File generalConfigFile = new File(generalFolder.getAbsolutePath(), Constant.GENERAL_CONFIG_NAME);
		try {
			generalConfig = configurationProvider.load(generalConfigFile);
		} catch(IOException e) {
			Log.error("Failed to load general config file at:", generalConfigFile.getAbsolutePath());
		}
		Log.setDebug(getGeneralConfig().getBoolean("debug"));

		// Startup database
		Database.setup(
				getGeneralConfig().getString("settings.sessionTracker.database"),
				getGeneralConfig().getString("settings.sessionTracker.username"),
				getGeneralConfig().getString("settings.sessionTracker.password"),
				getGeneralConfig().getBoolean("debug")
		);

		// Start features
		this.getProxy().getPluginManager().registerListener(this, this);
		this.getProxy().getPluginManager().registerListener(this, new SwitchJoinLeaveMessages(this));
		syncCommandsBungee = new SyncCommandsBungee(this);
		new SessionTracker(this);
		api = new Api();
	}

	@Override
	public void onDisable() {
		if(syncCommandsBungee != null) {
			syncCommandsBungee.stop();
		}
		if(api != null) {
			api.stop();
		}
	}

	/**
	 * Get the general config file
	 * @return The config file
	 */
	public Configuration getGeneralConfig() {
		return generalConfig;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PreLoginEvent event) {

		Log.info("PreLoginEvent of", event.getConnection().getName(), " ip:", event.getConnection().getAddress().getHostString());
		for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
			if(player.getName().equalsIgnoreCase(event.getConnection().getName())) {
				event.setCancelled(true);
				event.setCancelReason(ChatColor.DARK_RED + "You cannot login with the same name as an online player!");
				Log.warn("Blocked an attempt to login with the same name as an online player, name: "+event.getConnection().getName()+", ip: "+event.getConnection().getAddress().getHostString());
			}
		}
	}

	/**
	 * Get the api server
	 * @return Api
	 */
	public Api getApi() {
		return api;
	}

	/**
	 * Get SyncCommandsBungee instance
	 * @return SyncCommandsBungee
	 */
	public SyncCommandsBungee getSyncCommandsBungee() {
		return syncCommandsBungee;
	}

	/**
	 * Get the name of this server
	 *
	 * @param result The id of the server to get the name for
	 * @return The display name of this server
	 */
	public String getServerName(String result) {
		Configuration servers = getGeneralConfig().getSection("servers");
		if(servers != null) {
			for(String id : servers.getKeys()) {
				if(result.equalsIgnoreCase(id)
						|| result.equalsIgnoreCase(servers.getString(id + ".name"))
						|| result.equalsIgnoreCase(servers.getString(id + ".directory"))) {
					result = servers.getString(id + ".name");
				}
			}
		}
		if(result == null || result.length() == 0) {
			result = "UNKNOWN";
		}
		return result;
	}

	/**
	 * Join string parts with a glue
	 * @param parts The parts to join
	 * @param glue  The glue to put between the parts
	 * @return Glued string parts with the glue
	 */
	public static String join(Object[] parts, String glue) {
		return join(parts, glue, 0);
	}

	/**
	 * Join string parts with a glue
	 * @param parts  The parts to join
	 * @param glue   The glue to put between the parts
	 * @param offset The position to start from
	 * @return Glued string parts with the glue
	 */
	public static String join(Object[] parts, String glue, int offset) {
		StringBuilder result = new StringBuilder();
		if(offset < 0) {
			offset = 0;
		}
		for(int i = offset; i < parts.length; i++) {
			result.append(parts[i]);
			if(i != parts.length-1) {
				result.append(glue);
			}
		}
		return result.toString();
	}
}
