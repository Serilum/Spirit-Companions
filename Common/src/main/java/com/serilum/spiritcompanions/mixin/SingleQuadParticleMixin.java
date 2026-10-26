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

	@ModifyArg(method = "extractRotatedQuad(Lnet/minecraft/client/renderer/state/level/QuadParticleRenderState;Lorg/joml/Quaternionf;FFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ARGB;colorFromFloat(FFFF)I"), index = 0)
	public float extractRotatedQuad_alpha(float alpha) {
		return alpha * spiritCompanions$alphaMultiplier;
	}
}
