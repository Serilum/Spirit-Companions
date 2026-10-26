package com.serilum.spiritcompanions.networking.packets;

import com.natamus.collective.implementations.networking.api.Dispatcher;
import com.natamus.collective.implementations.networking.data.PacketContext;
import com.natamus.collective.implementations.networking.data.Side;
import com.serilum.spiritcompanions.util.EntityData;
import com.serilum.spiritcompanions.util.Reference;
import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class ToClientSyncEntityDataPacket {
	public static final Identifier CHANNEL = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "to_client_sync_entity_data_packet");

	private final int entityId;
	private final CompoundTag data;

	public ToClientSyncEntityDataPacket(int entityIdIn, CompoundTag dataIn) {
		this.entityId = entityIdIn;
		this.data = dataIn;
	}

	public static ToClientSyncEntityDataPacket decode(FriendlyByteBuf buf) {
		return new ToClientSyncEntityDataPacket(buf.readVarInt(), buf.readNbt());
	}

	public void encode(FriendlyByteBuf buf) {
		buf.writeVarInt(entityId);
		buf.writeNbt(data);
	}

	public static void handle(PacketContext<ToClientSyncEntityDataPacket> ctx) {
		if (!ctx.side().equals(Side.CLIENT)) {
			return;
		}

		UtilClient.applySyncedEntityData(ctx.message().entityId, ctx.message().data);
	}

	public static void syncToTrackers(Entity entity) {
		if (entity.level().isClientSide()) {
			return;
		}

		CompoundTag data = EntityData.getStored(entity);
		if (data.isEmpty()) {
			return;
		}

		Dispatcher.sendToClientsLoadingChunk(new ToClientSyncEntityDataPacket(entity.getId(), data), entity.level().getChunkAt(entity.blockPosition()));
	}

	public static void syncToPlayer(Entity entity, ServerPlayer player) {
		CompoundTag data = EntityData.getStored(entity);
		if (data.isEmpty()) {
			return;
		}

		Dispatcher.sendToClient(new ToClientSyncEntityDataPacket(entity.getId(), data), player);
	}
}
