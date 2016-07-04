package me.wiefferink.gocraft.versions.v1_8_R3.packetwrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.util.Vector;

import java.util.UUID;

public class WrapperPlayServerNamedEntitySpawn extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.NAMED_ENTITY_SPAWN;

	public WrapperPlayServerNamedEntitySpawn() {
		super(new PacketContainer(TYPE), TYPE);
		this.handle.getModifier().writeDefaults();
	}

	public WrapperPlayServerNamedEntitySpawn(PacketContainer packet) {
		super(packet, TYPE);
	}

	public int getEntityId() {
		return this.handle.getIntegers().read(0);
	}

	public void setEntityId(int value) {
		this.handle.getIntegers().write(0, value);
	}

	public UUID getPlayerUuid() {
		return this.handle.getSpecificModifier(UUID.class).read(0);
	}

	public void setPlayerUuid(UUID value) {
		this.handle.getSpecificModifier(UUID.class).write(0, value);
	}

	public Vector getPosition() {
		return new Vector(getX(), getY(), getZ());
	}

	public void setPosition(Vector position) {
		setX(position.getX());
		setY(position.getY());
		setZ(position.getZ());
	}

	public double getX() {
		return this.handle.getIntegers().read(1) / 32.0D;
	}

	public void setX(double value) {
		this.handle.getIntegers().write(1, (int) Math.floor(value * 32.0D));
	}

	public double getY() {
		return this.handle.getIntegers().read(2) / 32.0D;
	}

	public void setY(double value) {
		this.handle.getIntegers().write(2, (int) Math.floor(value * 32.0D));
	}

	public double getZ() {
		return this.handle.getIntegers().read(3) / 32.0D;
	}

	public void setZ(double value) {
		this.handle.getIntegers().write(3, (int) Math.floor(value * 32.0D));
	}

	public float getYaw() {
		return this.handle.getBytes().read(0) * 360.0F / 256.0F;
	}

	public void setYaw(float value) {
		this.handle.getBytes().write(0, (byte) (value * 256.0F / 360.0F));
	}

	public float getPitch() {
		return this.handle.getBytes().read(1) * 360.0F / 256.0F;
	}

	public void setPitch(float value) {
		this.handle.getBytes().write(1, (byte) (value * 256.0F / 360.0F));
	}

	public int getCurrentItem() {
		return this.handle.getIntegers().read(4);
	}

	public void setCurrentItem(int value) {
		this.handle.getIntegers().write(4, value);
	}

	public WrappedDataWatcher getMetadata() {
		return this.handle.getDataWatcherModifier().read(0);
	}

	public void setMetadata(WrappedDataWatcher value) {
		this.handle.getDataWatcherModifier().write(0, value);
	}
}