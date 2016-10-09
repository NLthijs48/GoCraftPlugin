package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffMessagesCommands extends Feature {

	public StaffMessagesCommands() {
		command("StaffBroadcast");
		command("DisplayStaffMessage");
	}

	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if(command.equalsIgnoreCase("StaffBroadcast")) {
			if (!sender.hasPermission("gocraft.sendstaffBroadcast")) {
				plugin.message(sender, "staffbroadcast-noPermission");
				return;
			}

			if (args.length <= 1) {
				plugin.message(sender, "staffbroadcast-help");
				return;
			}

			String type = args[0];
			String message = args[1];
			for (int i = 2; i < args.length; i++) {
				message += " " + args[i];
			}
			Utils.sendStaffMessage(type, message);

			// Only display to players, in console it will only cause spam (it will already receive the resulting message)
			if (sender instanceof Player) {
				plugin.message(sender, "staffbroadcast-success", type, message);
			}
			plugin.increaseStatistic("command.staffbroadcast.local");
		} else if(command.equalsIgnoreCase("DisplayStaffMessage")) {
			if (!sender.hasPermission("gocraft.displayStaffMessage")) {
				plugin.message(sender, "displaystaffmessage-noPermission");
				return;
			}

			if (args.length == 0) {
				plugin.message(sender, "displaystaffmessage-help");
				return;
			}

			// Construct the message
			String message = args[0];
			for (int i = 1; i < args.length; i++) {
				message += " " + args[i];
			}
			Utils.displayStaffMessage(message);
			plugin.increaseStatistic("command.staffbroadcast.broadcasted");
		}
	}

}























