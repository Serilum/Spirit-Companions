package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Player.class, priority = 1001)
public class PlayerMixin {
	@Inject(method = "touch(Lnet/minecraft/world/entity/Entity;)V", at = @At(value = "HEAD"), cancellable = true)
	public void touch(Entity entity, CallbackInfo ci) {
		if (!Util.isSpirit(entity)) {
			return;
		}

		ci.cancel();
	}
}
