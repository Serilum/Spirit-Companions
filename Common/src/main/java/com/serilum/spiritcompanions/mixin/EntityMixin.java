package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.data.SpiritEntityData;
import com.serilum.spiritcompanions.util.EntityData;
import com.serilum.spiritcompanions.util.Util;
import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 1001)
public class EntityMixin implements SpiritEntityData {
	@Unique private CompoundTag spiritCompanions$stored = new CompoundTag();

	@Override
	public CompoundTag spiritCompanions$getStored() {
		return spiritCompanions$stored;
	}

	@Override
	public void spiritCompanions$setStored(CompoundTag data) {
		spiritCompanions$stored = data;
	}

	@Inject(method = "saveWithoutId(Lnet/minecraft/world/level/storage/ValueOutput;)V", at = @At(value = "TAIL"))
	public void saveWithoutId(ValueOutput output, CallbackInfo ci) {
		if (!spiritCompanions$stored.isEmpty()) {
			output.store(EntityData.KEY, CompoundTag.CODEC, spiritCompanions$stored);
		}
	}

	@Inject(method = "load(Lnet/minecraft/world/level/storage/ValueInput;)V", at = @At(value = "TAIL"))
	public void load(ValueInput input, CallbackInfo ci) {
		spiritCompanions$stored = input.read(EntityData.KEY, CompoundTag.CODEC).orElse(new CompoundTag());
	}

	@ModifyVariable(method = "playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", at = @At(value = "HEAD"), argsOnly = true, ordinal = 1)
	public float playSound_pitch(float pitch) {
		if (!Util.isSpirit((Entity)(Object)this)) {
			return pitch;
		}

		return Util.getSpiritPitch(pitch);
	}

	@Inject(method = "push(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "HEAD"), cancellable = true)
	public void push(Entity entity, CallbackInfo ci) {
		if (!Util.isSpirit((Entity)(Object)this) && !Util.isSpirit(entity)) {
			return;
		}

		ci.cancel();
	}

	@Inject(method = "onClientRemoval()V", at = @At(value = "HEAD"))
	public void onClientRemoval(CallbackInfo ci) {
		Entity entity = (Entity)(Object)this;
		if (!(entity instanceof LivingEntity livingEntity)) {
			return;
		}

		if (!Util.isSpirit(livingEntity)) {
			return;
		}

		UtilClient.spawnSpiritBurst(livingEntity);
	}
}
