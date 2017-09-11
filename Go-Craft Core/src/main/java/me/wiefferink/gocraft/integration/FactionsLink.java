package me.wiefferink.gocraft.integration;

import com.massivecraft.factions.chat.ChatFormatter;
import org.bukkit.command.CommandSender;

public class FactionsLink {

	/**
	 * Get the Factions chat prefix
	 * @param sender Sender of the message
	 * @param recipient Receiver of the message
	 * @return Prefix that shows the faction of the sender, the role of the sender and the relative faction status compared to the receiver
	 */
	public String getChatPrefix(CommandSender sender, CommandSender recipient) {
		return ChatFormatter.format("{factions_relcolor}{factions_roleprefix}{factions_name|rp}", sender, recipient);
	}

}
