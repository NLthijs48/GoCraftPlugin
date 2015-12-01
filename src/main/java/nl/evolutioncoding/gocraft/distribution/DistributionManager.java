package nl.evolutioncoding.gocraft.distribution;

import nl.evolutioncoding.gocraft.GoCraft;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DistributionManager {

	private GoCraft plugin;
	private Map<String, List<String>> serverGroups;
	private Map<String, File> serverPluginFolders;
	private File pluginDataFolder;

	public DistributionManager(GoCraft plugin) {
		this.plugin = plugin;
		pluginDataFolder = new File(plugin.getGeneralFolder().getAbsolutePath() + File.separator + GoCraft.generalPluginDataFoldername);

		initializeServerGroups();
		initializeServerPluginFolders();
	}

	/**
	 * Get the ids of all servers
	 * @return The list of ids for all servers
	 */
	public List<String> getServerIds() {
		List<String> result = new ArrayList<>();
		ConfigurationSection servers = plugin.getGeneralConfig().getConfigurationSection("servers");
		if(servers != null) {
			result.addAll(servers.getKeys(false));
		}
		return result;
	}

	/**
	 * Initialize the servers plugin folder paths
	 */
	private void initializeServerPluginFolders() {
		serverPluginFolders = new HashMap<>();
		for(String serverId : getServerIds()) {
			File serverFile = new File(plugin.getGeneralFolder().getParent() + File.separator + serverId + File.separator + "plugins");
			if(serverFile.isDirectory()) {
				serverPluginFolders.put(serverId, serverFile);
			} else {
				plugin.getLogger().warning("Incorrect serverPluginFolder file: " + serverFile.getAbsolutePath());
			}
		}
	}

	/**
	 * Resolve and clean the server groups
	 */
	private void initializeServerGroups() {
		serverGroups = new HashMap<>();
		ConfigurationSection serverGroupsSection = plugin.getGeneralConfig().getConfigurationSection("serverGroups");
		if(serverGroupsSection != null) {
			for(String serverGroup : serverGroupsSection.getKeys(false)) {
				List<String> serverGroupContent = new ArrayList<>();
				String serverGroupString = serverGroupsSection.getString(serverGroup);
				if(serverGroupString == null) {
					continue;
				}
				for(String serverId : serverGroupString.split(", ")) {
					if(plugin.getGeneralConfig().isSet("servers." + serverId)) {
						serverGroupContent.add(serverId);
					} else {
						plugin.getLogger().warning("Invalid serverId '" + serverId + "' for serverGroup " + serverGroup);
					}
				}
				serverGroups.put(serverGroup, serverGroupContent);
			}
		}
	}

	/**
	 * Perform an update on the plugin data
	 * @param executor The CommandSender that executed the update
	 */
	public void updatePluginData(final CommandSender executor) {
		plugin.message(executor, "update-started");
		new BukkitRunnable() {
			@Override
			public void run() {
				List<String> warnings = new ArrayList<>();
				int pluginsUpdated = 0;
				int jarsUpdated = 0;
				ConfigurationSection pushPlugins = plugin.getGeneralConfig().getConfigurationSection("plugins");
				if(pushPlugins == null) {
					warnings.add("No pushPlugins section");
					return;
				}

				// All files in the pluginData folder
				File[] files = pluginDataFolder.listFiles();
				if(files == null) {
					warnings.add("No files found in the pluginData folder: " + pluginDataFolder.getAbsolutePath());
					return;
				}

				// Execute all plugin pushing
				for(String pushPlugin : pushPlugins.getKeys(false)) {
					ConfigurationSection pushPluginSection = pushPlugins.getConfigurationSection(pushPlugin);
					List<String> servers = resolveServers(pushPluginSection.getString("pushTo"));

					// Search jarfile to push
					File newPluginJar = null;
					for(File file : files) {
						if(matchPluginFileName(pushPlugin, file)) {
							if(newPluginJar == null) {
								newPluginJar = file;
							} else {
								warnings.add("Found second .jar file match: " + file.getAbsolutePath() + ", first=" + newPluginJar.getAbsolutePath());
							}
						}
					}
					if(newPluginJar == null) {
						warnings.add("No new plugin jar found for " + pushPlugin);
						continue;
					}

					List<String> pushedTo = new ArrayList<>();
					// Push to the specified servers
					for(String server : servers) {
						// Remove existing plugin file
						File oldPluginJar = null;
						File[] existingFiles = serverPluginFolders.get(server).listFiles();
						if(existingFiles != null) {
							for(File file : existingFiles) {
								if(matchPluginFileName(pushPlugin, file)) {
									if(oldPluginJar == null) {
										oldPluginJar = file;
									} else {
										warnings.add("Found second old .jar file: " + file.getAbsolutePath());
									}
								}
							}
						}

						// Determine to push or not
						if(oldPluginJar == null || FileUtils.isFileNewer(newPluginJar, oldPluginJar)) {
							// Delete old one
							if(oldPluginJar != null && !oldPluginJar.delete()) {
								warnings.add("Could not delete old plugin: " + oldPluginJar.getAbsolutePath());
							}

							File newFileName = new File(serverPluginFolders.get(server).getAbsolutePath() + File.separator + pushPlugin + " DISTRIBUTED.jar");
							try {
								FileUtils.copyFile(newPluginJar, newFileName);
								boolean permissionsResult = newFileName.setExecutable(true, false);
								permissionsResult = permissionsResult && newFileName.setReadable(true, false);
								permissionsResult = permissionsResult && newFileName.setWritable(true, false);
								if(!permissionsResult) {
									warnings.add("Could not correctly set permissions: " + newFileName.getAbsolutePath());
								}
								jarsUpdated++;
								pushedTo.add(plugin.getServerName(server));
							} catch(IOException e) {
								warnings.add("Could not copy plugin to target " + newFileName.getAbsolutePath() + ", exception:");
								e.printStackTrace();
							}
						}
					}
					if(pushedTo.size() > 0) {
						plugin.message(executor, "update-pluginHeader", pushPlugin);
						plugin.message(executor, "update-pushedPluginTo", StringUtils.join(pushedTo, ", "));
						for(String warning : warnings) {
							plugin.message(executor, "update-warning", warning);
						}
						pluginsUpdated++;
					}
				}
				plugin.message(executor, "update-done", pluginsUpdated, jarsUpdated);

			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Check if a file matches a plugin name
	 * @param test  The plugin name to test for
	 * @param match The match to test
	 * @return true if the match is of the test plugin, otherwise false
	 */
	public boolean matchPluginFileName(String test, File match) {
		test = test.toLowerCase();
		if(!match.isFile() || match.getName().lastIndexOf(".") < 0 || !match.getName().substring(match.getName().lastIndexOf("."), match.getName().length()).equalsIgnoreCase(".jar")) {
			return false;
		}
		String name = match.getName().substring(0, match.getName().lastIndexOf(".")).toLowerCase(); // Strip .jar
		return name.startsWith(test + " ") || name.equalsIgnoreCase(test);
	}

	/**
	 * Resolve a server specification to a list of servers
	 * @param serverSpecifier The server specification (comma-separated list)
	 * @return The list of servers indicated by the serverSpecifier
	 */
	public List<String> resolveServers(String serverSpecifier) {
		List<String> result = new ArrayList<>();
		for(String id : serverSpecifier.split(", ")) {
			List<String> groupContent = serverGroups.get(id);
			if(groupContent != null) {
				result.addAll(groupContent);
			} else {
				if(plugin.getGeneralConfig().isSet("servers." + id)) {
					result.add(id);
				} else {
					plugin.getLogger().warning("server-/group-id '" + id + "' cannot be resolved");
				}
			}
		}
		return result;
	}


}
