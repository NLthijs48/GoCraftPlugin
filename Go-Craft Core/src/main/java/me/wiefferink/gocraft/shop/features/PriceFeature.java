package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.shop.signs.KitSign;
import me.wiefferink.gocraft.tools.Utils;
import net.milkbowl.vault.economy.EconomyResponse;

public class PriceFeature extends ShopFeature {

	private double price;
	private double sellPrice;

	public PriceFeature(Kit kit) {
		super(kit);
		price = kit.getDetails().getDouble("price");
		if (price < 0) {
			price = Double.MAX_VALUE;
		}
		sellPrice = kit.getDetails().getDouble("sellPrice");
		if (sellPrice < 0) {
			sellPrice = Double.MAX_VALUE;
		}
	}

	@Override
	public boolean allowsBuy(ShopSession session) {
		return GoCraft.getInstance().getEconomy().has(session.getPlayer(), getPrice());
	}

	@Override
	public void indicateRestrictedBuy(ShopSession session) {
		GoCraft.getInstance().message(session.getPlayer(), "shop-lowMoney", getFormattedPrice(), kit.getName(), session.getFormattedBalance());
	}

	@Override
	public boolean executeBuy(ShopSession session, KitSign sign) {
		double price = getPrice();
		if (sign != null) {
			price = sign.getPrice();
		}
		EconomyResponse response = GoCraft.getInstance().getEconomy().withdrawPlayer(session.getPlayer(), price);
		if (!response.transactionSuccess()) {
			GoCraft.getInstance().message(session.getPlayer(), "shop-chargeFailed");
			return false;
		}
		session.updateBalance();
		return true;
	}

	@Override
	public boolean executeSell(ShopSession session, KitSign sign) {
		double price = getSellPrice();
		if (sign != null) {
			price = sign.getSellPrice();
		}
		EconomyResponse response = GoCraft.getInstance().getEconomy().depositPlayer(session.getPlayer(), price);
		if (!response.transactionSuccess()) {
			GoCraft.getInstance().message(session.getPlayer(), "shop-depositFailed");
			return false;
		}
		session.updateBalance();
		return true;
	}

	@Override
	public String getBuyStatusLine(ShopSession session) {
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

	@Override
	public String getSellStatusLine(ShopSession session) {
		if (session == null) {
			return null;
		}
		if (getPrice() == 0) {
			return "&2Give away";
		} else {
			return "&2Sell price: &l" + getFormattedSellPrice();
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
	 * Get the sell price of the kit
	 * @return The sell price of the kit
	 */
	public double getSellPrice() {
		return sellPrice;
	}

	/**
	 * Get a formatted string of the kit price
	 * @return A formatted string of the kit price
	 */
	public String getFormattedPrice() {
		return Utils.formatCurrency(getPrice());
	}

	/**
	 * Get a formatted string of the kit sell price
	 * @return A formatted string of the kit sell price
	 */
	public String getFormattedSellPrice() {
		return Utils.formatCurrency(getSellPrice());
	}
}
