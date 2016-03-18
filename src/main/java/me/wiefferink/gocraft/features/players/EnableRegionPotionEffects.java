package me.wiefferink.gocraft.features.players;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class EnableRegionPotionEffects implements Listener {
	public final String configLine = "enableRegionPotionEffects";
	public final String configLinePotions = "regionPotionEffects";
	private GoCraft plugin;
	
	public EnableRegionPotionEffects(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine) && plugin.getWorldGuardLink() != null) {
			this.plugin = plugin;
			new BukkitRunnable() {
				@Override
				public void run() {
					refreshPotionEffects();				
				}
		    }.runTaskTimer(plugin, 1, 15L);
		}
	}

	/**
	 * Refresh all potion effects
	 */
	public void refreshPotionEffects() {
		List<Player> playersFirst = new ArrayList<>();
		for(Player player : Bukkit.getOnlinePlayers()) {
			playersFirst.add(player);
		}
		final ConfigurationSection regions = plugin.getConfig().getConfigurationSection("regionPotionEffects");
		final List<Player> players = playersFirst;
		new BukkitRunnable() {
			private int current = 0;
			
			@Override
			public void run() {
				for(int i=0; i<3; i++) {
					if(current < players.size()) {
						if(regions == null) {
							current++;	
							continue;
						}
						Player player = players.get(current);
						// Find applicable regions
						RegionManager manager = plugin.getWorldGuardLink().get().getRegionManager(player.getWorld());
						if(manager == null) {
							current++;	
							continue;
						}
						ApplicableRegionSet wgRegions = manager.getApplicableRegions(player.getLocation());
						// Apply effect for each region
						for(ProtectedRegion region : wgRegions) {
							ConfigurationSection effects = regions.getConfigurationSection(region.getId());
							if(effects == null) {
								continue;
							}
							for(String effect : effects.getKeys(false)) {
								PotionEffectType realEffect = PotionEffectType.getByName(effect);
								if(realEffect == null) {
									plugin.getLogger().info("Wrong potion effect in the config: " + effect);
								} else {
									player.addPotionEffect(new PotionEffect(realEffect, 59, effects.getInt(effect)), true);
								}
							}
						}
						current++;		
					}
				}
				if(current >= players.size()) {
					this.cancel();
				}
			}
		}.runTaskTimer(plugin, 1, 1);
	}
	
}





