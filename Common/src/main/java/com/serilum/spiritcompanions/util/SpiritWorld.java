package com.serilum.spiritcompanions.util;

import com.natamus.collective.functions.WorldFunctions;
import com.serilum.spiritcompanions.config.ConfigHandler;
import com.serilum.spiritcompanions.data.Constants;
import com.serilum.spiritcompanions.data.Variables;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.Vec3;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SpiritWorld {
	public static boolean tryEnterSpiritWorld(ServerLevel serverLevel, Mob spirit) {
		UUID ownerUUID = Util.getSpiritOwnerUUID(spirit);
		if (ownerUUID == null) {
			return false;
		}

		ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
		if (!shouldEnterSpiritWorld(spirit, owner)) {
			return false;
		}

		sendToSpiritWorld(serverLevel, spirit, ownerUUID);
		return true;
	}

	private static boolean shouldEnterSpiritWorld(Mob spirit, ServerPlayer owner) {
		if (Util.getSpiritState(spirit).returningToHusk != null) {
			return false;
		}

		if (owner == null) {
			return true;
		}

		int spiritWorldDistance = getSpiritWorldDistance();
		if (spiritWorldDistance == 0) {
			return false;
		}

		if (owner.level() != spirit.level()) {
			return true;
		}

		return owner.distanceTo(spirit) > spiritWorldDistance;
	}

	private static int getSpiritWorldDistance() {
		if (ConfigHandler.spiritWorldDistance == 0) {
			return 0;
		}
		return Math.max(ConfigHandler.spiritWorldDistance, 16);
	}

	private static void sendToSpiritWorld(ServerLevel serverLevel, Mob spirit, UUID ownerUUID) {
		CompoundTag storedSpirit = new CompoundTag();
		storedSpirit.put("entity", Util.saveMobCopyData(serverLevel, spirit));
		storedSpirit.putBoolean("staying", Util.isSpiritStaying(spirit));
		storedSpirit.putString("dimension", serverLevel.dimension().identifier().toString());
		storedSpirit.putDouble("x", spirit.getX());
		storedSpirit.putDouble("y", spirit.getY());
		storedSpirit.putDouble("z", spirit.getZ());

		MinecraftServer server = serverLevel.getServer();
		ListTag storedSpirits = getStoredSpirits(server, ownerUUID);
		storedSpirits.add(storedSpirit);
		saveStoredSpirits(server, ownerUUID, storedSpirits);

		spirit.ejectPassengers();
		Util.playSoulEffects(serverLevel, spirit);
		spirit.discard();
	}

	public static void sendAllSpiritsToSpiritWorld(ServerPlayer owner) {
		List<Mob> ownedSpirits = new ArrayList<>();
		for (ServerLevel serverLevel : owner.level().getServer().getAllLevels()) {
			for (Entity entity : serverLevel.getAllEntities()) {
				if (!(entity instanceof Mob mob)) {
					continue;
				}

				if (Util.isSpirit(mob) && owner.getUUID().equals(Util.getSpiritOwnerUUID(mob))) {
					ownedSpirits.add(mob);
				}
			}
		}

		for (Mob spirit : ownedSpirits) {
			if (Util.getSpiritState(spirit).returningToHusk != null) {
				continue;
			}

			sendToSpiritWorld((ServerLevel)spirit.level(), spirit, owner.getUUID());
		}
	}

	public static void updateOwnerSpirits(ServerPlayer owner) {
		if (!owner.isAlive()) {
			return;
		}

		sendFarSpiritsToSpiritWorld(owner);
		returnSpiritsFromSpiritWorld(owner);
	}

	private static void sendFarSpiritsToSpiritWorld(ServerPlayer owner) {
		List<Mob> farSpirits = new ArrayList<>();
		for (Mob spirit : Variables.spiritStates.keySet()) {
			if (spirit.isRemoved() || !Util.isSpirit(spirit)) {
				continue;
			}

			if (!owner.getUUID().equals(Util.getSpiritOwnerUUID(spirit))) {
				continue;
			}

			if (shouldEnterSpiritWorld(spirit, owner)) {
				farSpirits.add(spirit);
			}
		}

		for (Mob spirit : farSpirits) {
			sendToSpiritWorld((ServerLevel)spirit.level(), spirit, owner.getUUID());
		}
	}

	private static void returnSpiritsFromSpiritWorld(ServerPlayer owner) {
		MinecraftServer server = owner.level().getServer();
		ListTag storedSpirits = getStoredSpirits(server, owner.getUUID());
		if (storedSpirits.isEmpty()) {
			return;
		}

		ListTag remainingSpirits = new ListTag();
		for (int i = 0; i < storedSpirits.size(); i++) {
			CompoundTag storedSpirit = storedSpirits.getCompoundOrEmpty(i);
			Vec3 returnPosition = getReturnPosition(owner, storedSpirit);
			if (returnPosition == null) {
				remainingSpirits.add(storedSpirit);
				continue;
			}

			returnSpirit(owner.level(), owner.getUUID(), storedSpirit.getCompoundOrEmpty("entity"), returnPosition);
		}

		if (remainingSpirits.size() == storedSpirits.size()) {
			return;
		}

		saveStoredSpirits(server, owner.getUUID(), remainingSpirits);
	}

	private static Vec3 getReturnPosition(ServerPlayer owner, CompoundTag storedSpirit) {
		if (!storedSpirit.getBooleanOr("staying", false)) {
			RandomSource random = owner.getRandom();
			return owner.position().add(random.nextDouble() * 6.0 - 3.0, 1.0, random.nextDouble() * 6.0 - 3.0);
		}

		ServerLevel serverLevel = owner.level();
		if (!serverLevel.dimension().identifier().toString().equals(storedSpirit.getStringOr("dimension", ""))) {
			return null;
		}

		Vec3 stayPosition = new Vec3(storedSpirit.getDoubleOr("x", 0.0), storedSpirit.getDoubleOr("y", 0.0), storedSpirit.getDoubleOr("z", 0.0));
		if (!serverLevel.isLoaded(BlockPos.containing(stayPosition))) {
			return null;
		}

		int spiritWorldDistance = getSpiritWorldDistance();
		if (spiritWorldDistance == 0) {
			return stayPosition;
		}

		if (owner.position().distanceTo(stayPosition) > spiritWorldDistance - 4) {
			return null;
		}
		return stayPosition;
	}

	private static void returnSpirit(ServerLevel serverLevel, UUID ownerUUID, CompoundTag spiritData, Vec3 position) {
		Mob spirit = Util.loadMobCopy(serverLevel, spiritData);
		if (spirit == null) {
			return;
		}

		spirit.snapTo(position.x, position.y, position.z, spirit.getYRot(), spirit.getXRot());
		Util.makeSpirit(serverLevel, spirit, ownerUUID, 20);
		Util.playSoulEffects(serverLevel, spirit);
	}

	public static int countStoredSpirits(MinecraftServer server, UUID ownerUUID) {
		return getStoredSpirits(server, ownerUUID).size();
	}

	public static int countStoredFollowingSpirits(MinecraftServer server, UUID ownerUUID) {
		ListTag storedSpirits = getStoredSpirits(server, ownerUUID);
		int followingSpirits = 0;
		for (int i = 0; i < storedSpirits.size(); i++) {
			if (!storedSpirits.getCompoundOrEmpty(i).getBooleanOr("staying", false)) {
				followingSpirits++;
			}
		}
		return followingSpirits;
	}

	private static ListTag getStoredSpirits(MinecraftServer server, UUID ownerUUID) {
		String spiritWorldPath = getSpiritWorldPath(server);
		if (!spiritWorldPath.equals(Variables.spiritWorldPath)) {
			Variables.spiritWorldPath = spiritWorldPath;
			Variables.spiritWorldSpirits.clear();
		}

		ListTag storedSpirits = Variables.spiritWorldSpirits.get(ownerUUID);
		if (storedSpirits == null) {
			storedSpirits = readStoredSpirits(spiritWorldPath, ownerUUID);
			Variables.spiritWorldSpirits.put(ownerUUID, storedSpirits);
		}
		return storedSpirits;
	}

	private static ListTag readStoredSpirits(String spiritWorldPath, UUID ownerUUID) {
		File file = new File(spiritWorldPath + File.separator + ownerUUID + ".snbt");
		if (!file.isFile()) {
			return new ListTag();
		}

		try {
			String content = Files.readString(file.toPath());
			return TagParser.parseCompoundFully(content).getListOrEmpty("spirits");
		}
		catch (Exception ignored) {
			Constants.logger.warn("[" + Reference.NAME + "] Unable to read the spirit world file " + file.getPath() + ".");
		}
		return new ListTag();
	}

	private static void saveStoredSpirits(MinecraftServer server, UUID ownerUUID, ListTag storedSpirits) {
		String spiritWorldPath = getSpiritWorldPath(server);
		Variables.spiritWorldSpirits.put(ownerUUID, storedSpirits);

		File file = new File(spiritWorldPath + File.separator + ownerUUID + ".snbt");
		if (storedSpirits.isEmpty()) {
			boolean ignoredDelete = file.delete();
			return;
		}

		File dir = new File(spiritWorldPath);
		boolean ignoredMkdirs = dir.mkdirs();

		CompoundTag data = new CompoundTag();
		data.put("spirits", storedSpirits);
		try {
			PrintWriter writer = new PrintWriter(file, StandardCharsets.UTF_8);
			writer.println(data);
			writer.close();
		}
		catch (IOException ignored) {
			Constants.logger.warn("[" + Reference.NAME + "] Unable to write the spirit world file " + file.getPath() + ".");
		}
	}

	private static String getSpiritWorldPath(MinecraftServer server) {
		return WorldFunctions.getWorldPath(server) + File.separator + "data" + File.separator + Reference.MOD_ID + File.separator + "spirit_world";
	}
}
