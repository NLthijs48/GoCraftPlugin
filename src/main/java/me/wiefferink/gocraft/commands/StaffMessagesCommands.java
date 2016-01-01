package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StaffMessagesCommands implements CommandExecutor {


	public final String configLine = "enableStaffMessageCommands";
	private GoCraft plugin;

	public StaffMessagesCommands(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getCommand("StaffBroadcast").setExecutor(this);
			plugin.getCommand("DisplayStaffMessage").setExecutor(this);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equalsIgnoreCase("StaffBroadcast")) {
			if(!sender.hasPermission("gocraft.sendstaffBroadcast")) {
				plugin.message(sender, "staffbroadcast-noPermission");
				return true;
			}

			if(args.length <= 1) {
				plugin.message(sender, "staffbroadcast-help");
				return true;
			}

			String type = args[0];
			String message = args[1];
			for(int i = 2; i < args.length; i++) {
				message += " " + args[i];
			}
			Utils.sendStaffMessage(type, message);

			// Only display to players, in console it will only cause spam (it will already receive the resulting message)
			if(sender instanceof Player) {
				plugin.message(sender, "staffbroadcast-success", type, message);
			}
			return true;
		} else if(command.getName().equalsIgnoreCase("DisplayStaffMessage")) {
			if(!sender.hasPermission("gocraft.displayStaffMessage")) {
				plugin.message(sender, "displaystaffmessage-noPermission");
				return true;
			}

			if(args.length == 0) {
				plugin.message(sender, "displaystaffmessage-help");
				return true;
			}

			// Construct the message
			String message = args[0];
			for(int i = 1; i < args.length; i++) {
				message += " " + args[i];
			}
			Utils.displayStaffMessage(message);
		} else {
			return false;
		}
		return true;
	}

}























