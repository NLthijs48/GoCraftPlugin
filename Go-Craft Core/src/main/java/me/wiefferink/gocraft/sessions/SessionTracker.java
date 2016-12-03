package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.tools.storage.Database;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import org.hibernate.Session;

import javax.persistence.NoResultException;
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
	private Map<UUID, BungeeSession> onlineBungee;
	private Map<UUID, ServerSession> onlineServer;

	public SessionTracker(GoCraftBungee plugin) {
		this.plugin = plugin;
		this.onlineBungee = new HashMap<>();
		this.onlineServer = new HashMap<>();

		if(Database.isReady()) {
			ensureConsistentEntries();
			plugin.getProxy().getPluginManager().registerListener(plugin, this);
		} else {
			GoCraftBungee.error("SessionTracker: Database is not ready yet, did not start");
		}
	}

	/**
	 * Crash recovery, close lingering sessions
	 */
	private void ensureConsistentEntries() {
		BungeeSession.ensureConsistency();
		ServerSession.ensureConsistency();
	}

	/**
	 * Get or create a GCPlayer
	 * @param player The player data to get/create it from
	 * @return The created or loaded GCPlayer
	 */
	public GCPlayer getCreatePlayer(ProxiedPlayer player) {
		GCPlayer result;
		try {
			result = Database.getSession()
					.createQuery("FROM GCPlayer WHERE uuid = :uuid", GCPlayer.class)
					.setParameter("uuid", player.getUniqueId().toString())
					.getSingleResult();
		} catch(NoResultException e) {
			result = new GCPlayer(player.getUniqueId(), player.getName());
			Database.getSession().save(result);
		}
		return result;
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onServerSwitch(ServerConnectEvent event) {
		// Ignore cancelled events and initial server joins
		if(event.isCancelled()) {
			return;
		}

		GoCraftBungee.info("ServerConnectEvent of", event.getPlayer().getName(), "from", (event.getPlayer().getServer() == null ? "nothing" : event.getPlayer().getServer().getInfo().getName()), "to", event.getTarget().getName());
		ProxiedPlayer player = event.getPlayer();
		ServerInfo server = event.getTarget();
		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			Session session = Database.getSession();

			GCPlayer gcPlayer = getCreatePlayer(player);

			// Start BungeeSession if not started already
			BungeeSession bungeeSession = onlineBungee.get(player.getUniqueId());
			if(bungeeSession == null) {
				bungeeSession = new BungeeSession(gcPlayer, player.getPendingConnection().getAddress().getAddress().getHostAddress());
				onlineBungee.put(player.getUniqueId(), bungeeSession);
				session.save(bungeeSession);
			}

			// End old ServerSession, does not exist for initial join
			ServerSession oldServerSession = onlineServer.get(player.getUniqueId());
			boolean sameServer = false;
			if(oldServerSession != null) {
				sameServer = oldServerSession.getServerName().equals(server.getName());
				if(!sameServer) {
					oldServerSession.hasLeft();
					session.update(oldServerSession);
					onlineServer.remove(player.getUniqueId());
				}
			}

			// Start new ServerSession
			if(!sameServer) {
				ServerSession newServerSession = new ServerSession(bungeeSession, server.getName());
				onlineServer.put(player.getUniqueId(), newServerSession);
				session.save(newServerSession);
			}

			Database.closeSession();
		});

	}

	@EventHandler()
	public void onPlayerLogout(PlayerDisconnectEvent event) {
		GoCraftBungee.info("PlayerDisconnectEvent of", event.getPlayer().getName(), "ip:", event.getPlayer().getAddress().getHostString());

		UUID player = event.getPlayer().getUniqueId();
		plugin.getProxy().getScheduler().runAsync(plugin, () -> {
			Session session = Database.getSession();

			// Bungee
			BungeeSession bungeeSession = onlineBungee.remove(player);
			bungeeSession.hasLeft();
			session.update(bungeeSession);

			// Server
			ServerSession serverSession = onlineServer.remove(player);
			serverSession.hasLeft();
			session.update(serverSession);

			Database.closeSession();
		});
	}

}
