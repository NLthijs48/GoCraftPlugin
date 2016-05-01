package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PingCommand implements CommandExecutor {


	public final String configLine = "enablePingCommand";
	private GoCraft plugin;

	public PingCommand(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getCommand("Ping").setExecutor(this);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("gocraft.ping")) {
			plugin.message(sender, "ping-noPermission");
			return true;
		}

		Player target = null;
		boolean self = true;
		if (args.length > 0) {
			target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				plugin.message(sender, "ping-playerNotFound", args[0]);
				return true;
			}
			self = false;
		}
		if (target == null && sender instanceof Player) {
			target = (Player) sender;
		}

		if (target == null) {
			plugin.message(sender, "ping-noTarget");
			return true;
		}

		int ping = Utils.getPing(target);
		if (self) {
			plugin.message(sender, "ping-successSelf", ping);
		} else {
			plugin.message(sender, "ping-successOther", target.getName(), ping);
		}

		return true;
	}

}
