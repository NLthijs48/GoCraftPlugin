package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.shop.signs.KitSign;
import me.wiefferink.gocraft.tools.ItemBuilder;
import me.wiefferink.gocraft.tools.Utils;

public class ItemsFeature extends Feature {

	public ItemsFeature(Kit kit) {
		super(kit);
	}

	@Override
	public boolean allowsBuy(ShopSession session) {
		return Utils.inventoryRoom(session.getPlayer()) >= kit.getItems().size();
	}

	@Override
	public void indicateRestrictedBuy(ShopSession session) {
		GoCraft.getInstance().message(session.getPlayer(), "shop-lowSpace", kit.getItems().size(), Utils.inventoryRoom(session.getPlayer()));
	}

	@Override
	public boolean executeBuy(ShopSession session, KitSign sign) {
		boolean result = true;
		for (ItemBuilder builder : kit.getItems()) {
			result &= session.getPlayer().getInventory().addItem(builder.getItemStack().clone()).size() == 0;
		}
		return result;
	}

	@Override
	public String getBuyStatusLine(ShopSession session) {
		if (!allowsBuy(session)) {
			return "&4Requires " + kit.getItems().size() + " free inventory slots";
		}
		return null;
	}


	@Override
	public boolean allowsSell(ShopSession session) {
		boolean result = true;
		for (ItemBuilder builder : kit.getItems()) {
			result &= Utils.hasItems(session.getPlayer().getInventory(), builder.getItemStack()) >= builder.getItemStack().getAmount();
		}
		return result;
	}

	@Override
	public void indicateRestrictedSell(ShopSession session) {
		GoCraft.getInstance().message(session.getPlayer(), "shop-noItems");
	}

	@Override
	public boolean executeSell(ShopSession session, KitSign sign) {
		boolean result = true;
		for (ItemBuilder builder : kit.getItems()) {
			result &= Utils.removeItems(session.getPlayer().getInventory(), builder.getItemStack());
		}
		return result;
	}

	@Override
	public String getSellStatusLine(ShopSession session) {
		if (!allowsSell(session)) {
			return "&4Requires the items displayed above";
		}
		return null;
	}
}
