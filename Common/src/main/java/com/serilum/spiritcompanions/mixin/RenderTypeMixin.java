package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.data.Variables;
import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderType.class, priority = 1001)
public class RenderTypeMixin {
	@Inject(method = "entityCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;", at = @At(value = "HEAD"), cancellable = true)
	private static void entityCutoutNoCull(ResourceLocation texture, CallbackInfoReturnable<RenderType> cir) {
		if (!UtilClient.usesSpiritRendering(Variables.renderingEntity)) {
			return;
		}

		cir.setReturnValue(UtilClient.getSpiritRenderType(texture));
	}

	@Inject(method = "armorCutoutNoCull(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;", at = @At(value = "HEAD"), cancellable = true)
	private static void armorCutoutNoCull(ResourceLocation texture, CallbackInfoReturnable<RenderType> cir) {
		if (!UtilClient.usesSpiritRendering(Variables.renderingEntity)) {
			return;
		}

		cir.setReturnValue(UtilClient.getSpiritRenderType(texture));
	}
}
