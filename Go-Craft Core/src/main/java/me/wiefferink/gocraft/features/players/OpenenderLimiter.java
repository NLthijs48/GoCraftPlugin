package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.tools.Utils;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.ArrayList;
import java.util.Arrays;

public class OpenenderLimiter implements Listener {
	public final String configLine = "disableOpenEnderSelf";
	private GoCraft plugin;

	public OpenenderLimiter(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}


	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		String fullMessage = event.getMessage();
		boolean hasArguments = fullMessage.contains(" ");
		String command, arguments = "";
		if (hasArguments) {
			command = fullMessage.substring(1, fullMessage.indexOf(" "));
			arguments = fullMessage.substring(fullMessage.indexOf(" ") + 1);
		} else {
			command = fullMessage.substring(1);
		}
		command = command.toLowerCase();
		ArrayList<String> enderchestCommands = new ArrayList<>(Arrays.asList(
				"openinv:openender", "openinv:oe", "oe", "openender"));
		if (enderchestCommands.contains(command) &&
				(!hasArguments || (arguments.toLowerCase().contains(event.getPlayer().getName().toLowerCase())))) {
			if (Utils.isInPvpArea(event.getPlayer())) {
				event.setCancelled(true);
				event.getPlayer().sendMessage(ChatColor.RED + "You can only use /openender on yourself outside of the PVP area");
			}
		}
	}
}
