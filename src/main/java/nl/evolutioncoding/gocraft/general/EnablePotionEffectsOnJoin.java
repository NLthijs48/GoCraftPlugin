package nl.evolutioncoding.gocraft.general;

import nl.evolutioncoding.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Set;

public class EnablePotionEffectsOnJoin implements Listener {
	
	public final String configLine = "enablePotionEffectsOnJoin";
	public final String configLinePotions = "potionEffectsOnJoin";
	private GoCraft plugin;
	
	public EnablePotionEffectsOnJoin(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Stay at full hunger
	@EventHandler
	public void onHungerChange(FoodLevelChangeEvent event) {
		if(plugin.onThisWorld(configLine, event.getEntity())) {
			event.setFoodLevel(20);
		}
	}
	
	// Give potion effects on join
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(plugin.onThisWorld(configLine, event.getPlayer())) {
			if(plugin.getConfig().getConfigurationSection(configLinePotions) == null) {
				return;
			}
			Set<String> effects = plugin.getConfig().getConfigurationSection(configLinePotions).getKeys(false);	
			for(String effect : effects) {
				PotionEffectType realEffect = PotionEffectType.getByName(effect);
				if(realEffect == null) {
					plugin.getLogger().info("Wrong potion effect in the config: " + effect);
				} else {
					event.getPlayer().addPotionEffect(new PotionEffect(realEffect, Integer.MAX_VALUE, plugin.getConfig().getInt(configLinePotions + "." + effect), true));
				}
			}
		}
	}
}
