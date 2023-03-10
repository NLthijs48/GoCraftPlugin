package me.wiefferink.gocraft.inspector;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitJoinListener implements Listener {
	GoCraft plugin;

	public QuitJoinListener(GoCraft plugin) {
		this.plugin = plugin;
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerLeave(PlayerQuitEvent event) {
		plugin.getInspectionManager().handlePlayerStopped(event.getPlayer());
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		plugin.getInspectionManager().handlePlayerJoined(event.getPlayer(), false);
	}

}
