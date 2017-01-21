package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.messages.Message;
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

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerJoin(PlayerJoinEvent event) {
		event.setJoinMessage(null);
		if(inWorld(event) && event.getPlayer().hasPermission("gocraft.staff")) {
			return;
		}
		broadcast(event.getPlayer(), Message.fromKey("general-joinedServer").replacements(event.getPlayer().getName()));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerLeave(PlayerQuitEvent event) {
		event.setQuitMessage(null);
		if(inWorld(event) && event.getPlayer().hasPermission("gocraft.staff")) {
			return;
		}
		broadcast(event.getPlayer(), Message.fromKey("general-leftServer").replacements(event.getPlayer().getName()));
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onPlayerKick(PlayerKickEvent event) {
		if(inWorld(event) && event.getPlayer().hasPermission("gocraft.staff")) {
			event.setLeaveMessage(null);
		}
		// Don't send custom leave message, onPlayerLeave is also triggered
	}

	/**
	 * Broadcast a message to all players
	 * @param self The player itself
	 * @param message The message to broadcast
	 */
	private void broadcast(Player self, Message message) {
		for(Player player : Bukkit.getOnlinePlayers()) {
			if(!self.equals(player)) {
				message.send(player);
			}
		}
	}
}
