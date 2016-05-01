package me.wiefferink.gocraft.inspector.actions;

import me.wiefferink.gocraft.inspector.Inspection;
import me.wiefferink.gocraft.tools.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;

public class NCPAction extends InventoryAction {

	public NCPAction(Inspection inspection) {
		super(inspection);
	}

	@Override
	public boolean isActive() {
		return Bukkit.getServer().getPluginManager().getPlugin("NoCheatPlus") != null && inspection.hasInspected() && inspection.getInspected().isOnline();
	}

	@Override
	public ItemBuilder getItem() {
		return new ItemBuilder(Material.WOOL)
				.setData(14)
				.setName("&2Check NCP violations")
				.addAction("Click");
	}

	@Override
	public void handleClick() {
		inspection.getInspector().performCommand("nocheatplus:ncp info " + inspection.getInspected().getName());
		inspection.getInspector().closeInventory();
	}

}
