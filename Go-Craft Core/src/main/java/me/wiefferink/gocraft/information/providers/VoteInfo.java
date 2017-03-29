package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.gocraft.votes.VoteManager;
import me.wiefferink.interactivemessenger.processing.Message;

public class VoteInfo extends InformationProvider {

	@Override
	public void showAsync(InformationRequest request) {
		database(session -> {
			GCPlayer gcPlayer = Database.getPlayer(request.getAbout().getUniqueId());

			// Votes in total
			long totalVotes = (long)session.createQuery("SELECT count(*) FROM Vote WHERE gcPlayer = :player")
					.setParameter("player", gcPlayer)
					.uniqueResult();
			if(totalVotes > 0) {
				request.message(Message.fromKey("information-totalVotes").replacements(totalVotes));
			}

			// Votes this month
			long monthVotes = (long)session.createQuery("SELECT count(*) from Vote WHERE gcPlayer = :player AND at < :monthEnd AND at >= :monthStart")
					.setParameter("player", gcPlayer)
					.setParameter("monthEnd", VoteManager.getMonthEnd().getTime())
					.setParameter("monthStart", VoteManager.getMonthStart().getTime())
					.uniqueResult();
			request.message(Message.fromKey("information-monthVotes").replacements(monthVotes));
		});
	}

}
