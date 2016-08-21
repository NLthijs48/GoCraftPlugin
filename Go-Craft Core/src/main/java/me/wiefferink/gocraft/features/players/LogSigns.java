package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.messages.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class LogSigns extends Feature {

	public final String configLine = "signLoggingEnabled";

	public LogSigns() {
		listen("signLoggingEnabled");
	}

	/**
	 * Called when a sign is changed
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();

		if(inWorld(event) && (player.hasPermission("gocraft.signLog"))) {
			SimpleDateFormat time = new SimpleDateFormat(config.getString("signLogTimeFormat"));
			plugin.logLine(GoCraft.signLog,
					Message.fromKey("signLogfileLine")
							.replacements(
									time.format(Calendar.getInstance().getTime()),
									event.getBlock().getLocation().getWorld().getName(),
									event.getBlock().getLocation().getBlockX(),
									event.getBlock().getLocation().getBlockY(),
									event.getBlock().getLocation().getBlockZ(),
									player.getName(),
									event.getLine(0),
									event.getLine(1),
									event.getLine(2),
									event.getLine(3)
							).getPlain());
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("gocraft.signTell")) {
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


















