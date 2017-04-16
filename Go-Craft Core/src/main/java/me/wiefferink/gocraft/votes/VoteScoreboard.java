package me.wiefferink.gocraft.votes;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.sessions.GCPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

import java.util.Date;
import java.util.List;

public class VoteScoreboard extends Feature {

	boolean secondObjective = false;

	public VoteScoreboard() {
		if(listen("showVoteScoreboard")) {
			asyncTimer(20*10, this::updateScoreboard);
		}
	}

	/**
	 * Update the scoreboard (should be called async)
	 */
	public void updateScoreboard() {
		database(session -> {
			// Collect data
			Date monthStart = VoteManager.getMonthStart().getTime();
			Date monthEnd = VoteManager.getMonthEnd().getTime();
			List<Object[]> playerVoteCounts = session.createQuery(
					"SELECT gcPlayer as gcPlayer, count(*) as votes " +
							"FROM Vote " +
							"WHERE at < :monthEnd AND at >= :monthStart " +
							"GROUP BY gcPlayer_id " +
							"ORDER BY count(*) DESC")
					.setParameter("monthStart", monthStart)
					.setParameter("monthEnd", monthEnd)
					.setMaxResults(10)
					.getResultList();
			long monthVoteCount = (long) session.createQuery("SELECT count(*) from Vote WHERE at < :monthEnd AND at >= :monthStart")
					.setParameter("monthStart", monthStart)
					.setParameter("monthEnd", monthEnd)
					.uniqueResult();

			// Update scoreboard
			sync(() -> {
				// Create objective
				Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
				String objectiveName = secondObjective ? "votes1" : "votes2";

				Objective objective = board.getObjective(objectiveName);
				if(objective != null) {
					objective.unregister();
				}
				objective = board.registerNewObjective(objectiveName, "dummy");
				objective.setDisplayName("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Vote Top 10");

				// Add top 10
				for(Object[] row : playerVoteCounts) {
					Score score = objective.getScore(((GCPlayer)row[0]).getName());
					score.setScore(((Long)row[1]).intValue());
				}

				// Add general stats
				objective.getScore(" ").setScore(-1);
				objective.getScore("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Month total:").setScore(-2);
				objective.getScore(" " + monthVoteCount).setScore(-3);
				objective.getScore("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Get rewards:").setScore(-5);
				objective.getScore(" " + ChatColor.UNDERLINE + "/vote").setScore(-6);

				// Swap to the new one
				objective.setDisplaySlot(DisplaySlot.SIDEBAR);

				secondObjective = !secondObjective;
			});
		});
	}

}
