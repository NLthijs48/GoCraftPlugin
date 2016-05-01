package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.tools.Utils;
import net.milkbowl.vault.economy.EconomyResponse;

public class PriceFeature extends Feature {

	private double price;

	public PriceFeature(Kit kit) {
		super(kit);
		price = kit.getDetails().getDouble("price");
		if (price < 0) {
			price = Double.MAX_VALUE;
		}
	}

	@Override
	public boolean allows(ShopSession session) {
		return GoCraft.getInstance().getEconomy().has(session.getPlayer(), getPrice());
	}

	@Override
	public void indicateRestricted(ShopSession session) {
		GoCraft.getInstance().message(session.getPlayer(), "shop-lowMoney", getPrice(), kit.getName(), GoCraft.getInstance().getEconomy().getBalance(session.getPlayer()));
	}

	@Override
	public boolean execute(ShopSession session) {
		EconomyResponse response = GoCraft.getInstance().getEconomy().withdrawPlayer(session.getPlayer(), getPrice());
		if (!response.transactionSuccess()) {
			GoCraft.getInstance().message(session.getPlayer(), "shop-chargeFailed");
			return false;
		}
		session.updateBalance();
		return true;
	}

	@Override
	public String getStatusLine(ShopSession session) {
		if (session == null) {
			return null;
		}
		if (getPrice() == 0) {
			return "&2For free";
		} else {
			if (session.getBalance() >= getPrice()) {
				return "&2Price: &l" + getFormattedPrice();
			} else {
				return "&4Price: &l" + getFormattedPrice() + "&r&4, your balance: " + session.getFormattedBalance();
			}
		}
	}

	/**
	 * Get the price of the kit
	 * @return The price of the kit
	 */
	public double getPrice() {
		return price;
	}

	/**
	 * Get a formatted string of the kit price
	 * @return A formatted string of the kit price
	 */
	public String getFormattedPrice() {
		return Utils.formatCurrency(getPrice());
	}
}
