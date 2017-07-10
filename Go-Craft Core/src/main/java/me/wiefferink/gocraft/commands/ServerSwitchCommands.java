package me.wiefferink.gocraft.commands;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

import java.util.List;

public class ServerSwitchCommands extends Feature {

	public ServerSwitchCommands() {
		ConfigurationSection servers = plugin.getGeneralConfig().getConfigurationSection("servers");
		if(servers == null) {
			return;
		}

		permission("switch", "Switch to servers using a command", PermissionDefault.TRUE);

		// Create a command for each server, with aliases for their different identifiers
		for(String serverKey : servers.getKeys(false)) {
			ConfigurationSection serverSection = servers.getConfigurationSection(serverKey);
			if(serverSection == null) {
				continue;
			}

			String bungeeId = serverSection.getString("bungeeId");
			String name = serverSection.getString("name");
			String directory = serverSection.getString("directory");
			List<String> aliases = Utils.listOrSingle(serverSection, "alias");
			aliases.add(name);
			aliases.add(directory);

			if("DEFAULT".equalsIgnoreCase(serverKey)) {
				continue;
			}

			if(bungeeId == null || name == null || directory == null) {
				Log.warn("Cannot register server command for", serverKey, "because either bungeeId, name or directory is null:", bungeeId, name, directory);
				continue;
			}

			// BungeeId as main command, so that command.getName() is the bungeeId
			command(bungeeId, "Switch to server "+name, "/"+name.toLowerCase(), aliases.toArray(new String[aliases.size()]));
		}
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		Log.debug("ServerSwitch, sender:", sender.getName(), "command:", command.getName(), "label:", label);
		if(!sender.hasPermission("gocraft.switch")) {
			plugin.message(sender, "switch-noPermission");
			return;
		}

		if(!(sender instanceof Player)) {
			plugin.message(sender, "general-playerOnly");
			return;
		}

		// Check if logged in (should not be necessary since AuthMe removes all permissions when not logged in, and has a command whitelist)
		Player player = (Player) sender;
		if(plugin.getAuthMeLink() != null && !plugin.getAuthMeLink().get().isAuthenticated(player)) {
			plugin.message(sender, "switch-notLoggedIn");
			return;
		}

		// Switching to current server
		if(command.getName().equalsIgnoreCase(plugin.getBungeeId())) {
			plugin.message(sender, "switch-sameServer", plugin.getServerName());
			return;
		}

		plugin.getSyncCommandsServer().runCommand("switch", player.getUniqueId().toString(), command.getName());
		plugin.message(sender, "switch-go", plugin.getServerName(command.getName()));
	}

}


















