package nl.evolutioncoding.gocraft.general;

import nl.evolutioncoding.gocraft.GoCraft;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class DisableHungerLoss implements Listener {
	
	public final String configLine = "disableHungerLoss";
	private GoCraft plugin;
	
	public DisableHungerLoss(GoCraft plugin) {
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
}
