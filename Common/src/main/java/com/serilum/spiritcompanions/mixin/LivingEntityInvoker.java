package com.serilum.spiritcompanions.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = LivingEntity.class, priority = 1001)
public interface LivingEntityInvoker {
	@Invoker("dropEquipment")
	void invokeDropEquipment(ServerLevel serverLevel);
}
