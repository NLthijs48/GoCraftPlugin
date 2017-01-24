package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.interactivemessenger.processing.Message;
import me.wiefferink.gocraft.tools.Utils;

public class PingInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		if(!request.getAbout().isOnline()) {
			return;
		}

		request.message(Message.fromKey("information-ping").replacements(Utils.getPing(request.getAbout())));
	}

}

