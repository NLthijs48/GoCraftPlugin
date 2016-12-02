package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.GoCraftBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.hibernate.Session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
	Get onlineBungee players at time X:
		SELECT count(*) FROM bungeeSessions WHERE X>joinTime AND (leaveTime==NULL OR leaveTime>X));
	On certain server
		SELECT count(*) FROM serverSessions WHERE X>joinTime AND (leaveTime==NULL OR leaveTime>X));
	Certain player was onlineBungee:
		SELECT * FROM bungeeSessions WHERE uuid==uuid ORDER BY joinTime DESC LIMIT 10;
	On which server:
		SELECT * FROM serverSessions WHERE uuid==uuid ORDER BY joinTime DESC LIMIT 10;
*/

public class SessionTracker implements Listener {

	private GoCraftBungee plugin;
	private SessionConnector connector;
	private Map<UUID, BungeeSession> onlineBungee;
	private Map<UUID, ServerSession> onlineServer;

	public SessionTracker(GoCraftBungee plugin) {
		this.plugin = plugin;
		this.onlineBungee = new HashMap<>();
		this.onlineServer = new HashMap<>();

		connector = new SessionConnector(
				plugin.getGeneralConfig().getString("settings.sessionTracker.database"),
				plugin.getGeneralConfig().getString("settings.sessionTracker.username"),
				plugin.getGeneralConfig().getString("settings.sessionTracker.password")
		);
		if(connector.isReady()) {
			plugin.getProxy().getPluginManager().registerListener(plugin, this);
			ensureConsistentEntries();
		} else {
			GoCraftBungee.error("SessionTracker: Failed to setup sessionFactory");
		}
	}

	/**
	 * Crash recovery, close lingering sessions
	 */
	private void ensureConsistentEntries() {
		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			BungeeSession.ensureConsistency(connector);
			ServerSession.ensureConsistency(connector);
		});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerSwitch(ServerConnectEvent event) {
		// Ignore cancelled events and initial server joins
		if(event.isCancelled()) {
			return;
		}

		GoCraftBungee.info("ServerConnectEvent of", event.getPlayer().getName(), "from", (event.getPlayer().getServer() == null ? "nothing" : event.getPlayer().getServer().getInfo().getName()), "to", event.getTarget().getName());
		UUID player = event.getPlayer().getUniqueId();
		ServerInfo server = event.getTarget();
		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			Session session = connector.session();
			session.beginTransaction();

			BungeeSession bungeeSession = onlineBungee.get(player);
			if(bungeeSession == null) {
				bungeeSession = new BungeeSession(event.getPlayer().getUniqueId(), event.getPlayer().getName(), event.getPlayer().getPendingConnection().getAddress().getAddress().getHostAddress());
				onlineBungee.put(player, bungeeSession);
				session.save(bungeeSession);
			}

			// End old ServerSession, does not exist for initial join
			ServerSession oldServerSession = onlineServer.get(player);
			boolean sameServer = false;
			if(oldServerSession != null) {
				sameServer = oldServerSession.getServer().equals(server.getName());
				if(!sameServer) {
					oldServerSession.hasLeft();
					session.update(oldServerSession);
					onlineServer.remove(player);
				}
			}

			// Start new ServerSession
			if(!sameServer) {
				ServerSession newServerSession = new ServerSession(bungeeSession, server.getName());
				onlineServer.put(player, newServerSession);
				session.save(newServerSession);
			}

			session.getTransaction().commit();
			session.clear();
		});

	}

	@EventHandler()
	public void onPlayerLogout(PlayerDisconnectEvent event) {
		GoCraftBungee.info("PlayerDisconnectEvent of", event.getPlayer().getName(), "ip:", event.getPlayer().getAddress().getHostString());

		UUID player = event.getPlayer().getUniqueId();
		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			Session session = connector.session();
			session.beginTransaction();

			// Bungee
			BungeeSession bungeeSession = onlineBungee.remove(player);
			bungeeSession.hasLeft();
			session.update(bungeeSession);

			// Server
			ServerSession serverSession = onlineServer.remove(player);
			serverSession.hasLeft();
			session.update(serverSession);

			session.getTransaction().commit();
			session.clear();
		});
	}

}
