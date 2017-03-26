package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.interactivemessenger.processing.Message;

import java.util.Calendar;

public class VoteInfo extends InformationProvider {

	@Override
	public void showAsync(InformationRequest request) {
		database(session -> {
			GCPlayer gcPlayer = Database.getPlayer(request.getAbout().getUniqueId());

			// Votes in total
			Long totalVotes = (Long)session.createQuery("SELECT count(*) FROM Vote WHERE gcPlayer = :player")
					.setParameter("player", gcPlayer)
					.uniqueResult();
			if(totalVotes != null && totalVotes > 0) {
				request.message(Message.fromKey("information-totalVotes").replacements(totalVotes));
			}

			// Votes this month
			Calendar monthStart = Calendar.getInstance();
			monthStart.set(Calendar.DAY_OF_MONTH,	monthStart.getActualMinimum(Calendar.DAY_OF_MONTH));
			monthStart.set(Calendar.HOUR_OF_DAY, 0);
			monthStart.set(Calendar.MINUTE, 0);
			monthStart.set(Calendar.SECOND, 0);
			monthStart.set(Calendar.MILLISECOND, 0);
			Calendar monthEnd = Calendar.getInstance();
			monthEnd.set(Calendar.DAY_OF_MONTH,	monthEnd.getActualMaximum(Calendar.DAY_OF_MONTH));
			monthEnd.set(Calendar.HOUR_OF_DAY, 23);
			monthEnd.set(Calendar.MINUTE, 59);
			monthEnd.set(Calendar.SECOND, 59);
			monthEnd.set(Calendar.MILLISECOND, 999);
			Long monthVotes = (Long)session.createQuery("SELECT count(*) from Vote WHERE gcPlayer = :player AND at < :monthEnd AND at >= :monthStart")
					.setParameter("player", gcPlayer)
					.setParameter("monthEnd", monthEnd.getTime())
					.setParameter("monthStart", monthStart.getTime())
					.uniqueResult();
			request.message(Message.fromKey("information-monthVotes").replacements(monthVotes));
		});
	}

}
