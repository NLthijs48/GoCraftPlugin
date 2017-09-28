package me.wiefferink.gocraft.api.messages.out;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.api.WebClient;
import me.wiefferink.gocraft.tools.storage.Database;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteStatusReponse extends Response {

	public Map<String, VoteSiteStatus> status;

	public VoteStatusReponse(WebClient client) {
		super("voteSites/STATUS_UPDATE");

		Database.run(session -> {
			// TODO improve by getting GCPlayers by ip, then getting votes for those players as well (maybe only if 1 player found?)

			// Try to find the connected client
			Log.debug("Building status, client ip:", client.getIp());
			@SuppressWarnings("unchecked")
			List<Object[]> voteSites = session.createQuery("" +
					"select max(vote.at) as lastVoted, serviceName " +
					"from " +
					"Vote as vote " +
					"where " +
					"vote.address = :ip " +
					"group by serviceName")
				.setParameter("ip", client.getIp())
				.getResultList();

			// Collect results
			status = new HashMap<>();
			for(Object[] voteSiteDetails : voteSites) {
				Date lastVoted = (Date)voteSiteDetails[0];
				String serviceId = (String)voteSiteDetails[1];

				status.put(serviceId, new VoteSiteStatus(lastVoted));
			}
		});
	}

	/**
	 * Status of a vote site
	 */
	private class VoteSiteStatus {
		public VoteSiteStatus(Date lastVoted) {
			this.lastVoted = lastVoted.getTime();
		}
		public long lastVoted;
	}
}
