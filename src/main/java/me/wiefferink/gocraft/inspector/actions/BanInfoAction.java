package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.GoCraft;
import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.utils.ItemBuilder;
import org.bukkit.Material;

public class BanInfoAction extends InventoryAction {

	public BanInfoAction(Inspection inspection) {
		super(inspection);
	}

	@Override
	public boolean isActive() {
		return GoCraft.getInstance().getBanManagerLink() != null && inspection.hasInspected();
	}

	@Override
	public ItemBuilder getItem() {
		return new ItemBuilder(Material.IRON_FENCE)
				.setName("&2Check ban/mute info")
				.addAction("Click");
	}

	@Override
	public void handleClick() {
		inspection.getInspector().performCommand("banmanager:bminfo " + inspection.getInspected().getName());
		inspection.getInspector().closeInventory();
	}
}
