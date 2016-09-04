package me.wiefferink.gocraft;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class GoCraftBungee extends net.md_5.bungee.api.plugin.Plugin implements Listener {

	@Override
	public void onEnable() {
		this.getProxy().getPluginManager().registerListener(this, this);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPreLogin(PreLoginEvent event) {
		getLogger().info("PreLoginEvent of "+event.getConnection().getName()+", ip: "+event.getConnection().getAddress().getHostString());
		for(ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
			if(player.getName().equalsIgnoreCase(event.getConnection().getName())) {
				event.setCancelled(true);
				event.setCancelReason(ChatColor.DARK_RED+"You cannot login with the same name as an online player!");
				getLogger().warning("Blocked an attempt to login with the same name as an online player, name: "+event.getConnection().getName()+", ip: "+event.getConnection().getAddress().getHostString());
			}
		}
	}
}
