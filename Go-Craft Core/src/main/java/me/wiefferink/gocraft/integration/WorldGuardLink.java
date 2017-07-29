package me.wiefferink.gocraft.integration;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class WorldGuardLink {
	WorldGuardPlugin worldGuard;

	public WorldGuardLink() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("WorldGuard");
		if (!(plugin instanceof WorldGuardPlugin)) {
			Log.warn("Plugin with name WorldGuard found, but it is not the correct one");
		} else {
			this.worldGuard = (WorldGuardPlugin) plugin;
		}
	}

	public WorldGuardPlugin get() {
		return worldGuard;
	}

	/**
	 * Check if a player is in PVP area
	 * @param player The player to check
	 * @return true if the player is in a PVP area, otherwise false
	 */
	public boolean isInPvpArea(Player player) {
		return worldGuard != null && worldGuard
				.getRegionManager(player.getWorld())
				.getApplicableRegions(player.getLocation())
				.testState(GoCraft.getInstance().getWorldGuardLink().get().wrapPlayer(player), DefaultFlag.PVP);
	}
}
