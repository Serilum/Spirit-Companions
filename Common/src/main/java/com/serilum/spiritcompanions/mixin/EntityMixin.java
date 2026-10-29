package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.Util;
import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Entity.class, priority = 1001)
public class EntityMixin {
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
