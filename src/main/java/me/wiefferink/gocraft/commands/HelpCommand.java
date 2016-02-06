package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class HelpCommand implements CommandExecutor {


	public final String configLine = "enableHelpCommand";
	private GoCraft plugin;

	List<String> help;

	public HelpCommand(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			buildHelp();
			plugin.getCommand("Help").setExecutor(this);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("gocraft.help")) {
			plugin.message(sender, "help-noPermission");
			return true;
		}
		// Show the help page
		plugin.message(sender, "help-header");
		for (String rule : help) {
			plugin.messageNoPrefix(sender, "help-rule", rule);
		}
		return true;
	}

	/**
	 * Build the help list based on the config
	 */
	public void buildHelp() {
		this.help = new ArrayList<>();
		ConfigurationSection helpSection = plugin.getGeneralConfig().getConfigurationSection("help");
		if (helpSection == null) {
			plugin.getLogger().warning("Empty help section!");
			return;
		}
		for (String ruleKey : helpSection.getKeys(false)) {
			List<String> servers = plugin.getDistributionManager().resolveServers(ruleKey, new ArrayList<String>(), true);
			if (servers.contains(plugin.getServerId())) {
				if (helpSection.isList(ruleKey)) {
					help.addAll(helpSection.getStringList(ruleKey));
				} else {
					help.add(helpSection.getString(ruleKey));
				}
			}
		}
	}
}


















