package com.serilum.spiritcompanions.cmds;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.natamus.collective.functions.MessageFunctions;
import com.serilum.spiritcompanions.util.Reference;
import com.serilum.spiritcompanions.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public class CommandSpiritCompanions {
	public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
		dispatcher.register(Commands.literal("spiritcompanions").requires((iCommandSender) -> iCommandSender.permissions().hasPermission(Permissions.COMMANDS_ADMIN))
			.executes((command) -> {
				showUsage(command.getSource());
				return 1;
			})
			.then(Commands.literal("summon")
			.then(Commands.argument("entity", ResourceArgument.resource(buildContext, Registries.ENTITY_TYPE))
			.suggests((context, builder) -> SharedSuggestionProvider.suggestResource(Util.getSpiritEntityTypeIds(context.getSource().getLevel()), builder))
			.executes((command) -> {
				EntityType<?> entityType = ResourceArgument.getSummonableEntityType(command, "entity").value();
				return summonSpirit(command.getSource(), entityType);
			})))
		);
	}

	private static int summonSpirit(CommandSourceStack source, EntityType<?> entityType) {
		Player player;
		try {
			player = source.getPlayerOrException();
		}
		catch (CommandSyntaxException ex) {
			MessageFunctions.sendTranslatableMessage(source, "collective.shared.message.playeronly", ChatFormatting.RED);
			return 0;
		}

		Mob spirit = Util.summonSpirit(source.getLevel(), player, entityType);
		if (spirit == null) {
			MessageFunctions.sendTranslatableMessage(source, "collective.spiritcompanions.message.cannotbecomespirit", ChatFormatting.RED, entityType.getDescription());
			return 0;
		}

		MessageFunctions.sendTranslatableMessage(source, "collective.spiritcompanions.message.summonedspirit", ChatFormatting.DARK_GREEN, entityType.getDescription());
		return 1;
	}

	private static void showUsage(CommandSourceStack source) {
		MessageFunctions.sendTranslatableMessage(source, "collective.shared.message.usage", true, ChatFormatting.DARK_GREEN, Reference.NAME);
		MessageFunctions.sendMessage(source, " /spiritcompanions summon <entity>", ChatFormatting.DARK_GREEN);
		MessageFunctions.sendTranslatableMessage(source, "  ", "collective.spiritcompanions.message.summonspiritusage", ChatFormatting.DARK_GRAY);
	}
}
