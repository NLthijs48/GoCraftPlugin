package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraftBungee;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.config.Configuration;
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
        String to = event.getTarget().getName();
        Configuration servers = plugin.getGeneralConfig().getSection("servers");
        if (servers != null) {
            for (String key : servers.getKeys()) {
                if (servers.getString(key + ".bungeeId").equals(to)) {
                    String name = servers.getString(key + ".name");
                    if (name != null) to = name;
                    break;
                }
            }
        }
        if (event.getPlayer().getServer() == null) {
            plugin.getSyncCommandsBungee().runCommand(event.getTarget().getName(),
                    "broadcast general-joinedServer " + event.getPlayer().getDisplayName());
        } else if (!event.getPlayer().getServer().getInfo().getName().equals(event.getTarget().getName())) {
            plugin.getSyncCommandsBungee().runCommand(event.getPlayer().getServer().getInfo().getName(),
                    "broadcast general-switchedServer " + event.getPlayer().getDisplayName() + " " + GoCraftBungee.getInstance().getServerName(to));
        }
    }

}
