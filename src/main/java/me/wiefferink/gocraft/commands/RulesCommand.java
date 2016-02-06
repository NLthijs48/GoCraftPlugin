package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;

public class RulesCommand implements CommandExecutor {


	public final String configLine = "enableRulesCommand";
	private GoCraft plugin;

	List<String> rules;

	public RulesCommand(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			buildRules();
			plugin.getCommand("Rules").setExecutor(this);
		}
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("gocraft.help")) {
			plugin.message(sender, "help-noPermission");
			return true;
		}
		// Show the help
		plugin.message(sender, "help-header");
		for (String rule : rules) {
			plugin.messageNoPrefix(sender, "help-rule", rule);
		}
		return true;
	}

	/**
	 * Build the help list based on the config
	 */
	public void buildRules() {
		this.rules = new ArrayList<>();
		ConfigurationSection rulesSection = plugin.getGeneralConfig().getConfigurationSection("help");
		if (rulesSection == null) {
			plugin.getLogger().warning("Empty help section!");
			return;
		}
		for (String ruleKey : rulesSection.getKeys(false)) {
			List<String> servers = plugin.getDistributionManager().resolveServers(ruleKey, new ArrayList<String>(), true);
			if (servers.contains(plugin.getServerId())) {
				if (rulesSection.isList(ruleKey)) {
					rules.addAll(rulesSection.getStringList(ruleKey));
				} else {
					rules.add(rulesSection.getString(ruleKey));
				}
			}
		}
	}
}


















