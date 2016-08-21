package me.wiefferink.gocraft.shop;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.signs.KitSign;
import me.wiefferink.gocraft.shop.signs.ShopSign;
import me.wiefferink.gocraft.shop.signs.Sign;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;

public class SignManager implements Listener {

	private GoCraft plugin;
	private Map<String, Sign> signs;

	public SignManager() {
		plugin = GoCraft.getInstance();
		signs = new HashMap<>();
		Bukkit.getServer().getPluginManager().registerEvents(this, plugin);
		new BukkitRunnable() {
			@Override
			public void run() {
				importSigns();
			}
		}.runTask(plugin);
	}

	/**
	 * Import the signs that are defined in the config
	 */
	public void importSigns() {
		// Import signs
		ConfigurationSection signsSection = plugin.getLocalStorage().getConfigurationSection("shop.signs");
		if (signsSection != null) {
			for (String signKey : signsSection.getKeys(false)) {
				Sign sign;
				ConfigurationSection details = signsSection.getConfigurationSection(signKey);
				String type = details.getString("type");
				if ("kit".equals(type)) {
					Kit kit = plugin.getShop().getKits().get(details.getString("kit"));
					if (kit == null) {
						GoCraft.warn("Kit of sign at "+details.getString("location.x")+", "+details.getString("location.y")+", "+details.getString("location.z")+" does not exist: "+details.getString("kit"));
						continue;
					}
					sign = new KitSign(details, signKey, kit);
				} else if ("shop".equals(type)) {
					sign = new ShopSign(details, signKey);
				} else {
					GoCraft.warn("Incorrect sign type for key "+signKey+": "+type);
					continue;
				}
				signs.put(Utils.locationToString(sign.getLocation()), sign);
				sign.update();
			}
		}
	}

	/**
	 * Add a sign to the shop
	 */
	public void addSign(Sign sign) {
		signs.put(Utils.locationToString(sign.getLocation()), sign);
		sign.update();
	}

	/**
	 * Remove a sign from the shop
	 */
	public void removeSign(Sign sign) {
		signs.remove(Utils.locationToString(sign.getLocation()));
		// Remove from the config
		plugin.getLocalStorage().set("shop.signs." + sign.getKey(), null);
		plugin.saveLocalStorage();
	}

	/**
	 * Get a sign by location
	 * @return The sign at the given locaiton, or null if there is none
	 */
	public Sign getSign(Location location) {
		return signs.get(Utils.locationToString(location));
	}

	/**
	 * Handle players clicking the sign and forward the event to the sign itself
	 * @param event The event
	 */
	@EventHandler(ignoreCancelled = true)
	public void onPlayerInteract(PlayerInteractEvent event) {
		if (event.isCancelled()) {
			return;
		}
		Block block = event.getClickedBlock();
		// Check for clicking a sign and rightclicking
		if ((event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK)
				&& (block.getType() == Material.SIGN_POST || block.getType() == Material.WALL_SIGN)) {
			Sign sign = getSign(event.getClickedBlock().getLocation());
			if (sign != null) {
				Sign.ClickAction action = Sign.ClickAction.LEFT;
				if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
					action = Sign.ClickAction.RIGHT;
				}
				sign.handleClicked(action, event.getPlayer());
				event.setCancelled(true);
			}
		}
	}

	/**
	 * Check for placing new signs
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onSignPlace(SignChangeEvent event) {
		if (event.getLines().length == 0 || event.getLine(0) == null) {
			return;
		}

		if (event.getLine(0).equalsIgnoreCase("[gckit]")) {
			if (!event.getPlayer().hasPermission("gocraft.placeKitSign")) {
				plugin.message(event.getPlayer(), "shop-kitSignNoPermission");
				return;
			}

			if (event.getLines().length < 2 || event.getLine(1) == null || plugin.getShop().getKits().get(event.getLine(1)) == null) {
				plugin.message(event.getPlayer(), "shop-kitSignNoKit");
				return;
			}
			Kit kit = plugin.getShop().getKits().get(event.getLine(1));

			Double price = null;
			if (event.getLines().length >= 3 && event.getLine(2) != null && event.getLine(2).length() > 0) {
				try {
					price = Double.parseDouble(event.getLine(2));
				} catch (NumberFormatException e) {
					plugin.message(event.getPlayer(), "shop-kitSignWrongPrice", event.getLine(2));
					return;
				}
			}

			int number = 0;
			while (plugin.getLocalStorage().isConfigurationSection("shop.signs." + number)) {
				number++;
			}

			String path = "shop.signs." + number;
			plugin.getLocalStorage().set(path + ".kit", kit.getIdentifier());
			plugin.getLocalStorage().set(path + ".type", "kit");
			plugin.getLocalStorage().set(path + ".location", Utils.locationToConfig(event.getBlock().getLocation()));
			if (price != null) {
				plugin.getLocalStorage().set(path + ".price", price);
				plugin.message(event.getPlayer(), "shop-kitSignSuccessPrice", kit.getName(), Utils.formatCurrency(price));
			} else {
				plugin.message(event.getPlayer(), "shop-kitSignSuccess", kit.getName());
			}
			KitSign sign = new KitSign(plugin.getLocalStorage().getConfigurationSection(path), number + "", kit);
			addSign(sign);
			plugin.saveLocalStorage();
			event.setCancelled(true);
		} else if (event.getLine(0).equalsIgnoreCase("[gcshop]")) {
			if (!event.getPlayer().hasPermission("gocraft.placeShopSign")) {
				plugin.message(event.getPlayer(), "shop-shopSignNoPermission");
				return;
			}

			int number = 0;
			while (plugin.getLocalStorage().isConfigurationSection("shop.signs." + number)) {
				number++;
			}

			String path = "shop.signs." + number;
			plugin.getLocalStorage().set(path + ".type", "shop");
			plugin.getLocalStorage().set(path + ".location", Utils.locationToConfig(event.getBlock().getLocation()));
			plugin.message(event.getPlayer(), "shop-shopSignSuccess");
			ShopSign sign = new ShopSign(plugin.getLocalStorage().getConfigurationSection(path), number + "");
			addSign(sign);
			plugin.saveLocalStorage();
			event.setCancelled(true);
		}
	}
}
