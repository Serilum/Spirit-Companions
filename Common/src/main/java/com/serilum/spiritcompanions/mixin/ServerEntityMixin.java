package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.networking.packets.ToClientSyncEntityDataPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ServerEntity.class, priority = 1001)
public class ServerEntityMixin {
	@Shadow @Final private Entity entity;

	@Inject(method = "addPairing(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At(value = "TAIL"))
	public void addPairing(ServerPlayer player, CallbackInfo ci) {
		ToClientSyncEntityDataPacket.syncToPlayer(entity, player);
	}
}
