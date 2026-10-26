package com.serilum.spiritcompanions.item;

import com.serilum.spiritcompanions.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EmptySpiritLanternItem extends Item {
	public EmptySpiritLanternItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof Mob mob)) {
			return InteractionResult.PASS;
		}

		if (Util.isSpirit(mob)) {
			return InteractionResult.PASS;
		}

		if (!player.level().isClientSide()) {
			Util.tellSpiritLanternIsEmpty(player);
		}
		return InteractionResult.SUCCESS;
	}
}
