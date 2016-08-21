package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.SignChangeEvent;

public class DisableSignUseWhileMuted extends Feature {

	public DisableSignUseWhileMuted() {
		listen("disableSignUseWhileMuted");
	}

	// Stay at full hunger
	@EventHandler(ignoreCancelled = true)
	public void onSignPlace(SignChangeEvent event) {
		if(inWorld(event) && plugin.getBanManagerLink() != null && plugin.getBanManagerLink().get().getPlayerMuteStorage().isMuted(event.getPlayer().getUniqueId())) {
			event.setCancelled(true);
			plugin.message(event.getPlayer(), "general-mutedSignUse");
		}
	}
}
