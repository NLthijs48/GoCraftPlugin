package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;

public abstract class Feature {

	Kit kit;

	public Feature(Kit kit) {
		this.kit = kit;
	}

	Kit getKit() {
		return kit;
	}

	/**
	 * Indicates if buying this kit is allowed for this session
	 *
	 * @param session The session to check it for
	 * @return true if buying the kit is allowed by this feature, otherwise false
	 */
	public abstract boolean allows(ShopSession session);

	/**
	 * Indicate to the player why buying is restricted, should work if Feature#allows() returns false
	 *
	 * @param session The session to tell it to
	 */
	public abstract void indicateRestricted(ShopSession session);

	/**
	 * Perform the actions required when the kit is actually bought
	 *
	 * @param session The session to perform it for
	 * @return true if the execution succeeded, otherwise false
	 */
	public boolean execute(ShopSession session) {
		return true;
	}

	/**
	 * Get the statusline indicating the status of this feature
	 *
	 * @param session The session to create it for
	 * @return A string with the status of the feature, or null if none
	 */
	public String getStatusLine(ShopSession session) {
		return null;
	}
}
