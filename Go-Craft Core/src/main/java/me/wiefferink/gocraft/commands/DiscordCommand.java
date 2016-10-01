package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DiscordCommand extends Feature {

	public DiscordCommand() {
		command("Discord");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		plugin.message(sender, "discord-link");
		return true;
	}

}


















