package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public class PermissionFeature extends ShopFeature {

	private String permission;

	public PermissionFeature(Kit kit) {
		super(kit);
		permission = kit.getDetails().getString("permission");
		if (permission != null) {
			Permission perm = new Permission(permission);
			try {
				Bukkit.getPluginManager().addPermission(perm);
			} catch (IllegalArgumentException ignored) {
				// Already added
			}
		}
	}

	@Override
	public boolean allows(ShopSession session) {
		return permission == null || session.getPlayer().hasPermission(permission);
	}

	@Override
	public void indicateRestricted(ShopSession session) {
		if (permission.startsWith("gocraft.donator.")) {
			GoCraft.getInstance().message(session.getPlayer(), "shop-needRank", StringUtils.capitalize(permission.substring(16)));
		} else {
			GoCraft.getInstance().message(session.getPlayer(), "shop-needPermission", permission);
		}
	}

	@Override
	public String getStatusLine(ShopSession session) {
		if (permission == null) {
			return null;
		}
		if (!allowsBuy(session)) {
			if (permission.startsWith("gocraft.donator.")) {
				return "&4Required rank: " + StringUtils.capitalize(permission.substring(16)) + "\n   &eBuy this rank at\n   &e&nwww.go-craft.com";
			} else {
				return "&4Required permission: " + permission;
			}
		} else {
			if (permission.startsWith("gocraft.donator.")) {
				return "&2You have rank " + StringUtils.capitalize(permission.substring(16) + " or higher");
			} else {
				return "&2You have permission " + permission;
			}
		}
	}
}
