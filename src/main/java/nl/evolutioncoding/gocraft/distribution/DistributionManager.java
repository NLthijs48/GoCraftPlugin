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
			File serverFile = new File(plugin.getGeneralFolder().getParent()+File.separator+plugin.getGeneralConfig().getString("servers."+serverId+".directory")+File.separator+"plugins");
			if(serverFile.isDirectory()) {
				serverPluginFolders.put(serverId, serverFile);
			} else {
				plugin.getLogger().warning("Incorrect serverPluginFolder file: "+serverFile.getAbsolutePath()+"serverId="+serverId);
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
				String serverGroupString = serverGroupsSection.getString(serverGroup);
				List<String> warnings = new ArrayList<>();
				List<String> serverGroupContent = resolveServers(serverGroupString, warnings);
				serverGroups.put(serverGroup, serverGroupContent);
				for(String warning : warnings) {
					plugin.getLogger().warning("Warnings for serverGroup "+serverGroup+":");
					plugin.getLogger().warning("  "+warning);
				}
			}
		}
	}

	/**
	 * Perform an update on the plugin data sync
	 * @param executor The CommandSender that executed the update
	 * @param filter The filter specifying which servers should be pushed to
	 */
	public void updatePluginDataNow(CommandSender executor, String filter) {
		List<String> generalWarnings = new ArrayList<>();
		final List<String> include = resolveServers(filter, generalWarnings);

		int pluginsUpdated = 0;
		int jarsUpdated = 0;
		int configsUpdated = 0;
		ConfigurationSection pushPlugins = plugin.getGeneralConfig().getConfigurationSection("plugins");
		if(pushPlugins == null) {
			generalWarnings.add("No pushPlugins section");
			return;
		}

		// All files in the pluginData folder
		File[] files = pluginDataFolder.listFiles();
		if(files == null) {
			generalWarnings.add("No files found in the pluginData folder: "+pluginDataFolder.getAbsolutePath());
			return;
		}

		// Execute all plugin pushing
		for(String pushPlugin : pushPlugins.getKeys(false)) {
			List<String> pluginWarnings = new ArrayList<>();
			ConfigurationSection pushPluginSection = pushPlugins.getConfigurationSection(pushPlugin);
			String pushTo;
			if(pushPluginSection != null) {
				pushTo = pushPluginSection.getString("pushTo");
			} else {
				pushTo = pushPlugins.getString(pushPlugin);
			}
			if(pushTo == null) {
				pluginWarnings.add("Did not find a pushTo specification");
				continue;
			}
			List<String> servers = resolveServers(pushTo, pluginWarnings);

			// Search jarfile to push
			File newPluginJar = null;
			File newPluginConfig = null;
			for(File file : files) {
				if(file.isFile() && matchPluginFileName(pushPlugin, file)) {
					if(newPluginJar == null) {
						newPluginJar = file;
					} else {
						pluginWarnings.add("Found second .jar file match: "+file.getAbsolutePath()+", first="+newPluginJar.getAbsolutePath());
					}
				} else if(file.isDirectory() && matchPluginFileName(pushPlugin, file)) {
					if(newPluginConfig == null) {
						newPluginConfig = file;
					} else {
						pluginWarnings.add("Found second config folder match: "+file.getAbsolutePath()+", first="+newPluginConfig.getAbsolutePath());
					}
				}
			}

			List<String> pushedJarTo = new ArrayList<>();
			List<String> pushedConfigTo = new ArrayList<>();
			// Push to the specified servers
			for(String server : servers) {
				// Skip filtered servers
				if(include != null && !include.contains(server)) {
					continue;
				}

				if(newPluginJar != null) {
					// Find existing jar file
					File oldPluginJar = null;
					File[] existingFiles = serverPluginFolders.get(server).listFiles();
					if(existingFiles != null) {
						for(File file : existingFiles) {
							if(matchPluginFileName(pushPlugin, file)) {
								if(oldPluginJar == null) {
									oldPluginJar = file;
								} else {
									pluginWarnings.add("Found second old .jar file: "+file.getAbsolutePath());
								}
							}
						}
					}

					// Determine to push or not
					if(oldPluginJar == null || FileUtils.isFileNewer(newPluginJar, oldPluginJar)) {
						// Delete old one
						if(oldPluginJar != null && !oldPluginJar.delete()) {
							pluginWarnings.add("Deleting failed: "+oldPluginJar.getAbsolutePath());
						}

						File newFileName = new File(serverPluginFolders.get(server).getAbsolutePath()+File.separator+pushPlugin+" DISTRIBUTED.jar");
						try {
							FileUtils.copyFile(newPluginJar, newFileName);
							boolean permissionsResult = newFileName.setExecutable(true, false);
							permissionsResult = permissionsResult && newFileName.setReadable(true, false);
							permissionsResult = permissionsResult && newFileName.setWritable(true, false);
							if(!permissionsResult) {
								pluginWarnings.add("Setting permissions failed: "+newFileName.getAbsolutePath());
							}
							jarsUpdated++;
							pushedJarTo.add(plugin.getServerName(server));
						} catch(IOException e) {
							pluginWarnings.add("Copy failed: "+newFileName.getAbsolutePath()+", exception:");
							e.printStackTrace();
						}
					}
				}

				// Push config if required
				if(newPluginConfig != null) {
					File targetFolder = new File(serverPluginFolders.get(server).getAbsolutePath()+File.separator+pushPlugin);
					List<String> result = pushStructure(newPluginConfig, targetFolder, pluginWarnings, newPluginConfig);
					if(result.size() > 0) {
						configsUpdated += result.size();
						pushedConfigTo.add(plugin.getServerName(server));
					}
				}
			}
			if(pushedJarTo.size() > 0 || pushedConfigTo.size() > 0) {
				plugin.messageNoPrefix(executor, "update-pluginHeader", pushPlugin);
				if(pushedJarTo.size() > 0) {
					plugin.messageNoPrefix(executor, "update-pushedPluginTo", StringUtils.join(pushedJarTo, ", "));
				}
				if(pushedConfigTo.size() > 0) {
					plugin.messageNoPrefix(executor, "update-pushedConfigTo", StringUtils.join(pushedConfigTo, ", "));
				}
				for(String warning : pluginWarnings) {
					plugin.messageNoPrefix(executor, "update-warning", warning);
				}
				pluginsUpdated++;
			}
		}
		if(generalWarnings.size() > 0) {
			plugin.messageNoPrefix(executor, "update-generalWarnings");
			for(String warning : generalWarnings) {
				plugin.messageNoPrefix(executor, "update-warning", warning);
			}
		}
		if(pluginsUpdated > 0 || jarsUpdated > 0 || configsUpdated > 0) {
			plugin.messageNoPrefix(executor, "update-done", pluginsUpdated, jarsUpdated, configsUpdated);
		} else {
			plugin.messageNoPrefix(executor, "update-none");
		}
	}

	/**
	 * Perform an update on the plugin data async
	 * @param executor The CommandSender that executed the update
	 * @param filter   The filter specifying which servers should be pushed to
	 */
	public void updatePluginData(final CommandSender executor, final String filter) {
		plugin.message(executor, "update-started");
		plugin.loadGeneralConfig(); // Make sure we have the latest plugin info
		new BukkitRunnable() {
			@Override
			public void run() {
				updatePluginDataNow(executor, filter);
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Push a directory structure to another place, only replaces files that are newer in the source
	 * @param source The source directory
	 * @param target The target directory
	 * @param warnings The warnings list
	 * @param rootSource The root source to format messages with
	 * @return List with the file names that are pushed
	 */
	public List<String> pushStructure(File source, File target, List<String> warnings, File rootSource) {
		List<String> result = new ArrayList<>();
		File[] files = source.listFiles();
		if(files == null) {
			warnings.add("Incorrect directory, no files: "+source.getAbsolutePath());
			return result;
		}
		for(File file : files) {
			File fileTarget = new File(target.getAbsolutePath()+File.separator+file.getName());
			if(file.isDirectory()) {
				pushStructure(file, fileTarget, warnings, rootSource);
			} else if(file.isFile()) {
				if(fileTarget.exists() && !fileTarget.isFile()) {
					warnings.add("Target exists but is not a file: "+fileTarget.getAbsolutePath());
					continue;
				}
				if(!fileTarget.exists() || FileUtils.isFileNewer(file, fileTarget)) {
					try {
						FileUtils.copyFile(file, fileTarget);
						boolean permissionsResult = fileTarget.setExecutable(true, false);
						permissionsResult = permissionsResult && fileTarget.setReadable(true, false);
						permissionsResult = permissionsResult && fileTarget.setWritable(true, false);
						if(!permissionsResult) {
							warnings.add("Setting permissions failed: "+fileTarget.getAbsolutePath());
						}
						result.add(file.getAbsolutePath().replace(rootSource.getAbsolutePath(), "")); // Directory structure + filename starting from plugin folder to the file
					} catch(IOException e) {
						warnings.add("Copy failed: "+fileTarget.getAbsolutePath()+", exception: "+e.getMessage());
						e.printStackTrace();
					}
				}
			} else {
				warnings.add("Incorrect file: "+file.getAbsolutePath());
			}
		}
		return result;
	}

	/**
	 * Check if a file matches a plugin name
	 * @param test  The plugin name to test for
	 * @param match The match to test
	 * @return true if the match is of the test plugin, otherwise false
	 */
	public boolean matchPluginFileName(String test, File match) {
		test = test.toLowerCase();
		String name = match.getName().toLowerCase();
		if(name.endsWith(".jar")) {  // Strip .jar
			name = name.substring(0, match.getName().lastIndexOf(".")).toLowerCase();
		}
		return name.startsWith(test + " ") || name.equalsIgnoreCase(test);
	}

	/**
	 * Resolve a server specification to a list of servers
	 * @param serverSpecifier The server specification (comma-separated list)
	 * @return The list of servers indicated by the serverSpecifier
	 */
	public List<String> resolveServers(String serverSpecifier, List<String> warnings) {
		if(serverSpecifier == null) {
			return null;
		}
		List<String> result = new ArrayList<>();
		for(String id : serverSpecifier.split(", ")) {
			List<String> groupContent = serverGroups.get(id);
			if(groupContent != null) {
				if(groupContent.size() == 0) {
					warnings.add("Empty group: "+id);
				}
				result.addAll(groupContent);
			} else {
				ConfigurationSection serverSection = plugin.getGeneralConfig().getConfigurationSection("servers");
				boolean found = false;
				if(serverSection != null) {
					for(String key : serverSection.getKeys(false)) {
						if(id.equalsIgnoreCase(key)
								|| id.equalsIgnoreCase(serverSection.getString(key+".name"))
								|| id.equalsIgnoreCase(serverSection.getString(key+".directory"))) {
							result.add(key);
							found = true;
							break;
						}
					}
				}
				if(!found) {
					warnings.add("server-/group-id '" + id + "' cannot be resolved");
				}
			}
		}
		return result;
	}


}
