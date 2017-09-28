package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.rewards.Reward;
import me.wiefferink.gocraft.tools.storage.Database;
import me.wiefferink.gocraft.votes.Vote;
import org.hibernate.Session;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(indexes = {
		@Index(columnList = "uuid", name = "uuid"),
		@Index(columnList = "name", name = "name")
})
public class GCPlayer {

	@Id
	@GeneratedValue
	@Column
	private long id;

	@Column(nullable = false, length = 36, unique = true)
	private String uuid;

	@Column(nullable = false, length = 16, unique = true)
	private String name;

	@OneToMany(mappedBy = "gcPlayer", fetch = FetchType.LAZY)
	private Set<BungeeSession> bungeeSessions;

	@OneToMany(mappedBy = "gcPlayer", fetch = FetchType.LAZY)
	private Set<Vote> votes;

	@OneToMany(mappedBy = "gcPlayer", fetch = FetchType.LAZY)
	private Set<Reward> rewards;

	@Column(nullable = false)
	private boolean invisible = false;

	/**
	 * Constructor for Hibernate
	 */
	protected GCPlayer() {}

	/**
	 * Create a new GCPlayer
	 * @param uuid The UUID of the player
	 * @param name The name of the player
	 */
	public GCPlayer(UUID uuid, String name) {
		this.uuid = uuid.toString();
		this.name = name;
		this.bungeeSessions = new HashSet<>();
	}

	/**
	 * Get the UUID of the player
	 * @return The UUID of the player
	 */
	public String getUniqueId() {
		return uuid;
	}

	/**
	 * Change the uuid of the player
	 * @param uuid The new uuid to use
	 */
	public void setUniqueId(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Get the UUID of the player
	 * @return The unique id of the player that was online
	 */
	public UUID getPlayerUniqueId() {
		return UUID.fromString(uuid);
	}

	/**
	 * Get the name of the player
	 * @return The name of the player
	 */
	public String getName() {
		return name;
	}

	/**
	 * Change the name of the player
	 * @param name The new name of the player
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Check if the player is invisible
	 * @return true if invisible, otherwise false
	 */
	public boolean isInvisible() {
		return invisible;
	}

	/**
	 * Set invisibility of the player
	 * @param invisible true to make the player invisible (from the players list, /info, website, etc.), false to make visible
	 */
	public void setInvisible(boolean invisible) {
		this.invisible = invisible;
	}

	/**
	 * Get the BungeeSessions
	 * @return The bungeesessions linked to this user
	 */
	public Set<BungeeSession> getBungeeSessions() {
		return bungeeSessions;
	}

	/**
	 * Get the last BungeeSession
	 * @return The last BungeeSession or null if none
	 */
	public BungeeSession getLastBungeeSession() {
		return Database.get(session -> session.createQuery(
			"FROM BungeeSession WHERE gcPlayer = :player ORDER BY joinedBungee DESC", BungeeSession.class)
				.setParameter("player", this)
				.setMaxResults(1)
				.uniqueResult()
		);
	}

	/**
	 * Get the last ServerSession
	 * @return The last ServerSession or null if none
	 */
	public ServerSession getLastServerSession() {
		BungeeSession bungeeSession = getLastBungeeSession();
		if(bungeeSession != null) {
			return bungeeSession.getLastServerSession();
		}
		return null;
	}

	/**
	 * Fixes duplicate player object
	 */
	public static void ensureConsistency() {
		Database.run(session -> {
			int merged;
			do {
				merged = 0;
				// Get duplicate players (using LIKE prevents trailing spaces being ignored)
				List<GCPlayer> duplicatePlayers = session.createQuery(
								"FROM GCPlayer one " +
								"WHERE (" +
									"select count(*) " +
									"FROM GCPlayer two " +
									"WHERE one.name=two.name AND length(one.name)=length(two.name) " +
								") > 1" +
								"ORDER BY one.name, one.id", GCPlayer.class)
						.setMaxResults(20)
						.getResultList();

				// Group by name
				Map<String, LinkedList<GCPlayer>> players = new HashMap<>();
				for(GCPlayer player : duplicatePlayers) {
					players.computeIfAbsent(
							player.getName().toLowerCase(),
							k -> new LinkedList<>()
					).add(player);
				}

				// Fix each group of players
				for(LinkedList<GCPlayer> samePlayers : players.values()) {
					GCPlayer keepPlayer = samePlayers.remove();
					int merging = samePlayers.size();
					merge(keepPlayer, samePlayers, session);
					merged += merging;
				}

				if(players.size() > 0) {
					Log.warn("Found and fixed", players.size(), "players that have duplicate GCPlayer rows (merged " + merged + ")");
				}
			} while(merged > 0);
		});
	}

	/**
	 * Merge player objects together
	 * @param keepPlayer The player to keep
	 * @param samePlayers The players to merge into keepPlayer
	 * @param session The session to use for accessing the database
	 */
	public static void merge(GCPlayer keepPlayer, LinkedList<GCPlayer> samePlayers, Session session) {
		while(!samePlayers.isEmpty()) {
			GCPlayer mergePlayer = samePlayers.remove();

			// Move BungeeSession list
			for(BungeeSession bungeeSession : mergePlayer.bungeeSessions) {
				bungeeSession.setPlayer(keepPlayer);
				session.update(bungeeSession);
			}
			keepPlayer.bungeeSessions.addAll(mergePlayer.bungeeSessions);

			// Move votes
			for(Vote vote : mergePlayer.votes) {
				vote.setPlayer(keepPlayer);
				session.update(vote);
			}
			keepPlayer.votes.addAll(mergePlayer.votes);

			// Move rewards
			for(Reward reward : mergePlayer.rewards) {
				reward.setPlayer(keepPlayer);
				session.update(reward);
			}
			keepPlayer.rewards.addAll(mergePlayer.rewards);

			// Remove the duplicate player
			session.remove(mergePlayer);
		}
		session.update(keepPlayer);
	}

	@Override
	public boolean equals(Object object) {
		return object instanceof GCPlayer && ((GCPlayer)object).getUniqueId().equals(uuid);
	}

	@Override
	public String toString() {
		return "GCPlayer(name:"+name+", uuid:"+uuid+")";
	}

}
