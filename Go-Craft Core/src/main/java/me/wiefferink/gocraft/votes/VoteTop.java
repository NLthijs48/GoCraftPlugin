package me.wiefferink.gocraft.votes;

import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.storage.Database;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class VoteTop {

	/**
	 * Get the vote top for a certain time period.
	 * Needs to be called async, will do database queries.
	 *
	 * @param from Start date of the period to search in (inclusive)
	 * @param till End date of the period to search in (exlusive)
	 * @param rankStart Start of the top, 0 means from rank #1
	 * @param items Number of items from the start item
	 * @return List of @link{VoteTopEntry}s with the data
	 */
	public static List<VoteTopEntry> getVoteTop(Date from, Date till, int rankStart, int items) {
		return Database.get(session -> {
			@SuppressWarnings("unchecked")
			List<Object[]> playerVoteCounts = session.createQuery(
					"SELECT votePlayer, count(*) " +
							"FROM Vote vote " +
							"INNER JOIN vote.gcPlayer as votePlayer " +
							"WHERE vote.at < :tillDate AND vote.at >= :fromDate " +
							"GROUP BY votePlayer " +
							"ORDER BY count(*) DESC, votePlayer.name ASC")
					.setParameter("fromDate", from)
					.setParameter("tillDate", till)
					.setMaxResults(items)
					.setFirstResult(rankStart).getResultList();
			List<VoteTopEntry> result = new ArrayList<>();
			int rank = rankStart+1;
			for(Object[] playerAndCount : playerVoteCounts) {
				VoteTopEntry entry = new VoteTopEntry();
				entry.player = (GCPlayer) playerAndCount[0];
				entry.votes = (long)playerAndCount[1];
				entry.rank = rank;
				result.add(entry);
				rank++;
			}
			return result;
		});
	}


	/**
	 * Get the start of the month
	 * @param at Date to get the start from
	 * @return Start of the month
	 */
	public static Date getMonthStart(Calendar at) {
		Calendar result = Calendar.getInstance();
		result.setTimeInMillis(at.getTimeInMillis());
		result.set(Calendar.DAY_OF_MONTH, at.getActualMinimum(Calendar.DAY_OF_MONTH));
		result.set(Calendar.HOUR_OF_DAY, 0);
		result.set(Calendar.MINUTE, 0);
		result.set(Calendar.SECOND, 0);
		result.set(Calendar.MILLISECOND, 0);
		return result.getTime();
	}

	/**
	 * Get the start of the current month
	 * @return Start of the current month
	 */
	public static Date getMonthStart() {
		return getMonthStart(Calendar.getInstance());
	}

	/**
	 * Get the end of the month (actually the start of the next month)
	 * @param at Date to get the end from
	 * @return End of the month
	 */
	public static Date getNextMonthStart(Calendar at) {
		Date start = getMonthStart(at);
		Calendar result = Calendar.getInstance();
		result.setTimeInMillis(start.getTime());

		result.add(Calendar.MONTH, 1);
		return result.getTime();
	}

	/**
	 * Get the end of the current month
	 * @return End of the current month
	 */
	public static Date getNextMonthStart() {
		return getNextMonthStart(Calendar.getInstance());
	}
}
