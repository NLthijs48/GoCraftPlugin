package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class DisableSignUseWhileMuted implements Listener {

	public final String configLine = "disableSignUseWhileMuted";
	private GoCraft plugin;

	public DisableSignUseWhileMuted(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Stay at full hunger
	@EventHandler
	public void onSignPlace(SignChangeEvent event) {
		if (plugin.onThisWorld(configLine, event.getBlock())) {
			if (plugin.getBanManagerLink() != null && plugin.getBanManagerLink().get().getPlayerMuteStorage().isMuted(event.getPlayer().getUniqueId())) {
				event.setCancelled(true);
				plugin.message(event.getPlayer(), "general-mutedSignUse");
			}
		}
	}
}
