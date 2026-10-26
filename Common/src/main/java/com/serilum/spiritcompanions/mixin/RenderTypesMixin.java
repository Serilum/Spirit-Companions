package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.data.Variables;
import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderTypes.class, priority = 1001)
public class RenderTypesMixin {
	@Inject(method = "entityCutout(Lnet/minecraft/resources/Identifier;Z)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = @At(value = "HEAD"), cancellable = true)
	private static void entityCutout(Identifier texture, boolean affectsOutline, CallbackInfoReturnable<RenderType> cir) {
		if (Variables.submittingSpiritState == null) {
			return;
		}

		cir.setReturnValue(UtilClient.getSpiritRenderType(texture));
	}

	@Inject(method = "armorCutoutNoCull(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = @At(value = "HEAD"), cancellable = true)
	private static void armorCutoutNoCull(Identifier texture, CallbackInfoReturnable<RenderType> cir) {
		if (Variables.submittingSpiritState == null) {
			return;
		}

		cir.setReturnValue(UtilClient.getSpiritRenderType(texture));
	}
}
