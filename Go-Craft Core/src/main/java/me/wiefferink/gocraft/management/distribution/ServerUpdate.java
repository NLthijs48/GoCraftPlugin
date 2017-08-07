package me.wiefferink.gocraft.management.distribution;

import me.wiefferink.gocraft.tools.config.Config;
import org.bukkit.command.CommandSender;

import java.util.Set;

// TODO ensure config is reloaded before instantiating ServerUpdate

public class ServerUpdate {
	private CommandSender executor;
	private Set<String> servers;
	private boolean force;
	private Config generalConfig;

	/**
	 * Perform a server update
	 * @param executor CommandSender that should receive progress or null if none
	 * @param serverFilter Filter that indicates which servers should update
	 * @param operationFilter Filter to specify the operations that should be done, empty/null for all
	 * @param force When true all files will be pushed, ignoring if they are newer or not
	 * @param generalConfig Config where the update specification can be read from
	 */
	/*
	public ServerUpdate(CommandSender executor, String serverFilter, String operationFilter, boolean force, Config generalConfig) {
		this.executor = executor;
		this.force = force;
		this.generalConfig = generalConfig;

		final Set<String> include = resolveServers(serverFilter, generalWarnings);
	}
	*/

	private void warning(String id, Object... message) {

	}





	/**
	 * Resolve a server specification to a list of servers
	 * @param serverSpecifier The server specification (comma-separated list)
	 * @return The list of servers indicated by the serverSpecifier
	 */
	/*
	public Set<String> resolveServers(String serverSpecifier) {
		return resolveServers(serverSpecifier, 5);
	}
	*/

	/**
	 * Resolve a server specification to a list of servers
	 * @param serverSpecifier The server specification (comma-separated list)
	 * @param recursiveSteps The maximum number of recursive lookups
	 * @return The list of servers indicated by the serverSpecifier
	 */
	/*
	private Set<String> resolveServers(String serverSpecifier, int recursiveSteps) {
		Set<String> result = new TreeSet<>();
		if (serverSpecifier == null) {
			Config serverSection = generalConfig.getSection("servers");
			if (serverSection != null) {
				result.addAll(serverSection.getKeys());
			}
			return result;
		}
		for (String id : serverSpecifier.split(",( )?")) {
			Set<String> groupContent = serverGroups.get(id);
			if (groupContent != null) {
				// Find as server group
				if (groupContent.size() == 0) {
					warnings.add("Empty group: " + id);
				}
				result.addAll(groupContent);
			} else {
				// Find as server
				ConfigurationSection serverSection = plugin.getGeneralConfig().getConfigurationSection("servers");
				boolean found = false;
				if (serverSection != null) {
					for (String key : serverSection.getKeys(false)) {
						if (id.equalsIgnoreCase(key)
								|| id.equalsIgnoreCase(serverSection.getString(key + ".name"))
								|| id.equalsIgnoreCase(serverSection.getString(key + ".directory"))) {
							result.add(key);
							found = true;
							break;
						}
					}
				}
				// Find as plugin, use those instead
				if (!found && recursiveSteps > 0) {
					recursiveSteps--;
					ConfigurationSection pluginSection = plugin.getGeneralConfig().getConfigurationSection("plugins." + id);
					String pushTo = null;
					if (pluginSection != null) {
						pushTo = pluginSection.getString("pushTo");
					} else if (plugin.getGeneralConfig().isString("plugins." + id)) {
						pushTo = plugin.getGeneralConfig().getString("plugins." + id);
					}
					if (plugin.getGeneralConfig().isSet("plugins." + id)) {
						result.addAll(resolveServers(pushTo, warnings, recursiveSteps));
						found = true;
					}
				}

				if (!found) {
					warnings.add("server-/group-id '" + id + "' cannot be resolved");
				}
			}
		}
		return result;
	}
	*/


}
