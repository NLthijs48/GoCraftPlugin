package nl.evolutioncoding.gocraft.items;

import nl.evolutioncoding.gocraft.GoCraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisablePotionThrow implements Listener {
	
	public final String configLine = "disablePotionThrow";
	private GoCraft plugin;
	
	public DisablePotionThrow(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent Potion throw
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		int potionData = event.getPlayer().getItemInHand().getDurability() % 32768;
		if(plugin.onThisWorld(configLine, event.getPlayer())
				&& (event.getAction() == Action.RIGHT_CLICK_AIR 
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK) 
				&& potionData/16384 > 0) {
			event.setCancelled(true);
		}
	}
}
