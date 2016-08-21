package me.wiefferink.gocraft.features.other;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AddDefaultRank extends Feature {

	private static Set<String> correctRanks = new HashSet<>(Arrays.asList("default", "Builder", "Builder+", "Admin", "HeadMod", "Mod", "TrialMod"));

	public AddDefaultRank() {
		if(plugin.getPermissionProvider() != null) {
			listen("enableAddDefaultRank");
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(inWorld(event)) {
			String[] ranks = plugin.getPermissionProvider().getPlayerGroups(event.getPlayer());
			boolean alreadyHasRank = false;
			for(String rank : ranks) {
				alreadyHasRank |= correctRanks.contains(rank);
			}
			if(!alreadyHasRank) {
				plugin.getPermissionProvider().playerAddGroup(null, event.getPlayer(), "default");
			}
		}
	}
}
