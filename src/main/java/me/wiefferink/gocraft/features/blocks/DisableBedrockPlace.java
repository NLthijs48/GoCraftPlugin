package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class DisableBedrockPlace implements Listener {
	
	public final String configLine = "disableBedrockPlace";
	private GoCraft plugin;
	
	public DisableBedrockPlace(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent placing bedrock
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event) {
		if(plugin.onThisWorld(configLine, event.getBlock())
				&& event.getBlock().getType() == Material.BEDROCK && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}		
	}
}
