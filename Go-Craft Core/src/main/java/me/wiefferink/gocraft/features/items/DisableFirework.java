package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisableFirework implements Listener {

	public final String configLine = "disableFirework";
	private GoCraft plugin;

	public DisableFirework(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent firework use
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if (plugin.onThisWorld(configLine, event.getPlayer())
				&& (event.getAction() == Action.RIGHT_CLICK_AIR
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK)
				&& event.getPlayer().getItemInHand().getType() == Material.FIREWORK && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
}
