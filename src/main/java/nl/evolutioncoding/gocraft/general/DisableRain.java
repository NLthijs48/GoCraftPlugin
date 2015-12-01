package nl.evolutioncoding.gocraft.general;

import nl.evolutioncoding.gocraft.GoCraft;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.WeatherChangeEvent;

public class DisableRain implements Listener {
	
	public final String configLine = "disableRain";
	private GoCraft plugin;
	
	public DisableRain(GoCraft plugin) {
		if(plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent rain and thunder
	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) {
		if(plugin.onThisWorld(configLine, event.getWorld())
				&& event.toWeatherState()) {
			event.setCancelled(true);
		}
	}
}
