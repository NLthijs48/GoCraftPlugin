package me.wiefferink.gocraft.information;

import me.wiefferink.gocraft.GoCraft;

/**
 * Provider of information about a player
 */
public abstract class InformationProvider {

	public static GoCraft plugin = GoCraft.getInstance();

	/**
	 * Show the information to the sender (executed sync)
	 * @param request The information request to show messages for
	 */
	public void showSync(InformationRequest request) {}

	/**
	 * Show the information to the sender (executed async)
	 * @param request The information request to show messages for
	 */
	public void showAsync(InformationRequest request) {}
}
