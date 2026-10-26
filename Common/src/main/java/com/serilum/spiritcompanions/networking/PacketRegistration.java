package com.serilum.spiritcompanions.networking;

import com.natamus.collective.implementations.networking.api.Network;
import com.serilum.spiritcompanions.networking.packets.ToClientSyncEntityDataPacket;

public class PacketRegistration {

	public void init() {
		initClientPackets();
	}

	private void initClientPackets() {
		Network.registerPacket(ToClientSyncEntityDataPacket.CHANNEL, ToClientSyncEntityDataPacket.class, ToClientSyncEntityDataPacket::encode, ToClientSyncEntityDataPacket::decode, ToClientSyncEntityDataPacket::handle);
	}
}
