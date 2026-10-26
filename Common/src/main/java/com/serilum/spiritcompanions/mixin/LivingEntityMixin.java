package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.Util;
import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 1001)
public class LivingEntityMixin {
	@Inject(method = "tick()V", at = @At(value = "HEAD"))
	public void tick(CallbackInfo ci) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		if (!(livingEntity instanceof Mob mob)) {
			return;
		}

		if (Util.isHusk(mob)) {
			if (mob.level() instanceof ServerLevel serverLevel) {
				Util.updateHusk(serverLevel, mob);
			}
			return;
		}

		if (!Util.isSpirit(mob)) {
			if (mob.level().isClientSide()) {
				if (Util.hasSpiritCollar(mob)) {
					UtilClient.spawnSpiritCollarSparkle(mob);
				}
				return;
			}

			if (Util.isBeingExtracted(mob)) {
				Util.updateExtractedMob(mob);
			}

			if (mob.tickCount % 20 == 0) {
				Util.updateBond(mob);
			}
			return;
		}

		if (!(mob.level() instanceof ServerLevel serverLevel)) {
			UtilClient.spawnSpiritTrailParticles(mob);
			UtilClient.spawnSpiritAppearParticles(mob);
			return;
		}

		Util.updateSpirit(serverLevel, mob);
	}

	@Inject(method = "tickDeath()V", at = @At(value = "TAIL"))
	public void tickDeath(CallbackInfo ci) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		if (!(livingEntity instanceof Mob mob)) {
			return;
		}

		if (!(mob.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		Util.tickDyingPet(serverLevel, mob);
	}

	@Inject(method = "travel(Lnet/minecraft/world/phys/Vec3;)V", at = @At(value = "HEAD"), cancellable = true)
	public void travel(Vec3 input, CallbackInfo ci) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		if (!(livingEntity instanceof Mob spirit)) {
			return;
		}

		if (Util.isBeingExtracted(spirit)) {
			ci.cancel();
			return;
		}

		if (!Util.isSpirit(spirit)) {
			return;
		}

		if (Util.isSpiritWalking(spirit)) {
			return;
		}

		Util.flySpirit(spirit);
		ci.cancel();
	}

	@Inject(method = "isInvulnerableTo(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;)Z", at = @At(value = "HEAD"), cancellable = true)
	public void isInvulnerableTo(ServerLevel level, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (!Util.isSpirit((LivingEntity)(Object)this)) {
			return;
		}

		if (!Util.spiritIgnoresDamage(source)) {
			return;
		}

		cir.setReturnValue(true);
	}

	@Inject(method = "die(Lnet/minecraft/world/damagesource/DamageSource;)V", at = @At(value = "HEAD"))
	public void die(DamageSource source, CallbackInfo ci) {
		LivingEntity livingEntity = (LivingEntity)(Object)this;
		if (!(livingEntity instanceof Mob pet)) {
			return;
		}

		if (!(pet.level() instanceof ServerLevel serverLevel)) {
			return;
		}

		Util.onPetDeath(serverLevel, pet);
	}

	@Inject(method = "isPushable()Z", at = @At(value = "HEAD"), cancellable = true)
	public void isPushable(CallbackInfoReturnable<Boolean> cir) {
		if (!Util.isSpirit((LivingEntity)(Object)this)) {
			return;
		}

		cir.setReturnValue(false);
	}

	@Inject(method = "pushEntities()V", at = @At(value = "HEAD"), cancellable = true)
	public void pushEntities(CallbackInfo ci) {
		if (!Util.isSpirit((LivingEntity)(Object)this)) {
			return;
		}

		ci.cancel();
	}

	@Inject(method = "makeSound(Lnet/minecraft/sounds/SoundEvent;)V", at = @At(value = "TAIL"))
	public void makeSound(SoundEvent sound, CallbackInfo ci) {
		if (sound == null) {
			return;
		}

		LivingEntity livingEntity = (LivingEntity)(Object)this;
		if (!Util.isSpirit(livingEntity)) {
			return;
		}

		Util.playSpiritWhisper(livingEntity);
	}
}
