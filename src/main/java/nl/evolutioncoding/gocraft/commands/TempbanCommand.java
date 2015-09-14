package nl.evolutioncoding.gocraft.commands;

import java.util.ArrayList;
import java.util.Arrays;

import nl.evolutioncoding.gocraft.GoCraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class TempbanCommand implements Listener {
	
	public final String configLine = "redirectTempban";
	private GoCraft plugin;
	private ArrayList<String> tempbanCommands = new ArrayList<String>(Arrays.asList(
			"tempban", "banmanager:tempban", "bmtempban", "banmanager:bmtempban", 
			"tempbanip", "banmanager:tempbanip", "bmtempbanip", "banmanager:bmtempbanip"));	
	
	public TempbanCommand(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onCommand(PlayerCommandPreprocessEvent event) {
		//plugin.debug("CommandPreprocess: " + event.getMessage());
		//plugin.debug("  player: " + event.getPlayer().getName());		
		String fullMessage = event.getMessage();
		boolean hasArguments = fullMessage.indexOf(" ") != -1;
		String command, arguments = "";
		if(hasArguments) {
			command = fullMessage.substring(1, fullMessage.indexOf(" "));
			arguments = fullMessage.substring(fullMessage.indexOf(" ") +1);
		} else {
			command = fullMessage.substring(1);
		}
		command = command.toLowerCase();
		if(tempbanCommands.contains(command)) {
			String reason = null;
			String[] argumentArray = arguments.split(" ");
			if(argumentArray.length < 3) {
				plugin.message(event.getPlayer(), "tempban-help");
				event.setCancelled(true);
				return;
			} else {
				for(int i=2; i<argumentArray.length; i++) {
					if(reason == null) {
						reason = argumentArray[i];
					} else {
						reason += " " + argumentArray[i];
					}
				}
			}
			boolean success = true;
			success = success & event.getPlayer().performCommand("banmanager:tempban " + arguments);
			success = success & event.getPlayer().performCommand("banmanager:tempbanip " + arguments);
			if(success) {
				plugin.message(event.getPlayer(), "tempban-redirected", argumentArray[0], argumentArray[1], reason);
			} else {
				plugin.message(event.getPlayer(), "tempban-failed");
			}
			event.setCancelled(true);
		}
	}

}
