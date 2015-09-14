package nl.evolutioncoding.gocraft.blocks;

import nl.evolutioncoding.gocraft.GoCraft;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class DisableAnvilBreak implements Listener {

	public final String configLine = "disableAnvilBreak";
	@SuppressWarnings("unused")
	private GoCraft plugin;

	public DisableAnvilBreak(GoCraft plugin) {
		if (plugin.getConfig().getBoolean(configLine)) {
			this.plugin = plugin;
			plugin.getServer().getPluginManager().registerEvents(this, plugin);
		}
	}

	// Prevent anvil breaking
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onAnvilUse(PlayerInteractEvent e) {
		if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block block = e.getClickedBlock();
			if (block.getType() == Material.ANVIL) {
				/* Direction by looking there and then placing
				 * Direction\type:	normal	damaged	broken
				 * East				0		4		8
				 * South			1		5		9
				 * West				2		6		10
				 * North			3		7		11
				 */
				block.setData((byte)(block.getData()%4));
			}
		}
	}
}
