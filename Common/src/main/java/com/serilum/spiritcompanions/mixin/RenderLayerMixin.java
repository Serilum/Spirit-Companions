package com.serilum.spiritcompanions.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = RenderLayer.class, priority = 1001)
public class RenderLayerMixin {
	@Inject(method = "renderColoredCutoutModel(Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFF)V", at = @At(value = "HEAD"))
	private static void renderColoredCutoutModel_head(EntityModel<?> model, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity, float red, float green, float blue, CallbackInfo ci) {
		UtilClient.useCollarLayerColour();
	}

	@Inject(method = "renderColoredCutoutModel(Lnet/minecraft/client/model/EntityModel;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFF)V", at = @At(value = "RETURN"))
	private static void renderColoredCutoutModel_return(EntityModel<?> model, ResourceLocation texture, PoseStack poseStack, MultiBufferSource buffer, int packedLight, LivingEntity entity, float red, float green, float blue, CallbackInfo ci) {
		UtilClient.useLayerColour();
	}
}
