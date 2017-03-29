package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.tools.storage.Database;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(indexes = {
		@Index(columnList = "joinedBungee", name = "joinedBungee"),
		@Index(columnList = "leftBungee", name = "leftBungee"),
		@Index(columnList = "playerIp", name = "playerIp")
})
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
	 * Set the player (normally should never be used)
	 */
	public void setPlayer(GCPlayer newPlayer) {
		gcPlayer = newPlayer;
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
		Database.run(session -> {
			int fixedBungeeSessions = session.createQuery("UPDATE BungeeSession SET leftBungee = current_timestamp() WHERE leftBungee IS NULL").executeUpdate();
			if(fixedBungeeSessions > 0) {
				GoCraftBungee.warn("Closed", fixedBungeeSessions, " BungeeSession entries (crash recovery)");
			}
		});
	}

	@Override
	public String toString() {
		return "BungeeSession(player="+gcPlayer.getPlayerName()+", joined="+getJoined()+", left="+getLeft()+", ip="+getPlayerIp()+")";
	}
}
