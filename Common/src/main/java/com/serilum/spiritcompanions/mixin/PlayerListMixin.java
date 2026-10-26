package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.SpiritWorld;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PlayerList.class, priority = 1001)
public class PlayerListMixin {
	@Inject(method = "remove(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At(value = "HEAD"))
	public void remove(ServerPlayer player, CallbackInfo ci) {
		SpiritWorld.sendAllSpiritsToSpiritWorld(player);
	}
}
