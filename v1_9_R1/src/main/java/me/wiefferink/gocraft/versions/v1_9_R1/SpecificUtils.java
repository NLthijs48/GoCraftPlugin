package me.wiefferink.gocraft.versions.v1_9_R1;

import com.comphenix.protocol.wrappers.*;
import com.mojang.authlib.GameProfile;
import me.wiefferink.gocraft.interfaces.SpecificUtilsBase;
import me.wiefferink.gocraft.versions.v1_9_R1.packetwrapper.WrapperPlayServerNamedEntitySpawn;
import me.wiefferink.gocraft.versions.v1_9_R1.packetwrapper.WrapperPlayServerPlayerInfo;
import net.minecraft.server.v1_9_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
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
		net.minecraft.server.v1_9_R1.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);
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


	@Override
	public int sendFakePlayer(Location location, Player player, boolean visible, String name) {
		WrapperPlayServerNamedEntitySpawn entityWrapper = new WrapperPlayServerNamedEntitySpawn();
		entityWrapper.setEntityID(SpecificUtilsBase.random.nextInt(20000));
		entityWrapper.setPosition(location.toVector());
		entityWrapper.setPlayerUUID(UUID.randomUUID());
		entityWrapper.setYaw(SpecificUtils.random.nextFloat() * 360);
		entityWrapper.setPitch(SpecificUtils.random.nextFloat() * 90 - 45.0F);
		WrappedDataWatcher watcher = new WrappedDataWatcher();
		// Visibility
		WrappedDataWatcher.Serializer byteSerializer = WrappedDataWatcher.Registry.get(Byte.class);
		WrappedDataWatcher.WrappedDataWatcherObject visibleObject = new WrappedDataWatcher.WrappedDataWatcherObject(0, byteSerializer);
		watcher.setObject(visibleObject, visible ? 0 : (byte) 32);
		// Health
		WrappedDataWatcher.Serializer floatSerializer = WrappedDataWatcher.Registry.get(Float.class);
		WrappedDataWatcher.WrappedDataWatcherObject sixOject = new WrappedDataWatcher.WrappedDataWatcherObject(6, floatSerializer);
		watcher.setObject(sixOject, 0.5F);
		// Set metadata and send
		entityWrapper.setMetadata(watcher);
		getInfoWrapper(entityWrapper.getPlayerUUID(), EnumWrappers.PlayerInfoAction.ADD_PLAYER, name).sendPacket(player);
		entityWrapper.sendPacket(player);
		getInfoWrapper(entityWrapper.getPlayerUUID(), EnumWrappers.PlayerInfoAction.REMOVE_PLAYER, name).sendPacket(player);
		return entityWrapper.getEntityID();
	}

	private WrapperPlayServerPlayerInfo getInfoWrapper(UUID playeruuid, EnumWrappers.PlayerInfoAction action, String name) {
		WrapperPlayServerPlayerInfo wrapper = new WrapperPlayServerPlayerInfo();
		wrapper.setAction(action);
		WrappedGameProfile profile = new WrappedGameProfile(playeruuid, name);
		PlayerInfoData data = new PlayerInfoData(profile, 1, EnumWrappers.NativeGameMode.SURVIVAL, WrappedChatComponent.fromText(name));
		List<PlayerInfoData> listdata = new ArrayList<>();
		listdata.add(data);
		wrapper.setData(listdata);
		return wrapper;
	}
}