package nl.evolutioncoding.gocraft.other;

import nl.evolutioncoding.gocraft.GoCraft;
import nl.evolutioncoding.gocraft.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class AboveNetherPrevention implements Listener {

	public final String configLine = "enableAboveNetherPrevention";

	public AboveNetherPrevention(final GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			new BukkitRunnable() {
				@Override
				public void run() {
					for(Player player : Bukkit.getOnlinePlayers()) {
						if(player.getWorld().getEnvironment() == World.Environment.NETHER
								&& player.getLocation().getBlockY() >= 128) {
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
							plugin.message(player, "netherGlitching");
						}
					}
				}
			}.runTaskTimer(plugin, 40, 40);
		}
	}
}
