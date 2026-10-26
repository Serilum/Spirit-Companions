package com.serilum.spiritcompanions.data;

import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

import java.util.*;

public class Variables {
	public static WeakHashMap<Mob, SpiritState> spiritStates = new WeakHashMap<>();
	public static Set<Mob> husksMadeThisSession = Collections.newSetFromMap(new WeakHashMap<>());
	public static WeakHashMap<Player, Mob> spiritExtractionTargets = new WeakHashMap<>();
	public static WeakHashMap<Mob, PendingSpiritReturn> pendingSpiritReturns = new WeakHashMap<>();
	public static WeakHashMap<Player, Long> lastEmptyLanternInteractionTicks = new WeakHashMap<>();
	public static HashMap<UUID, ListTag> spiritWorldSpirits = new HashMap<>();

	public static List<ResourceLocation> spiritEntityTypeIds = new ArrayList<>();
	public static HashMap<String, ResourceLocation> spiritTextures = new HashMap<>();

	public static String spiritWorldPath = "";
	public static LivingEntity renderingEntity = null;
	public static boolean isRenderingHorseMarkings = false;
	public static boolean isRenderingSpiritShadow = false;
}
