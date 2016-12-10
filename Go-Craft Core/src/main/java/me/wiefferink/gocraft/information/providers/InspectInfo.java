package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.messages.Message;

public class InspectInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		if(!request.getTo().hasPermission("gocraft.inspect")) {
			return;
		}

		request.message(Message.fromKey("information-inspect").replacements(request.getAbout().getName()));
	}

}
