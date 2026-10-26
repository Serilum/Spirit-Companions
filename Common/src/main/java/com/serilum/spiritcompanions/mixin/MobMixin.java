package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Mob.class, priority = 1001)
public class MobMixin {
	@Inject(method = "checkDespawn()V", at = @At(value = "HEAD"), cancellable = true)
	public void checkDespawn(CallbackInfo ci) {
		if (!Util.isSpirit((Mob)(Object)this)) {
			return;
		}

		ci.cancel();
	}

	@Inject(method = "playAmbientSound()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V", shift = At.Shift.AFTER))
	public void playAmbientSound(CallbackInfo ci) {
		Mob mob = (Mob)(Object)this;
		if (!Util.isSpirit(mob)) {
			return;
		}

		Util.playSpiritWhisper(mob);
	}

	@Inject(method = "interact(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "HEAD"), cancellable = true)
	public void interact(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
		Mob mob = (Mob)(Object)this;
		if (Util.isHusk(mob)) {
			cir.setReturnValue(InteractionResult.CONSUME);
			return;
		}

		ItemStack itemStack = player.getItemInHand(hand);
		if (Util.isSpiritItem(itemStack)) {
			InteractionResult itemResult = itemStack.interactLivingEntity(player, mob, hand);
			if (itemResult.consumesAction()) {
				cir.setReturnValue(itemResult);
			}
			return;
		}

		if (Util.refusesNormalSaddle(mob, itemStack)) {
			if (!player.level().isClientSide()) {
				Util.refuseNormalSaddle(player, mob);
			}
			cir.setReturnValue(InteractionResult.SUCCESS);
			return;
		}

		if (!Util.isStayToggleInteraction(mob, player, hand)) {
			return;
		}

		if (!player.level().isClientSide()) {
			Util.toggleSpiritStaying(player, mob);
		}
		cir.setReturnValue(InteractionResult.SUCCESS);
	}

	@Inject(method = "setTarget(Lnet/minecraft/world/entity/LivingEntity;)V", at = @At(value = "HEAD"), cancellable = true)
	public void setTarget(LivingEntity target, CallbackInfo ci) {
		if (target == null) {
			return;
		}

		Mob mob = (Mob)(Object)this;
		if (!Util.isSpirit(mob) && !Util.isBeingExtracted(mob)) {
			return;
		}

		ci.cancel();
	}
}
