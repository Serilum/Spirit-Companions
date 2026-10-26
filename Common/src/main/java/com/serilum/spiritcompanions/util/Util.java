package com.serilum.spiritcompanions.util;

import com.serilum.spiritcompanions.config.ConfigHandler;
import com.serilum.spiritcompanions.data.PendingSpiritReturn;
import com.serilum.spiritcompanions.data.SpiritItems;
import com.serilum.spiritcompanions.data.SpiritState;
import com.serilum.spiritcompanions.data.Variables;
import com.serilum.spiritcompanions.mixin.LivingEntityInvoker;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Util {
	private static final Identifier spiritDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit");
	private static final Identifier spiritOwnerDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "owner");
	private static final Identifier spiritHuskDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "husk_uuid");
	private static final Identifier huskDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "husk");
	private static final Identifier stayingDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "staying");
	private static final Identifier huskSinceDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "husk_since");
	private static final Identifier spiritSinceDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_since");
	private static final Identifier spiritAppearTicksDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_appear_ticks");
	private static final Identifier extractingSinceDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "extracting_since");
	private static final Identifier extractionRecoveryFromDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "extraction_recovery_from");
	private static final Identifier extractionRecoverySinceDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "extraction_recovery_since");
	private static final Identifier spiritCollarDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_collar");
	private static final Identifier bondTicksDataId = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "bond_ticks");
	private static final int maxBondTicks = 7 * 24000;
	private static final String canisterSpiritKey = Reference.MOD_ID + ":spirit";
	private static final DustParticleOptions huskDustParticle = new DustParticleOptions(0x8C9096, 0.9F);

	public static boolean isSpirit(Entity entity) {
		return EntityData.getInt(entity, spiritDataId, 0) == 1;
	}

	public static void setSpirit(Entity entity) {
		EntityData.setInt(entity, spiritDataId, 1);
	}

	public static boolean hasSpiritCollar(Entity entity) {
		return EntityData.getInt(entity, spiritCollarDataId, 0) == 1;
	}

	public static boolean isHusk(Entity entity) {
		return EntityData.getInt(entity, huskDataId, 0) == 1;
	}

	private static void turnIntoHusk(Mob mob) {
		mob.ejectPassengers();
		mob.setNoAi(true);
		mob.setSilent(true);
		setStoredInt(mob, huskSinceDataId, (int)mob.level().getGameTime());
		EntityData.setInt(mob, huskDataId, 1);
		Variables.husksMadeThisSession.add(mob);
	}

	private static boolean wasHuskUnloaded(Mob husk) {
		return !Variables.husksMadeThisSession.contains(husk);
	}

	public static float getHuskOpacity(Entity husk) {
		if (ConfigHandler.huskWitherSeconds == 0) {
			return 1.0F;
		}

		float elapsedTicks = husk.level().getGameTime() - EntityData.getInt(husk, huskSinceDataId, 0);
		return Mth.clamp(1.0F - elapsedTicks / (ConfigHandler.huskWitherSeconds * 20.0F), 0.0F, 1.0F);
	}

	public static float getSpiritAppearProgress(Entity spirit) {
		float elapsedTicks = spirit.level().getGameTime() - EntityData.getInt(spirit, spiritSinceDataId, 0);
		float appearTicks = EntityData.getInt(spirit, spiritAppearTicksDataId, 60);
		return Mth.clamp(elapsedTicks / appearTicks, 0.0F, 1.0F);
	}

	public static void updateHusk(ServerLevel serverLevel, Mob husk) {
		if (wasHuskUnloaded(husk)) {
			witherHusk(serverLevel, husk);
			return;
		}

		if (ConfigHandler.huskWitherSeconds == 0) {
			return;
		}

		float opacity = getHuskOpacity(husk);
		if (opacity > 0.0F) {
			if (husk.getRandom().nextFloat() < 0.15F + (1.0F - opacity) * 0.6F) {
				spawnHuskDust(serverLevel, husk);
			}
			return;
		}

		witherHusk(serverLevel, husk);
	}

	private static void witherHusk(ServerLevel serverLevel, Mob husk) {
		playSoulEffects(serverLevel, husk);
		dropHuskGear(serverLevel, husk);
		husk.discard();
	}

	private static void dropHuskGear(ServerLevel serverLevel, Mob husk) {
		husk.dropPreservedEquipment(serverLevel);
		((LivingEntityInvoker)husk).invokeDropEquipment(serverLevel);
	}

	private static void spawnHuskDust(ServerLevel serverLevel, Mob husk) {
		Vec3 position = getRandomBodyPosition(husk);
		Vec3 wind = Vec3.directionFromRotation(0.0F, husk.getYRot()).reverse();
		serverLevel.sendParticles(huskDustParticle, position.x, position.y, position.z, 0, wind.x, 0.3, wind.z, 1.0);
	}

	public static Vec3 getRandomBodyPosition(Entity entity) {
		RandomSource random = entity.getRandom();
		double x = entity.getX() + (random.nextDouble() - 0.5) * entity.getBbWidth();
		double y = entity.getY() + random.nextDouble() * entity.getBbHeight();
		double z = entity.getZ() + (random.nextDouble() - 0.5) * entity.getBbWidth();
		return new Vec3(x, y, z);
	}

	private static void setStoredInt(Entity entity, Identifier id, int value) {
		CompoundTag data = EntityData.getStored(entity);
		data.putInt(id.toString(), value);
		EntityData.setStored(entity, data);
	}

	private static void reviveHusk(Mob husk) {
		husk.setNoAi(false);
		husk.setSilent(false);
		EntityData.setInt(husk, huskDataId, 0);
	}

	public static void setSpiritOwner(Mob spirit, Player owner) {
		if (spirit instanceof TamableAnimal tamableAnimal) {
			tamableAnimal.setTame(true, true);
			tamableAnimal.setOwner(owner);
		}
		else if (spirit instanceof AbstractHorse horse) {
			horse.setTamed(true);
			horse.setOwner(owner);
		}

		setSpiritOwnerUUID(spirit, owner.getUUID());
	}

	private static void setSpiritOwnerUUID(Mob spirit, UUID ownerUUID) {
		setStoredString(spirit, spiritOwnerDataId, ownerUUID.toString());
	}

	public static UUID getSpiritOwnerUUID(Mob spirit) {
		String ownerUUID = getStoredString(spirit, spiritOwnerDataId);
		if (ownerUUID.isEmpty()) {
			return null;
		}
		return UUID.fromString(ownerUUID);
	}

	public static LivingEntity getSpiritOwner(Mob spirit) {
		if (spirit instanceof TamableAnimal tamableAnimal) {
			return tamableAnimal.getOwner();
		}

		UUID ownerUUID = getSpiritOwnerUUID(spirit);
		if (ownerUUID == null) {
			return null;
		}

		return spirit.level().getPlayerByUUID(ownerUUID);
	}

	public static boolean isSpiritOwnedBy(Mob spirit, Player player) {
		return player.getUUID().equals(getSpiritOwnerUUID(spirit));
	}

	private static void setStoredString(Entity entity, Identifier id, String value) {
		CompoundTag data = EntityData.getStored(entity);
		data.putString(id.toString(), value);
		EntityData.setStored(entity, data);
	}

	private static String getStoredString(Entity entity, Identifier id) {
		return EntityData.getStored(entity).getStringOr(id.toString(), "");
	}

	public static CompoundTag saveMobCopyData(ServerLevel serverLevel, Mob mob) {
		TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, serverLevel.registryAccess());
		mob.saveWithoutId(output);

		CompoundTag data = output.buildResult();
		data.remove("UUID");
		data.remove("Passengers");
		data.remove("leash");
		data.putString("id", EntityType.getKey(mob.getType()).toString());
		return data;
	}

	public static Mob loadMobCopy(ServerLevel serverLevel, CompoundTag data) {
		Entity entity = EntityType.loadEntityRecursive(data, serverLevel, EntitySpawnReason.COMMAND, EntityProcessor.NOP);
		if (!(entity instanceof Mob mob)) {
			return null;
		}
		return mob;
	}

	public static void makeSpirit(ServerLevel serverLevel, Mob spirit, UUID ownerUUID, int appearTicks) {
		setSpiritOwnerUUID(spirit, ownerUUID);
		spirit.setPersistenceRequired();
		setStoredInt(spirit, spiritSinceDataId, (int)serverLevel.getGameTime());
		setStoredInt(spirit, spiritAppearTicksDataId, appearTicks);
		setSpirit(spirit);
		serverLevel.addFreshEntity(spirit);
	}

	public static void playSoulEffects(ServerLevel serverLevel, Entity entity) {
		serverLevel.sendParticles(ParticleTypes.SOUL, entity.getX(), entity.getY() + entity.getBbHeight() / 2.0, entity.getZ(), 16, entity.getBbWidth() / 2.0, entity.getBbHeight() / 2.0, entity.getBbWidth() / 2.0, 0.02);
		serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.NEUTRAL, 1.0F, 0.9F);
	}

	public static boolean startSpiritExtraction(ServerLevel serverLevel, Player player, Mob target) {
		if (!canExtractSpirit(player, target)) {
			showMessage(player, Component.translatable("collective.spiritcompanions.message.cannotextractspirit", target.getType().getDescription()), ChatFormatting.RED);
			return false;
		}

		if (isAtSpiritLimit(serverLevel.getServer(), player.getUUID())) {
			showSpiritLimitReached(player);
			return false;
		}

		Variables.spiritExtractionTargets.put(player, target);
		float alreadyDrained = Math.max(getHuskTintProgress(target), 0.0F);
		int extractingSince = (int)(serverLevel.getGameTime() - alreadyDrained * getExtractionTicks());
		setStoredInt(target, extractionRecoveryFromDataId, 0);
		EntityData.setInt(target, extractingSinceDataId, extractingSince);
		return true;
	}

	private static float getExtractionTicks() {
		return ConfigHandler.spiritExtractionSeconds * 20.0F;
	}

	public static float getHuskTintProgress(Entity entity) {
		float extractionProgress = getExtractionProgress(entity);
		if (extractionProgress >= 0.0F) {
			return extractionProgress;
		}

		float recoveryFrom = EntityData.getInt(entity, extractionRecoveryFromDataId, 0) / getExtractionTicks();
		if (recoveryFrom <= 0.0F) {
			return -1.0F;
		}

		float elapsedTicks = entity.level().getGameTime() - EntityData.getInt(entity, extractionRecoverySinceDataId, 0);
		float remaining = recoveryFrom - elapsedTicks / (getExtractionTicks() * 2.0F);
		if (remaining <= 0.0F) {
			return -1.0F;
		}
		return remaining;
	}

	public static float getExtractionProgress(Entity entity) {
		int extractingSince = EntityData.getInt(entity, extractingSinceDataId, 0);
		if (extractingSince == 0) {
			return -1.0F;
		}

		float extractionTicks = getExtractionTicks();
		float elapsedTicks = entity.level().getGameTime() - extractingSince;
		if (elapsedTicks > extractionTicks + 20.0F) {
			return -1.0F;
		}

		return Mth.clamp(elapsedTicks / extractionTicks, 0.0F, 1.0F);
	}

	public static boolean isBeingExtracted(Entity entity) {
		return getExtractionProgress(entity) >= 0.0F;
	}

	public static void updateExtractedMob(Mob mob) {
		keepFriendly(mob);
		mob.getNavigation().stop();
	}

	public static void tickSpiritExtraction(ServerLevel serverLevel, Player player, int usedTicks) {
		Mob target = Variables.spiritExtractionTargets.get(player);
		if (target == null) {
			return;
		}

		if (!isExtractionTargetInView(player, target)) {
			endSpiritExtraction(player, true);
			player.stopUsingItem();
			return;
		}

		if (target.getRandom().nextFloat() < 0.6F) {
			spawnExtractionParticle(serverLevel, target);
		}
		float extractionProgress = getExtractionProgress(target);
		if (usedTicks % 10 == 0) {
			float pitch = 0.6F + extractionProgress * 0.6F;
			serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.SOUL_ESCAPE.value(), SoundSource.NEUTRAL, 0.5F, pitch);
		}

		if (extractionProgress < 1.0F) {
			return;
		}

		endSpiritExtraction(player, false);
		player.stopUsingItem();
		extractSpirit(serverLevel, player, target);
	}

	public static void cancelSpiritExtraction(Player player) {
		endSpiritExtraction(player, true);
	}

	private static void endSpiritExtraction(Player player, boolean shouldRecover) {
		Mob target = Variables.spiritExtractionTargets.remove(player);
		if (target == null || !isBeingExtracted(target)) {
			return;
		}

		if (shouldRecover) {
			float drainedProgress = getExtractionProgress(target);
			setStoredInt(target, extractionRecoveryFromDataId, (int)(drainedProgress * getExtractionTicks()));
			setStoredInt(target, extractionRecoverySinceDataId, (int)target.level().getGameTime());

			int recoveryTicks = (int)(drainedProgress * getExtractionTicks() * 2.0F);
			if (recoveryTicks > 0) {
				target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, recoveryTicks, 1, true, true));
			}
		}
		EntityData.setInt(target, extractingSinceDataId, 0);
	}

	private static boolean isExtractionTargetInView(Player player, Mob target) {
		if (!target.isAlive() || isSpirit(target) || isHusk(target)) {
			return false;
		}

		if (player.distanceTo(target) > 6.0) {
			return false;
		}

		return target.isLookingAtMe(player, 0.15, false, false, target.getY(0.5));
	}

	private static void spawnExtractionParticle(ServerLevel serverLevel, Mob target) {
		Vec3 position = getRandomBodyPosition(target);
		serverLevel.sendParticles(ParticleTypes.SOUL, position.x, position.y, position.z, 0, 0.0, 0.02, 0.0, 1.0);
	}

	private static void extractSpirit(ServerLevel serverLevel, Player player, Mob target) {
		if (!canExtractSpirit(player, target)) {
			return;
		}

		if (isAtSpiritLimit(serverLevel.getServer(), player.getUUID())) {
			showSpiritLimitReached(player);
			return;
		}

		CompoundTag spiritData = saveMobCopyData(serverLevel, target);
		removeCarriedItems(spiritData);
		Mob spirit = loadMobCopy(serverLevel, spiritData);
		if (spirit == null) {
			return;
		}

		setSpiritOwner(spirit, player);
		setStoredString(spirit, spiritHuskDataId, target.getStringUUID());
		makeSpirit(serverLevel, spirit, player.getUUID(), 60);

		turnIntoHusk(target);
		playSoulEffects(serverLevel, target);
		emptySpiritLantern(player);
		showMessage(player, Component.translatable("collective.spiritcompanions.message.extractedspirit", target.getType().getDescription()), ChatFormatting.DARK_GREEN);
	}

	public static void tellSpiritLanternIsEmpty(Player player) {
		long gameTime = player.level().getGameTime();
		Long lastInteractionTick = Variables.lastEmptyLanternInteractionTicks.get(player);
		Variables.lastEmptyLanternInteractionTicks.put(player, gameTime);
		if (lastInteractionTick != null && gameTime - lastInteractionTick <= 6) {
			return;
		}

		showHint(player, Component.translatable("collective.spiritcompanions.message.spiritlanternempty"));
	}

	private static void emptySpiritLantern(Player player) {
		Variables.lastEmptyLanternInteractionTicks.put(player, player.level().getGameTime());
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack lantern = player.getItemInHand(hand);
			if (lantern.is(SpiritItems.SPIRIT_LANTERN)) {
				player.setItemInHand(hand, ItemUtils.createFilledResult(lantern, player, new ItemStack(SpiritItems.EMPTY_SPIRIT_LANTERN)));
				return;
			}
		}
	}

	public static boolean isRefusedByCampfire(BlockState campfireState, ItemStack itemStack) {
		return itemStack.is(SpiritItems.EMPTY_SPIRIT_LANTERN) && !campfireState.is(Blocks.SOUL_CAMPFIRE);
	}

	public static void refuseNormalCampfire(LivingEntity sourceEntity) {
		if (sourceEntity instanceof Player player) {
			showHint(player, Component.translatable("collective.spiritcompanions.message.needssoulcampfire"));
		}
	}

	private static boolean canExtractSpirit(Player player, Mob target) {
		if (target instanceof EnderDragon || target instanceof WitherBoss) {
			return false;
		}

		if (isSpirit(target) || isHusk(target)) {
			return false;
		}

		if (target instanceof TamableAnimal tamableAnimal && tamableAnimal.isTame() && !tamableAnimal.isOwnedBy(player)) {
			return false;
		}

		if (target instanceof AbstractHorse horse && horse.getOwnerReference() != null && !horse.getOwnerReference().matches(player)) {
			return false;
		}

		return true;
	}

	public static boolean isAtSpiritLimit(MinecraftServer server, UUID ownerUUID) {
		return countSpiritsOf(server, ownerUUID) >= ConfigHandler.maxSpiritsPerPlayer;
	}

	private static int countSpiritsOf(MinecraftServer server, UUID ownerUUID) {
		int spiritCount = SpiritWorld.countStoredFollowingSpirits(server, ownerUUID);
		for (ServerLevel level : server.getAllLevels()) {
			for (Entity entity : level.getAllEntities()) {
				if (!(entity instanceof Mob mob)) {
					continue;
				}

				if (isSpirit(mob) && !isSpiritStaying(mob) && ownerUUID.equals(getSpiritOwnerUUID(mob))) {
					spiritCount++;
				}
			}
		}
		return spiritCount;
	}

	public static boolean refusesToFollow(TamableAnimal spirit) {
		if (!isSpirit(spirit) || !spirit.isOrderedToSit()) {
			return false;
		}

		if (!(spirit.level() instanceof ServerLevel serverLevel)) {
			return false;
		}

		UUID ownerUUID = getSpiritOwnerUUID(spirit);
		if (ownerUUID == null || !isAtSpiritLimit(serverLevel.getServer(), ownerUUID)) {
			return false;
		}

		if (spirit.getOwner() instanceof Player owner) {
			showSpiritLimitReached(owner);
		}
		return true;
	}

	public static void ringSpiritBell(ServerLevel serverLevel, Player player, Mob spirit) {
		if (!isSpiritOwnedBy(spirit, player)) {
			showMessage(player, Component.translatable("collective.spiritcompanions.message.notyourspirit"), ChatFormatting.RED);
			return;
		}

		SpiritState spiritState = getSpiritState(spirit);
		long gameTime = serverLevel.getGameTime();
		if (gameTime - spiritState.releaseRequestedTick > 100) {
			spiritState.releaseRequestedTick = gameTime;
			serverLevel.playSound(null, spirit.getX(), spirit.getY(), spirit.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 0.6F, 1.4F);
			showHint(player, Component.translatable("collective.spiritcompanions.message.ringagaintorelease", spirit.getType().getDescription()));
			return;
		}

		releaseSpirit(serverLevel, player, spirit);
	}

	private static void releaseSpirit(ServerLevel serverLevel, Player player, Mob spirit) {
		serverLevel.playSound(null, spirit.getX(), spirit.getY(), spirit.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.0F, 1.2F);
		playSoulEffects(serverLevel, spirit);

		Entity husk = getSpiritHusk(serverLevel, spirit);
		if (husk instanceof Mob huskMob && isHusk(huskMob)) {
			SpiritState spiritState = getSpiritState(spirit);
			spiritState.returningToHusk = huskMob;
			spiritState.isWalking = false;
			spiritState.flightMovement = Vec3.ZERO;
			showMessage(player, Component.translatable("collective.spiritcompanions.message.spiritreturning", spirit.getType().getDescription()), ChatFormatting.DARK_GREEN);
			return;
		}

		fadeSpirit(serverLevel, spirit);
		showMessage(player, Component.translatable("collective.spiritcompanions.message.spiritfaded", spirit.getType().getDescription()), ChatFormatting.DARK_GREEN);
	}

	private static void fadeSpirit(ServerLevel serverLevel, Mob spirit) {
		playSoulEffects(serverLevel, spirit);
		spirit.discard();
	}

	private static void updateReturningSpirit(ServerLevel serverLevel, Mob spirit, Mob husk) {
		spirit.noPhysics = true;
		spirit.setOnGround(false);

		if (husk.isRemoved() || !isHusk(husk)) {
			fadeSpirit(serverLevel, spirit);
			return;
		}

		if (spirit.distanceTo(husk) > 0.6) {
			return;
		}

		reviveHusk(husk);
		playSoulEffects(serverLevel, husk);
		spirit.discard();

		LivingEntity owner = getSpiritOwner(spirit);
		if (owner instanceof Player ownerPlayer) {
			showMessage(ownerPlayer, Component.translatable("collective.spiritcompanions.message.spiritreturnedtobody", spirit.getType().getDescription()), ChatFormatting.DARK_GREEN);
		}
	}

	private static Entity getSpiritHusk(ServerLevel serverLevel, Mob spirit) {
		String huskUUID = getStoredString(spirit, spiritHuskDataId);
		if (huskUUID.isEmpty()) {
			return null;
		}
		return serverLevel.getEntity(UUID.fromString(huskUUID));
	}

	public static boolean isSpiritItem(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return false;
		}

		return itemStack.is(SpiritItems.SPIRIT_LANTERN) || itemStack.is(SpiritItems.EMPTY_SPIRIT_LANTERN) || itemStack.is(SpiritItems.SPIRIT_BELL) || itemStack.is(SpiritItems.SPIRIT_COLLAR) || itemStack.is(SpiritItems.SPIRIT_CANISTER);
	}

	public static boolean holdsSpirit(ItemStack canister) {
		CustomData customData = canister.get(DataComponents.CUSTOM_DATA);
		if (customData == null) {
			return false;
		}
		return customData.copyTag().contains(canisterSpiritKey);
	}

	private static CompoundTag getCanisterSpiritData(ItemStack canister) {
		CustomData customData = canister.get(DataComponents.CUSTOM_DATA);
		if (customData == null) {
			return new CompoundTag();
		}
		return customData.copyTag().getCompoundOrEmpty(canisterSpiritKey);
	}

	public static void storeSpiritInCanister(ServerLevel serverLevel, Player player, InteractionHand hand, ItemStack canister, Mob spirit) {
		if (holdsSpirit(canister)) {
			showHint(player, Component.translatable("collective.spiritcompanions.message.canisterfull"));
			return;
		}

		if (!isSpiritOwnedBy(spirit, player)) {
			showMessage(player, Component.translatable("collective.spiritcompanions.message.notyourspirit"), ChatFormatting.RED);
			return;
		}

		if (getSpiritState(spirit).returningToHusk != null) {
			return;
		}

		CompoundTag canisterData = new CompoundTag();
		canisterData.put(canisterSpiritKey, saveMobCopyData(serverLevel, spirit));

		ItemStack filledCanister = canister.copyWithCount(1);
		filledCanister.set(DataComponents.CUSTOM_DATA, CustomData.of(canisterData));
		filledCanister.set(DataComponents.MAX_STACK_SIZE, 1);
		filledCanister.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("collective.spiritcompanions.tooltip.canistercontains", spirit.getType().getDescription()).withStyle(ChatFormatting.AQUA))));
		player.setItemInHand(hand, ItemUtils.createFilledResult(canister, player, filledCanister));

		spirit.ejectPassengers();
		playSoulEffects(serverLevel, spirit);
		spirit.discard();
		showMessage(player, Component.translatable("collective.spiritcompanions.message.spiritstoredincanister", spirit.getType().getDescription()), ChatFormatting.DARK_GREEN);
	}

	public static void releaseSpiritFromCanister(ServerLevel serverLevel, Player player, InteractionHand hand, ItemStack canister, BlockPos releasePos) {
		if (isAtSpiritLimit(serverLevel.getServer(), player.getUUID())) {
			showSpiritLimitReached(player);
			return;
		}

		Mob spirit = loadMobCopy(serverLevel, getCanisterSpiritData(canister));
		if (spirit == null) {
			return;
		}

		spirit.snapTo(releasePos.getX() + 0.5, releasePos.getY(), releasePos.getZ() + 0.5, player.getYRot() + 180.0F, 0.0F);
		setSpiritOwner(spirit, player);
		if (spirit instanceof TamableAnimal tamableAnimal) {
			tamableAnimal.setOrderedToSit(false);
			tamableAnimal.setInSittingPose(false);
		}
		setStoredInt(spirit, stayingDataId, 0);
		makeSpirit(serverLevel, spirit, player.getUUID(), 20);
		playSoulEffects(serverLevel, spirit);

		ItemStack emptyCanister = canister.copyWithCount(1);
		emptyCanister.remove(DataComponents.CUSTOM_DATA);
		emptyCanister.remove(DataComponents.MAX_STACK_SIZE);
		emptyCanister.remove(DataComponents.LORE);
		player.setItemInHand(hand, ItemUtils.createFilledResult(canister, player, emptyCanister));
		showMessage(player, Component.translatable("collective.spiritcompanions.message.spiritreleasedfromcanister", spirit.getType().getDescription()), ChatFormatting.DARK_GREEN);
	}

	public static boolean isStayToggleInteraction(Mob mob, Player player, InteractionHand hand) {
		if (mob instanceof TamableAnimal) {
			return false;
		}

		if (!player.isShiftKeyDown()) {
			return false;
		}

		if (!player.getItemInHand(hand).isEmpty()) {
			return false;
		}

		return isSpirit(mob);
	}

	public static boolean toggleSpiritStaying(Player player, Mob spirit) {
		if (!isSpiritOwnedBy(spirit, player)) {
			showMessage(player, Component.translatable("collective.spiritcompanions.message.notyourspirit"), ChatFormatting.RED);
			return false;
		}

		if (EntityData.getInt(spirit, stayingDataId, 0) == 1) {
			if (isAtSpiritLimit(player.level().getServer(), player.getUUID())) {
				showSpiritLimitReached(player);
				return false;
			}

			getSpiritState(spirit).landingPosition = null;
			EntityData.setInt(spirit, stayingDataId, 0);
			showMessage(player, Component.translatable("collective.spiritcompanions.message.spiritfollows", spirit.getType().getDescription()), ChatFormatting.DARK_GREEN);
		}
		else {
			getSpiritState(spirit).landingPosition = null;
			EntityData.setInt(spirit, stayingDataId, 1);
			showMessage(player, Component.translatable("collective.spiritcompanions.message.spiritstays", spirit.getType().getDescription()), ChatFormatting.DARK_GREEN);
		}
		return true;
	}

	public static void putOnSpiritCollar(Player player, TamableAnimal pet, ItemStack itemStack) {
		if (!(pet instanceof Cat || pet instanceof Wolf) || !pet.isOwnedBy(player) || isSpirit(pet)) {
			showMessage(player, Component.translatable("collective.spiritcompanions.message.cannotwearspiritcollar"), ChatFormatting.RED);
			return;
		}

		if (hasSpiritCollar(pet)) {
			showHint(player, Component.translatable("collective.spiritcompanions.message.alreadywearsspiritcollar", pet.getType().getDescription()));
			return;
		}

		EntityData.setInt(pet, spiritCollarDataId, 1);
		pet.setPersistenceRequired();
		itemStack.shrink(1);
		pet.playSound(SoundEvents.SOUL_ESCAPE.value(), 1.0F, 1.2F);
		showMessage(player, Component.translatable("collective.spiritcompanions.message.putonspiritcollar", pet.getType().getDescription()), ChatFormatting.DARK_GREEN);
	}

	public static void updateBond(Mob pet) {
		if (!(pet instanceof OwnableEntity ownableEntity)) {
			return;
		}

		if (!(ownableEntity.getOwner() instanceof Player owner)) {
			return;
		}

		if (owner.level() != pet.level()) {
			return;
		}

		if (!pet.hasPassenger(owner) && pet.distanceTo(owner) > 16.0) {
			return;
		}

		int bondTicks = EntityData.getInt(pet, bondTicksDataId, 0);
		if (bondTicks >= maxBondTicks) {
			return;
		}

		setStoredInt(pet, bondTicksDataId, Math.min(bondTicks + 20, maxBondTicks));
	}

	private static boolean rollsBondReturn(Mob pet) {
		int bondDays = EntityData.getInt(pet, bondTicksDataId, 0) / 24000;
		return pet.getRandom().nextFloat() < bondDays / 7.0F;
	}

	public static void onPetDeath(ServerLevel serverLevel, Mob pet) {
		if (isSpirit(pet) || isHusk(pet)) {
			return;
		}

		if (!(pet instanceof OwnableEntity ownableEntity)) {
			return;
		}

		EntityReference<LivingEntity> ownerReference = ownableEntity.getOwnerReference();
		if (ownerReference == null) {
			return;
		}

		UUID ownerUUID = ownerReference.getUUID();
		boolean wearsSpiritCollar = hasSpiritCollar(pet);
		if (!wearsSpiritCollar) {
			if (!rollsBondReturn(pet) || isAtSpiritLimit(serverLevel.getServer(), ownerUUID)) {
				return;
			}
		}

		CompoundTag petData = saveMobCopyData(serverLevel, pet);
		removeCarriedItems(petData);
		Variables.pendingSpiritReturns.put(pet, new PendingSpiritReturn(petData, ownerUUID, wearsSpiritCollar));
	}

	private static void removeCarriedItems(CompoundTag mobData) {
		mobData.remove("equipment");
		mobData.remove("Items");
		mobData.putBoolean("ChestedHorse", false);
	}

	public static void tickDyingPet(ServerLevel serverLevel, Mob body) {
		PendingSpiritReturn pendingSpiritReturn = Variables.pendingSpiritReturns.get(body);
		if (pendingSpiritReturn == null) {
			return;
		}

		if (body.deathTime < 5) {
			return;
		}

		Variables.pendingSpiritReturns.remove(body);
		EntityData.setInt(body, spiritCollarDataId, 0);
		returnPetAsSpirit(serverLevel, pendingSpiritReturn, body);
	}

	private static void returnPetAsSpirit(ServerLevel serverLevel, PendingSpiritReturn pendingSpiritReturn, Mob body) {
		Mob spirit = loadMobCopy(serverLevel, pendingSpiritReturn.petData);
		if (spirit == null) {
			return;
		}

		UUID ownerUUID = pendingSpiritReturn.ownerUUID;
		spirit.snapTo(body.getX(), body.getY(), body.getZ(), body.getYRot(), body.getXRot());
		setStoredInt(spirit, spiritCollarDataId, 0);
		spirit.setHealth(spirit.getMaxHealth());
		makeSpirit(serverLevel, spirit, ownerUUID, ConfigHandler.collarSpiritReturnSeconds * 20);
		playSoulEffects(serverLevel, spirit);

		Player owner = serverLevel.getPlayerByUUID(ownerUUID);
		if (owner == null) {
			return;
		}

		if (pendingSpiritReturn.wasWearingSpiritCollar) {
			showMessage(owner, Component.translatable("collective.spiritcompanions.message.spiritreturnedfromcollar", spirit.getType().getDescription()), ChatFormatting.DARK_GREEN);
		}
		else {
			showMessage(owner, Component.translatable("collective.spiritcompanions.message.spiritreturnedfrombond", spirit.getType().getDescription()), ChatFormatting.DARK_GREEN);
		}
	}

	public static boolean isSpiritStaying(Mob spirit) {
		if (spirit instanceof TamableAnimal tamableAnimal && tamableAnimal.isOrderedToSit()) {
			return true;
		}

		return EntityData.getInt(spirit, stayingDataId, 0) == 1;
	}

	public static SpiritState getSpiritState(Mob spirit) {
		if (spirit.level().isClientSide()) {
			return new SpiritState();
		}

		SpiritState spiritState = Variables.spiritStates.get(spirit);
		if (spiritState == null) {
			spiritState = new SpiritState();
			spiritState.orbitStartTicks = spirit.getRandom().nextInt(420);
			Variables.spiritStates.put(spirit, spiritState);
		}
		return spiritState;
	}

	public static void updateSpirit(ServerLevel serverLevel, Mob spirit) {
		keepFriendly(spirit);

		if (getSpiritAppearProgress(spirit) < 1.0F && spirit.getRandom().nextInt(4) == 0) {
			spawnExtractionParticle(serverLevel, spirit);
		}

		Mob returningToHusk = getSpiritState(spirit).returningToHusk;
		if (returningToHusk != null) {
			updateReturningSpirit(serverLevel, spirit, returningToHusk);
			return;
		}

		if (SpiritWorld.tryEnterSpiritWorld(serverLevel, spirit)) {
			return;
		}

		updateSpiritMovement(spirit);
	}

	private static void keepFriendly(Mob spirit) {
		if (spirit.getTarget() != null) {
			spirit.setTarget(null);
		}

		spirit.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);

		if (spirit instanceof Creeper creeper && creeper.getSwellDir() > 0) {
			creeper.setSwellDir(-1);
		}

		spirit.clearFire();
	}

	private static void updateSpiritMovement(Mob spirit) {
		SpiritState spiritState = getSpiritState(spirit);
		if (spirit.isVehicle()) {
			walkAsMount(spirit, spiritState);
			return;
		}

		LivingEntity owner = getSpiritOwner(spirit);

		updateOwnerMovementTicks(spirit, spiritState, owner);
		updateFlightTicks(spirit, spiritState);

		boolean followsOwnerMovement;
		if (spiritState.isFlyingAround) {
			followsOwnerMovement = spiritState.ownerStillTicks <= spiritState.landingDelayTicks;
		}
		else {
			followsOwnerMovement = spiritState.ownerMovingTicks > spiritState.takeOffDelayTicks;
		}

		boolean isStaying = isSpiritStaying(spirit);
		boolean canFly = owner != null && !isStaying;
		spiritState.isFlyingAround = canFly && (followsOwnerMovement || spiritState.flightTicksLeft > 0);

		if (spiritState.isFlyingAround) {
			if (spiritState.isWalking) {
				spiritState.flightMovement = Vec3.ZERO;
			}
			spiritState.isWalking = false;
			spiritState.landingPosition = null;
		}
		else if (!spiritState.isWalking) {
			if (spiritState.landingPosition == null) {
				if (isStaying) {
					spiritState.landingPosition = findLandingPositionBelow(spirit);
				}
				else {
					spiritState.landingPosition = findLandingPosition(spirit, owner);
				}
			}

			if (hasLanded(spirit, spiritState.landingPosition)) {
				spiritState.isWalking = true;
				spiritState.landingPosition = null;
			}
		}
		else if (isStaying) {
			spirit.getNavigation().stop();
		}
		else {
			walkBackToOwner(spirit, owner);
		}

		spirit.noPhysics = !spiritState.isWalking;
		spirit.resetFallDistance();

		if (!spiritState.isWalking) {
			spirit.setOnGround(false);
		}
	}

	private static void walkAsMount(Mob spirit, SpiritState spiritState) {
		spiritState.isWalking = true;
		spiritState.isFlyingAround = false;
		spiritState.landingPosition = null;
		spiritState.flightTicksLeft = 0;
		spirit.noPhysics = false;
		spirit.resetFallDistance();

		if (hasSpiritSaddle(spirit)) {
			return;
		}

		dropRidersThroughSpirit(spirit);
	}

	public static boolean hasSpiritSaddle(LivingEntity spirit) {
		return spirit.getItemBySlot(EquipmentSlot.SADDLE).is(SpiritItems.SPIRIT_SADDLE);
	}

	private static void dropRidersThroughSpirit(Mob spirit) {
		List<Entity> riders = new ArrayList<>(spirit.getPassengers());
		for (Entity rider : riders) {
			double seatY = rider.getY();
			rider.stopRiding();
			rider.teleportTo(spirit.getX(), seatY, spirit.getZ());

			if (rider instanceof Player player) {
				player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 30, 0, false, false, false));
				showHint(player, Component.translatable("collective.spiritcompanions.message.needsspiritsaddle", spirit.getType().getDescription()));
			}
		}
	}

	public static boolean refusesNormalSaddle(Mob mob, ItemStack itemStack) {
		return isSpirit(mob) && itemStack.is(Items.SADDLE) && mob.isEquippableInSlot(itemStack, EquipmentSlot.SADDLE);
	}

	public static boolean isReturningToHusk(Mob spirit) {
		return getSpiritState(spirit).returningToHusk != null;
	}

	public static void refuseNormalSaddle(Player player, Mob spirit) {
		showHint(player, Component.translatable("collective.spiritcompanions.message.cannotholdnormalsaddle", spirit.getType().getDescription()));
	}

	private static void showSpiritLimitReached(Player player) {
		showMessage(player, Component.translatable("collective.spiritcompanions.message.maxspirits", ConfigHandler.maxSpiritsPerPlayer), ChatFormatting.RED);
	}

	private static void showHint(Player player, MutableComponent hint) {
		showMessage(player, hint, ChatFormatting.YELLOW);
	}

	private static void showMessage(Player player, MutableComponent message, ChatFormatting colour) {
		player.sendOverlayMessage(message.withStyle(colour));
	}

	private static void walkBackToOwner(Mob spirit, LivingEntity owner) {
		if (owner == null) {
			return;
		}

		if (spirit instanceof TamableAnimal) {
			return;
		}

		double walkBackDistance = Math.max(6.0, ConfigHandler.spiritOrbitDistance + 2.0);
		if (spirit.distanceTo(owner) < walkBackDistance) {
			return;
		}

		if (!spirit.getNavigation().isDone()) {
			return;
		}

		spirit.getNavigation().moveTo(owner, 1.0);
	}

	private static void updateOwnerMovementTicks(Mob spirit, SpiritState spiritState, LivingEntity owner) {
		if (owner == null) {
			return;
		}

		RandomSource random = spirit.getRandom();
		Vec3 ownerPosition = owner.position();
		if (ownerPosition.distanceTo(spiritState.lastOwnerPosition) > 0.05) {
			if (spiritState.ownerMovingTicks == 0) {
				spiritState.takeOffDelayTicks = 5 + random.nextInt(35);
			}
			spiritState.ownerMovingTicks++;
			spiritState.ownerStillTicks = 0;
		}
		else {
			if (spiritState.ownerStillTicks == 0) {
				spiritState.landingDelayTicks = 20 + random.nextInt(80);
			}
			spiritState.ownerStillTicks++;
			spiritState.ownerMovingTicks = 0;
		}

		spiritState.lastOwnerPosition = ownerPosition;
	}

	private static void updateFlightTicks(Mob spirit, SpiritState spiritState) {
		if (spiritState.flightTicksLeft > 0) {
			spiritState.flightTicksLeft--;
			return;
		}

		if (!spiritState.isWalking) {
			return;
		}

		if (ConfigHandler.averageSecondsBetweenSpontaneousFlights == 0) {
			return;
		}

		RandomSource random = spirit.getRandom();
		if (random.nextInt(ConfigHandler.averageSecondsBetweenSpontaneousFlights * 20) == 0) {
			spiritState.flightTicksLeft = 100 + random.nextInt(100);
		}
	}

	private static Vec3 findLandingPosition(Mob spirit, LivingEntity owner) {
		if (owner == null) {
			return spirit.position();
		}

		RandomSource random = spirit.getRandom();
		BlockPos ownerPos = owner.blockPosition();

		for (int attempt = 0; attempt < 10; attempt++) {
			int x = Mth.floor(spirit.getX() + random.nextDouble() * 3.0 - 1.5);
			int z = Mth.floor(spirit.getZ() + random.nextDouble() * 3.0 - 1.5);

			double distanceToOwner = owner.position().distanceTo(new Vec3(x + 0.5, owner.getY(), z + 0.5));
			if (distanceToOwner < 1.0 || distanceToOwner > ConfigHandler.spiritOrbitDistance) {
				continue;
			}

			for (int yOffset = 1; yOffset >= -2; yOffset--) {
				BlockPos landingPos = new BlockPos(x, ownerPos.getY() + yOffset, z);
				if (isLandingSpot(spirit, landingPos)) {
					return Vec3.atBottomCenterOf(landingPos);
				}
			}
		}

		return owner.position();
	}

	private static Vec3 findLandingPositionBelow(Mob spirit) {
		BlockPos spiritPos = spirit.blockPosition();
		for (int yOffset = 0; yOffset >= -16; yOffset--) {
			BlockPos landingPos = spiritPos.offset(0, yOffset, 0);
			if (isLandingSpot(spirit, landingPos)) {
				return Vec3.atBottomCenterOf(landingPos);
			}
		}

		return spirit.position();
	}

	private static boolean isLandingSpot(Mob spirit, BlockPos landingPos) {
		Level level = spirit.level();
		if (!level.getFluidState(landingPos).isEmpty()) {
			return false;
		}

		BlockPos groundPos = landingPos.below();
		if (!level.getBlockState(groundPos).isFaceSturdy(level, groundPos, Direction.UP)) {
			return false;
		}

		AABB landingBox = spirit.getDimensions(spirit.getPose()).makeBoundingBox(Vec3.atBottomCenterOf(landingPos));
		return level.noCollision(spirit, landingBox);
	}

	private static boolean hasLanded(Mob spirit, Vec3 landingPosition) {
		if (!spirit.level().noCollision(spirit)) {
			return false;
		}

		return spirit.position().distanceTo(landingPosition) < 0.5;
	}

	public static boolean isSpiritWalking(Mob spirit) {
		if (spirit.isVehicle()) {
			return true;
		}
		return getSpiritState(spirit).isWalking;
	}

	public static void flySpirit(Mob spirit) {
		SpiritState spiritState = getSpiritState(spirit);
		double maxSpeed = 0.35;
		if (spiritState.returningToHusk != null) {
			maxSpeed = 0.12;
		}

		Vec3 targetPosition = getSpiritTargetPosition(spirit);
		Vec3 desiredMovement = targetPosition.subtract(spirit.position()).scale(0.08);
		if (desiredMovement.length() > maxSpeed) {
			desiredMovement = desiredMovement.normalize().scale(maxSpeed);
		}

		Vec3 movement = spiritState.flightMovement.lerp(desiredMovement, 0.15);
		spiritState.flightMovement = movement;
		spirit.setDeltaMovement(movement);
		spirit.move(MoverType.SELF, movement);

		if (movement.horizontalDistanceSqr() > 0.0001) {
			float yaw = (float)Mth.atan2(movement.z, movement.x) * Mth.RAD_TO_DEG - 90.0F;
			spirit.setYRot(yaw);
			spirit.yBodyRot = yaw;
		}
	}

	private static Vec3 getSpiritTargetPosition(Mob spirit) {
		SpiritState spiritState = getSpiritState(spirit);
		if (spiritState.returningToHusk != null) {
			return spiritState.returningToHusk.position();
		}

		LivingEntity owner = getSpiritOwner(spirit);
		if (owner == null) {
			return spirit.position();
		}

		if (!spiritState.isFlyingAround) {
			if (spiritState.landingPosition == null) {
				return owner.position();
			}
			return spiritState.landingPosition;
		}

		float orbitAngle = (spirit.tickCount + spiritState.orbitStartTicks) * 0.015F;
		float bobOffset = Mth.sin(spirit.tickCount * 0.04F) * 0.25F;
		double orbitDistance = ConfigHandler.spiritOrbitDistance;
		return owner.position().add(Mth.cos(orbitAngle) * orbitDistance, 1.0 + bobOffset, Mth.sin(orbitAngle) * orbitDistance);
	}

	public static boolean spiritIgnoresDamage(DamageSource source) {
		return !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
	}

	public static float getSpiritPitch(float pitch) {
		return pitch * 0.8F;
	}

	public static void playSpiritWhisper(LivingEntity spirit) {
		if (spirit.isSilent()) {
			return;
		}

		float pitch = 0.9F + spirit.getRandom().nextFloat() * 0.3F;
		spirit.level().playSound(null, spirit.getX(), spirit.getY(), spirit.getZ(), SoundEvents.SOUL_ESCAPE.value(), spirit.getSoundSource(), 0.8F, pitch);
	}

	public static List<Identifier> getSpiritEntityTypeIds(ServerLevel serverLevel) {
		if (!Variables.spiritEntityTypeIds.isEmpty()) {
			return Variables.spiritEntityTypeIds;
		}

		List<Identifier> spiritEntityTypeIds = new ArrayList<>();
		for (EntityType<?> entityType : BuiltInRegistries.ENTITY_TYPE) {
			if (!entityType.canSummon() || !entityType.isEnabled(serverLevel.enabledFeatures())) {
				continue;
			}

			Entity entity = entityType.create(serverLevel, EntitySpawnReason.COMMAND);
			if (entity instanceof Mob) {
				spiritEntityTypeIds.add(EntityType.getKey(entityType));
			}
		}

		Variables.spiritEntityTypeIds = spiritEntityTypeIds;
		return spiritEntityTypeIds;
	}

	@SuppressWarnings("deprecation")
	public static Mob summonSpirit(ServerLevel serverLevel, Player owner, EntityType<?> entityType) {
		Entity entity = entityType.create(serverLevel, EntitySpawnReason.COMMAND);
		if (!(entity instanceof Mob spirit)) {
			return null;
		}

		float yaw = serverLevel.getRandom().nextFloat() * 360.0F;
		spirit.snapTo(owner.getX(), owner.getY(), owner.getZ(), yaw, 0.0F);
		spirit.yHeadRot = yaw;
		spirit.yBodyRot = yaw;
		spirit.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(spirit.blockPosition()), EntitySpawnReason.COMMAND, null);

		setSpiritOwner(spirit, owner);
		makeSpirit(serverLevel, spirit, owner.getUUID(), 60);
		return spirit;
	}
}
