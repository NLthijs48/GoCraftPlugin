package me.wiefferink.gocraft.inspector;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitJoinListener implements Listener {
    InspectionManager manager;

    public QuitJoinListener(InspectionManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        manager.handlePlayerStopped(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        manager.handlePlayerStopped(event.getPlayer());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        manager.restoreOldInventory(event.getPlayer());
    }

}
