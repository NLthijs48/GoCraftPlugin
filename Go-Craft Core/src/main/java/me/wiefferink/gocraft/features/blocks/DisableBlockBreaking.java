package me.wiefferink.gocraft.features.blocks;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

public class DisableBlockBreaking implements Listener {

	public final String configLine = "disableBlockBreaking";
	private GoCraft plugin;

	public DisableBlockBreaking(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onBlockBreak(BlockBreakEvent event) {
		if (!event.getPlayer().isOp() && plugin.onThisWorld(configLine, event.getBlock())) {
			event.setCancelled(true);
		}
	}
}
