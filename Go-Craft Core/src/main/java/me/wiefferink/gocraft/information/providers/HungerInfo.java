package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.ChatColor;

public class HungerInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		StringBuilder hunger = new StringBuilder();
		int foodNumber = request.getAbout().getFoodLevel();
		if(foodNumber < 7) {
			hunger.append(ChatColor.RED);
		} else if(foodNumber < 13) {
			hunger.append(ChatColor.GOLD);
		} else {
			hunger.append(ChatColor.GREEN);
		}
		for(int i = 0; i < 20; i++) {
			if(i == foodNumber) {
				hunger.append(ChatColor.GRAY);
			}
			hunger.append("▌");
		}
		Message saturation = Message.empty();
		if(request.getAbout().getSaturation() > 0) {
			saturation = Message.fromKey("information-itemHungerSaturation").replacements(((double)Math.round(request.getAbout().getSaturation()*10))/10);
		}

		request.message(Message.fromKey("information-itemHunger").replacements(hunger.toString(), foodNumber, 20, saturation));
	}
}
