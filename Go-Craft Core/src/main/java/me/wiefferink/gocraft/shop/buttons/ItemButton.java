package me.wiefferink.gocraft.shop.buttons;

import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.tools.ItemBuilder;

public class ItemButton implements Button {

	private ItemBuilder displayItem;

	public ItemButton(ItemBuilder displayItem) {
		this.displayItem = displayItem;
	}

	@Override
	public ItemBuilder getButton() {
		return getButton(null);
	}

	@Override
	public ItemBuilder getButton(ShopSession session) {
		return displayItem;
	}

	@Override
	public void onClick(ShopSession shopSession, ShopSession.ClickAction action) {
		// Do nothing
	}

}
