package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class EnablePotionEffectsOnJoin extends Feature {

	public final String configLinePotions = "potionEffectsOnJoin";

	public EnablePotionEffectsOnJoin() {
		listen("enablePotionEffectsOnJoin");
	}

	// Give potion effects on join
	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(inWorld(event) && getConfig().getConfigurationSection(configLinePotions) != null) {
			Set<String> effects = getConfig().getConfigurationSection(configLinePotions).getKeys(false);
			for (String effect : effects) {
				PotionEffectType realEffect = PotionEffectType.getByName(effect);
				if (realEffect == null) {
					Log.warn("Wrong potion effect in the config: "+effect);
				} else {
					event.getPlayer().addPotionEffect(new PotionEffect(realEffect, Integer.MAX_VALUE, getConfig().getInt(configLinePotions+"."+effect), true));
				}
			}
		}
	}
}
