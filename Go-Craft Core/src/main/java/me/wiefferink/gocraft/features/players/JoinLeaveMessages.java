package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
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
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerLeave(PlayerQuitEvent event) {
		event.setQuitMessage(null);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
	public void onPlayerKick(PlayerKickEvent event) {
		event.setLeaveMessage(null);
	}
}
