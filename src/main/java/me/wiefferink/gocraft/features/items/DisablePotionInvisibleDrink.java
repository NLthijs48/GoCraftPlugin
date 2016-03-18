package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class DisablePotionInvisibleDrink implements Listener {
	
	public final String configLine = "disablePotionInvisibleDrink";
	private GoCraft plugin;
	
	public DisablePotionInvisibleDrink(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent drinking of invisibility potions
	@EventHandler
	public void onPotionDrink(PlayerItemConsumeEvent event) {
		if(plugin.onThisWorld(configLine, event.getPlayer())
				&& !event.getPlayer().isOp() && event.getItem().getType() == Material.POTION && (
				event.getItem().isSimilar(new ItemStack(Material.POTION, 1, (short)8270))
				|| event.getItem().isSimilar(new ItemStack(Material.POTION, 1, (short)8238))
				)) {
			event.setCancelled(true);
		}
	}
}
