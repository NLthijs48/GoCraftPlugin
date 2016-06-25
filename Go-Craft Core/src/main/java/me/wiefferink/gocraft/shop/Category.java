package me.wiefferink.gocraft.shop;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.buttons.Button;
import me.wiefferink.gocraft.tools.ItemBuilder;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class Category implements View, Button {

	private Map<Integer, Kit> kits;
	private Shop shop;
	private ItemBuilder button;
	private String name;
	private int currentSlot;

	public Category(ConfigurationSection information, Shop shop) {
		this.shop = shop;
		currentSlot = 0;
		kits = new HashMap<>();
		name = information.getString("name");
		String buttonString = information.getString("button");
		if (buttonString != null && buttonString.length() > 0) {
			button = shop.stringToItem(buttonString, "button of category " + getName());
		}
		if (button != null && !button.hasCustomName()) {
			button.setName("&2" + getName());
		}
	}

	/**
	 * Get the name of the category
	 * @return The name of the category
	 */
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public boolean equals(Object object) {
		return object != null && object instanceof Category && ((Category) object).getName().equals(getName());
	}

	/**
	 * Get the kit present in this category
	 * @return A list with kits in this category
	 */
	public Map<Integer, Kit> getKits() {
		return kits;
	}

	/**
	 * Required space to place the kits in the inventory
	 * @return The number of required slots
	 */
	public int getRequiredSize() {
		return currentSlot;
	}

	/**
	 * Add a kit to the category
	 * @param kit The kit to add
	 */
	public void addKit(Kit kit) {
		if (kit.onNewLine()) {
			currentSlot = (int) (Math.ceil(((double) currentSlot) / 9) * 9);
		}
		kits.put(currentSlot, kit);
		currentSlot++;
		if (button == null) {
			button = kit.getButton();
		}
	}

	/**
	 * Show the main shop view
	 * @param session The session to show it to
	 */
	@Override
	public void show(ShopSession session) {
		session.setView(this);
		Inventory inventory = Bukkit.createInventory(null, shop.getInventorySize(), Utils.fixColors("&0&l" + getName()));
		// Create the inventory
		for (int key : kits.keySet()) {
			ItemBuilder itemBuilder = kits.get(key).getButton(session);
			if (itemBuilder != null) {
				inventory.setItem(key, itemBuilder.getItemStack());
			} else {
				GoCraft.debug("No button item for item " + key + " in main shop GUI");
			}
		}
		shop.addMenu(inventory, session);
		session.getPlayer().openInventory(inventory);
		shop.addSession(session);
	}


	@Override
	public void clickItem(ShopSession session, ShopSession.ClickAction action, int slot) {
		Button button = kits.get(slot);
		if (button != null) {
			button.onClick(session, action);
		}
	}

	@Override
	public ItemBuilder getButton() {
		return getButton(null);
	}

	@Override
	public ItemBuilder getButton(ShopSession session) {
		return button;
	}

	@Override
	public void onClick(ShopSession session, ShopSession.ClickAction action) {
		show(session);
		Utils.playSound(session.getPlayer(), "click", "ui.button.click", 0.5F, 1F);
	}
}
