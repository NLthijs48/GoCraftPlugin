package me.wiefferink.gocraft.api.messages.out;

import me.wiefferink.gocraft.api.types.WebsitePlayer;
import me.wiefferink.gocraft.votes.VoteTop;
import me.wiefferink.gocraft.votes.VoteTopEntry;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class VoteTopResponse extends Response {

	public VoteRanking ranking;

	public VoteTopResponse(int year, int month, int start, int items) {
		super("voting/TOP_UPDATE");

		this.ranking = new VoteRanking();
		this.ranking.year = year;
		this.ranking.month = month;
		this.ranking.start = start;
		this.ranking.end = start+items;

		Calendar date = Calendar.getInstance();
		date.set(Calendar.YEAR, year);
		date.set(Calendar.MONTH, month-1); // Zero-indexed month
		List<VoteTopEntry> entries = VoteTop.getVoteTop(VoteTop.getMonthStart(date), VoteTop.getNextMonthStart(date), start, items);
		this.ranking.entries = new ArrayList<>();
		for(VoteTopEntry entry : entries) {
			this.ranking.entries.add(new VoteTopEntryWebsite(new WebsitePlayer(entry.player), entry.rank, entry.votes));
		}
	}

	private class VoteRanking {
		public List<VoteTopEntryWebsite> entries;
		public int year;
		public int month;
		public int start;
		public int end;
	}

	/**
	 * Entry of a vote top
	 */
	private class VoteTopEntryWebsite {
		public VoteTopEntryWebsite(WebsitePlayer player, int rank, long votes) {
			this.player = player;
			this.rank = rank;
			this.votes = votes;
		}
		public WebsitePlayer player;
		public int rank;
		public long votes;
	}
}
