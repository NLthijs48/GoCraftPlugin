package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.messages.Message;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.GameMode;

public class GamemodeInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		if(!request.getTo().hasPermission("gocraft.staff")) {
			return;
		}

		Message OPPart = Message.none();
		if(request.getAboutOffline().isOp()) {
			OPPart = Message.fromKey("information-gameModeOP");
		}

		// Admins have a gamemode selector, staff only sees gamemode status, could improve this by actually checking essentials gamemode permissions
		Message gameModes = Message.none();
		if(request.getTo().hasPermission("gocraft.admin")) {
			for(GameMode gameMode : GameMode.values()) {
				if(!gameModes.isEmpty()) {
					gameModes.append(", ");
				}
				if(request.getAbout().getGameMode() == gameMode) {
					gameModes.append(Message.fromKey("information-gameModeSelected").replacements(StringUtils.capitalize(gameMode.name().toLowerCase())));
				} else {
					gameModes.append(Message.fromKey("information-gameModeNotSelected").replacements(StringUtils.capitalize(gameMode.name().toLowerCase()), request.getAbout().getName()));
				}
			}
		} else {
			gameModes = Message.fromString(StringUtils.capitalize(request.getAbout().getGameMode().name().toLowerCase()));
		}

		request.message(Message.fromKey("information-gameMode").replacements(gameModes, OPPart));
	}
}
