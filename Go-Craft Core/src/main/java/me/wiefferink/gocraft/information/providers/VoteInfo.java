package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.gocraft.votes.VoteTop;
import me.wiefferink.interactivemessenger.processing.Message;

public class VoteInfo extends InformationProvider {

	@Override
	public void showAsync(InformationRequest request) {
		Database.run(session -> {
			GCPlayer player = Database.getPlayer(request.getAbout().getUniqueId(), request.getAbout().getName());
			if(player == null) {
				return;
			}

			// Votes in total
			long totalVotes = (long)session.createQuery("SELECT count(*) FROM Vote WHERE gcPlayer = :player")
					.setParameter("player", player)
					.uniqueResult();
			if(totalVotes > 0) {
				request.message(Message.fromKey("information-totalVotes").replacements(totalVotes));
			}

			// Votes this month
			long monthVotes = (long)session.createQuery("SELECT count(*) from Vote WHERE gcPlayer = :player AND at < :nextMonthStart AND at >= :monthStart")
					.setParameter("player", player)
					.setParameter("nextMonthStart", VoteTop.getNextMonthStart())
					.setParameter("monthStart", VoteTop.getMonthStart())
					.uniqueResult();
			request.message(Message.fromKey("information-monthVotes").replacements(monthVotes));
		});
	}

}
