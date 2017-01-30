package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.permissions.PermissionDefault;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class LogSigns extends Feature {

	public final String configLine = "signLoggingEnabled";

	public LogSigns() {
		permission("signLog", "Track signs placed by these players", PermissionDefault.NOT_OP);
		permission("signTell", "Log placed signs to these players", PermissionDefault.OP);
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
			SimpleDateFormat time = new SimpleDateFormat(getConfig().getString("signLogTimeFormat"));
			List<String> lines = Arrays.asList(event.getLine(0), event.getLine(1), event.getLine(2), event.getLine(3));
			boolean empty = true;
			for(int i=0; i<lines.size(); i++) {
				if(lines.get(i).startsWith("&0")) {
					lines.set(i, lines.get(i).substring(2));
				}
				empty &= lines.get(i).isEmpty();
			}
			if(empty) { // No need to log empty signs
				return;
			}
			plugin.logLine(GoCraft.signLog,
					Message.fromKey("signLogfileLine")
							.replacements(
									time.format(Calendar.getInstance().getTime()),
									event.getBlock().getLocation().getWorld().getName(),
									event.getBlock().getLocation().getBlockX(),
									event.getBlock().getLocation().getBlockY(),
									event.getBlock().getLocation().getBlockZ(),
									player.getName(),
									lines.get(0),
									lines.get(1),
									lines.get(2),
									lines.get(3)
							).getPlain());
			// Strip color
			for(int i=0; i<lines.size(); i++) {
				lines.set(i, ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', lines.get(i))));
			}
			for (Player p : Bukkit.getOnlinePlayers()) {
				if (p.hasPermission("gocraft.signTell")) {
					plugin.message(p, "signNotifyLine",
							player.getName(),
							lines.get(0),
							lines.get(1),
							lines.get(2),
							lines.get(3),
							event.getBlock().getLocation().getWorld().getName(),
							event.getBlock().getLocation().getBlockX(),
							event.getBlock().getLocation().getBlockY(),
							event.getBlock().getLocation().getBlockZ());
				}
			}
		}
	}
}


















