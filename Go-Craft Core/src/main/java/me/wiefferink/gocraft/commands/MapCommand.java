package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MapCommand extends Feature {

	public MapCommand() {
		command("map", "Get a link to the online map");
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		plugin.message(sender, "map-link");
		plugin.increaseStatistic("command.map.used");
	}

}
