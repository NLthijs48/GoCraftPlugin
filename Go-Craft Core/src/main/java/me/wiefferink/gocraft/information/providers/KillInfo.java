package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.interactivemessenger.processing.Message;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class KillInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		if(plugin.getGoPVPLink() != null) {
			// Get stats
			String playerName = request.getAbout().getName();
			int kills = plugin.getGoPVPLink().get().getFileManager().getKills(playerName);
			int deaths = plugin.getGoPVPLink().get().getFileManager().getDeaths(playerName);
			String kdString;
			double kd = ((double)plugin.getGoPVPLink().get().getFileManager().getKills(playerName)) / ((double)plugin.getGoPVPLink().get().getFileManager().getDeaths(playerName));
			if(!((Double)kd).isNaN() && !((Double)kd).isInfinite()) {
				BigDecimal bigDecimal = new BigDecimal(kd);
				bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP);
				kd = bigDecimal.doubleValue();
				kdString = ""+kd;
			} else {
				kdString = "-";
			}

			// Add messages
			request.message(Message.fromKey("information-kills").replacements(kills));
			request.message(Message.fromKey("information-deaths").replacements(deaths));
			request.message(Message.fromKey("information-killDeath").replacements(kdString));
		}
	}
}
