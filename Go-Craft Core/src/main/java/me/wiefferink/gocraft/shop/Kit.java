package me.wiefferink.gocraft.shop;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.shop.buttons.Button;
import me.wiefferink.gocraft.shop.buttons.BuyButton;
import me.wiefferink.gocraft.shop.buttons.ItemButton;
import me.wiefferink.gocraft.shop.buttons.SellButton;
import me.wiefferink.gocraft.shop.features.CooldownFeature;
import me.wiefferink.gocraft.shop.features.ItemsFeature;
import me.wiefferink.gocraft.shop.features.MapKitsFeature;
import me.wiefferink.gocraft.shop.features.PermissionFeature;
import me.wiefferink.gocraft.shop.features.PriceFeature;
import me.wiefferink.gocraft.shop.features.ShopFeature;
import me.wiefferink.gocraft.shop.signs.KitSign;
import me.wiefferink.gocraft.tools.ItemBuilder;
import me.wiefferink.gocraft.tools.Utils;
import me.wiefferink.interactivemessenger.processing.Message;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Kit implements Button, View {

	private ItemBuilder button;
	private List<ItemBuilder> items;
	private String name;
	private Map<Integer, Button> buttons;
	private GoCraft plugin;
	private Shop shop;
	private ConfigurationSection details;
	private String identifier;
	private boolean onNewLine;
	private Map<String, ShopFeature> features;

	public Kit(ConfigurationSection details, String identifier, Shop shop) {
		this.details = details;
		this.identifier = identifier;
		this.plugin = GoCraft.getInstance();
		this.shop = shop;
		name = details.getString("name");

		buttons = new HashMap<>();
		features = new LinkedHashMap<>();
		features.put("price", new PriceFeature(this));
		features.put("cooldown", new CooldownFeature(this));
		features.put("items", new ItemsFeature(this));
		features.put("permission", new PermissionFeature(this));
		if (GoCraft.getInstance().getMapSwitcherLink() != null) {
			features.put("maps", new MapKitsFeature(this));
		}

		onNewLine = details.getBoolean("onNewLine");
	}

	public void setup() {
		// Setup items
		items = new ArrayList<>();
		if (!details.isList("items")) {
			String itemString = details.getString("items");
			if (itemString == null || itemString.isEmpty()) {
				Log.warn("Kit with zero items: "+getName());
				return;
			}
			ItemBuilder item = shop.stringToItem(itemString, "kit " + getName());
			if(!item.hasCustomName() && item.getItemStack().getType() != Material.MONSTER_EGG) {
				item.setName(ChatColor.DARK_GREEN + getName());
			}
			items.add(item);
			buttons.put(0, new ItemButton(item));
		} else {
			List<String> itemStrings = details.getStringList("items");
			if (itemStrings.size() == 0) {
				Log.warn("Kit with zero items: "+getName());
				return;
			}
			// Items
			for (int i = 0; i < itemStrings.size(); i++) {
				ItemBuilder item = shop.stringToItem(itemStrings.get(i), "kit " + getName());
				items.add(item);
				buttons.put(i, new ItemButton(item));
			}
		}

		// Sell/buy buttons
		if (details.isSet("price")) {
			buttons.put(shop.getInventorySize() - 1, new BuyButton(this));
		}
		if (details.isSet("sellPrice")) {
			buttons.put(shop.getInventorySize() - 2, new SellButton(this));
		}

		// Display item
		String buttonString = details.getString("button");
		if (buttonString != null && buttonString.length() > 0) {
			button = shop.stringToItem(buttonString, "button of kit " + getName());
		} else {
			button = items.get(0).copy();
		}
		if (button != null && !button.hasCustomName()) {
			button.setName("&6&l" + getName());
		}
		if (button != null) {
			button.addAction("Left click for details");
			button.hideAllAttributes();
		}
	}

	@Override
	public String toString() {
		return name;
	}

	////////// BUTTON ACTIONS
	@Override
	public ItemBuilder getButton() {
		return getButton(null);
	}

	@Override
	public ItemBuilder getButton(ShopSession session) {
		ItemBuilder result = button;
		if (session != null) {
			result = result.copy();
			boolean allowBuy = true;
			List<ShopFeature> list = new ArrayList<>(features.values());
			for (int i = list.size() - 1; i >= 0; i--) {
				result.addLore(list.get(i).getBuyStatusLine(session), true);
				allowBuy &= list.get(i).allowsBuy(session);
			}
			if (allowBuy) {
				result.addAction("Right click to buy");
			}
		}
		return result;
	}

	@Override
	public void onClick(ShopSession session, ShopSession.ClickAction action) {
		if (action == ShopSession.ClickAction.LEFT) {
			show(session);
			Utils.playSound(session.getPlayer(), "click", "ui.button.click", 0.5F, 1F);
		} else {
			buy(session);
			session.refreshView();
		}
	}

	////////// VIEW ACTIONS
	@Override
	public void clickItem(ShopSession session, ShopSession.ClickAction action, int slot) {
		Button button = buttons.get(slot);
		if (button != null) {
			button.onClick(session, action);
		}
	}

	/**
	 * Show the kit detail view
	 * @param session The session to show it to
	 */
	@Override
	public void show(ShopSession session) {
		session.setView(this);
		Inventory inventory = Bukkit.createInventory(null, shop.getInventorySize(), Utils.applyColors("&0&l"+name));
		// Create the inventory
		for (int key : buttons.keySet()) {
			inventory.setItem(key, buttons.get(key).getButton(session).getItemStack());
		}
		shop.addMenu(inventory, session);
		session.getPlayer().openInventory(inventory);
		plugin.getShop().addSession(session);
	}

	/**
	 * Get the detailed specification of the kit
	 * @return The ConfigurationSection with the kit details
	 */
	public ConfigurationSection getDetails() {
		return details;
	}

	/**
	 * Get the list of features
	 * @return The map with features
	 */
	public Map<String, ShopFeature> getFeatures() {
		return features;
	}

	/**
	 * Get the items defined for this kit
	 * @return The list of items defined for this kit
	 */
	public List<ItemBuilder> getItems() {
		return items;
	}

	/**
	 * Get the identifier of the kit, only used internally
	 * @return The identifier of the kit
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Place on new line
	 * @return true if the kit should be placed on a new line, otherwise false
	 */
	public boolean onNewLine() {
		return onNewLine;
	}

	/**
	 * Buy the kit
	 * @param session The sesstion to do it for
	 */
	public void buy(ShopSession session) {
		buy(session, null);
	}

	public void buy(ShopSession session, KitSign sign) {
		Player player = session.getPlayer();

		// Check features
		for(ShopFeature feature : features.values()) {
			if (!feature.allowsBuy(session)) {
				feature.indicateRestrictedBuy(session);
				Utils.playSound(session.getPlayer(), "anvil.land", "block.anvil.land", 0.4F, 0.8F);
				return;
			}
		}

		// Perform actions
		boolean fail = false;
		for(ShopFeature feature : features.values()) {
			if (!feature.executeBuy(session, sign)) {
				fail = true;
				break;
			}
		}
		if (fail) {
			plugin.message(session.getPlayer(), "shop-buyFailed", getName());
			return;
		}

		Utils.playSound(session.getPlayer(), "level.up", "entity.player.levelup", 0.7F, 1.5F);
		String cooldown = "";
		if (getCooldownFeature().getCooldown() > 60000) { // More than a minute
			cooldown = Message.fromKey("shop-cooldownInfo").replacements(getCooldownFeature().getRawCooldown()).getPlain();
		}
		double price = getPriceFeature().getPrice();
		String formatPrice = getPriceFeature().getFormattedPrice();
		if (sign != null) {
			formatPrice = sign.getFormattedPrice();
			price = sign.getPrice();
		}
		if (price > 0) {
			plugin.message(session.getPlayer(), "shop-boughtKit", getName(), formatPrice, session.getFormattedBalance(), cooldown);
		} else {
			plugin.message(session.getPlayer(), "shop-receivedKit", getName(), cooldown);
		}
		shop.increaseStatistic("bought." + getIdentifier());
	}


	/**
	 * Sell the kit
	 * @param session The sesstion to do it for
	 */
	public void sell(ShopSession session) {
		sell(session, null);
	}

	public void sell(ShopSession session, KitSign sign) {
		Player player = session.getPlayer();

		// Check features
		for(ShopFeature feature : features.values()) {
			if (!feature.allowsSell(session)) {
				feature.indicateRestrictedSell(session);
				Utils.playSound(session.getPlayer(), "anvil.land", "block.anvil.land", 0.4F, 0.8F);
				return;
			}
		}

		// Perform actions
		boolean fail = false;
		for(ShopFeature feature : features.values()) {
			if (!feature.executeSell(session, sign)) {
				fail = true;
				break;
			}
		}
		if (fail) {
			plugin.message(session.getPlayer(), "shop-sellFailed", getName());
			return;
		}

		Utils.playSound(session.getPlayer(), "level.up", "entity.player.levelup", 0.7F, 1.5F);
		double price = getPriceFeature().getSellPrice();
		String formatPrice = getPriceFeature().getFormattedSellPrice();
		/* Future sell sign support
		if (sign != null) {
			formatPrice = sign.getFormattedPrice();
			price = sign.getPrice();
		}
		*/
		plugin.message(session.getPlayer(), "shop-soldKit", getName(), formatPrice, session.getFormattedBalance());
		shop.increaseStatistic("sold." + getIdentifier());
	}

	/**
	 * Get the name of the kit
	 * @return The name of the kit
	 */
	public String getName() {
		return name;
	}


	////////// Get features
	public PriceFeature getPriceFeature() {
		return (PriceFeature) features.get("price");
	}

	public ItemsFeature getItemsFeature() {
		return (ItemsFeature) features.get("items");
	}

	public CooldownFeature getCooldownFeature() {
		return (CooldownFeature) features.get("cooldown");
	}

	public PermissionFeature getPermissionFeature() {
		return (PermissionFeature) features.get("permission");
	}

}
