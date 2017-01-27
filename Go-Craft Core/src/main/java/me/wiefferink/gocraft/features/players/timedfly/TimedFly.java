package me.wiefferink.gocraft.features.players.timedfly;

import me.wiefferink.gocraft.sessions.GCPlayer;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table
public class TimedFly {

	@Id
	@GeneratedValue
	@Column
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private GCPlayer buyer;

	@Column(nullable = false)
	private long duration;

	@Column(nullable = false)
	private Date boughtAt;

	@Column
	private Date startedAt = null;

	@Column
	private boolean done = false;

	@Column
	private String server;

	/**
	 * Constructor for Hibernate
	 */
	TimedFly() {}

	/**
	 * Create a TimedFly instance
	 * @param buyer The player that bought it
	 * @param duration For how long it has been bought
	 * @param server The id of the server
	 */
	public TimedFly(GCPlayer buyer, long duration, String server) {
		this.buyer = buyer;
		this.duration = duration;
		this.boughtAt = new Date();
		this.server = server;
	}

	/**
	 * Get the buyer of the timed permission
	 * @return The buyer
	 */
	public GCPlayer getBuyer() {
		return buyer;
	}

	/**
	 * Get the date/time at which the permission has been bought
	 * @return The buy date/time
	 */
	public Date getBoughtAt() {
		return boughtAt;
	}

	/**
	 * Get the date/time at which the permission has been given out
	 * @return The start date/time, or null if not started yet
	 */
	public Date getStartedAt() {
		return startedAt;
	}

	/**
	 * Check if the timed permission is completed
	 * @return true if the permisison is over, false it not over
	 */
	public boolean isDone() {
		return done;
	}

	/**
	 * Get the duration of the bought permission
	 * @return Duration of the permission in milliseconds
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Get the server the permission is bought for
	 * @return The server
	 */
	public String getServer() {
		return server;
	}

}
