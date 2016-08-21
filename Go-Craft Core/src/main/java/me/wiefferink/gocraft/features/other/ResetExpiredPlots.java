package me.wiefferink.gocraft.features.other;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class ResetExpiredPlots extends Feature {

	public final String configLine = "enableExpiredPlotsRestoration";

	public ResetExpiredPlots() {
		if(listen("enableExpiredPlotsRestoration")) {
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "plotme resetexpired plotworld");
				}
			}.runTaskTimer(plugin, 6000, 12000);
		}
	}
}
