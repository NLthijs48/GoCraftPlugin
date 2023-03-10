package me.wiefferink.gocraft.shop;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.Log;
import me.wiefferink.gocraft.features.Feature;
import me.wiefferink.gocraft.shop.buttons.Button;
import me.wiefferink.gocraft.tools.ItemBuilder;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Shop extends Feature {

	private GoCraft plugin;
	private Map<String, Kit> kits;
	private Map<UUID, ShopSession> shopSessions;
	private ConfigurationSection shopSection;
	private Map<Integer, Button> buttons;
	private Map<String, Category> categories;
	private Category homeCategory;
	private SignManager signManager;
	private int inventorySize;

	// Inventory action lists
	private List<InventoryAction> leftClick = new ArrayList<>(Arrays.asList(InventoryAction.COLLECT_TO_CURSOR, InventoryAction.PICKUP_ALL, InventoryAction.PLACE_ALL, InventoryAction.COLLECT_TO_CURSOR, InventoryAction.MOVE_TO_OTHER_INVENTORY));
	private List<InventoryAction> rightClick = new ArrayList<>(Arrays.asList(InventoryAction.DROP_ONE_CURSOR, InventoryAction.PICKUP_HALF, InventoryAction.PLACE_ONE));
	private List<InventoryAction> middleClick = new ArrayList<>(Collections.singleton(InventoryAction.CLONE_STACK));
	private List<InventoryAction> notOnOther = new ArrayList<>(Arrays.asList(InventoryAction.COLLECT_TO_CURSOR, InventoryAction.DROP_ALL_CURSOR, InventoryAction.DROP_ALL_SLOT, InventoryAction.DROP_ONE_CURSOR, InventoryAction.DROP_ONE_SLOT, InventoryAction.HOTBAR_MOVE_AND_READD, InventoryAction.HOTBAR_SWAP, InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_HALF, InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_SOME, InventoryAction.PLACE_ALL, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME, InventoryAction.SWAP_WITH_CURSOR, InventoryAction.UNKNOWN, InventoryAction.NOTHING, InventoryAction.MOVE_TO_OTHER_INVENTORY));
	private List<InventoryAction> notOnSelf = new ArrayList<>(Arrays.asList(InventoryAction.MOVE_TO_OTHER_INVENTORY, InventoryAction.UNKNOWN, InventoryAction.NOTHING, InventoryAction.COLLECT_TO_CURSOR));
	private static Map<String, Enchantment> enchantmentMap;

	static {
		enchantmentMap = new HashMap<>();
		for (Enchantment enchant : Enchantment.values()) {
			enchantmentMap.put(enchant.getName().toLowerCase(), enchant);
		}
		enchantmentMap.put("protection", Enchantment.PROTECTION_ENVIRONMENTAL);
		enchantmentMap.put("fireprotection", Enchantment.PROTECTION_FIRE);
		enchantmentMap.put("featherfalling", Enchantment.PROTECTION_FALL);
		enchantmentMap.put("blastprotection", Enchantment.PROTECTION_EXPLOSIONS);
		enchantmentMap.put("projectileprotection", Enchantment.PROTECTION_PROJECTILE);
		enchantmentMap.put("respiration", Enchantment.OXYGEN);
		enchantmentMap.put("aquaaffinity", Enchantment.WATER_WORKER);
		enchantmentMap.put("thorns", Enchantment.THORNS);
		enchantmentMap.put("depthstrider", Enchantment.DEPTH_STRIDER);
		enchantmentMap.put("sharpness", Enchantment.DAMAGE_ALL);
		enchantmentMap.put("smite", Enchantment.DAMAGE_UNDEAD);
		enchantmentMap.put("baneofarthropods", Enchantment.DAMAGE_ARTHROPODS);
		enchantmentMap.put("knockback", Enchantment.KNOCKBACK);
		enchantmentMap.put("fireaspect", Enchantment.FIRE_ASPECT);
		enchantmentMap.put("looting", Enchantment.LOOT_BONUS_MOBS);
		enchantmentMap.put("efficiency", Enchantment.DIG_SPEED);
		enchantmentMap.put("silktouch", Enchantment.SILK_TOUCH);
		enchantmentMap.put("unbreaking", Enchantment.DURABILITY);
		enchantmentMap.put("fortune", Enchantment.LOOT_BONUS_BLOCKS);
		enchantmentMap.put("power", Enchantment.ARROW_DAMAGE);
		enchantmentMap.put("punch", Enchantment.ARROW_KNOCKBACK);
		enchantmentMap.put("flame", Enchantment.ARROW_FIRE);
		enchantmentMap.put("infinity", Enchantment.ARROW_INFINITE);
		enchantmentMap.put("luckofthesea", Enchantment.LUCK);
		enchantmentMap.put("lure", Enchantment.LURE);
	}

	public Shop(GoCraft plugin) {
		this.plugin = plugin;
		shopSessions = new HashMap<>();
		buttons = new HashMap<>();
		kits = new HashMap<>();
		categories = new HashMap<>();
		shopSection = getConfig().getConfigurationSection("shop");

		// Check if we should really enable
		if (shopSection == null) { // Shop not in use
			return;
		}
		ConfigurationSection kitsSection = shopSection.getConfigurationSection("kits");
		if (kitsSection == null) {
			Log.warn("Kits section of the shop is empty!");
			return;
		}

		permission("placeKitSign", "Place kit shop signs to buy kits (for possibly another price)");
		permission("placeShopSign", "Place shop sign that opens the shop when clicked");
		permission("shop", "Use the item shop");
		command("shop", "Open the item shop", "/shop");
		listen();

		// Setup kits and start
		ConfigurationSection categoriesSection = shopSection.getConfigurationSection("categories");
		for (String kitString : kitsSection.getKeys(false)) {
			ConfigurationSection kitSection = kitsSection.getConfigurationSection(kitString);
			Kit kit = new Kit(kitSection, kitString, this);
			kits.put(kit.getIdentifier(), kit);
			String categoryString = kitSection.getString("categories");
			if (categoryString == null || categoryString.isEmpty()) {
				continue;
			}
			String[] categoryParts = categoryString.split(",( )?"); // Comma or comma-space separated
			for (String categoryPart : categoryParts) {
				Category category = categories.get(categoryPart);
				if (category == null) {
					ConfigurationSection categorySection = null;
					if (categoriesSection != null) {
						categorySection = categoriesSection.getConfigurationSection(categoryPart);
					}
					if (categorySection == null) {
						Log.warn("Category "+categoryPart+" specified in kit "+kitString+" not found in the categories list!");
						continue;
					}
					category = new Category(categorySection, this);
					categories.put(categoryPart, category);
				}
				category.addKit(kit);
			}
		}

		// Set the home category to the first one in the config
		if (categoriesSection != null) {
			for (String key : categoriesSection.getKeys(false)) {
				if (categories.get(key) != null) {
					homeCategory = categories.get(key);
					break;
				}
			}
		}

		// Build main shop GUI
		int maxItems = 0;
		for (Category category : categories.values()) {
			maxItems = Math.max(maxItems, category.getRequiredSize());
		}
		inventorySize = (int) (Math.ceil(((double) maxItems) / 9.0) + 2) * 9; // Rows required by biggest category + 2 (one spacer, one menu bar)
		// Close button
		int current = inventorySize - 9;
		if (categoriesSection != null) {
			for (String category : categoriesSection.getKeys(false)) {
				Category actualCategory = categories.get(category);
				if (actualCategory == null) {
					continue;
				}
				buttons.put(current, actualCategory);
				current++;
			}
		}

		// Call setup (inventory size is known at this point)
		for (Kit kit : kits.values()) {
			kit.setup();
		}

		// Register localstorage cleaners
		GoCraft.getInstance().registerLocalStorageCleaner("kitcooldowns", config -> {
			boolean save = false;
			ConfigurationSection playersSection = config.getConfigurationSection("players");
			if (playersSection != null) {
				long currentTime = Calendar.getInstance().getTimeInMillis();
				for (String playerKey : playersSection.getKeys(false)) {
					ConfigurationSection cooldownSection = playersSection.getConfigurationSection(playerKey + ".shop.cooldowns");
					if (cooldownSection != null) {
						for (String kitKey : cooldownSection.getKeys(false)) {
							if (cooldownSection.getLong(kitKey) <= currentTime) {
								cooldownSection.set(kitKey, null);
								save = true;
							}
						}
						if (cooldownSection.getKeys(false).size() == 0) {
							playersSection.set(playerKey + ".shop.cooldowns", null);
							save = true;
						}
						if (playersSection.getConfigurationSection(playerKey + ".shop").getKeys(false).size() == 0) {
							playersSection.set(playerKey + ".shop", null);
							save = true;
						}
					}
					ConfigurationSection playerSection = playersSection.getConfigurationSection(playerKey);
					if (playerSection.getKeys(false).size() == 0) {
						playersSection.set(playerKey, null);
						save = true;
					}
				}
			}
			return save;
		});

		// Needs to be after contructor completion
		sync(() -> signManager = new SignManager());
	}

	@Override
	public void onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(plugin.getShop() == null) {
			plugin.message(sender, "shop-notEnabled");
			return;
		}

		if(!(sender instanceof Player)) {
			plugin.message(sender, "general-playerOnly");
			return;
		}
		Player player = (Player)sender;
		plugin.getShop().open(player);
	}

	/**
	 * Get the sign manager
	 * @return The sign manager
	 */
	public SignManager getSignManager() {
		return signManager;
	}

	/**
	 * Get the size of the shop view
	 * @return The size of the shop view
	 */
	public int getInventorySize() {
		return inventorySize;
	}

	/**
	 * Open the kit shop for the player
	 * @param player The player to open the shop for
	 */
	public void open(Player player) {
		if (!player.hasPermission("gocraft.shop")) {
			plugin.message(player, "shop-noPermission");
			return;
		}
		if (player.getGameMode() == GameMode.CREATIVE) {
			plugin.message(player, "shop-notSurvival");
			return;
		}
		if (Utils.isInPvpArea(player)) {
			plugin.message(player, "shop-notInPVP");
			return;
		}
		ShopSession session = new ShopSession(player);
		if (homeCategory != null) {
			homeCategory.show(session);
			plugin.increaseStatistic("shop.opened");
		} else {
			plugin.message(player, "shop-noHomeCategory");
		}
	}

	/**
	 * Add common buttons on the bottom row
	 * @param inventory The inventory to apply the buttons to
	 */
	public void addMenu(Inventory inventory, ShopSession session) {
		for (Integer key : buttons.keySet()) {
			Button button = buttons.get(key);
			if (button == null) {
				Log.warn("Button with key "+key+" is null");
				continue;
			}
			ItemBuilder itemBuilder = button.getButton(session);
			if (itemBuilder == null) {
				Log.warn("ItemBuilder provided by button with key "+key+" is null");
				continue;
			}
			itemBuilder = itemBuilder.copy();
			if (buttons.get(key) instanceof Category) {
				if (buttons.get(key).equals(session.getLastCategory())) {
					itemBuilder.setAmount(11);
					itemBuilder.addLore("&9&l>Current category<");
				} else {
					itemBuilder.addAction("View this category");
				}
			}
			itemBuilder.hideAllAttributes();
			inventory.setItem(key, itemBuilder.getItemStack());
		}
	}

	/**
	 * Try to click a menu button
	 * @param slot The slot to click
	 * @param action The click action
	 * @param session The session to click it in
	 * @return true if a button is found and clicked, otherwise false
	 */
	public boolean clickMenu(int slot, ShopSession.ClickAction action, ShopSession session) {
		if (buttons.containsKey(slot)) {
			buttons.get(slot).onClick(session, action);
			return true;
		}
		return false;
	}

	/**
	 * Add a shop session to the shop
	 * @param session The session to add
	 */
	public void addSession(ShopSession session) {
		ShopSession oldSession = shopSessions.put(session.getPlayer().getUniqueId(), session);
		if(oldSession != null) {
			Log.warn("Found old ShopSession while adding a new one, player:", session.getPlayer().getName());
			oldSession.close();
		}
	}

	/**
	 * Remove the shop session
	 * @param session The session to remove
	 */
	public void removeSession(ShopSession session) {
		shopSessions.remove(session.getPlayer().getUniqueId());
	}

	/**
	 * Inventory closing, remove the ShopSession
	 * @param event The InventoryCloseEvent
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onInventoryClose(InventoryCloseEvent event) {
		shopSessions.remove(event.getPlayer().getUniqueId());
	}

	// Block certain actions that cause a starter item to be moved to another inventory
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onInventoryClick(InventoryClickEvent event) {
		ShopSession session = shopSessions.get(event.getWhoClicked().getUniqueId());
		if (session == null) {
			return;
		}

		// Close inventory when clicking outside
		if (event.getRawSlot() == -999 && event.getAction() == org.bukkit.event.inventory.InventoryAction.NOTHING) {
			event.getWhoClicked().closeInventory();
		}

		// Detect and prevent moving items around
		if (event.getClickedInventory() != null && (event.getClickedInventory().getType() == InventoryType.PLAYER)) {
			if (notOnSelf.contains(event.getAction())) {
				event.setCancelled(true);
			}
		} else {
			if (notOnOther.contains(event.getAction())) {
				event.setCancelled(true);
			}
		}

		// Perform right/left click
		if (event.getClickedInventory() != event.getView().getTopInventory()) {
			return;
		}
		if (leftClick.contains(event.getAction())) {
			session.clickSlot(event.getSlot(), ShopSession.ClickAction.LEFT);
		} else if (rightClick.contains(event.getAction())) {
			session.clickSlot(event.getSlot(), ShopSession.ClickAction.RIGHT);
		} else if (middleClick.contains(event.getAction())) {
			session.clickSlot(event.getSlot(), ShopSession.ClickAction.MIDDLE);
		}
	}

	// Prevent dragging items to other inventory
	@EventHandler(ignoreCancelled = true)
	public void onItemDrag(InventoryDragEvent event) {
		if (shopSessions.get(event.getWhoClicked().getUniqueId()) == null) {
			return;
		}
		if (event.getInventory().getType() != InventoryType.PLAYER) {
			int size = event.getView().getTopInventory().getSize();
			boolean cancel = false;
			for (int slot : event.getRawSlots()) {
				cancel |= slot < size;
			}
			event.setCancelled(cancel);
			//debug("  dragevent cancelled "+cancel+" type=" + event.getView().getTopInventory().getType() + ", " + event.getInventory().getType()+", slots="+ event.getRawSlots()+", thing="+event.getNewItems().keySet().toString());
		}
	}

	/**
	 * Call when server is stopping
	 */
	public void handleServerStop() {
		new HashSet<>(shopSessions.values())
			.forEach(shopSession -> shopSession.getPlayer().closeInventory());
		shopSessions.clear();
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onPlayerLeave(PlayerQuitEvent event) {
		ShopSession session = getShopSession(event.getPlayer().getUniqueId());
		if(session != null) {
			removeSession(session);
		}
	}

	/**
	 * Get a ShopSession for a player
	 * @param player The UUID of the player to get the ShopSession for
	 */
	public ShopSession getShopSession(UUID player) {
		return shopSessions.get(player);
	}

	/**
	 * Get the config of the shop
	 * @return The config of the shop
	 */
	public ConfigurationSection getShopSection() {
		return shopSection;
	}

	/**
	 * Get the kits defined in the shop
	 * @return The kits defined for this shop
	 */
	public Map<String, Kit> getKits() {
		return kits;
	}

	/**
	 * Increase a statistic for tracking shop usage
	 * @param key The key to track it with
	 */
	public void increaseStatistic(String key) {
		GoCraft.getInstance().increaseStatistic("shop." + key);
	}

	/**
	 * Translate item string input to a ItemBuilder instance
	 * @param itemString The itemstring to convert to an item
	 * @return The ItemBuilder build from the itemString
	 */
	public ItemBuilder stringToItem(String itemString, String debugId) {
		ItemBuilder result = null;
		String[] parts = itemString.split(", ");
		for (int i = 0; i < parts.length; i++) {
			// Item Id
			if (i == 0) {
				int id = 166;
				int data = 0;
				String[] split = parts[i].split(":");
				if (split.length < 1) {
					Log.warn("  No id provided for "+debugId);
					return null;
				}
				try {
					id = Integer.parseInt(split[0]);
				} catch (NumberFormatException e) {
					Log.warn("  Incorrect item id "+split[0]+" for "+debugId);
				}
				if (split.length > 1) {
					try {
						data = Integer.parseInt(split[1]);
					} catch (NumberFormatException e) {
						Log.warn("  Incorrect data value "+split[1]+" for "+debugId);
					}
				}
				result = new ItemBuilder(id, 1, data);
			}
			// Optional item amount
			else if (i == 1 && Utils.isNumeric(parts[i])) {
				result.setAmount(Integer.parseInt(parts[i]));
			}
			// Other attributes
			else {
				String[] split = parts[i].split(":");
				if (split.length < 1) {
					Log.warn("  Incorrect attribute: "+parts[i]+" for "+debugId);
					continue;
				}
				String identifier = split[0];
				String value = "";
				if (split.length >= 2) {
					value = split[1];
				}
				if ("name".equalsIgnoreCase(identifier)) {
					result.setName(ChatColor.DARK_GREEN + value);
				} else if ("color".equalsIgnoreCase(identifier)) {
					if (split.length <= 3) {
						Log.warn("  Not enough numbers for the color attribute for", debugId+": "+parts[i]);
					} else {
						int red, green, blue;
						try {
							red = Integer.parseInt(split[1]);
							green = Integer.parseInt(split[2]);
							blue = Integer.parseInt(split[3]);
							result.setColor(red, green, blue);
						} catch (NumberFormatException e) {
							Log.warn("  Color part is not a number for", debugId+": "+parts[i]);
						}
					}
				} else if ("lore".equalsIgnoreCase(identifier)) {
					if (split.length < 2) {
						Log.warn("  No arguments for lore for", debugId);
					} else {
						result.addLore(Utils.combineFrom(split, 1, ":"));
					}
				} else if ("action".equalsIgnoreCase(identifier)) {
					if (split.length < 2) {
						Log.warn("  No arguments for action for", debugId);
					} else {
						result.addAction(Utils.combineFrom(split, 1, ":"));
					}
				} else if("potion".equalsIgnoreCase(identifier)) {
					if(split.length < 2) {
						Log.warn("  No arguments for potion for", debugId);
					} else {
						boolean extended = (split.length > 2 && split[2] != null && split[2].equals("true"));
						boolean upgraded = (split.length > 3 && split[3] != null && split[3].equals("true"));
						try {
							PotionType potionType = PotionType.valueOf(split[1].toUpperCase());
							result.setPotionType(potionType, extended, upgraded);
						} catch(IllegalArgumentException e) {
							Log.warn("Incorrect potionType:", identifier, "for", debugId);
						}
					}
				} else if (enchantmentMap.containsKey(identifier.toLowerCase())) {
					int number = 1;
					if (value != null && value.length() > 0) {
						try {
							number = Integer.parseInt(value);
						} catch (NumberFormatException ignored) {
						}
					}
					result.addEnchantment(enchantmentMap.get(identifier.toLowerCase()), number);
				} else {
					Log.warn("  Unknown identifier: "+identifier+" (with value '"+value+"') for "+debugId);
				}
			}
		}
		return result;
	}
}
