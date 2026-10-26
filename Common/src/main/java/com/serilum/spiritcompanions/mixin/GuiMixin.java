package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.client.gui.Gui;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Gui.class, priority = 1001)
public class GuiMixin {
	@Shadow private int overlayMessageTime;

	@Inject(method = "setOverlayMessage(Lnet/minecraft/network/chat/Component;Z)V", at = @At(value = "TAIL"))
	public void setOverlayMessage(Component component, boolean animateColor, CallbackInfo ci) {
		if (!UtilClient.isSpiritCompanionsMessage(component)) {
			return;
		}

		overlayMessageTime = UtilClient.getOverlayMessageTicks();
	}
}
