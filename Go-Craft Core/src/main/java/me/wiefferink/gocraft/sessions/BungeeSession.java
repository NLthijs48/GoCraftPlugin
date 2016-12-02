package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.GoCraftBungee;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "bungeeSessions")
public class BungeeSession {

	@Id @GeneratedValue
	@Column(name = "id")
	private long id;

	@Column(name = "player_uuid", nullable = false)
	private String uuid;

	@Column(name = "player_name", nullable = false)
	private String name;

	@Column(name = "player_ip", nullable = false)
	private String ip;

	@Column(name = "joined_bungee", nullable = false)
	private Date joined;

	@Column(name = "left_bungee")
	private Date left;

	@OneToMany(mappedBy = "bungeeSession", fetch = FetchType.LAZY)
	private Set<ServerSession> serverSessions;

	/**
	 * Constructor for Hibernate
	 */
	BungeeSession() {}

	/**
	 * Create a new BungeeSession
	 * @param playerUuid The UUID of the player
	 * @param playerName The name of the player
	 * @param ip The ip of the player
	 */
	public BungeeSession(UUID playerUuid, String playerName, String ip) {
		this.uuid = playerUuid.toString();
		this.name = playerName;
		this.ip = ip;
		this.joined = new Date();
		this.left = null;
		this.serverSessions = new HashSet<>();
	}

	/**
	 * Get the id
	 * @return The id of the BungeeSession
	 */
	public long getId() {
		return id;
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
	 * Get the ip address of the player
	 * @return The ip address of the player
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * Get the time at which the player joined
	 * @return The join time
	 */
	public Date getJoined() {
		return joined;
	}

	/**
	 * Get the time at which the player left
	 * @return The leave time or null if still online
	 */
	public Date getLeft() {
		return left;
	}

	/**
	 * Set the time at which the player left if not set yet
	 */
	public void hasLeft() {
		if(left == null) {
			left = new Date();
		}
	}

	/**
	 * Get the ServerSessions
	 * @return The serversessions linked to this BungeeSession
	 */
	public Set<ServerSession> getServerSessions() {
		return serverSessions;
	}


	/**
	 * Ensure all BungeeSession entries are consistent:
	 * - Set the left_bungee field to the current time if not set
	 * @param connector The connector
	 */
	public static void ensureConsistency(SessionConnector connector) {
		Session session = connector.session();
		session.beginTransaction();

		int fixedBungeeSessions = session.createQuery("UPDATE BungeeSession SET left_bungee = current_date() WHERE left_bungee IS NULL").executeUpdate();
		if(fixedBungeeSessions > 0) {
			GoCraftBungee.warn("Closed", fixedBungeeSessions, " BungeeSession entries (crash recovery)");
		}

		session.getTransaction().commit();
		session.clear();
	}
}
