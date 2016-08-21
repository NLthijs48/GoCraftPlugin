package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RulesCommand extends Feature {

	private List<String> rules;

	public RulesCommand() {
		buildRules();
		command("Rules");
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
			Set<String> servers = plugin.getDistributionManager().resolveServers(ruleKey, new ArrayList<>());
			if (servers.contains(plugin.getServerId())) {
				List<String> get = Utils.listOrSingle(rulesSection, ruleKey);
				if(get != null) {
					rules.addAll(get);
				}
			}
		}
	}
}


















