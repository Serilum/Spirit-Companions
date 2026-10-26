package com.serilum.spiritcompanions.neoforge.events;

import com.serilum.spiritcompanions.cmds.CommandSpiritCompanions;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public class NeoForgeSpiritEvents {
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent e) {
		CommandSpiritCompanions.register(e.getDispatcher(), e.getBuildContext());
	}
}
