package me.wiefferink.gocraft.shop.signs;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class KitSign extends Sign {

	private Kit kit;
	private Double price;

	public KitSign(ConfigurationSection details, String key, Kit kit) {
		super(details, key);
		this.kit = kit;
		if (details.isDouble("price")) {
			price = details.getDouble("price");
		}
	}

	/**
	 * Get the price of the kit, optionally changed by the sign
	 *
	 * @return The price of the kit sold by this sign
	 */
	public double getPrice() {
		if (price != null) {
			return price;
		}
		return kit.getPriceFeature().getPrice();
	}

	/**
	 * Get the formatted price for this kit sign
	 *
	 * @return The formatted price for this kit sign
	 */
	public String getFormattedPrice() {
		if (getPrice() == 0) {
			return "For free";
		}
		return Utils.formatCurrency(getPrice());
	}

	@Override
	public void update() {
		setLines(
				getPrice() == 0 ? "&2&l[Get]" : "&2&l[Buy]",
				"&l" + kit.getName(),
				"&l" + getFormattedPrice(),
				"&9Every " + kit.getCooldownFeature().getRawCooldown()
		);
	}

	@Override
	public void handleClicked(ClickAction clickAction, Player player) {
		if (clickAction == ClickAction.LEFT) {
			GoCraft.getInstance().message(player, "shop-kitSignInfo", kit.getName(), getFormattedPrice());
		} else {
			kit.buy(new ShopSession(player));
		}
	}
}
