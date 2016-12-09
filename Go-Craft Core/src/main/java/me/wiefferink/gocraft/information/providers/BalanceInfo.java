package me.wiefferink.gocraft.information.providers;

import me.wiefferink.gocraft.information.InformationProvider;
import me.wiefferink.gocraft.information.InformationRequest;
import me.wiefferink.gocraft.messages.Message;
import me.wiefferink.gocraft.tools.Utils;

public class BalanceInfo extends InformationProvider {

	@Override
	public void showSync(InformationRequest request) {
		if(plugin.getEconomy() != null) {
			double balance = plugin.getEconomy().getBalance(request.getAbout());
			request.message(Message.fromKey("information-balance").replacements(Utils.formatCurrency(balance)));
		}
	}
}
