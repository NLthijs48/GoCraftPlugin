package me.wiefferink.gocraft.sessions;

import me.wiefferink.gocraft.GoCraftBungee;
import org.hibernate.Session;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "serverSessions")
public class ServerSession {

	@Id @GeneratedValue
	@Column(name = "id")
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "bungee_session", nullable = false)
	private BungeeSession bungeeSession;

	@Column(name = "joined_server", nullable = false)
	private Date joined;

	@Column(name = "left_server")
	private Date left;

	@Column(name = "server", nullable = false)
	private String server;

	/**
	 * Constructor for Hibernate
	 */
	ServerSession() {}

	/**
	 * Create a new ServerSession
	 * @param bungeeSession The BungeeSession the ServerSession is happening in
	 */
	public ServerSession(BungeeSession bungeeSession, String server) {
		this.joined = new Date();
		this.left = null;
		this.server = server;
		this.bungeeSession = bungeeSession;
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
	 * Get the server that the player was on
	 * @return The name of the server
	 */
	public String getServer() {
		return server;
	}

	/**
	 * Get the bungeesession in which the server has been visited
	 * @return The BungeeSession
	 */
	public BungeeSession getBungeeSession() {
		return bungeeSession;
	}


	/**
	 * Ensure all ServerSession entries are consistent:
	 * - Set the left_bungee field to the current time if not set
	 * @param connector The connector
	 */
	public static void ensureConsistency(SessionConnector connector) {
		Session session = connector.session();
		session.beginTransaction();

		int fixedServerSessions = session.createQuery("UPDATE ServerSession SET left_server = current_date() WHERE left_server IS NULL").executeUpdate();
		if(fixedServerSessions > 0) {
			GoCraftBungee.warn("Closed", fixedServerSessions, "ServerSession entries (crash recovery)");
		}

		session.getTransaction().commit();
		session.clear();
	}

}
