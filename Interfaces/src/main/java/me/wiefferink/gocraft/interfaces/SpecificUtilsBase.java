package me.wiefferink.gocraft.interfaces;


import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.UUID;

public class SpecificUtilsBase {

	public static Random random = new Random();

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

	/**
	 * Send a fake player to a player
	 * @param location The location to spawn it at
	 * @param player The player to send it to
	 * @param visible true if the fake player should be visible, otherwise false
	 * @param name The name of the fake player
	 * @return The entity id of the fake player
	 */
	public int sendFakePlayer(Location location, Player player, boolean visible, String name) {
		return -1;
	}
}
