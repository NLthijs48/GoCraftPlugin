package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.tools.storage.Database;

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

	@Column(nullable = false, length = 16)
	private String name;

	@OneToMany(mappedBy = "gcPlayer", fetch = FetchType.LAZY)
	private Set<BungeeSession> bungeeSessions;

	/**
	 * Constructor for Hibernate
	 */
	GCPlayer() {}

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
	public String getPlayerName() {
		return name;
	}

	/**
	 * Get the BungeeSessions
	 * @return The bungeesessions linked to this user
	 */
	public Set<BungeeSession> getBungeeSessions() {
		return bungeeSessions;
	}

	/**
	 * Fixes duplicate player object
	 */
	public static void ensureConsistency() {
		Database.run((session -> {
			// Get duplicate players
			List<GCPlayer> duplicatePlayers = session.createQuery("SELECT one FROM GCPlayer one WHERE (select count(*) FROM GCPlayer two WHERE one.uuid = two.uuid) > 1", GCPlayer.class)
					.getResultList();

			// Group per UUID
			Map<String, LinkedList<GCPlayer>> players = new HashMap<>();
			for(GCPlayer player : duplicatePlayers) {
				LinkedList<GCPlayer> playerSet = players.get(player.getUniqueId());
				if(playerSet == null) {
					playerSet = new LinkedList<>();
					players.put(player.getUniqueId(), playerSet);
				}
				playerSet.add(player);
			}

			// Fix each group of players
			for(LinkedList<GCPlayer> samePlayers : players.values()) {
				GCPlayer keepPlayer = samePlayers.remove();

				while(!samePlayers.isEmpty()) {
					GCPlayer mergePlayer = samePlayers.remove();
					for(BungeeSession bungeeSession : mergePlayer.bungeeSessions) {
						bungeeSession.setPlayer(keepPlayer);
						session.update(bungeeSession);
					}
					keepPlayer.bungeeSessions.addAll(mergePlayer.bungeeSessions);
					session.remove(mergePlayer);
				}
				session.update(keepPlayer);
			}

			if(players.size() > 0) {
				GoCraftBungee.warn("Found and fixed", players.size(), "players that have duplicate GCPlayer rows");
			}
		}));
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
