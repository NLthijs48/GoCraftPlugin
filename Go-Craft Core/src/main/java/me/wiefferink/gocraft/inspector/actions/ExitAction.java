package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.bukkitdo.Do;
import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.tools.ItemBuilder;
import org.bukkit.Material;

public class ExitAction extends InventoryAction {

	public ExitAction(Inspection inspection) {
		super(inspection);
	}

	@Override
	public ItemBuilder getItem() {
		return new ItemBuilder(Material.BARRIER)
				.setName("&2Stop inspection")
				.addAction("Stop");
	}

	@Override
	public void handleClick() {
		inspection.endInspection();
		Do.sync(inspection.getInspector()::closeInventory);
	}

	@Override
	public int getItemSlot() {
		return 8;
	}

}
