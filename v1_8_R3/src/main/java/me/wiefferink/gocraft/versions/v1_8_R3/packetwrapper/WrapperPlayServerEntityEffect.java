package me.wiefferink.gocraft.versions.v1_8_R3.packetwrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerEntityEffect extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.ENTITY_EFFECT;

	public WrapperPlayServerEntityEffect() {
		super(new PacketContainer(TYPE), TYPE);
		this.handle.getModifier().writeDefaults();
	}

	public WrapperPlayServerEntityEffect(PacketContainer packet) {
		super(packet, TYPE);
	}

	public int getEntityId() {
		return this.handle.getIntegers().read(0);
	}

	public void setEntityId(int value) {
		this.handle.getIntegers().write(0, value);
	}

	public byte getEffectId() {
		return this.handle.getBytes().read(0);
	}

	public void setEffectId(byte value) {
		this.handle.getBytes().write(0, value);
	}

	public byte getAmplifier() {
		return this.handle.getBytes().read(1);
	}

	public void setAmplifier(byte value) {
		this.handle.getBytes().write(1, value);
	}

	public int getDuration() {
		return this.handle.getIntegers().read(1);
	}

	public void setDuration(int value) {
		this.handle.getIntegers().write(1, value);
	}

	public boolean getHideParticles() {
		return this.handle.getBooleans().read(0);
	}

	public void setHideParticles(boolean value) {
		this.handle.getBooleans().write(0, value);
	}
}