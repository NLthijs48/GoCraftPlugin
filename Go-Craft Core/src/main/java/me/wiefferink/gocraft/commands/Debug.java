package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.Calendar;

public class Debug extends Feature {

	public Debug() {
		command("warn", "Send warning");
		command("error", "Send error");
		command("exception", "Send exception");
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.isOp()) {
			return;
		}

		if("warn".equalsIgnoreCase(command.getName())) {
			Log.warn("A warning from Go-Craft: ", Calendar.getInstance().getTime());
		} else if("error".equalsIgnoreCase(command.getName())) {
			Log.error("An error from Go-Craft: ", Calendar.getInstance().getTime());
		} else if("exception".equalsIgnoreCase(command.getName())) {
			throw new RuntimeException("An exception from Go-Craft: "+Calendar.getInstance().getTime());
		}
	}
}
