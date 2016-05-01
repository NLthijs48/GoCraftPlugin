package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {


	public final String configLine = "enableReloadCommand";
	private GoCraft plugin;

	public ReloadCommand(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getCommand("GCReload").setExecutor(this);
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
		return true;
	}

}
