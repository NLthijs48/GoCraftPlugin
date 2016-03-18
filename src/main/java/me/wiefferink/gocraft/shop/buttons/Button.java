package me.wiefferink.gocraft.shop.buttons;

import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.tools.ItemBuilder;

public interface Button {

	ItemBuilder getButton();

	ItemBuilder getButton(ShopSession session);

	void onClick(ShopSession shopSession, ShopSession.ClickAction action);

}
