package com.serilum.spiritcompanions.item;

import com.serilum.spiritcompanions.util.Util;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public class SpiritCanisterItem extends Item {
	public SpiritCanisterItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof Mob mob)) {
			return InteractionResult.PASS;
		}

		if (!Util.isSpirit(mob)) {
			return InteractionResult.PASS;
		}

		if (player.level() instanceof ServerLevel serverLevel) {
			Util.storeSpiritInCanister(serverLevel, player, hand, itemStack, mob);
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		ItemStack itemStack = context.getItemInHand();
		if (!Util.holdsSpirit(itemStack)) {
			return InteractionResult.PASS;
		}

		Player player = context.getPlayer();
		if (player == null) {
			return InteractionResult.PASS;
		}

		if (context.getLevel() instanceof ServerLevel serverLevel) {
			Util.releaseSpiritFromCanister(serverLevel, player, context.getHand(), itemStack, context.getClickedPos().relative(context.getClickedFace()));
		}
		return InteractionResult.SUCCESS;
	}
}
