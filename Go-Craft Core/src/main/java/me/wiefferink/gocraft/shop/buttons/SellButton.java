package me.wiefferink.gocraft.shop.buttons;

import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.shop.features.ShopFeature;
import me.wiefferink.gocraft.tools.ItemBuilder;
import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class SellButton implements Button {

	private ItemBuilder displayItem;
	private Kit kit;

	public SellButton(Kit kit) {
		this.kit = kit;
		displayItem = new ItemBuilder(Material.IRON_INGOT);
		displayItem.setName("&6&lSell " + kit.getName());
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
			result.addLore(list.get(i).getSellStatusLine(session), true);
			allowed &= list.get(i).allowsSell(session);
		}
		if (allowed) {
			result.addAction("Sell");
		} else {
			result.setAmount(0);
		}
		return result;
	}

	@Override
	public void onClick(ShopSession session, ShopSession.ClickAction action) {
		kit.sell(session);
		if (action == ShopSession.ClickAction.RIGHT) {
			session.close();
		} else {
			session.refreshView();
		}
	}

}
