package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.rendering.SpiritParticle;
import net.minecraft.client.particle.SingleQuadParticle;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = SingleQuadParticle.class, priority = 1001)
public class SingleQuadParticleMixin implements SpiritParticle {
	@Unique private float spiritCompanions$alphaMultiplier = 1.0F;

	@Override
	public void spiritCompanions$setAlphaMultiplier(float alphaMultiplier) {
		spiritCompanions$alphaMultiplier = alphaMultiplier;
	}

	@ModifyArg(method = "render(Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/client/Camera;F)V", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/VertexConsumer;color(FFFF)Lcom/mojang/blaze3d/vertex/VertexConsumer;"), index = 3)
	public float render_alpha(float alpha) {
		return alpha * spiritCompanions$alphaMultiplier;
	}
}
