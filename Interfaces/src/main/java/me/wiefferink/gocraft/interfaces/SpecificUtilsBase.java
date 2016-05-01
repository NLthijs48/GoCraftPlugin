package me.wiefferink.gocraft.interfaces;


import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SpecificUtilsBase {
	/**
	 * Get the current ping of the player
	 * @param player The player to get the ping for
	 * @return The current ping of the player
	 */
	public int getPing(Player player) {
		return -1;
	}

	/**
	 * Load player data of a (possibly) offline player by UUID
	 * @param uuid The UUID of the player to load
	 * @return The Player object of the target player
	 */
	public Player loadPlayer(UUID uuid) {
		return null;
	}

	/**
	 * Add glow to an item without an enchantment
	 * @return this
	 */
	public ItemStack addGlow(ItemStack item) {
		// Simply do not apply a glow
		return item;
	}
}
