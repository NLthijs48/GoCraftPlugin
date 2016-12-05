package me.wiefferink.gocraft.information;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Provider of information about a player
 */
public abstract class InformationProvider {

	public static GoCraft plugin = GoCraft.getInstance();

	/**
	 * Show the information to the sender
	 * @param about The Player to show the information about
	 * @param to The CommandSender to show the information to
	 */
	public abstract void show(Player about, CommandSender to);
}
