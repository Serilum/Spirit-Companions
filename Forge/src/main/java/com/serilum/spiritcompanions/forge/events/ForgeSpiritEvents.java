package com.serilum.spiritcompanions.forge.events;

import com.serilum.spiritcompanions.cmds.CommandSpiritCompanions;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ForgeSpiritEvents {
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent e) {
		CommandSpiritCompanions.register(e.getDispatcher(), e.getBuildContext());
	}
}
