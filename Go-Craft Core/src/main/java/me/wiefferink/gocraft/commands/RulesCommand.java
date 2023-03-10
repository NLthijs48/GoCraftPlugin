package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RulesCommand extends Feature {

	private List<String> rules;

	public RulesCommand() {
		permission("rules", "View the rules list", PermissionDefault.TRUE);
		command("rules", "Check the rules of the current server");

		buildRules();
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("gocraft.rules")) {
			plugin.message(sender, "rules-noPermission");
			return;
		}
		// Show the help
		plugin.message(sender, "rules-header");
		for (String rule : rules) {
			plugin.messageNoPrefix(sender, "rules-rule", rule);
		}
		plugin.increaseStatistic("command.rules.used");
	}

	/**
	 * Build the help list based on the config
	 */
	public void buildRules() {
		this.rules = new ArrayList<>();
		ConfigurationSection rulesSection = plugin.getGeneralConfig().getConfigurationSection("rules");
		if (rulesSection == null) {
			Log.warn("Empty rules section!");
			return;
		}
		for (String ruleKey : rulesSection.getKeys(false)) {
			Set<String> servers = plugin.getDistributionManager().resolveServers(ruleKey, new ArrayList<>());
			if (servers.contains(plugin.getServerId())) {
				rules.addAll(Utils.listOrSingle(rulesSection, ruleKey));
			}
		}
	}
}


















