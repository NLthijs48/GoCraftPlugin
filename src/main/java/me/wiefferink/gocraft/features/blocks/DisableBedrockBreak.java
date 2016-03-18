package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class DisableBedrockBreak implements Listener {
	
	public final String configLine = "disableBedrockBreak";
	private GoCraft plugin;
	
	public DisableBedrockBreak(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent breaking bedrock
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event) {
		if(plugin.onThisWorld(this.configLine, event.getBlock())
				&& event.getBlock().getType() == Material.BEDROCK && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}		
	}
}
