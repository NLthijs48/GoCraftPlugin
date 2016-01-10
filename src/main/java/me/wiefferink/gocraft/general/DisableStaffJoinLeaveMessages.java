package me.wiefferink.gocraft.general;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisableStaffJoinLeaveMessages implements Listener {

    public final String configLine = "disableStaffJoinLeaveMessages";
    private GoCraft plugin;

    public DisableStaffJoinLeaveMessages(GoCraft plugin) {
        if (plugin.getConfig().getBoolean(configLine)) {
            this.plugin = plugin;
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (plugin.onThisWorld(configLine, event.getPlayer())
                && event.getPlayer().hasPermission("gocraft.staff")) {
            event.setJoinMessage(null);
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        if (plugin.onThisWorld(configLine, event.getPlayer())
                && event.getPlayer().hasPermission("gocraft.staff")) {
            event.setQuitMessage(null);
        }
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (plugin.onThisWorld(configLine, event.getPlayer())
                && event.getPlayer().hasPermission("gocraft.staff")) {
            event.setLeaveMessage(null);
        }
    }
}
