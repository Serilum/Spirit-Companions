package com.serilum.spiritcompanions.mixin;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = Mob.class, priority = 1001)
public interface MobInvoker {
	@Invoker("getEquipmentDropChance")
	float invokeGetEquipmentDropChance(EquipmentSlot equipmentSlot);
}
