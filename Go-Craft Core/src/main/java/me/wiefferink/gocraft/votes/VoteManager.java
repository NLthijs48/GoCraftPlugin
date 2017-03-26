package me.wiefferink.gocraft.votes;

import com.vexsoftware.votifier.model.VotifierEvent;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.sessions.GCPlayer;
import me.wiefferink.gocraft.tools.storage.Database;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;

public class VoteManager extends Feature {

	public VoteManager() {
		listen("collectVotes");
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

}
