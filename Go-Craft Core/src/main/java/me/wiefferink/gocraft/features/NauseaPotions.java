package me.wiefferink.gocraft.features;

import me.wiefferink.gocraft.GoCraft;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class NauseaPotions implements Listener {

	public final String configLine = "enableNauseaPotions";
	private GoCraft plugin;
	private static final int fullTime = 20 * 20; // Max ticks

	public NauseaPotions(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPotionSplash(PotionSplashEvent event) {
		ThrownPotion potion = event.getPotion();
		if (potion != null
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
