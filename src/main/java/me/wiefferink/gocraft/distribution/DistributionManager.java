package me.wiefferink.gocraft.distribution;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.utils.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistributionManager {

	private GoCraft plugin;
	private Map<String, Set<String>> serverGroups;
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
				Set<String> serverGroupContent = resolveServers(serverGroupString, warnings);
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
	 * @param serverFilter The filter specifying which servers should be pushed to
	 * @param operationFilter The filter specifying the operations to perform
	 *                        - pluginJar
	 *                        - pluginConfig
	 *                        - permissions
	 */
	public void updatePluginDataNow(CommandSender executor, String serverFilter, String operationFilter) {
		// Prepare operations
		Set<String> operations = new HashSet<>();
		Set<String> operationsDone = new HashSet<>();
		if (operationFilter == null) { // Add all
			operations.addAll(Arrays.asList("pluginJar", "pluginConfig", "permissions"));
		} else {
			operations.addAll(Arrays.asList(operationFilter.split(",( )?")));
		}

		// Prepare update logger
		BufferedWriter updateLogger = null;
		try {
			updateLogger = new BufferedWriter(new FileWriter(plugin.getGeneralFolder().getAbsolutePath() + File.separator + "updates.log", true));
		} catch (IOException e) {
			plugin.getLogger().warning("Could not create writer to update.log:");
			e.printStackTrace();
		}
		updateMessage(updateLogger, executor, "update-started");

		plugin.loadGeneralConfig(); // Make sure we have the latest plugin info
		List<String> generalWarnings = new ArrayList<>();
		final Set<String> include = resolveServers(serverFilter, generalWarnings);

		int pluginsUpdated = 0;
		int jarsUpdated = 0;
		int configsUpdated = 0;
		int permissionsUpdated = 0;
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
			String pushTo = null;
			if(pushPluginSection != null) {
				pushTo = pushPluginSection.getString("pushTo");
			} else if (pushPlugins.isString(pushPlugin)) {
				pushTo = pushPlugins.getString(pushPlugin);
			}
			Set<String> servers = resolveServers(pushTo, pluginWarnings);

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

				if(operations.contains("pluginJar") && newPluginJar != null) {
					operationsDone.add("pluginJar");
					// Find existing jar file
					File oldPluginJar = null;
					File[] existingFiles = serverPluginFolders.get(server).listFiles();
					if(existingFiles != null) {
						for(File file : existingFiles) {
							if(file.isFile() && matchPluginFileName(pushPlugin, file)) {
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
				if(operations.contains("pluginConfig") && newPluginConfig != null) {
					operationsDone.add("pluginConfig");
					File targetFolder = new File(serverPluginFolders.get(server).getAbsolutePath()+File.separator+pushPlugin);
					List<String> result = pushStructure(newPluginConfig, targetFolder, pluginWarnings, newPluginConfig, server);
					if(result.size() > 0) {
						configsUpdated += result.size();
						pushedConfigTo.add(plugin.getServerName(server));
					}
				}
			}
			if (pushedJarTo.size() > 0 || pushedConfigTo.size() > 0 || pluginWarnings.size() > 0) {
				updateMessage(updateLogger, executor, "update-pluginHeader", pushPlugin);
				if(pushedJarTo.size() > 0) {
					updateMessage(updateLogger, executor, "update-pushedPluginTo", StringUtils.join(pushedJarTo, ", "));
				}
				if(pushedConfigTo.size() > 0) {
					updateMessage(updateLogger, executor, "update-pushedConfigTo", StringUtils.join(pushedConfigTo, ", "));
				}
				for(String warning : pluginWarnings) {
					updateMessage(updateLogger, executor, "update-warning", warning);
				}
				pluginsUpdated++;
			}
		}
		// Update permissions
		if(operations.contains("permissions")) {
			operationsDone.add("permissions");
			List<String> serversUpdated = updatePermissionsNow(include, generalWarnings);
			permissionsUpdated = serversUpdated.size();
			if (permissionsUpdated > 0 && executor instanceof Player) {
				((Player) executor).performCommand("pex reload");
			}
		}

		// Check for leftover operations
		operations.removeAll(operationsDone);
		if (operations.size() > 0) {
			generalWarnings.add("Leftover operations: " + StringUtils.join(", ", operations));
		}

		// Display result (executor + log)
		if(generalWarnings.size() > 0) {
			updateMessage(updateLogger, executor, "update-generalWarnings");
			for(String warning : generalWarnings) {
				updateMessage(updateLogger, executor, "update-warning", warning);
			}
		}
		if (pluginsUpdated > 0 || jarsUpdated > 0 || configsUpdated > 0 || permissionsUpdated > 0) {
			updateMessage(updateLogger, executor, "update-done", pluginsUpdated, jarsUpdated, configsUpdated, permissionsUpdated);
			updateMessage(updateLogger, executor, "update-donePermissions", permissionsUpdated);
		} else {
			updateMessage(updateLogger, executor, "update-none");
		}
		try {
			if (updateLogger != null) {
				updateLogger.close();
			}
		} catch (IOException e) {
			plugin.getLogger().warning("Could not correctly close the updateLogger: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Log a message to a target and to the update log
	 *
	 * @param updateLogger The logger
	 * @param target       The target
	 * @param key          The message key
	 * @param args         The fill in arguments
	 */
	public void updateMessage(BufferedWriter updateLogger, Object target, String key, Object... args) {
		plugin.messageNoPrefix(target, key, args);
		plugin.configurableMessage("[" + Utils.getCurrentDateTime() + "] [" + plugin.getServerName() + "] ", updateLogger, key, args);
	}

	/**
	 * Perform an update on the plugin data async
	 * @param executor The CommandSender that executed the update
	 * @param filter   The filter specifying which servers should be pushed to
	 */
	public void updatePluginData(final CommandSender executor, final String filter, final String operationFilter) {
		new BukkitRunnable() {
			@Override
			public void run() {
				updatePluginDataNow(executor, filter, operationFilter);
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Push a directory structure to another place, only replaces files that are newer in the source
	 * @param source The source directory
	 * @param target The target directory
	 * @param warnings The warnings list
	 * @param rootSource The root source to format messages with
	 * @param server The id of the server it is pushed to
	 * @return List with the file names that are pushed
	 */
	public List<String> pushStructure(File source, File target, List<String> warnings, File rootSource, String server) {
		List<String> result = new ArrayList<>();
		File[] files = source.listFiles();
		if(files == null) {
			warnings.add("Incorrect directory, no files: "+source.getAbsolutePath());
			return result;
		}
		for(File file : files) {
			File fileTarget = new File(target.getAbsolutePath()+File.separator+file.getName());
			if(file.isDirectory()) {
				pushStructure(file, fileTarget, warnings, rootSource, server);
			} else if(file.isFile()) {
				if(fileTarget.exists() && !fileTarget.isFile()) {
					warnings.add("Target exists but is not a file: "+fileTarget.getAbsolutePath());
					continue;
				}
				fileTarget.getParentFile().mkdirs();
				if(!fileTarget.exists() || FileUtils.isFileNewer(file, fileTarget)) {
					// TODO handle binary files with copy instead of line by line
					try (
							BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
							BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileTarget), "UTF8"))) {
						// Only files for which we know a way to comment out lines we can add a header
						if (file.getName().endsWith(".yml")) {
							writer.write("\n\n\n# ========================================================================= #\n" +
									"# ------------------------------ DISTRIBUTED ------------------------------ #\n" +
									"# ----------------- edit this config in the GENERAL folder ---------------- #\n" +
									"# ========================================================================= #\n\n\n\n\n\n");
						}
						String line = reader.readLine();
						while (line != null) {
							writer.write(applyVariables(line, server, warnings) + "\n");
							line = reader.readLine();
						}

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
	 * Update the permissions files of the specified servers
	 * @param includeServers The servers to update the permissions for
	 * @param generalWarnings The list to collect warnings in
	 * @return List with servers that are updated
     */
	public List<String> updatePermissionsNow(Set<String> includeServers, List<String> generalWarnings) {
		List<String> result = new ArrayList<>();

		// Build permissions structure
		// Server -> (group -> permissions)
		Map<String, Map<String, List<String>>> permissions = new HashMap<>();
		ConfigurationSection pluginSection = plugin.getGeneralConfig().getConfigurationSection("plugins");
		if(pluginSection == null) {
			generalWarnings.add("No plugin section");
			return result;
		}
		for(String pluginKey : pluginSection.getKeys(false)) {
			ConfigurationSection permissionsSection = pluginSection.getConfigurationSection(pluginKey+".permissions");
			if(permissionsSection == null) {
				continue;
			}
			for (String sectionKey : permissionsSection.getKeys(false)) {
				ConfigurationSection currentSection = permissionsSection.getConfigurationSection(sectionKey);
				String servers = currentSection.getString("servers");
				if (servers == null) { // default to all servers the plugin is on
					servers = pluginKey;
				}
				Set<String> toServers = resolveServers(servers, generalWarnings);
				String rawGroups = currentSection.getString("groups");
				if (rawGroups == null || rawGroups.isEmpty()) {
					generalWarnings.add("No groups specified in permissions for " + pluginKey);
					continue;
				}
				List<String> toGroups = Arrays.asList(rawGroups.split(",( )?"));
				// Add permissions to the correct groups and servers
				for (String server : toServers) {
					Map<String, List<String>> groupsPermissions = permissions.get(server);
					if (groupsPermissions == null) {
						groupsPermissions = new HashMap<>();
						permissions.put(server, groupsPermissions);
					}
					for (String group : toGroups) {
						List<String> groupPermissions = groupsPermissions.get(group);
						if (groupPermissions == null) {
							groupPermissions = new ArrayList<>();
							groupsPermissions.put(group, groupPermissions);
						}
						if (currentSection.isList("permissions")) {
							groupPermissions.addAll(currentSection.getStringList("permissions"));
						} else if (currentSection.isSet("permissions")) {
							groupPermissions.add(currentSection.getString("permissions"));
						} else {
							generalWarnings.add("No permissions specified for group '" + group + "' on " + server);
						}
					}
				}
			}
		}

		// Write permissions to the files
		for(String server : includeServers) {
			if (serverPluginFolders.get(server) == null) {
				generalWarnings.add("No plugin path found for server: " + server);
				continue;
			}
			File newFile = new File(serverPluginFolders.get(server).getAbsolutePath() + File.separator + "PermissionsEx" + File.separator + "permissions.new.yml");
			File currentFile = new File(serverPluginFolders.get(server).getAbsolutePath() + File.separator + "PermissionsEx" + File.separator + "permissions.yml");
			File oldFile = new File(serverPluginFolders.get(server).getAbsolutePath() + File.separator + "PermissionsEx" + File.separator + "permissions.old.yml");
			if (!currentFile.isFile() || !currentFile.exists()) {
				generalWarnings.add("No current permisisons file: " + server);
				continue;
			}
			try(
					BufferedReader reader = new BufferedReader(new FileReader(currentFile));
					BufferedWriter writer = new BufferedWriter(new FileWriter(newFile))
					) {
				Map<String, List<String>> serverPermissions = permissions.get(server);
				if (serverPermissions == null) {
					continue;
				}
				String line = reader.readLine();
				boolean inGroupsSection = false;
				boolean inOldDistSection = false;
				List<String> currentGroupPermissions = null;
				while(line != null) {
					if(line.startsWith("groups:")) {
						inGroupsSection = true;
					} else if(line.matches("[^ ].*")) {
						inGroupsSection = false;
						currentGroupPermissions = null;
						inOldDistSection = false;
					}
					if(inGroupsSection) {
						if(line.matches("  [^ ].*")) {
							String groupName = line.substring(2, line.length() - 1);
							currentGroupPermissions = serverPermissions.get(groupName);
							writer.write(line + "\n");
						} else {
							if(line.startsWith("    permissions:")) {
								// insert permissions
								writer.write(line + "\n");
								if(currentGroupPermissions != null) {
									writer.write("    ##### START DISTRIBUTED PERMISSIONS\n");
									for(String permission : currentGroupPermissions) {
										writer.write("    - " + permission + "\n");
									}
									writer.write("    ##### END DISTRIBUTED PERMISSIONS\n\n");
								}
							} else if (line.startsWith("    ##### START DISTRIBUTED PERMISSIONS")) {
								inOldDistSection = true;
							} else if (line.startsWith("    ##### END DISTRIBUTED PERMISSIONS")) {
								inOldDistSection = false;
							} else if (!inOldDistSection &&
									(currentGroupPermissions == null
											|| (line.length() > 6
											&& !currentGroupPermissions.contains(line.substring(6))))) { // Only copy permission line if it is not already in the distributed section
								writer.write(line + "\n");
							}
						}
					} else {
						writer.write(line + "\n");
					}
					line = reader.readLine();
				}

				// Delete old file
				if (oldFile.exists() && !oldFile.delete()) {
					generalWarnings.add("Could not delete old permissions file: " + server);
					continue;
				}
			} catch (IOException e) {
				generalWarnings.add("Exception while writing permissions of "+server+": "+e.getMessage());
				e.printStackTrace();
			}
			// Move current file to old file
			if (!currentFile.renameTo(oldFile)) {
				generalWarnings.add("Could not move current permisisons file to old: " + server);
				continue;
			}

			// Move new file to current file
			if (!newFile.renameTo(currentFile)) {
				generalWarnings.add("Could not move new permisisons file to active: " + server);
				if (!oldFile.renameTo(currentFile)) {
					generalWarnings.add("  and could not restore previous one!");
				}
				continue;
			}

			// Set permissions of current file
			boolean permissionsResult = currentFile.setExecutable(true, false);
			permissionsResult = permissionsResult && currentFile.setReadable(true, false);
			permissionsResult = permissionsResult && currentFile.setWritable(true, false);
			if (!permissionsResult) {
				generalWarnings.add("Could not set permissions of permissions file: " + server);
			}

			result.add(server);
		}
		return result;
	}

	/**
	 * Apply variable replacement to a config line
	 *
	 * @param line     The line to check for variables
	 * @param server   The server to replace it for
	 * @param warnings The list of warnings
	 * @return The result string where all variables are replaced with values from the config
	 */
	public String applyVariables(String line, String server, List<String> warnings) {
		Pattern variables = Pattern.compile("<<<(?<variable>[a-zA-Z0-9-_:]+)>>>");
		Matcher matcher = variables.matcher(line);
		while (matcher.find()) {
			String rawVariable = matcher.group("variable");
			String actualServer = server;
			String variable;
			String[] parts = rawVariable.split(":");

			if (parts.length > 1) {
				actualServer = parts[0];
				variable = parts[1];
			} else {
				variable = parts[0];
			}
			String value;
			if ("id".equals(variable)) {
				value = actualServer;
			} else {
				value = plugin.getGeneralConfig().getString("servers." + actualServer + "." + variable);
			}
			if (value == null) {
				warnings.add("Variable value for '" + variable + "' could not be found");
				value = "";
			}
			line = matcher.replaceFirst(value);
			matcher = variables.matcher(line);
		}
		return line;
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
	 *
	 * @param serverSpecifier The server specification (comma-separated list)
	 * @param warnings        The warning list
	 * @return The list of servers indicated by the serverSpecifier
	 */
	public Set<String> resolveServers(String serverSpecifier, List<String> warnings) {
		return resolveServers(serverSpecifier, warnings, 5);
	}

	/**
	 * Resolve a server specification to a list of servers
	 * @param serverSpecifier The server specification (comma-separated list)
	 * @param warnings The warning list
	 * @param recursiveSteps The maximum number of recursive lookups
	 * @return The list of servers indicated by the serverSpecifier
	 */
	public Set<String> resolveServers(String serverSpecifier, List<String> warnings, int recursiveSteps) {
		Set<String> result = new TreeSet<>();
		if(serverSpecifier == null) {
			ConfigurationSection serverSection = plugin.getGeneralConfig().getConfigurationSection("servers");
			if(serverSection != null) {
				result.addAll(serverSection.getKeys(false));
			}
			return result;
		}
		for(String id : serverSpecifier.split(",( )?")) {
			Set<String> groupContent = serverGroups.get(id);
			if(groupContent != null) {
				// Find as server group
				if(groupContent.size() == 0) {
					warnings.add("Empty group: "+id);
				}
				result.addAll(groupContent);
			} else {
				// Find as server
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

				if(!found) {
					warnings.add("server-/group-id '" + id + "' cannot be resolved");
				}
			}
		}
		return result;
	}


}
