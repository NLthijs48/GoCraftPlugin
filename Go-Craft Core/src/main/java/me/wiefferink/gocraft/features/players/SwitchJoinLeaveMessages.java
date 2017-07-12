package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraftBungee;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class SwitchJoinLeaveMessages implements Listener {
    private GoCraftBungee plugin;

    public SwitchJoinLeaveMessages(GoCraftBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        if(event.getPlayer().hasPermission("gocraft.staff")) {
            return;
        }

        plugin.getSyncCommandsBungee().runCommand(event.getPlayer().getServer().getInfo().getName(), "broadcast general-leftServer " + event.getPlayer().getDisplayName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void ServerConnectEvent(ServerConnectEvent event) {
        if(event.getPlayer().hasPermission("gocraft.staff")) {
            return;
        }

        String from = event.getPlayer().getServer().getInfo().getName();
        String fromName = GoCraftBungee.getInstance().getServerName(from);
        String to = event.getTarget().getName();
        String toName = GoCraftBungee.getInstance().getServerName(to);
        String player = event.getPlayer().getDisplayName();

        // Leave
        if (event.getPlayer().getServer() == null) {
            plugin.getSyncCommandsBungee().runCommand(to,"broadcast general-joinedServer " + player);
        }

        // Switch
        else if (!from.equals(to)) {
            // Old server
            plugin.getSyncCommandsBungee().runCommand(from,"broadcast general-switchedServer " + player + " " + toName);
            // New server
            plugin.getSyncCommandsBungee().runCommand(to,"broadcast general-joinedServerFrom " + player + " " + fromName);
        }
    }

}
