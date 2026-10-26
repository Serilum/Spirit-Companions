package com.serilum.spiritcompanions.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.serilum.spiritcompanions.data.Variables;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.HorseMarkingLayer;
import net.minecraft.client.renderer.entity.state.HorseRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = HorseMarkingLayer.class, priority = 1001)
public class HorseMarkingLayerMixin {
	@Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HorseRenderState;FF)V", at = @At(value = "HEAD"))
	public void submit_head(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, HorseRenderState state, float yRot, float xRot, CallbackInfo ci) {
		Variables.isSubmittingHorseMarkings = true;
	}

	@Inject(method = "submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/HorseRenderState;FF)V", at = @At(value = "TAIL"))
	public void submit_tail(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, HorseRenderState state, float yRot, float xRot, CallbackInfo ci) {
		Variables.isSubmittingHorseMarkings = false;
	}
}
