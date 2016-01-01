package me.wiefferink.gocraft.items;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class DisableItemDrops implements Listener {
	
	public final String configLine = "disableItemDrops";
	private GoCraft plugin;
	
	public DisableItemDrops(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent dropping items from inventory
	@EventHandler
	public void onItemDrop(PlayerDropItemEvent event) {
		if(plugin.onThisWorld(configLine, event.getPlayer())
				&& !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
}
