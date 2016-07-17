package me.wiefferink.gocraft.shop.signs;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.tools.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public abstract class Sign {

	ConfigurationSection details;
	String key;
	Location location;

	public enum ClickAction {
		LEFT, RIGHT
	}

	/**
	 * Constructor
	 * @param details The information about the sign
	 */
	public Sign(ConfigurationSection details, String key) {
		this.details = details;
		this.key = key;
		location = Utils.configToLocation(details.getConfigurationSection("location"));
	}

	/**
	 * Get the config key used for the sign data
	 * @return The config key
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Change the lines of a sign
	 * @param lines The lines to set on the sign
	 */
	public void setLines(String... lines) {
		Block block = location.getBlock();
		if (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
			GoCraft.getInstance().getShop().getSignManager().removeSign(this);
			return;
		}
		org.bukkit.block.Sign signState = (org.bukkit.block.Sign) block.getState();
		for (int i = 0; i < lines.length && i < 4; i++) {
			if (lines[i] == null || lines[i].contains("null")) { // Bit of a hack...
				continue;
			}
			signState.setLine(i, Utils.applyColors(lines[i]));
		}
		signState.update();
	}

	/**
	 * Update the contents of the sign
	 */
	public abstract void update();

	/**
	 * Handle a click event by a player
	 * @param clickAction The action to executeBuy
	 * @param player The player that clicked
	 */
	public abstract void handleClicked(ClickAction clickAction, Player player);

	/**
	 * Get the location of the sign
	 * @return The location of the sign
	 */
	public Location getLocation() {
		return location;
	}
}
