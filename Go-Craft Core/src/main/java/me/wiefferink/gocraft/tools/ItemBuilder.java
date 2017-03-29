package me.wiefferink.gocraft.tools;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.material.SpawnEgg;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class ItemBuilder {
	private ItemStack item;
	private boolean hasCustomName = false;

	// CONSTRUCTORS
	public ItemBuilder(Material material) {
		this(material, 1);
	}

	public ItemBuilder(Material material, int amount) {
		this(material, amount, 0);
	}

	public ItemBuilder(Material material, int amount, int data) {
		this(new ItemStack(material, amount, (short)data), false);
	}

	public ItemBuilder(int material) {
		this(material, 1);
	}

	public ItemBuilder(int material, int amount) {
		this(material, amount, 0);
	}

	public ItemBuilder(int material, int amount, int data) {
		//noinspection deprecation
		this(new ItemStack(material, amount, (short)data), false);
	}

	public ItemBuilder(ItemStack item) {
		this(item, true);
	}

	private ItemBuilder(ItemStack item, boolean clone) {
		if(clone) {
			item = item.clone();
		} else {
			if(item.getType() == Material.MONSTER_EGG) {
				@SuppressWarnings("deprecation") SpawnEgg egg = new SpawnEgg((byte)item.getDurability());
				item = egg.toItemStack(item.getAmount());
			} else if(!Bukkit.getBukkitVersion().startsWith("1.8")
					&& (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION)) {
				try {
					@SuppressWarnings("deprecation") Potion potion = Potion.fromDamage(item.getDurability());
					item = potion.toItemStack(item.getAmount());
				} catch(IllegalArgumentException e) {
					Log.warn("Wrong potion: type:", item.getType(), "durability:", item.getDurability());
				}
			}
		}
		this.item = item;
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
		if (meta != null) {
			meta.setDisplayName(Utils.applyColors("&r"+name));
			item.setItemMeta(meta);
		}
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
	 * @param level The level of the enchantment to add
	 * @return this
	 */
	public ItemBuilder addEnchantment(Enchantment enchantment, int level) {
		item.addUnsafeEnchantment(enchantment, level);
		return this;
	}

	/**
	 * Add an enchantment to the item with level 1
	 * @param enchantment The enchantment to add
	 * @return this
	 */
	public ItemBuilder addEnchantment(Enchantment enchantment) {
		return addEnchantment(enchantment, 1);
	}

	/**
	 * Add glow to an item without an enchantment
	 * @return this
	 */
	public ItemBuilder addGlow() {
		item = GoCraft.getInstance().getSpecificUtils().addGlow(item);
		return this;
	}

	/**
	 * Add flags to the item
	 * @param itemFlags The flags to add
	 * @return this
	 */
	public ItemBuilder addFlags(ItemFlag... itemFlags) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			meta.addItemFlags(itemFlags);
			item.setItemMeta(meta);
		}
		return this;
	}

	/**
	 * Hide all attributes from the item
	 * @return this
	 */
	public ItemBuilder hideAllAttributes() {
		addFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_DESTROYS, ItemFlag.HIDE_PLACED_ON, ItemFlag.HIDE_POTION_EFFECTS, ItemFlag.HIDE_UNBREAKABLE);
		return this;
	}

	/**
	 * Set the color of the item in case it is leather
	 * @param red The red color intensity
	 * @param green The green color intensity
	 * @param blue The blue color intensity
	 * @return this
	 */
	public ItemBuilder setColor(int red, int green, int blue) {
		ItemMeta meta = item.getItemMeta();
		if (meta != null && meta instanceof LeatherArmorMeta) {
			try {
				((LeatherArmorMeta) meta).setColor(Color.fromRGB(red, green, blue));
			} catch (IllegalArgumentException ignored) {
			}
			item.setItemMeta(meta);
		}
		return this;
	}

	/**
	 * Add a lore
	 * @param lore The string to add as lore
	 * @return this
	 */
	public ItemBuilder addLore(String lore) {
		return addLore(lore, false);
	}

	/**
	 * Add a lore
	 * @param lore The string to add as lore
	 * @param asFirst true if it should appear as first lore, otherwise false
	 * @return this
	 */
	public ItemBuilder addLore(String lore, boolean asFirst) {
		if (lore == null) {
			return this;
		}
		ItemMeta meta = item.getItemMeta();
		if (meta != null) {
			List<String> lores = meta.getLore();
			if (lores == null) {
				lores = new ArrayList<>();
			}
			String[] loreParts = lore.split("\n");
			int index = 0;
			for (String lorePart : loreParts) {
				lorePart = Utils.applyColors("&r"+lorePart);
				if (asFirst) {
					lores.add(index, lorePart);
					index++;
				} else {
					lores.add(lorePart);
				}
			}
			meta.setLore(lores);
			item.setItemMeta(meta);
		}
		return this;
	}


	/**
	 * Set the potion type of potions and tipped arrows
	 * @param type The potion type
	 * @return this
	 */
	public ItemBuilder setPotionType(PotionType type) {
		return setPotionType(type, false, false);
	}

	/**
	 * Set the potion type of potions and tipped arrows
	 * @param type    The potion type
	 * @param extend  Extend the potion
	 * @param upgrade Upgrade the potion
	 * @return this
	 */
	public ItemBuilder setPotionType(PotionType type, boolean extend, boolean upgrade) {
		if(type == null || !(item.getType() == Material.POTION || item.getType() == Material.TIPPED_ARROW)) {
			return this;
		}
		PotionMeta potionMeta = (PotionMeta)item.getItemMeta();
		potionMeta.setBasePotionData(new PotionData(type, extend, upgrade));
		item.setItemMeta(potionMeta);
		return this;
	}

	/**
	 * Add an action text to an item, used for menu items
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
	 * @return true if the item has a custom name, otherwise false
	 */
	public boolean hasCustomName() {
		return hasCustomName;
	}

	/**
	 * Copy the ItemBuilder
	 * @return A copy of this ItemBuilder
	 */
	public ItemBuilder copy() {
		return new ItemBuilder(item.clone());
	}

}
