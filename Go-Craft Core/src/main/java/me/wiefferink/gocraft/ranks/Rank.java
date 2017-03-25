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
@Table
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

}
