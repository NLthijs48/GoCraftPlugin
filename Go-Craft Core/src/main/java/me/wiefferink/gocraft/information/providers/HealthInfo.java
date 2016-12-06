package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.messages.Message;
import org.bukkit.ChatColor;

public class HealthInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		String health = "";
		int healthNumber = (int)request.getAbout().getHealth();
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

		request.message(Message.fromKey("information-itemHealth").replacements(health, healthNumber, request.getAbout().getMaxHealth()));
	}
}
