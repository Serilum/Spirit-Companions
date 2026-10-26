package com.serilum.spiritcompanions.data;

import net.minecraft.nbt.CompoundTag;

import java.util.UUID;

public class PendingSpiritReturn {
	public final CompoundTag petData;
	public final UUID ownerUUID;
	public final boolean wasWearingSpiritCollar;

	public PendingSpiritReturn(CompoundTag petData, UUID ownerUUID, boolean wasWearingSpiritCollar) {
		this.petData = petData;
		this.ownerUUID = ownerUUID;
		this.wasWearingSpiritCollar = wasWearingSpiritCollar;
	}
}
