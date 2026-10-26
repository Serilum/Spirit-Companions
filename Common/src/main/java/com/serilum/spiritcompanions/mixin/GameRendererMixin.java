package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GameRenderer.class, priority = 1001)
public class GameRendererMixin {
	@Inject(method = "pick(F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyePosition(F)Lnet/minecraft/world/phys/Vec3;", ordinal = 0))
	public void pick(float partialTicks, CallbackInfo ci) {
		UtilClient.pickBlockForSpiritItems(partialTicks);
	}
}
