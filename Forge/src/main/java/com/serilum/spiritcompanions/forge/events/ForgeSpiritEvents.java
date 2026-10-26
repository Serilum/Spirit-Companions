package com.serilum.spiritcompanions.forge.events;

import com.serilum.spiritcompanions.cmds.CommandSpiritCompanions;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.listener.SubscribeEvent;

public class ForgeSpiritEvents {
	public static void registerEventsInBus() {
		// BusGroup.DEFAULT.register(MethodHandles.lookup(), ForgeSpiritEvents.class);

		RegisterCommandsEvent.BUS.addListener(ForgeSpiritEvents::registerCommands);
	}

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent e) {
		CommandSpiritCompanions.register(e.getDispatcher(), e.getBuildContext());
	}
}
