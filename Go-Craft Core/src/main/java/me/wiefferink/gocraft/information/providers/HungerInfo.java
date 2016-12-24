package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.messages.Message;
import org.bukkit.ChatColor;

public class HungerInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		String health = "";
		int foodNumber = request.getAbout().getFoodLevel();
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
		if(request.getAbout().getSaturation() > 0) {
			saturation = Message.fromKey("information-itemHungerSaturation").replacements(((double)Math.round(request.getAbout().getSaturation()*10))/10);
		}

		request.message(Message.fromKey("information-itemHunger").replacements(health, foodNumber, 20, saturation));
	}
}
