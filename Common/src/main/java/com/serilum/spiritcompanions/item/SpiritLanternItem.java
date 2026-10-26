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
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;

public class SpiritLanternItem extends Item {
	public SpiritLanternItem(Properties properties) {
		super(properties);
	}

	@Override
	public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
		if (!(target instanceof Mob mob)) {
			return InteractionResult.PASS;
		}

		if (Util.isSpirit(mob) || Util.isHusk(mob)) {
			return InteractionResult.PASS;
		}

		if (player.level() instanceof ServerLevel serverLevel && !Util.startSpiritExtraction(serverLevel, player, mob)) {
			return InteractionResult.FAIL;
		}

		player.startUsingItem(hand);
		return InteractionResult.SUCCESS;
	}

	@Override
	public int getUseDuration(ItemStack itemStack, LivingEntity user) {
		return 72000;
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack itemStack) {
		return ItemUseAnimation.TOOT_HORN;
	}

	@Override
	public void onUseTick(Level level, LivingEntity livingEntity, ItemStack itemStack, int ticksRemaining) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return;
		}

		if (!(livingEntity instanceof Player player)) {
			return;
		}

		Util.tickSpiritExtraction(serverLevel, player, getUseDuration(itemStack, livingEntity) - ticksRemaining);
	}

	@Override
	public boolean releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int remainingTime) {
		if (livingEntity instanceof Player player) {
			Util.cancelSpiritExtraction(player);
		}
		return false;
	}
}
