package me.wiefferink.gocraft.features.other;

import me.wiefferink.gocraft.features.Feature;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NauseaPotions extends Feature {

	private static final int fullTime = 20 * 20; // Max ticks

	public NauseaPotions() {
		listen("enableNauseaPotions");
	}

	@EventHandler(ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {

		ThrownPotion potion = event.getPotion();
		if(inWorld(potion) && potion != null
				&& potion.getItem() != null
				&& potion.getItem().hasItemMeta()
				&& potion.getItem().getItemMeta().hasDisplayName()) {
			String name = potion.getItem().getItemMeta().getDisplayName();
			if ((ChatColor.RESET + "" + ChatColor.DARK_GREEN + "Nausea").equals(name)) {
				for (LivingEntity entity : event.getAffectedEntities()) {
					entity.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, (int) Math.ceil(fullTime * event.getIntensity(entity)), 1));
				}
				event.setCancelled(true);
			}
		}
	}

}
