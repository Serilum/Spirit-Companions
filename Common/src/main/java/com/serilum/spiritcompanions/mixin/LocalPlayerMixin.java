package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LocalPlayer.class, priority = 1001)
public class LocalPlayerMixin {
	@ModifyVariable(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At(value = "STORE"), name = "blockHitResult")
	private static HitResult pick_blockHitResult(HitResult blockHitResult, Entity cameraEntity, double blockInteractionRange, double entityInteractionRange, float partialTicks) {
		return UtilClient.pickBlockForSpiritItems(blockHitResult, cameraEntity, Math.max(blockInteractionRange, entityInteractionRange), partialTicks);
	}
}
