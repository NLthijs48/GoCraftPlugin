package me.wiefferink.gocraft.shop.signs;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class ShopSign extends Sign {

	public ShopSign(ConfigurationSection details, String key) {
		super(details, key);
	}

	@Override
	public void update() {
		setLines(
				"",
				"&lOpen the shop",
				"&9&l<Click>"
		);
	}

	@Override
	public void handleClicked(ClickAction clickAction, Player player) {
		if (GoCraft.getInstance().getShop() == null) {
			GoCraft.getInstance().message(player, "shop-notEnabled");
			return;
		}
		GoCraft.getInstance().getShop().open(player);
	}
}
