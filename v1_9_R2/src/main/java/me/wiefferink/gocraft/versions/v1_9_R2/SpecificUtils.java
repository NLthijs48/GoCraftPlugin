package me.wiefferink.gocraft.versions.v1_9_R2;

import com.mojang.authlib.GameProfile;
import me.wiefferink.gocraft.interfaces.SpecificUtilsBase;
import net.minecraft.server.v1_9_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_9_R2.CraftServer;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R2.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class SpecificUtils extends SpecificUtilsBase {

	@Override
	public int getPing(Player player) {
		CraftPlayer cp = (CraftPlayer) player;
		EntityPlayer ep = cp.getHandle();
		return ep.ping;
	}

	@Override
	public Player loadPlayer(UUID uuid) {
		OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
		if (player == null) {
			return null;
		}
		// Check if the player is online
		if (player.getPlayer() != null) {
			return player.getPlayer();
		}
		// Load offline player data
		GameProfile profile = new GameProfile(uuid, player.getName());
		MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
		EntityPlayer entity = new EntityPlayer(server, server.getWorldServer(0), profile, new PlayerInteractManager(server.getWorldServer(0)));

		// Get the bukkit entity
		Player target = entity.getBukkitEntity();
		if (target != null) {
			target.loadData();
		}
		return target;
	}

	@Override
	public ItemStack addGlow(ItemStack item) {
		net.minecraft.server.v1_9_R2.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
		NBTTagCompound tag = null;
		if (!nmsStack.hasTag()) {
			tag = new NBTTagCompound();
			nmsStack.setTag(tag);
		}
		if (tag == null) {
			tag = nmsStack.getTag();
		}
		NBTTagList ench = new NBTTagList();
		tag.set("ench", ench);
		nmsStack.setTag(tag);
		return CraftItemStack.asCraftMirror(nmsStack);
	}
}