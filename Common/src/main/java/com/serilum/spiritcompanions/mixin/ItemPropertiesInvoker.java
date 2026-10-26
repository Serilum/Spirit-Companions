package com.serilum.spiritcompanions.mixin;

import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value = ItemProperties.class, priority = 1001)
public interface ItemPropertiesInvoker {
	@Invoker("register")
	static void invokeRegister(Item item, ResourceLocation name, ClampedItemPropertyFunction property) {
		throw new AssertionError();
	}
}
