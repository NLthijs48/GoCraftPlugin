package me.wiefferink.gocraft.utils;

import me.wiefferink.gocraft.GoCraft;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

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
		meta.setDisplayName(GoCraft.getInstance().fixColors(name));
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

}
