package me.wiefferink.gocraft.shop.buttons;

import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.tools.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.Sound;

public class CloseButton implements Button {

	private ItemBuilder displayItem;

	public CloseButton() {
		displayItem = new ItemBuilder(Material.BARRIER)
				.setName("&4Close shop")
				.addAction("Close");
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
	public void onClick(ShopSession session, ShopSession.ClickAction action) {
		session.close();
		session.getPlayer().playSound(session.getPlayer().getLocation(), Sound.CLICK, 0.5F, 0.7F);
	}

}
