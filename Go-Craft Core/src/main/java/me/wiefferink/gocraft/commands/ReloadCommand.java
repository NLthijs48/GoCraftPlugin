package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.CommandSender;

public class ReloadCommand extends Feature {

	public ReloadCommand() {
		command("gcreload", "Reload the configuration files of the Go-Craft plugin");
	}

	@Override
	public void onCommand(CommandSender sender, String command, String[] args) {
		if (!sender.hasPermission("gocraft.reload")) {
			plugin.message(sender, "reload-noPermission");
			return;
		}

		plugin.reload();
		plugin.message(sender, "reload-success");
		plugin.increaseStatistic("command.reload.used");
	}

}
