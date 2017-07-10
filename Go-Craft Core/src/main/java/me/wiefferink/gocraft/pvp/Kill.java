package me.wiefferink.gocraft.pvp;

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
		@Index(columnList = "at", name = "at"),
		@Index(columnList = "discarded", name = "discarded")
})
public class Kill {

	@Id
	@GeneratedValue
	@Column
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn // Can be null
	private GCPlayer killer;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private GCPlayer died;

	@Column(nullable = false)
	private Date at;

	@Column(nullable = false)
	private boolean discarded;

	protected Kill() {}

	public Kill(GCPlayer died, GCPlayer killer) {
		this.died = died;
		this.killer = killer;
		this.at = new Date();
	}

	/**
	 * Get the player that killed
	 * @return GCPlayer that killed
	 */
	public GCPlayer getKiller() {
		return killer;
	}

	/**
	 * Get the player that died
	 * @return GCPlayer that died
	 */
	public GCPlayer getDied() {
		return died;
	}

	/**
	 * Get the kill date
	 * @return Date this kill happened
	 */
	public Date getAt() {
		return at;
	}

	/**
	 * Check if this kill is discarded (for example because the killer is caught as hacker)
	 * @return true if this kill is discarded, otherwise false
	 */
	public boolean isDiscarded() {
		return discarded;
	}

	@Override
	public String toString() {
		return "Kill(killer"+killer.getName()+", died="+died.getName()+", at="+at+", discarded="+discarded+")";
	}
}
