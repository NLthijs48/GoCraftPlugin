package me.wiefferink.gocraft.votes;

import com.vexsoftware.votifier.model.VotifierEvent;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.PageDisplay;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.permissions.PermissionDefault;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class VoteManager extends Feature {

	public VoteManager() {
		listen("collectVotes");
		command("votetop", "Vote top 10", "/votetop [page]");
		permission("votetop", "Access to the /votetop command", PermissionDefault.TRUE);
	}

	@EventHandler
	public void voteEvent(VotifierEvent event) {
		OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(event.getVote().getUsername());
		if(offlinePlayer == null || offlinePlayer.getName() == null) {
			GoCraft.warn("Voting user not found: ", event.getVote().getUsername());
			return;
		}

		async(() ->
			database(session -> {
				GCPlayer gcPlayer = Database.getCreatePlayer(offlinePlayer.getUniqueId(), offlinePlayer.getName());
				Vote vote = new Vote(
						gcPlayer,
						event.getVote().getServiceName(),
						event.getVote().getAddress(),
						event.getVote().getTimeStamp()
				);
				session.save(vote);
				GoCraft.debug("Received vote:", vote);
			})
		);
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("gocraft.votetop")) {
			plugin.message(sender, "votetop-noPermission");
			return;
		}

		async(() ->
			database(session -> {
				Date monthStart = VoteManager.getMonthStart().getTime();
				Date monthEnd = VoteManager.getMonthEnd().getTime();

				// Count players that voted in this month
				long playerCount = (long)session.createQuery("SELECT count(distinct gcPlayer_id) from Vote WHERE at < :monthEnd AND at >= :monthStart")
						.setParameter("monthStart", monthStart)
						.setParameter("monthEnd", monthEnd)
						.uniqueResult();

				PageDisplay display = new PageDisplay(sender, (int)playerCount, "/votetop") {

					@Override
					public Message renderHeader() {
						return Message.fromKey("votetop-header").prefix();
					}

					@Override
					public Message renderEmpty() {
						return Message.fromKey("votetop-empty").prefix();
					}

					@Override
					public boolean renderItems(int itemStart, int itemEnd) {
						// Fetch the players and vote counts
						// TODO check if we can prevent a select per gcPlayer
						@SuppressWarnings("unchecked")
						List<Map<String,Object>> playerVoteCounts = session.createQuery(
								"SELECT new map(gcPlayer as gcPlayer, count(*) as votes) " +
										"FROM Vote " +
										"WHERE at < :monthEnd AND at >= :monthStart " +
										"GROUP BY gcPlayer_id " +
										"ORDER BY count(*) DESC")
								.setParameter("monthStart", monthStart)
								.setParameter("monthEnd", monthEnd)
								.setMaxResults(itemEnd - itemStart + 1)
								.setFirstResult(itemStart).getResultList();
						// TODO make result more pretty and typesafe
						int index = 1;
						for(Map<String, Object> playerAndCount : playerVoteCounts) {
							message(Message.fromKey("votetop-item").replacements(index, ((GCPlayer)playerAndCount.get("gcPlayer")).getPlayerName(), playerAndCount.get("votes")));
							index++;
						}
						return true;
					}
				}.renderPage(args.length > 0 ? args[0] : null);

				sync(display::show);
			})
		);
	}

	public static Calendar getMonthStart() {
		Calendar result = Calendar.getInstance();
		result.set(Calendar.DAY_OF_MONTH, result.getActualMinimum(Calendar.DAY_OF_MONTH));
		result.set(Calendar.HOUR_OF_DAY, 0);
		result.set(Calendar.MINUTE, 0);
		result.set(Calendar.SECOND, 0);
		result.set(Calendar.MILLISECOND, 0);
		return result;
	}

	public static Calendar getMonthEnd() {
		Calendar result = Calendar.getInstance();
		result.set(Calendar.DAY_OF_MONTH, result.getActualMaximum(Calendar.DAY_OF_MONTH));
		result.set(Calendar.HOUR_OF_DAY, 23);
		result.set(Calendar.MINUTE, 59);
		result.set(Calendar.SECOND, 59);
		result.set(Calendar.MILLISECOND, 999);
		return result;
	}

}
