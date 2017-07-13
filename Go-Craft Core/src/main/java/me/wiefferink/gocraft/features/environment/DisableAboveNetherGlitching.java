package me.wiefferink.gocraft.features.environment;

import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.gocraft.tools.scheduling.Do;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class DisableAboveNetherGlitching extends Feature {

	public DisableAboveNetherGlitching() {
		if(!listen("disableAboveNetherGlitching")) {
			return;
		}

		Do.syncTimer(40, () -> {
			for(Player player : Bukkit.getOnlinePlayers()) {
				if(inWorld(player)
						&& player.getWorld().getEnvironment() == World.Environment.NETHER
						&& player.getLocation().getBlockY() >= 128
				) {
					// Try to move the player down
					Location tryLoc = player.getLocation().subtract(0, 5, 0);
					while(!Utils.isSafe(tryLoc) && tryLoc.getBlockY() > 5) {
						tryLoc = tryLoc.subtract(0, 1, 0);
					}
					if(Utils.isSafe(tryLoc)) {
						player.teleport(tryLoc);
					} else {
						player.teleport(Bukkit.getWorld("world").getSpawnLocation());
					}
					plugin.message(player, "other-netherGlitching");
				}
			}
		});
	}
}
