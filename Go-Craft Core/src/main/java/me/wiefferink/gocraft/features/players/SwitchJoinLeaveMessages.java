package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraftBungee;
import me.wiefferink.gocraft.Log;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.HashMap;
import java.util.Map;

public class SwitchJoinLeaveMessages implements Listener {
    private GoCraftBungee plugin;
    private Map<String, ServerInfo> lastOnline;

    public SwitchJoinLeaveMessages(GoCraftBungee plugin) {
        this.plugin = plugin;
        this.lastOnline = new HashMap<>();
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        lastOnline.remove(event.getPlayer().getName().toLowerCase());
        if(event.getPlayer().hasPermission("gocraft.staff")) {
            return;
        }

        ServerInfo leftServer;
        if(event.getPlayer().getServer() != null) {
            leftServer = event.getPlayer().getServer().getInfo();
        } else {
            leftServer = lastOnline.get(event.getPlayer().getName().toLowerCase());
        }

        if(leftServer == null) {
            Log.warn("Don't know which server has been left by", event.getPlayer().getDisplayName());
            return;
        }

        plugin.getSyncCommandsBungee().runCommand(leftServer.getName(), "broadcast general-leftServer " + event.getPlayer().getDisplayName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerSwitched(ServerConnectedEvent event) {
        // Save old server, player might not actually connect to the server yet though
        lastOnline.put(event.getPlayer().getName().toLowerCase(), event.getPlayer().getServer() != null ? event.getPlayer().getServer().getInfo() : null);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void playerJoinedServer(ServerSwitchEvent event) {
        if(event.getPlayer().hasPermission("gocraft.staff")) {
            return;
        }

        ServerInfo fromServer = lastOnline.get(event.getPlayer().getName().toLowerCase());
        String to = event.getPlayer().getServer().getInfo().getName();
        String toName = GoCraftBungee.getInstance().getServerName(to);
        String player = event.getPlayer().getDisplayName();
        Log.info("ServerConnectEvent of", player, "from", (fromServer == null ? "nothing" : plugin.getServerName(fromServer.getName())), "to", toName);

        // Network join
        if (fromServer == null) {
            plugin.getSyncCommandsBungee().runCommand(to,"broadcast general-joinedServer " + player);
        }

        // Server switch
        else if (!fromServer.getName().equals(to)) {
            String from = fromServer.getName();
            String fromName = GoCraftBungee.getInstance().getServerName(from);

            // Old server
            plugin.getSyncCommandsBungee().runCommand(from,"broadcast general-switchedServer " + player + " " + toName);
            // New server
            plugin.getSyncCommandsBungee().runCommand(to,"broadcast general-joinedServerFrom " + player + " " + fromName);
        }
    }

}
