package me.wiefferink.gocraft.features.environment;

import me.wiefferink.gocraft.features.Feature;
import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.WeatherChangeEvent;

public class DisableRain extends Feature {

	public DisableRain() {
		listen("disableRain");
	}

	// Prevent rain and thunder
	@EventHandler(ignoreCancelled = true)
	public void onWeatherChange(WeatherChangeEvent event) {
		event.setCancelled(inWorld(event) && event.toWeatherState());
	}
}
