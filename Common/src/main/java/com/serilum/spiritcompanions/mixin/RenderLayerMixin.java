package com.serilum.spiritcompanions.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.UvMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = RenderLayer.class, priority = 1001)
public class RenderLayerMixin {
	@ModifyArg(method = "renderColoredCutoutModel(Lnet/minecraft/client/model/Model;Lnet/minecraft/resources/Identifier;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/OrderedSubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V"), index = 6)
	private static int renderColoredCutoutModel_color(Model<?> model, Object state, PoseStack poseStack, RenderType renderType, int lightCoords, int overlayCoords, int color, UvMapping uvMapping, int outlineColor) {
		return UtilClient.getCollarLayerColour(state, color);
	}
}
