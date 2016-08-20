package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class MapCommand extends Feature {

	public MapCommand() {
		command("Map");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		plugin.message(sender, "map-link");
		return true;
	}

}


















