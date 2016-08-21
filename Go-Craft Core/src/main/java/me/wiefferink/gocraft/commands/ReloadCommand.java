package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends Feature {

	public ReloadCommand() {
		if(config.getBoolean("enableReloadCommand")) {
			command("GCReload");
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("gocraft.reload")) {
			plugin.message(sender, "reload-noPermission");
			return true;
		}

		plugin.reload();
		plugin.message(sender, "reload-success");
		plugin.increaseStatistic("command.reload.used");
		return true;
	}

}
