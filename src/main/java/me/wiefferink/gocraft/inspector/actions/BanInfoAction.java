package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.ItemBuilder;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class BanInfoAction extends InventoryAction {

	public BanInfoAction(Inspection inspection) {
		super(inspection);
	}

	@Override
	public boolean isActive() {
		return GoCraft.getInstance().getBanManagerLink() != null && inspection.hasInspected();
	}

	@Override
	public ItemStack getItem() {
		return new ItemBuilder(Material.IRON_FENCE)
				.setName(ChatColor.GREEN + "Check ban/mute info")
				.addAction("Click")
				.getItemStack();
	}

	@Override
	public void handleClick() {
		inspection.getInspector().performCommand("banmanager:bminfo " + inspection.getInspected().getName());
		inspection.getInspector().closeInventory();
	}
}
