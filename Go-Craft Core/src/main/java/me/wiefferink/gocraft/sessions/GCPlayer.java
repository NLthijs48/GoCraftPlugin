package me.wiefferink.gocraft.sessions;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table
public class GCPlayer {

	@Id
	@GeneratedValue
	@Column
	private long id;

	@Column(nullable = false, length = 36)
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

}
