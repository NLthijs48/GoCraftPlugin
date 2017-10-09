package me.wiefferink.gocraft.shop.buttons;

import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.shop.features.ShopFeature;
import me.wiefferink.gocraft.tools.ItemBuilder;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class BuyButton implements Button {

	private ItemBuilder displayItem;
	private Kit kit;

	public BuyButton(Kit kit) {
		this.kit = kit;
		displayItem = new ItemBuilder(Material.GOLD_INGOT);
		if (kit.getPriceFeature().getPrice() == 0) {
			displayItem.setName("&6&lGet " + kit.getName());
		} else {
			displayItem.setName("&6&lBuy " + kit.getName());
		}
	}

	@Override
	public ItemBuilder getButton() {
		return getButton(null);
	}

	@Override
	public ItemBuilder getButton(ShopSession session) {
		ItemBuilder result = displayItem.copy();
		boolean allowed = true;
		List<ShopFeature> list = new ArrayList<>(kit.getFeatures().values());
		for (int i = list.size() - 1; i >= 0; i--) {
			result.addLore(list.get(i).getBuyStatusLine(session), true);
			allowed &= list.get(i).allowsBuy(session);
		}
		if (allowed) {
			result.addAction("Buy");
		}
		return result;
	}

	@Override
	public void onClick(ShopSession session, ShopSession.ClickAction action) {
		kit.buy(session);
		if (action == ShopSession.ClickAction.RIGHT) {
			session.close();
		} else {
			session.refreshView();
		}
	}

}
