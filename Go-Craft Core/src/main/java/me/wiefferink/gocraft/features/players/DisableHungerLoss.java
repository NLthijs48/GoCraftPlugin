package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class DisableHungerLoss extends Feature {

	public DisableHungerLoss() {
		listen("disableHungerLoss");
	}

	// Stay at full hunger
	@EventHandler(ignoreCancelled = true)
	public void onHungerChange(FoodLevelChangeEvent event) {
		if(inWorld(event)) {
			event.setFoodLevel(20);
		}
	}
}
