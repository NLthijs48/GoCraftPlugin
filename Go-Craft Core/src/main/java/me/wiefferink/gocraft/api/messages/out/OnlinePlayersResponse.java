package me.wiefferink.gocraft.api.messages.out;

import me.wiefferink.gocraft.api.types.WebsitePlayer;
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
		super("players/UPDATE");

		players = new HashMap<>();
		Database.run(session -> {
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> onlinePlayers = session.createQuery(
					"SELECT new map(gcPlayer as player, serverSession as server) " +
							"FROM ServerSession serverSession " +
							"INNER JOIN serverSession.bungeeSession as bungeeSession " +
							"INNER JOIN bungeeSession.gcPlayer as gcPlayer " +
							"WHERE serverSession.leftServer=null " +
							"AND gcPlayer.invisible = false " +
							"ORDER BY gcPlayer.name")
					.getResultList();

			for(Map<String, Object> onlinePlayerDetails : onlinePlayers) {
				GCPlayer player = (GCPlayer) onlinePlayerDetails.get("player");
				ServerSession server = (ServerSession) onlinePlayerDetails.get("server");

				players.computeIfAbsent(server.getServerName(), key -> new ArrayList<>())
						.add(new WebsitePlayer(player.getName(), player.getUniqueId()));
			}
		});
	}

}
