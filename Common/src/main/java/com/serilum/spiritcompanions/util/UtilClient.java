package com.serilum.spiritcompanions.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.serilum.spiritcompanions.config.ConfigHandler;
import com.serilum.spiritcompanions.data.SpiritItems;
import com.serilum.spiritcompanions.data.Variables;
import com.serilum.spiritcompanions.mixin.ItemPropertiesInvoker;
import com.serilum.spiritcompanions.rendering.SpiritParticle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.io.InputStream;
import java.util.Optional;

public class UtilClient {
	private static final int spiritColourFrom = 0xB4E8FF;
	private static final int spiritColourTo = 0x96CEF5;
	private static final int huskColour = 0xFF8C9096;
	private static final RenderType spiritShadowRenderType = RenderType.entityShadow(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "textures/misc/spirit_shadow.png"));

	public static void registerItemProperties() {
		ItemPropertiesInvoker.invokeRegister(SpiritItems.SPIRIT_LANTERN, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "charging"), (itemStack, level, livingEntity, seed) -> getSpiritLanternCharging(itemStack, livingEntity));
		ItemPropertiesInvoker.invokeRegister(SpiritItems.SPIRIT_CANISTER, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "filled"), (itemStack, level, livingEntity, seed) -> getSpiritCanisterFilled(itemStack));
	}

	private static float getSpiritLanternCharging(ItemStack itemStack, LivingEntity livingEntity) {
		if (livingEntity == null || !livingEntity.isUsingItem() || livingEntity.getUseItem() != itemStack) {
			return 0.0F;
		}
		return Math.max(livingEntity.getTicksUsingItem() * 0.01F, 0.01F);
	}

	private static float getSpiritCanisterFilled(ItemStack itemStack) {
		if (Util.holdsSpirit(itemStack)) {
			return 1.0F;
		}
		return 0.0F;
	}

	public static RenderType getSpiritRenderType(ResourceLocation texture) {
		return RenderType.entityTranslucent(getSpiritTexture(texture));
	}

	private static ResourceLocation getSpiritTexture(ResourceLocation texture) {
		int colourPercentage = ConfigHandler.spiritOriginalColourPercentage;
		if (colourPercentage >= 100) {
			return texture;
		}

		String textureKey = texture + "#" + colourPercentage;
		ResourceLocation spiritTexture = Variables.spiritTextures.get(textureKey);
		if (spiritTexture == null) {
			spiritTexture = createSpiritTexture(texture, colourPercentage);
			Variables.spiritTextures.put(textureKey, spiritTexture);
		}
		return spiritTexture;
	}

	private static ResourceLocation createSpiritTexture(ResourceLocation texture, int colourPercentage) {
		Minecraft minecraft = Minecraft.getInstance();
		Optional<Resource> resource = minecraft.getResourceManager().getResource(texture);
		if (!resource.isPresent()) {
			return texture;
		}

		try {
			InputStream inputStream = resource.get().open();
			NativeImage image = NativeImage.read(inputStream);
			inputStream.close();
			desaturateImage(image, colourPercentage / 100.0F);

			ResourceLocation spiritTexture = ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit/" + texture.getNamespace() + "/" + texture.getPath() + "/" + colourPercentage);
			minecraft.getTextureManager().register(spiritTexture, new DynamicTexture(image));
			return spiritTexture;
		}
		catch (Exception ignored) {
			return texture;
		}
	}

	private static void desaturateImage(NativeImage image, float colourFraction) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int pixel = image.getPixelRGBA(x, y);
				int grey = Math.round(FastColor.ABGR32.red(pixel) * 0.299F + FastColor.ABGR32.green(pixel) * 0.587F + FastColor.ABGR32.blue(pixel) * 0.114F);
				int red = Math.round(Mth.lerp(colourFraction, grey, FastColor.ABGR32.red(pixel)));
				int green = Math.round(Mth.lerp(colourFraction, grey, FastColor.ABGR32.green(pixel)));
				int blue = Math.round(Mth.lerp(colourFraction, grey, FastColor.ABGR32.blue(pixel)));
				image.setPixelRGBA(x, y, FastColor.ABGR32.color(FastColor.ABGR32.alpha(pixel), blue, green, red));
			}
		}
	}

	public static int getSpiritLight(LivingEntity entity, int packedLight) {
		if (!Util.isSpirit(entity)) {
			return packedLight;
		}
		return LightTexture.FULL_BRIGHT;
	}

	public static HitResult pickBlockForSpiritItems(HitResult blockHitResult, Entity cameraEntity, double range, float partialTicks) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || !isHoldingSpiritItem(player)) {
			return blockHitResult;
		}

		Vec3 from = cameraEntity.getEyePosition(partialTicks);
		Vec3 to = from.add(cameraEntity.getViewVector(partialTicks).scale(range));
		return cameraEntity.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, cameraEntity));
	}

	private static boolean isHoldingSpiritItem(LocalPlayer player) {
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack itemStack = player.getItemInHand(hand);
			if (Util.isSpiritItem(itemStack) || itemStack.is(SpiritItems.SPIRIT_SADDLE)) {
				return true;
			}
		}
		return false;
	}

	public static void applySpiritLanternCharge(PoseStack poseStack, ItemStack itemStack, InteractionHand hand, float partialTicks) {
		if (!itemStack.is(SpiritItems.SPIRIT_LANTERN)) {
			return;
		}

		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null || !player.isUsingItem() || player.getUsedItemHand() != hand) {
			return;
		}

		float ticksCharging = player.getTicksUsingItem() + partialTicks;
		float charge = Math.min(ticksCharging / (ConfigHandler.spiritExtractionSeconds * 20.0F), 1.0F);
		float shake = Mth.sin(ticksCharging * 1.3F) * charge * 0.006F;

		poseStack.translate(0.0F, charge * 0.06F + shake, charge * 0.05F);
		poseStack.scale(1.0F + charge * 0.15F, 1.0F + charge * 0.15F, 1.0F);
	}

	public static boolean isSpiritCompanionsMessage(Component message) {
		if (!(message.getContents() instanceof TranslatableContents translatableContents)) {
			return false;
		}

		String key = translatableContents.getKey();
		return key.startsWith(Reference.MOD_ID + ".") || key.startsWith("collective." + Reference.MOD_ID + ".");
	}

	public static int getOverlayMessageTicks() {
		return ConfigHandler.overlayMessageSeconds * 20;
	}

	public static boolean usesSpiritRendering(LivingEntity entity) {
		if (entity == null) {
			return false;
		}

		return Util.isSpirit(entity) || Util.isHusk(entity);
	}

	public static RenderType getSpiritShadowRenderType(RenderType renderType) {
		if (!Variables.isRenderingSpiritShadow) {
			return renderType;
		}
		return spiritShadowRenderType;
	}

	public static int getSpiritShadowColour(int colour, float y) {
		if (!Variables.isRenderingSpiritShadow) {
			return colour;
		}

		float heightFade = Mth.clamp(1.0F + y, 0.0F, 1.0F);
		float intensity = ConfigHandler.spiritShadowIntensityPercentage / 100.0F;
		return FastColor.ARGB32.color(Math.round(FastColor.ARGB32.alpha(colour) * heightFade * intensity), spiritColourFrom);
	}

	public static int getSpiritLayerColour(LivingEntity entity, int colour) {
		if (entity == null) {
			return colour;
		}

		float extractionProgress = Util.getHuskTintProgress(entity);
		if (extractionProgress >= 0.0F) {
			return FastColor.ARGB32.multiply(colour, FastColor.ARGB32.lerp(extractionProgress, 0xFFFFFFFF, huskColour));
		}

		if (Util.isHusk(entity)) {
			return multiplyAlpha(FastColor.ARGB32.multiply(colour, huskColour), Util.getHuskOpacity(entity));
		}

		if (!Util.isSpirit(entity)) {
			return colour;
		}

		int translucencyPercentage = getTranslucencyPercentage(entity);
		return multiplyAlpha(getSpiritTintedColour(colour, entity.tickCount, translucencyPercentage), Util.getSpiritAppearProgress(entity));
	}

	private static int getTranslucencyPercentage(LivingEntity entity) {
		if (Util.hasSpiritSaddle(entity)) {
			return ConfigHandler.spiritTranslucencyPercentage / 2;
		}
		return ConfigHandler.spiritTranslucencyPercentage;
	}

	public static int getSpiritModelColour(int colour) {
		if (Variables.isRenderingHorseMarkings) {
			return getHorseMarkingsColour(colour);
		}
		return getSpiritEquipmentColour(colour);
	}

	private static int getHorseMarkingsColour(int colour) {
		if (!usesSpiritRendering(Variables.renderingEntity)) {
			return colour;
		}
		return multiplyAlpha(getSpiritLayerColour(Variables.renderingEntity, colour), 0.5F);
	}

	public static int getCollarLayerColour(int colour) {
		LivingEntity entity = Variables.renderingEntity;
		if (entity != null && Util.hasSpiritCollar(entity)) {
			return FastColor.ARGB32.opaque(getShimmerColour(entity.tickCount, ConfigHandler.spiritTranslucencyPercentage));
		}

		return getSpiritLayerColour(entity, colour);
	}

	public static int getSpiritEquipmentColour(int colour) {
		LivingEntity entity = Variables.renderingEntity;
		if (entity == null || !Util.isSpirit(entity)) {
			return colour;
		}

		return FastColor.ARGB32.opaque(getSpiritTintedColour(colour, entity.tickCount, ConfigHandler.spiritTranslucencyPercentage));
	}

	public static int getSpiritTintedColour(int tintedColour, float ageInTicks, int translucencyPercentage) {
		return FastColor.ARGB32.multiply(tintedColour, getShimmerColour(ageInTicks, translucencyPercentage));
	}

	public static int getShimmerColour(float ageInTicks, int translucencyPercentage) {
		float wave = (Mth.sin(ageInTicks * 0.035F) + 1.0F) / 2.0F;
		int rgb = FastColor.ARGB32.lerp(wave, spiritColourFrom, spiritColourTo);

		int baseAlpha = Math.round((100 - translucencyPercentage) * 2.55F);
		int alpha = Mth.clamp(Mth.lerpInt(wave, baseAlpha - 5, baseAlpha + 5), 0, 255);
		return FastColor.ARGB32.color(alpha, rgb);
	}

	private static int multiplyAlpha(int colour, float alphaMultiplier) {
		return FastColor.ARGB32.color(Math.round(FastColor.ARGB32.alpha(colour) * alphaMultiplier), colour);
	}

	public static void spawnSpiritTrailParticles(LivingEntity spirit) {
		if (!ConfigHandler.showSpiritParticleTrail) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.options.particles().get().equals(ParticleStatus.MINIMAL)) {
			return;
		}

		RandomSource random = spirit.getRandom();
		Vec3 movement = spirit.position().subtract(spirit.xo, spirit.yo, spirit.zo);
		boolean isMoving = movement.lengthSqr() > 0.0004;

		double particlesPerTick = 0.125;
		if (isMoving) {
			particlesPerTick = 0.5;
		}
		particlesPerTick *= ConfigHandler.spiritParticleTrailAmountModifier;

		int particleCount = (int)particlesPerTick;
		if (random.nextDouble() < particlesPerTick - particleCount) {
			particleCount++;
		}

		Vec3 trailOrigin = getTrailOrigin(spirit, movement);
		for (int i = 0; i < particleCount; i++) {
			spawnSpiritTrailParticle(minecraft, spirit, trailOrigin);
		}
	}

	private static Vec3 getTrailOrigin(LivingEntity spirit, Vec3 movement) {
		Vec3 backDirection = Vec3.directionFromRotation(0.0F, spirit.yBodyRot).reverse();
		double backDistance = spirit.getBbWidth() * 0.5 + 0.2;
		return spirit.position().add(backDirection.scale(backDistance)).subtract(movement.scale(2.0));
	}

	private static void spawnSpiritTrailParticle(Minecraft minecraft, LivingEntity spirit, Vec3 trailOrigin) {
		RandomSource random = spirit.getRandom();
		double x = trailOrigin.x + (random.nextDouble() - 0.5) * spirit.getBbWidth() * 0.5;
		double y = trailOrigin.y + (0.3 + random.nextDouble() * 0.5) * spirit.getBbHeight();
		double z = trailOrigin.z + (random.nextDouble() - 0.5) * spirit.getBbWidth() * 0.5;

		createSpiritParticle(minecraft, random, x, y, z, Vec3.ZERO, 0.3F, 0.35F);
	}

	public static void spawnSpiritAppearParticles(LivingEntity spirit) {
		float appearProgress = Util.getSpiritAppearProgress(spirit);
		if (appearProgress >= 1.0F) {
			return;
		}

		RandomSource random = spirit.getRandom();
		if (random.nextFloat() > 1.0F - appearProgress) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.options.particles().get().equals(ParticleStatus.MINIMAL)) {
			return;
		}

		spawnSpiritBodyParticle(minecraft, spirit, random, 0.01);
	}

	public static void spawnSpiritBurst(LivingEntity spirit) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.options.particles().get().equals(ParticleStatus.MINIMAL)) {
			return;
		}

		RandomSource random = spirit.getRandom();
		int particleCount = 12 + (int)(spirit.getBbWidth() * spirit.getBbHeight() * 8.0F);
		for (int i = 0; i < particleCount; i++) {
			spawnSpiritBodyParticle(minecraft, spirit, random, 0.06);
		}
	}

	private static void spawnSpiritBodyParticle(Minecraft minecraft, LivingEntity spirit, RandomSource random, double speed) {
		Vec3 position = Util.getRandomBodyPosition(spirit);
		Vec3 velocity = new Vec3((random.nextDouble() - 0.5) * speed, random.nextDouble() * speed, (random.nextDouble() - 0.5) * speed);
		createSpiritParticle(minecraft, random, position.x, position.y, position.z, velocity, 0.4F, 0.6F);
	}

	public static void spawnSpiritCollarSparkle(LivingEntity pet) {
		RandomSource random = pet.getRandom();
		if (random.nextInt(25) != 0) {
			return;
		}

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.options.particles().get().equals(ParticleStatus.MINIMAL)) {
			return;
		}

		Vec3 neck = pet.position().add(Vec3.directionFromRotation(0.0F, pet.yBodyRot).scale(pet.getBbWidth() * 0.45));
		double x = neck.x + (random.nextDouble() - 0.5) * 0.2;
		double y = pet.getY() + pet.getBbHeight() * 0.75;
		double z = neck.z + (random.nextDouble() - 0.5) * 0.2;
		createSpiritParticle(minecraft, random, x, y, z, Vec3.ZERO, 0.25F, 0.6F);
	}

	private static void createSpiritParticle(Minecraft minecraft, RandomSource random, double x, double y, double z, Vec3 velocity, float scale, float alphaMultiplier) {
		Particle particle = minecraft.particleEngine.createParticle(ParticleTypes.END_ROD, x, y, z, velocity.x, velocity.y, velocity.z);
		if (!(particle instanceof SimpleAnimatedParticle spiritParticle)) {
			return;
		}

		spiritParticle.setColor(spiritColourFrom);
		spiritParticle.setFadeColor(spiritColourTo);
		spiritParticle.setLifetime(20 + random.nextInt(15));
		spiritParticle.scale(scale);
		((SpiritParticle)spiritParticle).spiritCompanions$setAlphaMultiplier(alphaMultiplier);
	}
}
