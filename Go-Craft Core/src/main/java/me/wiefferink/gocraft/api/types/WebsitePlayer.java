package me.wiefferink.gocraft.api.types;

import me.wiefferink.gocraft.sessions.GCPlayer;

/**
 * Player with details relevant for the website
 */
public class WebsitePlayer {
	public WebsitePlayer(String name, String uuid) {
		this.name = name;
		this.uuid = uuid;
	}
	public WebsitePlayer(GCPlayer player) {
		this.name = player.getName();
		this.uuid = player.getUniqueId();
	}

	public String game = "minecraft";
	public String name;
	public String uuid;
}
