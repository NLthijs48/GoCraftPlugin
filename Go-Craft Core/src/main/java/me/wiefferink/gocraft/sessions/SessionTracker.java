package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.api.messages.out.OnlinePlayersResponse;
import me.wiefferink.gocraft.tools.storage.Database;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

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
			Log.error("SessionTracker: Database is not ready yet, did not start");
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
	public void onServerSwitch(ServerSwitchEvent event) {
		ProxiedPlayer player = event.getPlayer();
		ServerInfo server = event.getPlayer().getServer().getInfo();
		plugin.getProxy().getScheduler().runAsync(plugin, () ->
			Database.run(session -> {
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

				// Broadcast to website
				GoCraftBungee.getInstance().getApi().broadcast(new OnlinePlayersResponse());
			})
		);
	}

	@EventHandler()
	public void onPlayerLogout(PlayerDisconnectEvent event) {
		Log.info("PlayerDisconnectEvent of", event.getPlayer().getName(), "ip:", event.getPlayer().getAddress().getHostString());

		UUID player = event.getPlayer().getUniqueId();
		plugin.getProxy().getScheduler().runAsync(plugin, () ->
			Database.run(session -> {
				// Bungee
				BungeeSession bungeeSession = onlineBungee.remove(player);
				bungeeSession.hasLeft();
				session.update(bungeeSession);

				// Server
				ServerSession serverSession = onlineServer.remove(player);
				serverSession.hasLeft();
				session.update(serverSession);

				// Broadcast to website
				GoCraftBungee.getInstance().getApi().broadcast(new OnlinePlayersResponse());
			})
		);
	}

}
