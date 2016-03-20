package me.wiefferink.gocraft.shop.features;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.shop.Kit;
import me.wiefferink.gocraft.shop.ShopSession;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;

public class PermissionFeature extends Feature {

	private String permission;

	public PermissionFeature(Kit kit) {
		super(kit);
		permission = kit.getDetails().getString("permission");
		if (permission != null) {
			Permission perm = new Permission(permission);
			try {
				Bukkit.getPluginManager().addPermission(perm);
			} catch (IllegalArgumentException e) {
				GoCraft.getInstance().getLogger().warning("Could not add the following permission to be used as kit permission for +" + kit.getName() + ": " + perm.getName());
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
}
