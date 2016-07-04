package me.wiefferink.gocraft.tools.packetwrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import org.bukkit.util.Vector;

public class WrapperPlayClientUseEntity extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Client.USE_ENTITY;

	public WrapperPlayClientUseEntity() {
		super(new PacketContainer(TYPE), TYPE);
		this.handle.getModifier().writeDefaults();
	}

	public WrapperPlayClientUseEntity(PacketContainer packet) {
		super(packet, TYPE);
	}

	public int getTarget() {
		return this.handle.getIntegers().read(0);
	}

	public void setTarget(int value) {
		this.handle.getIntegers().write(0, value);
	}

	public EnumWrappers.EntityUseAction getType() {
		return this.handle.getEntityUseActions().read(0);
	}

	public void setType(EnumWrappers.EntityUseAction value) {
		this.handle.getEntityUseActions().write(0, value);
	}

	public Vector getTargetVector() {
		return this.handle.getVectors().read(0);
	}

	public void setTargetVector(Vector value) {
		this.handle.getVectors().write(0, value);
	}
}