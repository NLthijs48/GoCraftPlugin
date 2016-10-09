package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.CommandSender;

public class DiscordCommand extends Feature {

	public DiscordCommand() {
		command("Discord");
	}

	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		plugin.message(sender, "discord-link");
	}

}


















