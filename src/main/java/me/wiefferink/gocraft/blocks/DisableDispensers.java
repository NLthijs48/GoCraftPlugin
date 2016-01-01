package me.wiefferink.gocraft.blocks;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDispenseEvent;

public class DisableDispensers implements Listener {
	
	public final String configLine = "disableDispensers";
	private GoCraft plugin;
	
	public DisableDispensers(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent dispensing items
	@EventHandler
	public void onBlockDispense(BlockDispenseEvent event) {
		if(plugin.onThisWorld(configLine, event.getBlock())) {
			event.setCancelled(true);
		}
	}
}
