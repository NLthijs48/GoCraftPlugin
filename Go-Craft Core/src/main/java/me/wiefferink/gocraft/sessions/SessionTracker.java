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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

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
		GCPlayer.ensureConsistency();
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

			GCPlayer gcPlayer = Database.getCreatePlayer(player.getUniqueId(), player.getName());

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
