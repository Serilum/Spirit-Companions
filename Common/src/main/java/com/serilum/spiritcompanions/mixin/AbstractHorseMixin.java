package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractHorse.class, priority = 1001)
public class AbstractHorseMixin {
	@Inject(method = "readAdditionalSaveData(Lnet/minecraft/nbt/CompoundTag;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/animal/horse/AbstractHorse;updateContainerEquipment()V"))
	public void readAdditionalSaveData(CompoundTag compoundTag, CallbackInfo ci) {
		Util.restoreSpiritSaddle((AbstractHorse)(Object)this, compoundTag);
	}
}
