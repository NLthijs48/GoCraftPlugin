package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.messages.Message;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GamemodeInfo extends InformationProvider {

	@Override
	public void show(Player about, CommandSender to) {
		if(!to.hasPermission("gocraft.staff")) {
			return;
		}

		Message OPPart = Message.none();
		if(to.isOp()) {
			OPPart = Message.fromKey("information-gameModeOP");
		}

		// Admins have a gamemode selector, staff only sees gamemode status, could improve this by actually checking essentials gamemode permissions
		Message gameModes = Message.none();
		if(to.hasPermission("gocraft.admin")) {
			for(GameMode gameMode : GameMode.values()) {
				if(!gameModes.isEmpty()) {
					gameModes.append(", ");
				}
				if(about.getGameMode() == gameMode) {
					gameModes.append(Message.fromKey("information-gameModeSelected").replacements(StringUtils.capitalize(gameMode.name().toLowerCase())));
				} else {
					gameModes.append(Message.fromKey("information-gameModeNotSelected").replacements(StringUtils.capitalize(gameMode.name().toLowerCase()), about.getName()));
				}
			}
		} else {
			gameModes = Message.fromString(StringUtils.capitalize(about.getGameMode().name().toLowerCase()));
		}

		plugin.messageNoPrefix(to, "information-gameMode", gameModes, OPPart);
	}
}
