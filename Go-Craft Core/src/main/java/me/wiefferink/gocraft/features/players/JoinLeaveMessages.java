package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Replace join and leave messages with interactive versions
 */
public class JoinLeaveMessages extends Feature {

	public JoinLeaveMessages() {
		configKey = "disableStaffJoinLeaveMessages";
		listen();
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);
		Bukkit.getLogger().info(">>> "+event.getPlayer().getName()+" joined "+getPlayerDetails(event.getPlayer()));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerLeave(PlayerQuitEvent event) {
		event.setQuitMessage(null);
		Bukkit.getLogger().info("<<< "+event.getPlayer().getName()+" left "+getPlayerDetails(event.getPlayer()));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerKick(PlayerKickEvent event) {
		event.setLeaveMessage(null);
		Bukkit.getLogger().info("<<< " + event.getPlayer().getName() + " got kicked: "+event.getReason()+" "+ getPlayerDetails(event.getPlayer()));
	}

	private String getPlayerDetails(Player player) {
		String result = player.getAddress().getAddress().getHostAddress();
		if(!result.equalsIgnoreCase(player.getAddress().getHostName())) {
			result += ", "+player.getAddress().getHostName();
		}
		return "("+player.getUniqueId()+", "+result+")";
	}
}
