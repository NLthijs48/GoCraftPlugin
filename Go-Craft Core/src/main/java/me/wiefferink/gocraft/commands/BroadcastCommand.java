package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BroadcastCommand extends Feature {

	public BroadcastCommand() {
		permission("broadcast", "Broadcast messages across all servers", PermissionDefault.OP);
		command("broadcast", "Broadcast a message to all servers", "/broadcast message...");
		command("localbroadcast", "Show a broadcast on this server", "/dobroadcast message...");
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if("broadcast".equalsIgnoreCase(command.getName())) {
			if(!sender.hasPermission("gocraft.broadcast")) {
				plugin.message(sender, "broadcast-noPermission");
				return;
			}

			List<String> doArgs = new ArrayList<>(Arrays.asList(args));
			doArgs.add(0, "localbroadcast");
			plugin.getSyncCommandsServer().runCommand("syncServers", doArgs.toArray(new String[doArgs.size()]));
			plugin.increaseStatistic("command.broadcast.used");
		} else if("localbroadcast".equalsIgnoreCase(command.getName())) {
			if(!sender.hasPermission("gocraft.broadcast")) {
				plugin.message(sender, "broadcast-noPermission");
				return;
			}

			Utils.broadcast(Message.fromKey("broadcast-message").replacements(StringUtils.join(args, " ")));
            plugin.increaseStatistic("command.localbroadcast.used");
		}
	}

}
