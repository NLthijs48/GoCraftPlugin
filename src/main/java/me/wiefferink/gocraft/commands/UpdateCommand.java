package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UpdateCommand implements CommandExecutor {

	public final String configLine = "enableUpdateCommand";
	private GoCraft plugin;

	public UpdateCommand(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getCommand("update").setExecutor(this);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("gocraft.update")) {
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
		plugin.getDistributionManager().updatePluginData(sender, serverFilter, operationFilter);
		return true;
	}

}

































