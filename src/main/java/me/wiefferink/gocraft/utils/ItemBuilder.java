package me.wiefferink.gocraft.utils;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
	private ItemStack item;
	private boolean hasCustomName = false;

	// CONSTRUCTORS
	public ItemBuilder(Material material) {
		item = new ItemStack(material);
	}

	public ItemBuilder(Material material, int amount) {
		item = new ItemStack(material, amount);
	}

	public ItemBuilder(Material material, int amount, int data) {
		item = new ItemStack(material, amount, (short)data);
	}

	public ItemBuilder(int material) {
		item = new ItemStack(material);
	}

	public ItemBuilder(int material, int amount) {
		item = new ItemStack(material, amount);
	}

	public ItemBuilder(int material, int amount, int data) {
		item = new ItemStack(material, amount, (short)data);
	}

	public ItemBuilder(ItemStack item) {
		this.item = item.clone();
	}

	// METHODS

	/**
	 * Set the name of the item
	 * @param name The name of the item
	 * @return this
	 */
	public ItemBuilder setName(String name) {
		hasCustomName = true;
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(GoCraft.getInstance().fixColors("&r" + name));
		item.setItemMeta(meta);
		return this;
	}

	/**
	 * Set the amount that the item has
	 * @param amount The amount to set
	 * @return this
	 */
	public ItemBuilder setAmount(int amount) {
		item.setAmount(amount);
		return this;
	}

	/**
	 * Set the data value
	 *
	 * @param data The data value to set
	 * @return this
	 */
	public ItemBuilder setData(int data) {
		item.setDurability((short) data);
		return this;
	}

	/**
	 * Add an enchantment to the item
	 * @param enchantment The enchantment to add
	 * @param level       The level of the enchantment to add
	 * @return this
	 */
	public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
		item.addUnsafeEnchantment(enchantment, level);
		return this;
	}


	/**
	 * Add a lore
	 *
	 * @param lore The string to add as lore
	 * @return this
	 */
	public ItemBuilder addLore(String lore) {
		return addLore(lore, false);
	}

	/**
	 * Add a lore
	 *
	 * @param lore    The string to add as lore
	 * @param asFirst true if it should appear as first lore, otherwise false
	 * @return this
	 */
	public ItemBuilder addLore(String lore, boolean asFirst) {
		ItemMeta meta = item.getItemMeta();
		if (meta == null) {
			GoCraft.debug("Could not add lore to item, no itemmeta: " + lore);
			return this;
		}
		List<String> lores = meta.getLore();
		if (lores == null) {
			lores = new ArrayList<>();
		}
		lore = GoCraft.getInstance().fixColors("&r" + lore);
		if (asFirst) {
			lores.add(0, lore);
		} else {
			lores.add(lore);
		}
		meta.setLore(lores);
		item.setItemMeta(meta);
		return this;
	}

	/**
	 * Add an action text to an item, used for menu items
	 *
	 * @param action The action string
	 * @return this
	 */
	public ItemBuilder addAction(String action) {
		return addLore(ChatColor.BLUE + "&l<" + action + ">");
	}

	/**
	 * Get the ItemStack produced by the builder
	 * @return The ItemStack produced by the builder
	 */
	public ItemStack getItemStack() {
		return item;
	}

	/**
	 * Check if the item has a custom name
	 *
	 * @return true if the item has a custom name, otherwise false
	 */
	public boolean hasCustomName() {
		return hasCustomName;
	}

	/**
	 * Clone the ItemBuilder
	 *
	 * @return A clone of this ItemBuilder
	 */
	public ItemBuilder clone() {
		return new ItemBuilder(item.clone());
	}

}
