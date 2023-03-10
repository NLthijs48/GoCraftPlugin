package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends Feature {

	public ReloadCommand() {
		permission("reload", "Reload configuration data");
		command("gcreload", "Reload the configuration files of the Go-Craft plugin");
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("gocraft.reload")) {
			plugin.message(sender, "reload-noPermission");
			return;
		}

		plugin.reload();
		plugin.message(sender, "reload-success");
		plugin.increaseStatistic("command.reload.used");
	}

}
