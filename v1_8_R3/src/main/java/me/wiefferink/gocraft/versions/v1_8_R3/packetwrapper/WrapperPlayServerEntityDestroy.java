package me.wiefferink.gocraft.versions.v1_8_R3.packetwrapper;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;

public class WrapperPlayServerEntityDestroy extends AbstractPacket {
	public static final PacketType TYPE = PacketType.Play.Server.ENTITY_DESTROY;

	public WrapperPlayServerEntityDestroy() {
		super(new PacketContainer(TYPE), TYPE);
		this.handle.getModifier().writeDefaults();
	}

	public WrapperPlayServerEntityDestroy(PacketContainer packet) {
		super(packet, TYPE);
	}

	public int getCount() {
		return this.handle.getIntegerArrays().read(0).length;
	}

	public int[] getEntityIds() {
		return this.handle.getIntegerArrays().read(0);
	}

	public void setEntityIds(int[] value) {
		this.handle.getIntegerArrays().write(0, value);
	}
}