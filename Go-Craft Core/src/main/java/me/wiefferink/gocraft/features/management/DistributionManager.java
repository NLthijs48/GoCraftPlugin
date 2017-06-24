package me.wiefferink.gocraft.features.management;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Constant;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DistributionManager extends Feature {

	private File self;
	private Map<String, Set<String>> serverGroups;
	private Map<String, File> serverPluginFolders;
	private File pluginDataFolder, rootDataFolder;
	private Set<String> binaryFiles = new HashSet<>(Arrays.asList("png", "jpg", "jpeg", "bmp", "jar"));

	public DistributionManager() {
		permission("update", "Update servers");
		command("update",
				"Update servers with the latest plugin files and configurations",
				"/update <servers...> [pluginJar,pluginConfig,permissions,rootfiles]",
				"forceUpdate"
		);

		self = new File(DistributionManager.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		pluginDataFolder = new File(plugin.getGeneralFolder().getAbsolutePath() + File.separator + Constant.GENERAL_PLUGIN_DATA_FOLDERNAME);
		rootDataFolder = new File(plugin.getGeneralFolder().getAbsolutePath() + File.separator + Constant.GENERAL_ROOT_DATA_FOLDERNAME);

		initializeServerGroups();
		initializeServerPluginFolders();
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!sender.hasPermission("gocraft.update")) {
			plugin.message(sender, "update-noPermission");
			return;
		}

		if(args.length == 0) {
			plugin.message(sender, "update-help");
			return;
		}

		boolean force = label.equalsIgnoreCase("forceUpdate");
		String serverFilter = args[0];
		String operationFilter = args.length>=2 ? args[1] : null;
		update(sender, serverFilter, operationFilter, force);
		plugin.increaseStatistic("command.update.used");
	}

	/**
	 * Get the ids of all servers
	 * @return The list of ids for all servers
	 */
	public List<String> getServerIds() {
		List<String> result = new ArrayList<>();
		ConfigurationSection servers = plugin.getGeneralConfig().getConfigurationSection("servers");
		if (servers != null) {
			result.addAll(servers.getKeys(false));
		}
		return result;
	}

	/**
	 * Initialize the servers plugin folder paths
	 */
	private void initializeServerPluginFolders() {
		serverPluginFolders = new HashMap<>();
		for (String serverId : getServerIds()) {
			if (serverId.equals("DEFAULT")) { // Server only used as backup variables, not to actually write to
				continue;
			}
			File serverFile = new File(plugin.getGeneralFolder().getParent() + File.separator + plugin.getGeneralConfig().getString("servers." + serverId + ".directory") + File.separator + "plugins");
			if (serverFile.isDirectory()) {
				serverPluginFolders.put(serverId, serverFile);
			} else {
				Log.warn("Incorrect serverPluginFolder file: "+serverFile.getAbsolutePath()+"serverId="+serverId);
			}
		}
	}

	/**
	 * Resolve and clean the server groups
	 */
	private void initializeServerGroups() {
		serverGroups = new HashMap<>();
		ConfigurationSection serverGroupsSection = plugin.getGeneralConfig().getConfigurationSection("serverGroups");
		if (serverGroupsSection != null) {
			for (String serverGroup : serverGroupsSection.getKeys(false)) {
				String serverGroupString = serverGroupsSection.getString(serverGroup);
				List<String> warnings = new ArrayList<>();
				Set<String> serverGroupContent = resolveServers(serverGroupString, warnings);
				serverGroups.put(serverGroup, serverGroupContent);
				for (String warning : warnings) {
					Log.warn("Warnings for serverGroup "+serverGroup+":");
					Log.warn("  "+warning);
				}
			}
		}
	}

	/**
	 * Perform an update on the plugin data sync
	 * @param executor The CommandSender that executed the update
	 * @param serverFilter The filter specifying which servers should be pushed to
	 * @param operationFilter The filter specifying the operations to perform
	 * @param force Always push, regardless of age
	 * - pluginJar
	 * - pluginConfig
	 * - permissions
	 */
	public void updateNow(final CommandSender executor, String serverFilter, String operationFilter, boolean force) {
		List<String> generalWarnings = new ArrayList<>();

		// Prepare operations
		Set<String> operations = new HashSet<>();
		Set<String> operationsDone = new HashSet<>();
		if (operationFilter == null) { // Add all
			operations.addAll(Arrays.asList("pluginJar", "pluginConfig", "permissions", "rootfiles"));
		} else {
			operations.addAll(Arrays.asList(operationFilter.split(",( )?")));
		}

		// Prepare update logger
		BufferedWriter updateLogger = null;
		try {
			File updates = new File(plugin.getGeneralFolder().getAbsolutePath() + File.separator + "updates.log");
			if (!updates.exists()) {
				if(!updates.getParentFile().mkdirs()) {
					generalWarnings.add("Could not create directories to updates.log file: " + updates.getAbsolutePath());
				} else if (!updates.createNewFile()) {
					generalWarnings.add("Could not create updates.log file: " + updates.getAbsolutePath());
				}
			}
			updateLogger = new BufferedWriter(new FileWriter(updates, true));
		} catch (IOException e) {
			Log.warn("Could not create writer to update.log:");
			e.printStackTrace();
		}
		updateMessage(updateLogger, executor, "update-started", false);

		// Make sure we have the latest plugin info
		plugin.loadGeneralConfig();
		initializeServerGroups();
		initializeServerPluginFolders();

		final Set<String> include = resolveServers(serverFilter, generalWarnings);

		int pluginsUpdated = 0, jarsUpdated = 0, configsUpdated = 0, permissionsUpdated = 0, rootFilesUpdated = 0;
		ConfigurationSection pushPlugins = plugin.getGeneralConfig().getConfigurationSection("plugins");
		if (pushPlugins == null) {
			generalWarnings.add("No pushPlugins section");
			return;
		}

		// All files in the pluginData folder
		File[] files = pluginDataFolder.listFiles();
		if (files == null) {
			generalWarnings.add("No files found in the pluginData folder: " + pluginDataFolder.getAbsolutePath());
			return;
		}

		// Execute all plugin pushing
		for (String pushPlugin : pushPlugins.getKeys(false)) {
			List<String> pluginWarnings = new ArrayList<>();
			ConfigurationSection pushPluginSection = pushPlugins.getConfigurationSection(pushPlugin);
			String pushTo = null;
			if (pushPluginSection != null) {
				pushTo = pushPluginSection.getString("pushTo");
			} else if (pushPlugins.isString(pushPlugin)) {
				pushTo = pushPlugins.getString(pushPlugin);
			}
			Set<String> servers = resolveServers(pushTo, pluginWarnings);

			List<String> pushedJarTo = new ArrayList<>();
			List<String> pushedConfigTo = new ArrayList<>();
			// Push to the specified servers
			for (String server : servers) {
				// Skip filtered servers
				if (include != null && !include.contains(server)) {
					continue;
				}

				String serverVersion = applyVariables("<<<version>>>", server, pluginWarnings);

				// Search jarfile to push
				File newPluginJar = null;
				File newPluginConfig = null;
				String newJarVersion = null;
				for (File file : files) {
					if (file.isFile() && matchesFileName(pushPlugin, file) && file.getName().toLowerCase().endsWith(".jar")) {
						// Check if version matches
						String jarVersion = null;
						if (file.getName().contains(".")) {
							String firstPart = file.getName().substring(0, file.getName().lastIndexOf("."));
							if (firstPart.contains("@")) {
								jarVersion = firstPart.substring(firstPart.lastIndexOf("@") + 1, firstPart.length());
							}
						}
						if (versionMatches(serverVersion, jarVersion)) {
							// We don't have a jar yet or this jar is version specific while the other one is not
							if (newPluginJar == null || (newJarVersion==null && jarVersion != null)) {
								newPluginJar = file;
								newJarVersion = jarVersion;
							} else {
								pluginWarnings.add("Found second .jar file match: " + file.getAbsolutePath() + ", first=" + newPluginJar.getAbsolutePath());
							}
						}
					} else if (file.isDirectory() && matchesFileName(pushPlugin, file)) {
						if (newPluginConfig == null) {
							newPluginConfig = file;
						} else {
							pluginWarnings.add("Found second config folder match: " + file.getAbsolutePath() + ", first=" + newPluginConfig.getAbsolutePath());
						}
					}
				}

				// Push plugin jar
				if (operations.contains("pluginJar")) {
					if(newPluginJar == null) {
						if(pushPluginSection == null || !pushPluginSection.getBoolean("noJar")) {
							pluginWarnings.add("No jar file found for plugin: " + pushPlugin);
						}
					} else {
						operationsDone.add("pluginJar");
						// Find existing jar file
						File oldPluginJar = null;
						File[] existingFiles = serverPluginFolders.get(server).listFiles();
						if (existingFiles != null) {
							for (File file : existingFiles) {
								if (file.isFile() && matchesFileName(pushPlugin, file)) {
									if (oldPluginJar == null) {
										oldPluginJar = file;
									} else {
										pluginWarnings.add("Found second old .jar file: " + file.getAbsolutePath());
									}
								}
							}
						}

						// Determine to push or not
						if (oldPluginJar == null || FileUtils.isFileNewer(newPluginJar, oldPluginJar) || force) {
							// Delete old one
							if (oldPluginJar != null && !oldPluginJar.delete()) {
								pluginWarnings.add("Deleting failed: " + oldPluginJar.getAbsolutePath());
							}
							File newFileName = new File(serverPluginFolders.get(server).getAbsolutePath() + File.separator + pushPlugin + " DISTRIBUTED.jar");
							pluginWarnings.addAll(copyFile(newPluginJar, newFileName, server));
							jarsUpdated++;
							pushedJarTo.add(plugin.getServerName(server));
						}
					}
				}

				// Push config
				if (operations.contains("pluginConfig") && newPluginConfig != null) {
					operationsDone.add("pluginConfig");
					File targetFolder = new File(serverPluginFolders.get(server).getAbsolutePath() + File.separator + pushPlugin);
					List<String> result = pushStructure(newPluginConfig, targetFolder, pluginWarnings, newPluginConfig, server, force);
					if (result.size() > 0) {
						configsUpdated += result.size();
						pushedConfigTo.add(plugin.getServerName(server));
					}
				}
			}
			if (pushedJarTo.size() > 0 || pushedConfigTo.size() > 0 || pluginWarnings.size() > 0) {
				updateMessage(updateLogger, executor, "update-pluginHeader", false, pushPlugin);
				if (pushedJarTo.size() > 0) {
					updateMessage(updateLogger, executor, "update-pushedPluginTo", false, StringUtils.join(pushedJarTo, ", "));
				}
				if (pushedConfigTo.size() > 0) {
					updateMessage(updateLogger, executor, "update-pushedConfigTo", false, StringUtils.join(pushedConfigTo, ", "));
				}
				for (String warning : pluginWarnings) {
					updateMessage(updateLogger, executor, "update-warning", true, warning);
				}
				pluginsUpdated++;
			}
		}
		// Update permissions
		if (operations.contains("permissions")) {
			operationsDone.add("permissions");
			List<String> serversUpdated = updatePermissionsNow(include, generalWarnings);
			permissionsUpdated = serversUpdated.size();
			if (permissionsUpdated > 0 && executor instanceof Player) {
				new BukkitRunnable() {
					@Override
					public void run() {
						((Player) executor).performCommand("pex reload");
					}
				}.runTaskLater(plugin, 5L);
			}
		}

		// Update root files
		if (operations.contains("rootfiles")) {
			operationsDone.add("rootfiles");
			rootFilesUpdated = updateRootFiles(updateLogger, executor, include, generalWarnings, force);
		}

		// Check for leftover operations
		operations.removeAll(operationsDone);
		if (operations.size() > 0) {
			generalWarnings.add("Leftover operations: " + StringUtils.join(", ", operations));
		}

		// Display result (executor + log)
		if (generalWarnings.size() > 0) {
			updateMessage(updateLogger, executor, "update-generalWarnings", false);
			for (String warning : generalWarnings) {
				updateMessage(updateLogger, executor, "update-warning", true, warning);
			}
		}
		if (pluginsUpdated > 0 || jarsUpdated > 0 || configsUpdated > 0 || permissionsUpdated > 0 || rootFilesUpdated > 0) {
			updateMessage(updateLogger, executor, "update-done", false, pluginsUpdated, jarsUpdated, configsUpdated, permissionsUpdated, rootFilesUpdated);
		} else {
			updateMessage(updateLogger, executor, "update-none", false);
		}
		try {
			if (updateLogger != null) {
				updateLogger.close();
			}
		} catch (IOException e) {
			Log.warn("Could not correctly close the updateLogger:", ExceptionUtils.getStackTrace(e));
		}
	}

	/**
	 * Check if a version matches the server version
	 * @param serverVersion The version of the server
	 * @param toMatch The version to match
	 * @return true if the to match version is compatible, otherwise false
	 */
	private boolean versionMatches(String serverVersion, String toMatch) {
		return serverVersion != null && !serverVersion.isEmpty() && (toMatch == null || toMatch.isEmpty() || serverVersion.equalsIgnoreCase(toMatch));
	}

	/**
	 * Log a message to a target and to the update log
	 * @param updateLogger The logger
	 * @param target The target
	 * @param key The message key
	 * @param warn Send as warning
	 * @param args The fill in arguments
	 */
	public void updateMessage(BufferedWriter updateLogger, Object target, String key, boolean warn, Object... args) {
		if(warn && target instanceof ConsoleCommandSender) {
			Log.warn(ChatColor.stripColor(Message.fromKey(key).replacements(args).getPlain()));
		} else {
			plugin.messageNoPrefix(target, key, args);
		}
		StringBuilder prefix = new StringBuilder("[" + Utils.getCurrentDateTime() + "] [" + plugin.getServerName() + "] ");
		for (int i = plugin.getServerName().length(); i < 20; i++) {
			prefix.append(" ");
		}
		Message.fromKey(key).prepend(prefix.toString()).replacements(args).send(updateLogger);
	}

	/**
	 * Perform an update on the plugin data async
	 * @param executor The CommandSender that executed the update
	 * @param filter The filter specifying which servers should be pushed to
	 */
	public void update(final CommandSender executor, final String filter, final String operationFilter, final boolean force) {
		new BukkitRunnable() {
			@Override
			public void run() {
				updateNow(executor, filter, operationFilter, force);
			}
		}.runTaskAsynchronously(plugin);
	}

	/**
	 * Update root files
	 * @param include Servers to update
	 * @param generalWarnings List with warnings
	 * @param force Push regardless of age
	 * @return Count of updated files
	 */
	public int updateRootFiles(BufferedWriter updateLogger, CommandSender executor, Set<String> include, List<String> generalWarnings, boolean force) {
		int result = 0;
		ConfigurationSection rootFiles = plugin.getGeneralConfig().getConfigurationSection("rootfiles");
		if (rootFiles == null) {
			generalWarnings.add("No rootfiles section");
			return result;
		}

		// All files in the root data folder
		File[] files = rootDataFolder.listFiles();
		if (files == null) {
			generalWarnings.add("No files found in the root data folder: " + rootDataFolder.getAbsolutePath());
			return result;
		}

		// Execute all root file pushing
		List<String> pushedFileTo = new ArrayList<>();
		for (String pushFile : rootFiles.getKeys(false)) {
			List<String> fileWarnings = new ArrayList<>();
			ConfigurationSection pushToSection = rootFiles.getConfigurationSection(pushFile);
			String pushTo = null;
			if (pushToSection != null) {
				pushTo = pushToSection.getString("servers");
			} else if (rootFiles.isString(pushFile)) {
				pushTo = rootFiles.getString(pushFile);
			}
			Set<String> servers = resolveServers(pushTo, fileWarnings);

			// Determine source file name
			String sourceName = pushFile;
			if (pushToSection != null && pushToSection.contains("source")) {
				sourceName = pushToSection.getString("source");
			}
			// Determine target file name
			String targetName = sourceName;
			if (pushToSection != null && pushToSection.contains("target")) {
				targetName = pushToSection.getString("target");
			}

			// Search jarfile to push
			File newFile = null;
			for (File file : files) {
				if (file.isFile() && matchesFileName(sourceName, file)) {
					if (newFile == null) {
						newFile = file;
					} else {
						fileWarnings.add("Found second root file match: " + file.getAbsolutePath() + ", first: " + newFile.getAbsolutePath());
					}
				}
			}

			// Push to the specified servers
			for (String server : servers) {
				// Skip filtered servers
				if (include != null && !include.contains(server)) {
					continue;
				}

				if (newFile != null) {
					// Find existing file
					File oldFile = null;
					File[] existingFiles = serverPluginFolders.get(server).getParentFile().listFiles();
					if (existingFiles != null) {
						for (File file : existingFiles) {
							if (file.isFile() && matchesFileName(targetName, file)) {
								if (oldFile == null) {
									oldFile = file;
								} else {
									fileWarnings.add("Found second old root file: " + file.getAbsolutePath());
								}
							}
						}
					}

					// Determine to push or not
					if (oldFile == null || FileUtils.isFileNewer(newFile, oldFile) || force) {
						File newFileName = new File(serverPluginFolders.get(server).getParentFile().getAbsolutePath() + File.separator + targetName);
						fileWarnings.addAll(copyFile(newFile, newFileName, server));
						pushedFileTo.add(plugin.getServerName(server));
						result++;
					}
				}
			}
			if (pushedFileTo.size() > 0 || fileWarnings.size() > 0) {
				updateMessage(updateLogger, executor, "update-pluginHeader", false, pushFile);
				if (pushedFileTo.size() > 0) {
					updateMessage(updateLogger, executor, "update-pushedTo", false, StringUtils.join(pushedFileTo, ", "));
				}
				for (String warning : fileWarnings) {
					updateMessage(updateLogger, executor, "update-warning", true, warning);
				}
			}
		}
		return result;
	}

	/**
	 * Push a directory structure to another place, only replaces files that are newer in the source
	 * @param source The source directory
	 * @param target The target directory
	 * @param warnings The warnings list
	 * @param rootSource The root source to format messages with
	 * @param server The id of the server it is pushed to
	 * @param force Always push regardless of file age
	 * @return List with the file names that are pushed
	 */
	public List<String> pushStructure(File source, File target, List<String> warnings, File rootSource, String server, boolean force) {
		List<String> result = new ArrayList<>();
		File[] files = source.listFiles();
		if (files == null) {
			warnings.add("Incorrect directory, no files: " + source.getAbsolutePath());
			return result;
		}
		for (File file : files) {
			File fileTarget = new File(target.getAbsolutePath() + File.separator + file.getName());
			if (file.isDirectory()) {
				pushStructure(file, fileTarget, warnings, rootSource, server, force);
			} else if (file.isFile()) {
				if (fileTarget.exists() && !fileTarget.isFile()) {
					warnings.add("Target exists but is not a file: " + fileTarget.getAbsolutePath());
					continue;
				}
				fileTarget.getParentFile().mkdirs();
				if (!fileTarget.exists() || FileUtils.isFileNewer(file, fileTarget) || force) {
					warnings.addAll(copyFile(file, fileTarget, server));
					result.add(file.getAbsolutePath().replace(rootSource.getAbsolutePath(), "")); // Directory structure + filename starting from plugin folder to the file
				}
			} else {
				warnings.add("Incorrect file: " + file.getAbsolutePath());
			}
		}
		return result;
	}

	/**
	 * Copy a file from source to target, replace variable if in text format, set permissions
	 * @param source The source file
	 * @param target The target file
	 * @param server The server to apply variables for, or null for none
	 * @return A list with warnings, all is well when empty
	 */
	public List<String> copyFile(File source, File target, String server) {
		List<String> warnings = new ArrayList<>();
		String extension = source.getName().toLowerCase();
		if (extension.contains(".")) {
			extension = extension.substring(extension.lastIndexOf(".") + 1);
		}
		if (binaryFiles.contains(extension)) {
			try {
				FileUtils.copyFile(source, target);
			} catch (IOException e) {
				warnings.add("Binary copy failed: " + target.getAbsolutePath() + ", exception: " + e.getMessage());
				e.printStackTrace();
			}
		} else {
			try (
					BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(source), "UTF8"));
					BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(target), "UTF8"))) {
				// Only files for which we know a way to comment out lines we can add a header
				if (source.getName().endsWith(".yml") || source.getName().endsWith(".properties")) {
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

			} catch (IOException e) {
				warnings.add("Line-by-line copy failed: " + target.getAbsolutePath() + ", exception: " + e.getMessage());
				e.printStackTrace();
			}
		}
		if (!target.equals(self)) { // Setting own permissions obviously does not work
			boolean permissionsResult = target.setExecutable(true, false);
			permissionsResult = permissionsResult && target.setReadable(true, false);
			permissionsResult = permissionsResult && target.setWritable(true, false);
			if (!permissionsResult) {
				warnings.add("Setting permissions failed: " + target.getAbsolutePath());
			}
		}
		return warnings;
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
		if (pluginSection == null) {
			generalWarnings.add("No plugin section");
			return result;
		}
		for (String pluginKey : pluginSection.getKeys(false)) {
			ConfigurationSection permissionsSection = pluginSection.getConfigurationSection(pluginKey + ".permissions");
			if (permissionsSection == null) {
				continue;
			}
			for (String sectionKey : permissionsSection.getKeys(false)) {
				ConfigurationSection currentSection = permissionsSection.getConfigurationSection(sectionKey);
				if (currentSection == null) {
					generalWarnings.add("Incorrect permissions section: " + sectionKey + " in plugin " + pluginKey);
					continue;
				}
				String servers = currentSection.getString("servers");
				if (servers == null) { // default to all servers the plugin is on
					servers = pluginKey;
				}
				Set<String> toServers = resolveServers(servers, generalWarnings);
				String rawGroups = currentSection.getString("groups");
				if (rawGroups == null || rawGroups.isEmpty()) { // Could only be for a help entry
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
						List<String> get = Utils.listOrSingle(currentSection, "permissions");
						if(get != null) {
							// Prevent duplicates
							for(String addPerm : get) {
								if(!groupPermissions.contains(addPerm)) {
									groupPermissions.add(addPerm);
								}
							}
						}
					}
				}
			}
		}

		// Write permissions to the files
		for (String server : includeServers) {
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
			try (
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
				while (line != null) {
					if (line.startsWith("groups:")) {
						inGroupsSection = true;
					} else if (line.matches("[^ ].*")) {
						inGroupsSection = false;
						currentGroupPermissions = null;
						inOldDistSection = false;
					}
					if (inGroupsSection) {
						if (line.matches("  [^ ].*")) {
							String groupName = line.substring(2, line.length() - 1);
							currentGroupPermissions = serverPermissions.get(groupName);
							writer.write(line + "\n");
						} else {
							if (line.startsWith("    permissions:")) {
								// insert permissions
								writer.write(line + "\n");
								if (currentGroupPermissions != null) {
									writer.write("    ##### START DISTRIBUTED PERMISSIONS\n");
									for (String permission : currentGroupPermissions) {
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
				generalWarnings.add("Exception while writing permissions of " + server + ": " + e.getMessage());
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
	 * @param line The line to check for variables
	 * @param server The server to replace it for
	 * @param warnings The list of warnings
	 * @return The result string where all variables are replaced with values from the config
	 */
	public String applyVariables(String line, String server, List<String> warnings) {
		if (server == null) {
			return line;
		}
		Pattern variables = Pattern.compile("<<<(?<variable>[a-zA-Z0-9-_:.]+)>>>");
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
			String value = null;
			if ("id".equals(variable)) {
				value = actualServer;
			} else if (plugin.getGeneralConfig().isSet("servers." + actualServer + "." + variable)) {
				value = plugin.getGeneralConfig().getString("servers." + actualServer + "." + variable);
			} else if (plugin.getGeneralConfig().isSet("servers.DEFAULT." + variable)) {
				value = plugin.getGeneralConfig().getString("servers.DEFAULT." + variable);
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
	 * @param test The plugin name to test for
	 * @param match The match to test
	 * @return true if the match is of the test plugin, otherwise false
	 */
	public boolean matchesFileName(String test, File match) {
		test = test.toLowerCase();
		String name = match.getName().toLowerCase();
		if (name.endsWith(".jar")) {  // Strip .jar
			name = name.substring(0, match.getName().lastIndexOf(".")).toLowerCase();
		}
		if (test.endsWith(".jar")) {  // Strip .jar
			test = test.substring(0, test.lastIndexOf(".")).toLowerCase();
		}
		return name.startsWith(test + " ") || name.equalsIgnoreCase(test);
	}

	/**
	 * Resolve a server specification to a list of servers
	 * @param serverSpecifier The server specification (comma-separated list)
	 * @param warnings The warning list
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
		if (serverSpecifier == null) {
			ConfigurationSection serverSection = plugin.getGeneralConfig().getConfigurationSection("servers");
			if (serverSection != null) {
				result.addAll(serverSection.getKeys(false));
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


}
