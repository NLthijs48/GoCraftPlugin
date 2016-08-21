package me.wiefferink.gocraft.features.items;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class DisablePotionInvisibleDrink extends Feature {

	public DisablePotionInvisibleDrink() {
		listen("disablePotionInvisibleDrink");
	}

	// Prevent drinking of invisibility potions
	// Broken at 1.9+?
	@EventHandler(ignoreCancelled = true)
	public void onPotionDrink(PlayerItemConsumeEvent event) {
		if(inWorld(event)
				&& !event.getPlayer().isOp() && event.getItem().getType() == Material.POTION && (
				event.getItem().isSimilar(new ItemStack(Material.POTION, 1, (short) 8270))
						|| event.getItem().isSimilar(new ItemStack(Material.POTION, 1, (short) 8238))
		)) {
			event.setCancelled(true);
		}
	}
}
