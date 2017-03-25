package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class DiscordCommand extends Feature {

	public DiscordCommand() {
		command("discord", "Get a link to join Discord", "/discord", "voice", "teamspeak", "ts");
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		plugin.message(sender, "discord-link");
		plugin.increaseStatistic("command.discord.used");
	}

}


















