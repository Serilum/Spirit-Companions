package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.renderer.SubmitNodeCollection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = SubmitNodeCollection.class, priority = 1001)
public class SubmitNodeCollectionMixin {
	@ModifyVariable(method = "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/UvMapping;I)V", at = @At(value = "HEAD"), argsOnly = true, ordinal = 2)
	public int submitModel_tintedColor(int tintedColor) {
		return UtilClient.getHorseMarkingsColour(tintedColor);
	}
}
