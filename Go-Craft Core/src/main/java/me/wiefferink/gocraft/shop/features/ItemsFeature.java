package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.tools.ItemBuilder;
import me.wiefferink.gocraft.tools.Utils;

public class ItemsFeature extends Feature {

	public ItemsFeature(Kit kit) {
		super(kit);
	}

	@Override
	public boolean allows(ShopSession session) {
		// TODO consider stacking items onto existing ones?
		return Utils.inventoryRoom(session.getPlayer()) >= kit.getItems().size();
	}

	@Override
	public void indicateRestricted(ShopSession session) {
		GoCraft.getInstance().message(session.getPlayer(), "shop-lowSpace", kit.getItems().size(), Utils.inventoryRoom(session.getPlayer()));
	}

	@Override
	public boolean execute(ShopSession session) {
		boolean result = true;
		for (ItemBuilder builder : kit.getItems()) {
			result &= session.getPlayer().getInventory().addItem(builder.getItemStack().clone()).size() == 0;
		}
		return result;
	}

	@Override
	public String getStatusLine(ShopSession session) {
		if (!allows(session)) {
			return "&4Requires " + kit.getItems().size() + " free inventory slots";
		}
		return null;
	}
}
