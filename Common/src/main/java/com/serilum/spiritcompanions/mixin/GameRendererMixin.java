package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = GameRenderer.class, priority = 1001)
public class GameRendererMixin {
	@ModifyVariable(method = "pick(Lnet/minecraft/world/entity/Entity;DDF)Lnet/minecraft/world/phys/HitResult;", at = @At(value = "STORE"), ordinal = 0)
	private HitResult pick_blockHitResult(HitResult blockHitResult, Entity cameraEntity, double blockInteractionRange, double entityInteractionRange, float partialTicks) {
		return UtilClient.pickBlockForSpiritItems(blockHitResult, cameraEntity, Math.max(blockInteractionRange, entityInteractionRange), partialTicks);
	}
}
