package com.serilum.spiritcompanions.data;

import net.minecraft.nbt.CompoundTag;

public interface SpiritEntityData {
	CompoundTag spiritCompanions$getStored();

	void spiritCompanions$setStored(CompoundTag data);
}
