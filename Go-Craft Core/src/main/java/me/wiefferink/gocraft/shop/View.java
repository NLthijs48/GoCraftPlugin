package me.wiefferink.gocraft.shop;

public interface View {

	void show(ShopSession shopSession);

	void clickItem(ShopSession session, ShopSession.ClickAction action, int slot);
}
