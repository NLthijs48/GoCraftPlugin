package me.wiefferink.gocraft.information;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Provider of information about a player
 */
public interface InformationProvider {
	/**
	 * Show the information to the sender
	 * @param about The Player to show the information about
	 * @param to The CommandSender to show the information to
	 */
	void show(Player about, CommandSender to);
}
