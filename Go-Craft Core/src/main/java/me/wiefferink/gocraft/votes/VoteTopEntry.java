package me.wiefferink.gocraft.votes;

import me.wiefferink.gocraft.sessions.GCPlayer;

/**
 * Storage class for simple vote top entry
 */
public class VoteTopEntry {
	public GCPlayer player;
	public long votes;
	// Starting from 1
	public int rank;
}
