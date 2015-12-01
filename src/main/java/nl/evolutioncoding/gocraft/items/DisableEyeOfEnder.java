package nl.evolutioncoding.gocraft.items;

import nl.evolutioncoding.gocraft.GoCraft;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisableEyeOfEnder implements Listener {
	
	public final String configLine = "disableEyeOfEnder";
	private GoCraft plugin;
	
	public DisableEyeOfEnder(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent using xp bottles
	@EventHandler
	public void onItemUse(PlayerInteractEvent event) {
		if(plugin.onThisWorld(configLine, event.getPlayer())
				&& (event.getAction() == Action.RIGHT_CLICK_AIR 
				|| event.getAction() == Action.RIGHT_CLICK_BLOCK) 
				&& event.getPlayer().getItemInHand().getType() == Material.EYE_OF_ENDER && !event.getPlayer().isOp()) {
			event.setCancelled(true);
		}
	}
}
