package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.tools.storage.Database;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table
public class BungeeSession {

	@Id
	@GeneratedValue
	@Column
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private GCPlayer gcPlayer;

	@Column(nullable = false)
	private String playerIp;

	@Column(nullable = false)
	private Date joinedBungee;

	@Column
	private Date leftBungee;

	@OneToMany(mappedBy = "bungeeSession", fetch = FetchType.LAZY)
	private Set<ServerSession> serverSessions;

	/**
	 * Constructor for Hibernate
	 */
	BungeeSession() {}

	/**
	 * Create a new BungeeSession
	 * @param gcPlayer The gcPlayer that has joinedBungee
	 */
	public BungeeSession(GCPlayer gcPlayer, String ip) {
		this.gcPlayer = gcPlayer;
		this.playerIp = ip;
		this.joinedBungee = new Date();
		this.leftBungee = null;
		this.serverSessions = new HashSet<>();
	}

	/**
	 * Get the player
	 * @return The player that has this session
	 */
	public GCPlayer getPlayer() {
		return gcPlayer;
	}

	/**
	 * Get the playerIp address of the player
	 * @return The playerIp address of the player
	 */
	public String getPlayerIp() {
		return playerIp;
	}

	/**
	 * Get the time at which the player joinedBungee
	 * @return The join time
	 */
	public Date getJoined() {
		return joinedBungee;
	}

	/**
	 * Get the time at which the player leftBungee
	 * @return The leave time or null if still online
	 */
	public Date getLeft() {
		return leftBungee;
	}

	/**
	 * Set the time at which the player leftBungee if not set yet
	 */
	public void hasLeft() {
		if(leftBungee == null) {
			leftBungee = new Date();
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
	 */
	public static void ensureConsistency() {
		Session session = Database.getSession();

		int fixedBungeeSessions = session.createQuery("UPDATE BungeeSession SET leftBungee = current_date() WHERE leftBungee IS NULL").executeUpdate();
		if(fixedBungeeSessions > 0) {
			GoCraftBungee.warn("Closed", fixedBungeeSessions, " BungeeSession entries (crash recovery)");
		}

		Database.closeSession();
	}
}
