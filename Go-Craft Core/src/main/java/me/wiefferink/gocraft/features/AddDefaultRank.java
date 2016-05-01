package me.wiefferink.gocraft.features;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AddDefaultRank implements Listener {

	public final String configLine = "enableAddDefaultRank";

	private GoCraft plugin;
	private Set<String> correctRanks;

	public AddDefaultRank(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine) && plugin.getPermissionProvider() != null) {
			this.plugin = plugin;
			correctRanks = new HashSet<>(Arrays.asList("default", "Builder", "Builder+", "Admin", "HeadMod", "Mod", "TrialMod"));
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		String[] ranks = plugin.getPermissionProvider().getPlayerGroups(event.getPlayer());
		boolean alreadyHasRank = false;
		for (String rank : ranks) {
			alreadyHasRank |= correctRanks.contains(rank);
		}
		if (!alreadyHasRank) {
			plugin.getPermissionProvider().playerAddGroup(null, event.getPlayer(), "default");
		}
	}
}
