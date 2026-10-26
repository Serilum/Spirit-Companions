package com.serilum.spiritcompanions.mixin;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = AbstractHorse.class, priority = 1001)
public interface AbstractHorseAccessor {
	@Accessor
	SimpleContainer getInventory();
}
