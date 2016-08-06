package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.shop.signs.KitSign;

public abstract class ShopFeature {

	Kit kit;

	public ShopFeature(Kit kit) {
		this.kit = kit;
	}

	Kit getKit() {
		return kit;
	}

	/**
	 * Indicates if buying/selling this kit is allowed for this session
	 * @param session The session to check it for
	 * @return true if buying the kit is allowed by this feature, otherwise false
	 */
	public boolean allows(ShopSession session) {
		return true;
	}
	/**
	 * Indicates if buying this kit is allowed for this session
	 * @param session The session to check it for
	 * @return true if buying the kit is allowed by this feature, otherwise false
	 */
	public boolean allowsBuy(ShopSession session) {
		return allows(session);
	}

	/**
	 * Indicates if selling this kit is allowed for this session
	 * @param session The session to check it for
	 * @return true if selling the kit is allowed by this feature, otherwise false
	 */
	public boolean allowsSell(ShopSession session) {
		return allows(session);
	}

	/**
	 * Indicate to the player why buying/selling is restricted, should work if ShopFeature#allowsBuy() returns false
	 * @param session The session to tell it to
	 */
	public void indicateRestricted(ShopSession session) {
	}

	/**
	 * Indicate to the player why buying is restricted, should work if ShopFeature#allowsBuy() returns false
	 * @param session The session to tell it to
	 */
	public void indicateRestrictedBuy(ShopSession session) {
		indicateRestricted(session);
	}

	/**
	 * Indicate to the player why selling is restricted, should work if ShopFeature#allowsBuy() returns false
	 * @param session The session to tell it to
	 */
	public void indicateRestrictedSell(ShopSession session) {
		indicateRestricted(session);
	}

	/**
	 * Perform the actions required when the kit is actually bought
	 * @param session The session to perform it for
	 * @param sign The sign that it is executed for or null if none
	 * @return true if the execution succeeded, otherwise false
	 */
	public boolean executeBuy(ShopSession session, KitSign sign) {
		return true;
	}

	/**
	 * Perform the actions required when the kit is actually sold
	 * @param session The session to perform it for
	 * @param sign The sign that it is executed for or null if none
	 * @return true if the execution succeeded, otherwise false
	 */
	public boolean executeSell(ShopSession session, KitSign sign) {
		return true;
	}

	/**
	 * Get the statusline indicating the status of this feature
	 * @param session The session to create it for
	 * @return A string with the status of the feature, or null if none
	 */
	public String getStatusLine(ShopSession session) {
		return null;
	}
	/**
	 * Get the statusline indicating the status of this feature
	 * @param session The session to create it for
	 * @return A string with the status of the feature, or null if none
	 */
	public String getBuyStatusLine(ShopSession session) {
		return getStatusLine(session);
	}

	/**
	 * Get the statusline indicating the status of this feature
	 * @param session The session to create it for
	 * @return A string with the status of the feature, or null if none
	 */
	public String getSellStatusLine(ShopSession session) {
		return getStatusLine(session);
	}
}
