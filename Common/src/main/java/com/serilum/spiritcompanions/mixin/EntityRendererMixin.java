package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = EntityRenderer.class, priority = 1001)
public class EntityRendererMixin {
	@Inject(method = "finalizeRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;)V", at = @At(value = "TAIL"))
	public void finalizeRenderState(Entity entity, EntityRenderState state, CallbackInfo ci) {
		if (!UtilClient.isSpiritRenderState(state)) {
			return;
		}

		UtilClient.takeOverSpiritShadow(state);
	}
}
