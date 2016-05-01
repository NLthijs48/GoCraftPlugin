package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.tools.ItemBuilder;
import org.bukkit.Material;

public class EnderchestAction extends InventoryAction {

	public EnderchestAction(Inspection inspection) {
		super(inspection);
	}

	@Override
	public boolean isActive() {
		return inspection.hasInspected();
	}

	@Override
	public ItemBuilder getItem() {
		return new ItemBuilder(Material.ENDER_CHEST)
				.setName("&2Open enderchest")
				.addAction("Click");
	}

	@Override
	public void handleClick() {
		inspection.getInspector().closeInventory(); // close existing
		inspection.getInspector().performCommand("openinv:openender " + inspection.getInspected().getName());
	}

}
