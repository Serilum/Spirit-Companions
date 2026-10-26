package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.SpiritWorld;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerPlayer.class, priority = 1001)
public class ServerPlayerMixin {
	@Inject(method = "tick()V", at = @At(value = "TAIL"))
	public void tick(CallbackInfo ci) {
		ServerPlayer player = (ServerPlayer)(Object)this;
		if (player.tickCount % 20 != 0) {
			return;
		}

		SpiritWorld.updateOwnerSpirits(player);
	}
}
