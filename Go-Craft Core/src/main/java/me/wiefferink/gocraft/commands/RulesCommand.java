package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
		if (!sender.hasPermission("gocraft.rules")) {
			plugin.message(sender, "rules-noPermission");
			return true;
		}
		// Show the help
		plugin.message(sender, "rules-header");
		for (String rule : rules) {
			plugin.messageNoPrefix(sender, "rules-rule", rule);
		}
		plugin.increaseStatistic("command.rules.used");
		return true;
	}

	/**
	 * Build the help list based on the config
	 */
	public void buildRules() {
		this.rules = new ArrayList<>();
		ConfigurationSection rulesSection = plugin.getGeneralConfig().getConfigurationSection("rules");
		if (rulesSection == null) {
			GoCraft.warn("Empty rules section!");
			return;
		}
		for (String ruleKey : rulesSection.getKeys(false)) {
			Set<String> servers = plugin.getDistributionManager().resolveServers(ruleKey, new ArrayList<String>());
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


















