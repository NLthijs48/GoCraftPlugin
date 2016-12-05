package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HealthInfo extends InformationProvider {

	@Override
	public void show(Player about, CommandSender to) {
		String health = "";
		int healthNumber = (int)about.getHealth();
		if(healthNumber < 7) {
			health += ChatColor.RED;
		} else if(healthNumber < 13) {
			health += ChatColor.GOLD;
		} else {
			health += ChatColor.GREEN;
		}
		for(int i = 0; i < 20; i++) {
			if(i == healthNumber) {
				health += ChatColor.GRAY;
			}
			health += "▌";
		}
		plugin.messageNoPrefix(to, "information-itemHealth", health, healthNumber, about.getMaxHealth());
	}
}
