package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.sessions.ServerSession;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OnlinePlayersCommand extends Feature {

	public OnlinePlayersCommand() {
		command("online", "View which players are online", "/online", "list", "glist", "players");
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		async(() -> {
			// Get online players per server
			Map<String, List<GCPlayer>> players = new HashMap<>();
			Database.run(session -> {
				@SuppressWarnings("unchecked")
				List<Map<String,Object>> onlinePlayers = session.createQuery(
						"SELECT new map(gcPlayer as player, serverSession as server) " +
								"FROM ServerSession serverSession " +
								"INNER JOIN serverSession.bungeeSession as bungeeSession " +
								"INNER JOIN bungeeSession.gcPlayer as gcPlayer " +
								"WHERE serverSession.leftServer=null " +
								(sender.hasPermission("gocraft.staff") ? "" : "AND gcPlayer.invisible = false ") +
								"ORDER BY gcPlayer.name")
						.getResultList();

				for(Map<String, Object> onlinePlayerDetails : onlinePlayers) {
					GCPlayer player = (GCPlayer)onlinePlayerDetails.get("player");
					ServerSession server = (ServerSession)onlinePlayerDetails.get("server");

					players.computeIfAbsent(server.getServerName(), key -> new ArrayList<>())
							.add(player);
				}
			});

			// Display online list
			sync(() -> {
				plugin.message(sender, "online-header");
				for(String server : players.keySet()) {
					plugin.message(sender, "online-server", plugin.getServerName(server));
					Message serverPlayers = Message.empty();
					// TODO this might break max message length?
					for(GCPlayer player : players.get(server)) {
						if(!serverPlayers.isEmpty()) {
							serverPlayers.append(", ");
						}

						if(player.isInvisible()) {
							serverPlayers.append("[grey]");
						}
						serverPlayers.append(Message.fromKey("player").replacements(player.getName()));
						if(player.isInvisible()) {
							serverPlayers.append("[white]");
						}
					}
					plugin.message(sender, "online-players", serverPlayers);
				}
			});
		});

		plugin.increaseStatistic("command.online.used");
	}

}


















