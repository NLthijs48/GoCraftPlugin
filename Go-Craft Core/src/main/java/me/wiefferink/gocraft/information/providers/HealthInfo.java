package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.ChatColor;

public class HealthInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		StringBuilder health = new StringBuilder();
		int healthNumber = (int)request.getAbout().getHealth();
		if(healthNumber < 7) {
			health.append(ChatColor.RED);
		} else if(healthNumber < 13) {
			health.append(ChatColor.GOLD);
		} else {
			health.append(ChatColor.GREEN);
		}
		for(int i = 0; i < 20; i++) {
			if(i == healthNumber) {
				health.append(ChatColor.GRAY);
			}
			health.append("▌");
		}

		request.message(Message.fromKey("information-itemHealth").replacements(health.toString(), healthNumber, Math.round(request.getAbout().getMaxHealth())));
	}
}
