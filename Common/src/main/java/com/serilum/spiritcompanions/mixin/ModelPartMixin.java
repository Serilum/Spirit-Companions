package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = ModelPart.class, priority = 1001)
public class ModelPartMixin {
	@ModifyVariable(method = "compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At(value = "HEAD"), argsOnly = true, ordinal = 0)
	public float compile_red(float red) {
		return UtilClient.getModelRed(red);
	}

	@ModifyVariable(method = "compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At(value = "HEAD"), argsOnly = true, ordinal = 1)
	public float compile_green(float green) {
		return UtilClient.getModelGreen(green);
	}

	@ModifyVariable(method = "compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At(value = "HEAD"), argsOnly = true, ordinal = 2)
	public float compile_blue(float blue) {
		return UtilClient.getModelBlue(blue);
	}

	@ModifyVariable(method = "compile(Lcom/mojang/blaze3d/vertex/PoseStack$Pose;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V", at = @At(value = "HEAD"), argsOnly = true, ordinal = 3)
	public float compile_alpha(float alpha) {
		return UtilClient.getModelAlpha(alpha);
	}
}
