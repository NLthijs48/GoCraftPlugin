package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.messages.Message;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HungerInfo extends InformationProvider {

	@Override
	public void show(Player about, CommandSender to) {
		String health = "";
		int foodNumber = about.getFoodLevel();
		if(foodNumber < 7) {
			health += ChatColor.RED;
		} else if(foodNumber < 13) {
			health += ChatColor.GOLD;
		} else {
			health += ChatColor.GREEN;
		}
		for(int i = 0; i < 20; i++) {
			if(i == foodNumber) {
				health += ChatColor.GRAY;
			}
			health += "▌";
		}
		Message saturation = Message.none();
		if(about.getSaturation() > 0) {
			saturation = Message.fromKey("information-itemHungerSaturation").replacements(about.getSaturation());
		}
		plugin.messageNoPrefix(to, "information-itemHunger", health, foodNumber, 20.0, saturation);
	}
}
