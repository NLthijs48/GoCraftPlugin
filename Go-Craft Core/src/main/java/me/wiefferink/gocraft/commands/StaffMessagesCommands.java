package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffMessagesCommands extends Feature {

	public StaffMessagesCommands() {
		permission("sendStaffBroadcast", "Send a message to all staff on the network");
		command("staffbroadcast", "Broadcast a staff message to all servers", "/staffbroadcast <type> <message...>");
		permission("displayStaffMessage", "Display a staff message to all staff on this server");
		command("displaystaffmessage", "Display a staff message in the chat", "/displaystaffmessage <message...>");
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("StaffBroadcast")) {
			if (!sender.hasPermission("gocraft.sendstaffBroadcast")) {
				plugin.message(sender, "staffbroadcast-noPermission");
				return;
			}

			if (args.length <= 1) {
				plugin.message(sender, "staffbroadcast-help");
				return;
			}

			String type = args[0];
			String message = Utils.combineFrom(args, 1, " ");
			Utils.sendStaffMessage(type, message);

			// Only display to players, in console it will only cause spam (it will already receive the resulting message)
			if (sender instanceof Player) {
				plugin.message(sender, "staffbroadcast-success", type, message);
			}
			plugin.increaseStatistic("command.staffbroadcast.local");
		} else if(command.getName().equalsIgnoreCase("DisplayStaffMessage")) {
			if (!sender.hasPermission("gocraft.displayStaffMessage")) {
				plugin.message(sender, "displaystaffmessage-noPermission");
				return;
			}

			if (args.length == 0) {
				plugin.message(sender, "displaystaffmessage-help");
				return;
			}

			// Construct the message
			String message = StringUtils.join(args, " ");
			Utils.displayStaffMessage(message);
			plugin.increaseStatistic("command.staffbroadcast.broadcasted");
		}
	}

}























