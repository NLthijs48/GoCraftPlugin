package me.wiefferink.gocraft.features.other;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

public class ResetExpiredPlots implements Listener {

	public final String configLine = "enableExpiredPlotsRestoration";
	@SuppressWarnings("unused")
	private GoCraft plugin;

	public ResetExpiredPlots(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			new BukkitRunnable() {
				@Override
				public void run() {
					Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "plotme resetexpired plotworld");
				}
			}.runTaskTimer(plugin, 6000, 12000);
		}
	}
}
