package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = CampfireBlockEntity.class, priority = 1001)
public class CampfireBlockEntityMixin {
	@Inject(method = "placeFood(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "HEAD"), cancellable = true)
	public void placeFood(ServerLevel serverLevel, LivingEntity sourceEntity, ItemStack placeItem, CallbackInfoReturnable<Boolean> cir) {
		CampfireBlockEntity campfire = (CampfireBlockEntity)(Object)this;
		if (!Util.isRefusedByCampfire(campfire.getBlockState(), placeItem)) {
			return;
		}

		Util.refuseNormalCampfire(sourceEntity);
		cir.setReturnValue(false);
	}
}
