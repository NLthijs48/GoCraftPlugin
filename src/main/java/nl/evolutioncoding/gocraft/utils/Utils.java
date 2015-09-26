package nl.evolutioncoding.gocraft.utils;

import net.minecraft.server.v1_8_R3.EntityPlayer;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class Utils {

	private Utils() {}

	/**
	 * Get the ping of a player
	 * @param player The player to check
	 * @return The ping in ms
	 */
	public static int getPing(Player player) { 
		CraftPlayer cp = (CraftPlayer) player; 
		EntityPlayer ep = cp.getHandle(); 
		return ep.ping; 
	}

}
