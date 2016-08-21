package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisableStaffJoinLeaveMessages extends Feature {

	public DisableStaffJoinLeaveMessages() {
		listen("disableStaffJoinLeaveMessages");
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(inWorld(event) && event.getPlayer().hasPermission("gocraft.staff")) {
			event.setJoinMessage(null);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerLeave(PlayerQuitEvent event) {
		if(inWorld(event) && event.getPlayer().hasPermission("gocraft.staff")) {
			event.setQuitMessage(null);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerKick(PlayerKickEvent event) {
		if(inWorld(event) && event.getPlayer().hasPermission("gocraft.staff")) {
			event.setLeaveMessage(null);
		}
	}
}
