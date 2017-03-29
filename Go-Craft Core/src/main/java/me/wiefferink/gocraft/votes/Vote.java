package me.wiefferink.gocraft.votes;

import me.wiefferink.gocraft.sessions.GCPlayer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(indexes = {
		@Index(columnList = "serviceName", name = "serviceName"),
		@Index(columnList = "address", name = "address"),
		@Index(columnList = "at", name = "at")
})
public class Vote {

	@Id
	@GeneratedValue
	@Column
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private GCPlayer gcPlayer;

	@Column(nullable = false, length = 100)
	private String serviceName;

	@Column(nullable = false, length = 20)
	private String address;

	@Column(nullable = false)
	private Date at;

	@Column(nullable = false, length = 20)
	private String givenTimestamp;

	Vote() {}

	public Vote(GCPlayer gcPlayer, String serviceName, String address, String givenTimestamp) {
		this.gcPlayer = gcPlayer;
		this.serviceName = serviceName;
		this.address = address;
		this.givenTimestamp = givenTimestamp;
		this.at = new Date();
	}

	/**
	 * Get the player that did the vote
	 * @return The GCPlayer that voted
	 */
	public GCPlayer getGcPlayer() {
		return gcPlayer;
	}

	/**
	 * The website that has been voted on
	 * @return The website that has been voted on
	 */
	public String getServiceName() {
		return serviceName;
	}

	/**
	 * Get the vote date
	 * @return The date at which has been voted
	 */
	public Date getAt() {
		return at;
	}

	@Override
	public String toString() {
		return "Vote(player="+gcPlayer.getPlayerName()+", service="+serviceName+", address="+address+", givenTimestamp="+givenTimestamp+", at="+at+")";
	}
}
