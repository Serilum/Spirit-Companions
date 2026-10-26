package com.serilum.spiritcompanions.item;

import com.serilum.spiritcompanions.util.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SaddleItem;

public class SpiritSaddleItem extends SaddleItem {
	public SpiritSaddleItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof AbstractHorse horse) || !horse.isAlive() || horse.isSaddled() || !horse.isSaddleable()) {
			return super.interactLivingEntity(itemStack, player, target, hand);
		}

		if (!player.level().isClientSide()) {
			Util.equipSpiritSaddle(horse, itemStack);
		}
		return InteractionResult.sidedSuccess(player.level().isClientSide());
	}
}
