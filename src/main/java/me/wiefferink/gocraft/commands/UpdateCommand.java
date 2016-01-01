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

		String filter = null;
		if(args.length > 0) {
			filter = args[0];
			for(int i = 1; i < args.length; i++) {
				filter += ", " + args[i];
			}
			filter = args[0];
		}
		plugin.getDistributionManager().updatePluginData(sender, filter);
		return true;
	}

}

































