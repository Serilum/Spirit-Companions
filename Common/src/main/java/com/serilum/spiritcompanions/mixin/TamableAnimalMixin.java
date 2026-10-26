package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.Util;
import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = TamableAnimal.class, priority = 1001)
public class TamableAnimalMixin {
	@Inject(method = "setOrderedToSit(Z)V", at = @At(value = "HEAD"), cancellable = true)
	public void setOrderedToSit(boolean orderedToSit, CallbackInfo ci) {
		if (orderedToSit) {
			return;
		}

		if (!Util.refusesToFollow((TamableAnimal)(Object)this)) {
			return;
		}

		ci.cancel();
	}
}
