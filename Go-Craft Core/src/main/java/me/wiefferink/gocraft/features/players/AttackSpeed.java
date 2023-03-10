package me.wiefferink.gocraft.features.players;

import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

public class AttackSpeed extends Feature {

	public AttackSpeed() {
		if(listen("playerAttributes")) {
			Bukkit.getOnlinePlayers().forEach(this::setPlayerAttributes);
		}
	}

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event) {
		if(inWorld(event)) {
			setPlayerAttributes(event.getPlayer());
		}
	}

	/**
	 * Set the player attributes according to the config file
	 * @param player The player to set it for
	 */
	private void setPlayerAttributes(Player player) {
		ConfigurationSection attributesSection = getConfig().getConfigurationSection("playerAttributes");
		if(attributesSection != null) {
			for(String attribute : attributesSection.getKeys(false)) {
				try {
					Attribute parsed = Attribute.valueOf(attribute.toUpperCase());
					player.getAttribute(parsed).setBaseValue(attributesSection.getDouble(attribute));
				} catch(IllegalArgumentException e) {
					Log.warn("Incorrect player attribute: "+attribute);
				}
			}
		}
	}
}
