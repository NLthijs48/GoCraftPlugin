package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;

public class DisableFrostWalker extends Feature {

	public DisableFrostWalker() {
		listen("disableFrostWalker");
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		if(event.getCurrentItem() == null) {
			return;
		}

		// Remove frost walker when clicking the item
		event.getCurrentItem().removeEnchantment(Enchantment.FROST_WALKER);
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onEnchant(EnchantItemEvent event) {
		// Prevent getting frost walker enchant when enchanting something (it is a treasure enchant though so should not be possible)
		if(event.getEnchantsToAdd() != null) {
			event.getEnchantsToAdd().remove(Enchantment.FROST_WALKER);
		}
	}

	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onAnvil(PrepareAnvilEvent event) {
		// Prevent putting frost walker enchant on an item with a book
		if(event.getResult() != null) {
			ItemStack item = event.getResult();
			item.removeEnchantment(Enchantment.FROST_WALKER);
			event.setResult(item);
		}
	}

}
