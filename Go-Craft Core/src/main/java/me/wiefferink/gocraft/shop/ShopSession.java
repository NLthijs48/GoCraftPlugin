package me.wiefferink.gocraft.shop;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.entity.Player;

public class ShopSession {

	private Shop shop;
	private Player player;
	private double balance;
	private View currentView;
	private Category lastCategory;

	public enum ClickAction {
		LEFT, RIGHT, MIDDLE
	}

	public static int inventorySize; // Initialized by Shop

	public ShopSession(Player player) {
		this.shop = GoCraft.getInstance().getShop();
		this.player = player;
		updateBalance();
	}

	/**
	 * Close the shop
	 */
	public void close() {
		player.closeInventory();
		shop.removeSession(this);
	}

	/**
	 * Update the balance of the player
	 */
	public void updateBalance() {
		balance = GoCraft.getInstance().getEconomy().getBalance(player);
	}

	/**
	 * Click a slot
	 * @param slot The slot that has been clicked
	 * @param action The action that has been performed
	 */
	public void clickSlot(int slot, ClickAction action) {
		if (!shop.clickMenu(slot, action, this)) {
			currentView.clickItem(this, action, slot);
		}
	}

	/**
	 * Get the player that opened the shop
	 * @return The player that is using the shop
	 */
	public Player getPlayer() {
		return player;
	}

	/**
	 * Get the balance of the player
	 */
	public double getBalance() {
		return balance;
	}

	/**
	 * Get a formatted line of the balance of the player
	 * @return Formatted line of the balance of the player
	 */
	public String getFormattedBalance() {
		return Utils.formatCurrency(balance);
	}

	/**
	 * Set the view that is currently displayed
	 * @param view The view that is currently displayed
	 */
	public void setView(View view) {
		currentView = view;
		if (view instanceof Category) {
			lastCategory = (Category) view;
		}
	}

	/**
	 * Get the current view of the session
	 */
	public View getView() {
		return currentView;
	}

	/**
	 * Refresh the current view
	 */
	public void refreshView() {
		currentView.show(this);
	}

	/**
	 * Get the last shown category
	 * @return The category last shown
	 */
	public Category getLastCategory() {
		return lastCategory;
	}
}
