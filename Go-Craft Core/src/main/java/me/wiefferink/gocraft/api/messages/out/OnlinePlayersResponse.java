package me.wiefferink.gocraft.api.messages.out;

import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.sessions.ServerSession;
import me.wiefferink.gocraft.tools.storage.Database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlinePlayersResponse extends Response {

	public Map<String, List<WebsitePlayer>> players;

	public OnlinePlayersResponse() {
		super("onlinePlayers");

		players = new HashMap<>();
		Database.run(session -> {
			@SuppressWarnings("unchecked")
			List<Map<String,Object>> onlinePlayers = session.createQuery(
					"SELECT new map(gcPlayer as player, serverSession as server) " +
							"FROM ServerSession serverSession " +
							"INNER JOIN serverSession.bungeeSession as bungeeSession " +
							"INNER JOIN bungeeSession.gcPlayer as gcPlayer " +
							"WHERE serverSession.leftServer=null")
					.getResultList();

			for(Map<String, Object> onlinePlayerDetails : onlinePlayers) {
				GCPlayer player = (GCPlayer)onlinePlayerDetails.get("player");
				ServerSession server = (ServerSession)onlinePlayerDetails.get("server");

				List<WebsitePlayer> serverPlayers = players.get(server.getServerName());
				if(serverPlayers == null) {
					serverPlayers = new ArrayList<>();
					players.put(server.getServerName(), serverPlayers);
				}

				serverPlayers.add(new WebsitePlayer(player.getName(), player.getUniqueId()));
			}
		});
	}

	/**
	 * Player with details relevant for the website (just use GCPlayer in the future?)
	 */
	private class WebsitePlayer {
		public WebsitePlayer(String name, String uuid) {
			this.name = name;
			this.uuid = uuid;
		}
		public String name;
		public String uuid;
	}
}
