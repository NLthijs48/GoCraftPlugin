package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.messages.Message;
import org.bukkit.Location;

public class LocationInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		if(!request.getTo().hasPermission("gocraft.staff")) {
			return;
		}

		Location location = request.getAbout().getLocation();
		request.message(Message.fromKey("information-itemLocation").replacements(
				location.getWorld().getName(),
				location.getBlockX(),
				location.getBlockY(),
				location.getBlockZ(),
				Math.round(location.getYaw()),
				Math.round(location.getPitch()),
				request.getAbout()
		));
	}
}
