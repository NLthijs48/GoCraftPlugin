package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.CommandSender;

public class MapCommand extends Feature {

	public MapCommand() {
		command("Map");
	}

	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		plugin.message(sender, "map-link");
	}

}


















