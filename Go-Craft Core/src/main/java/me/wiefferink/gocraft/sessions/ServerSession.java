package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.tools.storage.Database;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table
public class ServerSession {

	@Id
	@GeneratedValue
	@Column
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private BungeeSession bungeeSession;

	@Column(nullable = false)
	private String serverName;

	@Column(nullable = false)
	private Date joinedServer;

	@Column
	private Date leftServer;

	/**
	 * Constructor for Hibernate
	 */
	ServerSession() {}

	/**
	 * Create a new ServerSession
	 * @param bungeeSession The BungeeSession the ServerSession is happening in
	 */
	public ServerSession(BungeeSession bungeeSession, String server) {
		this.joinedServer = new Date();
		this.leftServer = null;
		this.serverName = server;
		this.bungeeSession = bungeeSession;
	}

	/**
	 * Get the time at which the player joinedServer
	 * @return The join time
	 */
	public Date getJoined() {
		return joinedServer;
	}

	/**
	 * Get the time at which the player leftServer
	 * @return The leave time or null if still online
	 */
	public Date getLeft() {
		return leftServer;
	}

	/**
	 * Set the time at which the player leftServer if not set yet
	 */
	public void hasLeft() {
		if(leftServer == null) {
			leftServer = new Date();
		}
	}

	/**
	 * Get the serverName that the player was on
	 * @return The name of the serverName
	 */
	public String getServerName() {
		return serverName;
	}

	/**
	 * Get the bungeesession in which the serverName has been visited
	 * @return The BungeeSession
	 */
	public BungeeSession getBungeeSession() {
		return bungeeSession;
	}


	/**
	 * Ensure all ServerSession entries are consistent:
	 * - Set the left_bungee field to the current time if not set
	 */
	public static void ensureConsistency() {
		Session session = Database.getSession();

		int fixedServerSessions = session.createQuery("UPDATE ServerSession SET leftServer = current_timestamp() WHERE leftServer IS NULL").executeUpdate();
		if(fixedServerSessions > 0) {
			GoCraftBungee.warn("Closed", fixedServerSessions, "ServerSession entries (crash recovery)");
		}

		Database.closeSession();
	}


	@Override
	public String toString() {
		return "ServerSession(server="+getServerName()+", joined="+getJoined()+", left="+getLeft()+")";
	}
}
