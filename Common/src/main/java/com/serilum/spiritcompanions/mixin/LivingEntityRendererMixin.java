package com.serilum.spiritcompanions.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.serilum.spiritcompanions.data.Variables;
import com.serilum.spiritcompanions.rendering.SpiritRenderState;
import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.UvMapping;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntityRenderer.class, priority = 1001)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState> {
	@Shadow public abstract Identifier getTextureLocation(S state);

	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At(value = "TAIL"))
	public void extractRenderState(T entity, S state, float partialTicks, CallbackInfo ci) {
		UtilClient.setSpiritRenderState(state, entity);
	}

	@Inject(method = "getRenderType(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;ZZZ)Lnet/minecraft/client/renderer/rendertype/RenderType;", at = @At(value = "HEAD"), cancellable = true)
	public void getRenderType(S state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing, CallbackInfoReturnable<RenderType> cir) {
		if (!isBodyVisible) {
			return;
		}

		if (!UtilClient.usesSpiritRendering(state)) {
			return;
		}

		cir.setReturnValue(UtilClient.getSpiritRenderType(getTextureLocation(state)));
	}

	@Inject(method = "isShaking(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;)Z", at = @At(value = "RETURN"), cancellable = true)
	public void isShaking(S state, CallbackInfoReturnable<Boolean> cir) {
		if (!UtilClient.isBeingExtractedRenderState(state)) {
			return;
		}

		cir.setReturnValue(true);
	}

	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "HEAD"))
	public void submit_head(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		Variables.submittingSpiritState = null;
		if (UtilClient.usesSpiritRendering(state)) {
			Variables.submittingSpiritState = state;
		}
	}

	@Inject(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "TAIL"))
	public void submit_tail(S state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera, CallbackInfo ci) {
		Variables.submittingSpiritState = null;

		if (!((SpiritRenderState)state).spiritCompanions$isSpirit()) {
			return;
		}

		UtilClient.submitSpiritShadow(state, poseStack, submitNodeCollector);
	}

	@ModifyArg(method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"), index = 6)
	public int submit_tintedColor(Model<?> model, Object state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, int tintedColor, UvMapping uvMapping, int outlineColor) {
		return UtilClient.getSpiritLayerColour(state, tintedColor);
	}
}
