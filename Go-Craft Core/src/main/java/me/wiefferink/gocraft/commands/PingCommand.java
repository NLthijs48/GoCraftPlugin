package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand extends Feature {

	public PingCommand() {
		command("Ping");
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if (!sender.hasPermission("gocraft.ping")) {
			plugin.message(sender, "ping-noPermission");
			return;
		}

		Player target = null;
		boolean self = true;
		if (args.length > 0) {
			target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				plugin.message(sender, "ping-playerNotFound", args[0]);
				return;
			}
			self = false;
		}
		if (target == null && sender instanceof Player) {
			target = (Player) sender;
		}

		if (target == null) {
			plugin.message(sender, "ping-noTarget");
			return;
		}

		int ping = Utils.getPing(target);
		if (self) {
			plugin.message(sender, "ping-successSelf", ping);
			plugin.increaseStatistic("command.ping.self");
		} else {
			plugin.message(sender, "ping-successOther", target.getName(), ping);
			plugin.increaseStatistic("command.ping.other");
		}
	}

}
