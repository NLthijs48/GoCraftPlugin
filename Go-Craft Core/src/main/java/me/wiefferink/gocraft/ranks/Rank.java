package me.wiefferink.gocraft.ranks;

import me.wiefferink.gocraft.sessions.GCPlayer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table // TODO indexes
public class Rank {

	@Id
	@GeneratedValue
	@Column
	private long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(nullable = false)
	private GCPlayer gcPlayer;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(nullable = false, length = 50)
	private String servers;

	Rank() {}

	public Rank(GCPlayer gcPlayer, String name, String servers) {
		this.gcPlayer = gcPlayer;
		this.name = name;
		this.servers = servers;
	}

	/**
	 * Get the player this rank is linked to
	 * @return The GCPlayer the rank is linked to
	 */
	public GCPlayer getGcPlayer() {
		return gcPlayer;
	}

	/**
	 * Get the name of the rank
	 * @return Name of the rank
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the servers
	 * @return The servers the rank should be applied to
	 */
	public String getServers() {
		return servers;
	}
}
