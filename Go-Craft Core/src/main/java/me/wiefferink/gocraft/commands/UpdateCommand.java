package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class UpdateCommand extends Feature {

	public UpdateCommand() {
		if(plugin.getConfig().getBoolean("enableUpdateCommand")) {
			command("update");
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("gocraft.update")) {
			plugin.message(sender, "update-noPermission");
			return true;
		}

		String serverFilter = null;
		String operationFilter = null;
		if (args.length >= 1) {
			serverFilter = args[0];
		}
		if (args.length >= 2) {
			operationFilter = args[1];
		}
		plugin.getDistributionManager().update(sender, serverFilter, operationFilter);
		plugin.increaseStatistic("command.update.used");
		return true;
	}

}

































