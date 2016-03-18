package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;

public class DisableItemSpawning implements Listener {
	
	public final String configLine = "disableItemSpawning";
	private GoCraft plugin;
	
	public DisableItemSpawning(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent items dropping from breaking a container
	@EventHandler
	public void onItemSpawn(ItemSpawnEvent event) {
		if(plugin.onThisWorld(configLine, event.getLocation())) {
			event.setCancelled(true);			
		}
	}
}
