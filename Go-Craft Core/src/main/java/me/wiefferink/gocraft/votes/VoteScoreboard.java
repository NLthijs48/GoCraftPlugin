package me.wiefferink.gocraft.votes;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.storage.Database;
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
		Database.run(session -> {
			// Collect data
			Date monthStart = VoteTop.getMonthStart();
			Date nextMonthStart = VoteTop.getNextMonthStart();
			List<VoteTopEntry> voteTopEntries = VoteTop.getVoteTop(monthStart, nextMonthStart, 0, 10);
			long monthVoteCount = (long) session.createQuery("SELECT count(*) from Vote WHERE at < :nextMonthStart AND at >= :monthStart")
					.setParameter("monthStart", monthStart)
					.setParameter("nextMonthStart", nextMonthStart)
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
				for(VoteTopEntry entry : voteTopEntries) {
					Score score = objective.getScore(entry.player.getName());
					score.setScore((int)entry.votes);
				}

				// Add general stats
				objective.getScore(" ").setScore(-1);
				objective.getScore("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Month total:").setScore(-2);
				objective.getScore(" " + monthVoteCount).setScore(-3);
				objective.getScore("" + ChatColor.DARK_GREEN + ChatColor.BOLD + "Get rewards:").setScore(-4);
				objective.getScore(" " + ChatColor.UNDERLINE + "/vote").setScore(-5);

				// Swap to the new one
				objective.setDisplaySlot(DisplaySlot.SIDEBAR);

				secondObjective = !secondObjective;
			});
		});
	}

}
