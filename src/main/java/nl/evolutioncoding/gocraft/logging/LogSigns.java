package nl.evolutioncoding.gocraft.logging;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import nl.evolutioncoding.gocraft.GoCraft;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class LogSigns implements Listener {
	
	public final String configLine = "signLoggingEnabled";
	private GoCraft plugin;
	
	public LogSigns(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
			this.plugin = plugin;
		}
	}

	/**
	 * Called when a sign is changed
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.MONITOR)
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		
		if(plugin.onThisWorld(configLine, event.getBlock()) && (player.hasPermission("gocraft.signLog"))) {
			SimpleDateFormat time = new SimpleDateFormat(plugin.getConfig().getString("signLogTimeFormat"));
			plugin.logLine(GoCraft.signLog, plugin.getLanguageManager().getLang(
					"signLogfileLine", 
					time.format(Calendar.getInstance().getTime()), 
					event.getBlock().getLocation().getWorld().getName(),
					event.getBlock().getLocation().getBlockX(),
					event.getBlock().getLocation().getBlockY(),
					event.getBlock().getLocation().getBlockZ(),
					player.getName(), 
					event.getLine(0), 
					event.getLine(1), 
					event.getLine(2), 
					event.getLine(3)));
			for(Player p : Bukkit.getOnlinePlayers()) {
				if(p.hasPermission("gocraft.signTell")) {
					plugin.message(p, "signNotifyLine", 
							player.getName(), 
							ChatColor.stripColor(event.getLine(0)), 
							ChatColor.stripColor(event.getLine(1)), 
							ChatColor.stripColor(event.getLine(2)), 
							ChatColor.stripColor(event.getLine(3)),
							event.getBlock().getLocation().getWorld().getName(),
							event.getBlock().getLocation().getBlockX(),
							event.getBlock().getLocation().getBlockY(),
							event.getBlock().getLocation().getBlockZ());
				}
			}
		}
	}
}


















