package com.serilum.spiritcompanions.util;

import com.serilum.spiritcompanions.data.SpiritEntityData;
import com.serilum.spiritcompanions.networking.packets.ToClientSyncEntityDataPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;

public class EntityData {
	public static final String KEY = Reference.MOD_ID + "_entitydata";

	public static CompoundTag getStored(Entity entity) {
		if (entity instanceof SpiritEntityData holder) {
			return holder.spiritCompanions$getStored();
		}
		return new CompoundTag();
	}

	public static void setStored(Entity entity, CompoundTag data) {
		if (entity instanceof SpiritEntityData holder) {
			holder.spiritCompanions$setStored(data);
		}
	}

	public static int getInt(Entity entity, Identifier id, int defaultValue) {
		return getStored(entity).getIntOr(id.toString(), defaultValue);
	}

	public static void setInt(Entity entity, Identifier id, int value) {
		CompoundTag data = getStored(entity);
		data.putInt(id.toString(), value);
		setStored(entity, data);

		if (!entity.level().isClientSide()) {
			ToClientSyncEntityDataPacket.syncToTrackers(entity);
		}
	}
}
