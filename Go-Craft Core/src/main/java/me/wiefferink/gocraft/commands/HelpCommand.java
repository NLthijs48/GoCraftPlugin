package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.permissions.PermissionDefault;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HelpCommand extends Feature {

	private Map<String, List<String>> helpMap;

	public HelpCommand() {
		permission("help", "View the help list", PermissionDefault.TRUE);
		command("help", "Get a list of commands you can use");

		buildHelp();
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!sender.hasPermission("gocraft.help")) {
			plugin.message(sender, "help-noPermission");
			return;
		}
		// Show the help page
		plugin.message(sender, "help-header");
		ConfigurationSection ranksSection = plugin.getGeneralConfig().getConfigurationSection("ranks");
		if (ranksSection == null) {
			Log.warn("[/help] ranksSection does not exist!");
			return;
		}
		List<String> ranks = new ArrayList<>(ranksSection.getKeys(false));
		for (int i = ranks.size() - 1; i >= 0; i--) {
			List<String> entries = helpMap.get(ranks.get(i));
			Message rankPrefix = Message.fromString(ranksSection.getString(ranks.get(i) + ".prefix", ""));
			if (entries != null) {
				for (String entry : entries) {
					String[] parts = entry.split(" \\| ");
					// <info item>
					if(parts.length == 1) {
						plugin.messageNoPrefix(sender, "help-info", parts[0]);
					}
					// <command> | <description>
					else if(parts.length == 2) {
						if(rankPrefix.isEmpty()) {
							plugin.messageNoPrefix(sender, "help-rule", parts[0], parts[1]);
						} else {
							plugin.messageNoPrefix(sender, "help-ruleRank", rankPrefix, parts[0], parts[1]);
						}
					}
				}
			}
		}
		plugin.increaseStatistic("command.help.used");
	}

	/**
	 * Build the help list based on the config
	 */
	public void buildHelp() {
		helpMap = new HashMap<>();
		ConfigurationSection ranksSection = plugin.getGeneralConfig().getConfigurationSection("ranks");
		if (ranksSection == null) {
			Log.warn("[buildHelp] ranksSection does not exist!");
			return;
		}

		// Add entries from the general help section
		ConfigurationSection helpSection = plugin.getGeneralConfig().getConfigurationSection("help");
		if (helpSection != null) {
			for (String ruleKey : helpSection.getKeys(false)) {
				Set<String> servers = plugin.getDistributionManager().resolveServers(ruleKey, new ArrayList<>());
				if (servers.contains(plugin.getServerId())) {
					addHelpEntry("default", Utils.listOrSingle(helpSection, ruleKey));
				}
			}
		}

		// Add entries from the permissions sections
		ConfigurationSection pluginsSection = plugin.getGeneralConfig().getConfigurationSection("plugins");
		if (pluginsSection == null) {
			return;
		}
		for (String pluginKey : pluginsSection.getKeys(false)) {
			String pushTo = pluginsSection.getString(pluginKey + ".pushTo");
			Set<String> pushToServers = plugin.getDistributionManager().resolveServers(pushTo, new ArrayList<>());
			ConfigurationSection permissionsSection = pluginsSection.getConfigurationSection(pluginKey + ".permissions");
			if (pushTo == null || permissionsSection == null) {
				continue;
			}
			for (String permissionsKey : permissionsSection.getKeys(false)) {
				// Get the help entries
				List<String> helpEntries = null;
				if (permissionsSection.isList(permissionsKey + ".help")) {
					helpEntries = permissionsSection.getStringList(permissionsKey + ".help");
				} else if (permissionsSection.isSet(permissionsKey + ".help")) {
					helpEntries = Collections.singletonList(permissionsSection.getString(permissionsKey + ".help"));
				}
				if (helpEntries == null || helpEntries.isEmpty()) {
					continue;
				}
				// Get the applying groups
				String groups = permissionsSection.getString(permissionsKey + ".groups");
				if (groups == null) {
					groups = "default";
				}
				String lowestGroup = Utils.getLowestGroup(groups);
				String serversString = permissionsSection.getString(permissionsKey + ".servers");
				Set<String> servers = null;
				if (serversString != null) {
					servers = plugin.getDistributionManager().resolveServers(serversString, new ArrayList<>());
				}
				if ((servers != null && servers.contains(plugin.getServerId()))
						|| (servers == null && pushToServers != null && pushToServers.contains(plugin.getServerId()))) {
					for (String helpEntry : helpEntries) {
						if (lowestGroup == null) {
							lowestGroup = "default";
						}
						addHelpEntry(lowestGroup, helpEntry);
					}
				}
			}
		}
	}

	/**
	 * Add a help entry to the map
	 * @param rank The rank required for this help entry
	 * @param entry The help entry
	 */
	private void addHelpEntry(String rank, List<String> entry) {
		helpMap
				.computeIfAbsent(rank, key -> new ArrayList<>())
				.addAll(entry);
	}

	private void addHelpEntry(String rank, String entry) {
		addHelpEntry(rank, Collections.singletonList(entry));
	}

}


















