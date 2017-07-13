package me.wiefferink.gocraft.features.players;

import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.tools.scheduling.Do;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class EnableRegionPotionEffects extends Feature {
	public final String configLine = "enableRegionPotionEffects";
	public final String configLinePotions = "regionPotionEffects";

	public EnableRegionPotionEffects() {
		if(plugin.getWorldGuardLink() != null && listen("enableRegionPotionEffects")) {
			Do.syncTimer(15, this::refreshPotionEffects);
		}
	}

	/**
	 * Refresh all potion effects
	 */
	public void refreshPotionEffects() {
		List<Player> playersFirst = new ArrayList<>(Bukkit.getOnlinePlayers());
		final ConfigurationSection regions = getConfig().getConfigurationSection("regionPotionEffects");

		Do.forAll(3, Bukkit.getOnlinePlayers(), player -> {
			if(regions == null || !inWorld(player) || !player.isOnline()) {
				return;
			}

			// Find applicable regions
			RegionManager manager = plugin.getWorldGuardLink().get().getRegionManager(player.getWorld());
			if(manager == null) {
				return;
			}

			// Apply effect for each region
			ApplicableRegionSet wgRegions = manager.getApplicableRegions(player.getLocation());
			for(ProtectedRegion region : wgRegions) {
				ConfigurationSection effects = regions.getConfigurationSection(region.getId());
				if(effects == null) {
					continue;
				}
				for(String effect : effects.getKeys(false)) {
					PotionEffectType realEffect = PotionEffectType.getByName(effect);
					if(realEffect == null) {
						Log.warn("Wrong potion effect in the config: " + effect);
					} else {
						player.addPotionEffect(new PotionEffect(realEffect, 59, effects.getInt(effect)), true);
					}
				}
			}
		});
	}

}





