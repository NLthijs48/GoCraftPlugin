package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import me.wiefferink.gocraft.utils.Utils;

import java.util.Calendar;

public class CooldownFeature extends Feature {

	private long cooldown;
	private String rawCooldown;

	public CooldownFeature(Kit kit) {
		super(kit);
		rawCooldown = kit.getDetails().getString("cooldown");
		if (rawCooldown != null && !Utils.checkDuration(rawCooldown)) {
			GoCraft.getInstance().getLogger().warning("Cooldown of kit " + kit.getName() + " in wrong format: " + rawCooldown);
		}
		cooldown = Utils.durationStringToLong(rawCooldown);
	}

	@Override
	public boolean allows(ShopSession session) {
		return getCooldownLeft(session) == 0;
	}

	@Override
	public void indicateRestricted(ShopSession session) {
		GoCraft.getInstance().message(session.getPlayer(), "shop-cooldown", Utils.millisToHumanFormat(getCooldownLeft(session)));
	}

	@Override
	public boolean execute(ShopSession session) {
		if (hasCooldown()) {
			GoCraft.getInstance().getLocalStorage().set(
					"players." + session.getPlayer().getUniqueId().toString() + ".shop.cooldowns." + kit.getIdentifier(),
					Calendar.getInstance().getTimeInMillis() + getCooldown()
			);
			GoCraft.getInstance().saveLocalStorage();
		}
		return true;
	}

	@Override
	public String getStatusLine(ShopSession session) {
		if (session == null || !hasCooldown()) {
			return null;
		}
		// Check if a cooldown applies
		if (getCooldownLeft(session) == 0) {
			return "&2Cooldown: &l" + getRawCooldown();
		} else {
			return "&4Cooldown: &l" + Utils.millisToHumanFormat(getCooldownLeft(session)) + " left";
		}
	}

	/**
	 * Get leftover cooldown time
	 *
	 * @param session The session to calculate it for
	 * @return The leftover cooldown time
	 */
	public long getCooldownLeft(ShopSession session) {
		return Math.max(0, getCooldown(session) - Calendar.getInstance().getTimeInMillis());
	}

	/**
	 * Get the current cooldown time
	 *
	 * @param session The session to get it for
	 * @return The current cooldown time
	 */
	public long getCooldown(ShopSession session) {
		return GoCraft.getInstance().getLocalStorage().getLong(
				"players." + session.getPlayer().getUniqueId().toString() + ".shop.cooldowns." + kit.getIdentifier()
		);
	}

	/**
	 * Check if a cooldown is defined
	 *
	 * @return true if a cooldown applies, otherwise false
	 */
	public boolean hasCooldown() {
		return cooldown > 0;
	}

	/**
	 * Get the cooldown
	 *
	 * @return The cooldown time in milliseconds
	 */
	public long getCooldown() {
		return cooldown;
	}

	/**
	 * Get the raw cooldown as defined in the config
	 *
	 * @return The raw cooldown string
	 */
	public String getRawCooldown() {
		return rawCooldown;
	}
}
