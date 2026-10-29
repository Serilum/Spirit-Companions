package com.serilum.spiritcompanions.util;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.serilum.spiritcompanions.config.ConfigHandler;
import com.serilum.spiritcompanions.data.SpiritItems;
import com.serilum.spiritcompanions.data.Variables;
import com.serilum.spiritcompanions.rendering.SpiritParticle;
import com.serilum.spiritcompanions.rendering.SpiritRenderState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.SimpleAnimatedParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ParticleStatus;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UtilClient {
	private static final int spiritColourFrom = 0xB4E8FF;
	private static final int spiritColourTo = 0x96CEF5;
	private static final int huskColour = 0xFF8C9096;
	private static final RenderType spiritShadowRenderType = RenderTypes.entityShadow(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "textures/misc/spirit_shadow.png"));

	public static RenderType getSpiritRenderType(Identifier texture) {
		return RenderTypes.entityTranslucent(getSpiritTexture(texture));
	}

	private static Identifier getSpiritTexture(Identifier texture) {
		int colourPercentage = ConfigHandler.spiritOriginalColourPercentage;
		if (colourPercentage >= 100) {
			return texture;
		}

		String textureKey = texture + "#" + colourPercentage;
		Identifier spiritTexture = Variables.spiritTextures.get(textureKey);
		if (spiritTexture == null) {
			spiritTexture = createSpiritTexture(texture, colourPercentage);
			Variables.spiritTextures.put(textureKey, spiritTexture);
		}
		return spiritTexture;
	}

	private static Identifier createSpiritTexture(Identifier texture, int colourPercentage) {
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

			Identifier spiritTexture = Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit/" + texture.getNamespace() + "/" + texture.getPath() + "/" + colourPercentage);
			minecraft.getTextureManager().register(spiritTexture, new DynamicTexture(spiritTexture::toString, image));
			return spiritTexture;
		}
		catch (Exception ignored) {
			return texture;
		}
	}

	private static void desaturateImage(NativeImage image, float colourFraction) {
		for (int y = 0; y < image.getHeight(); y++) {
			for (int x = 0; x < image.getWidth(); x++) {
				int pixel = image.getPixel(x, y);
				int grey = Math.round(ARGB.red(pixel) * 0.299F + ARGB.green(pixel) * 0.587F + ARGB.blue(pixel) * 0.114F);
				int red = Math.round(Mth.lerp(colourFraction, grey, ARGB.red(pixel)));
				int green = Math.round(Mth.lerp(colourFraction, grey, ARGB.green(pixel)));
				int blue = Math.round(Mth.lerp(colourFraction, grey, ARGB.blue(pixel)));
				image.setPixel(x, y, ARGB.color(ARGB.alpha(pixel), red, green, blue));
			}
		}
	}

	public static void setSpiritRenderState(LivingEntityRenderState state, LivingEntity entity) {
		SpiritRenderState spiritRenderState = (SpiritRenderState)state;
		boolean isSpirit = Util.isSpirit(entity);
		boolean isHusk = Util.isHusk(entity);
		spiritRenderState.spiritCompanions$setSpirit(isSpirit);
		spiritRenderState.spiritCompanions$setHusk(isHusk);
		spiritRenderState.spiritCompanions$setSpiritCollar(Util.hasSpiritCollar(entity));
		spiritRenderState.spiritCompanions$setBeingExtracted(Util.isBeingExtracted(entity));
		spiritRenderState.spiritCompanions$setExtractionProgress(Util.getHuskTintProgress(entity));
		spiritRenderState.spiritCompanions$setOpacity(1.0F);
		spiritRenderState.spiritCompanions$setSpiritSaddle(Util.hasSpiritSaddle(entity));

		if (isSpirit) {
			state.lightCoords = LightCoordsUtil.FULL_BRIGHT;
			spiritRenderState.spiritCompanions$setOpacity(Util.getSpiritAppearProgress(entity));
		}
		else if (isHusk) {
			spiritRenderState.spiritCompanions$setOpacity(Util.getHuskOpacity(entity));
		}
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

	public static boolean isBeingExtractedRenderState(Object state) {
		if (!(state instanceof SpiritRenderState spiritRenderState)) {
			return false;
		}

		return spiritRenderState.spiritCompanions$isBeingExtracted();
	}

	public static boolean usesSpiritRendering(Object state) {
		if (!(state instanceof SpiritRenderState spiritRenderState)) {
			return false;
		}

		return spiritRenderState.spiritCompanions$isSpirit() || spiritRenderState.spiritCompanions$isHusk();
	}

	public static void takeOverSpiritShadow(EntityRenderState state) {
		List<EntityRenderState.ShadowPiece> spiritShadowPieces = ((SpiritRenderState)state).spiritCompanions$getShadowPieces();
		spiritShadowPieces.clear();
		spiritShadowPieces.addAll(state.shadowPieces);
		state.shadowPieces.clear();
	}

	public static void submitSpiritShadow(LivingEntityRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector) {
		List<EntityRenderState.ShadowPiece> shadowPieces = new ArrayList<>(((SpiritRenderState)state).spiritCompanions$getShadowPieces());
		if (shadowPieces.isEmpty()) {
			return;
		}

		float radius = state.shadowRadius;
		submitNodeCollector.submitCustomGeometry(poseStack, spiritShadowRenderType, (pose, buffer) -> {
			for (EntityRenderState.ShadowPiece shadowPiece : shadowPieces) {
				addSpiritShadowPiece(pose, buffer, radius, shadowPiece);
			}
		});
	}

	private static void addSpiritShadowPiece(PoseStack.Pose pose, VertexConsumer buffer, float radius, EntityRenderState.ShadowPiece shadowPiece) {
		AABB bounds = shadowPiece.shapeBelow().bounds();
		float x0 = shadowPiece.relativeX() + (float)bounds.minX;
		float x1 = shadowPiece.relativeX() + (float)bounds.maxX;
		float y = shadowPiece.relativeY() + (float)bounds.minY;
		float z0 = shadowPiece.relativeZ() + (float)bounds.minZ;
		float z1 = shadowPiece.relativeZ() + (float)bounds.maxZ;

		float heightAboveSurface = -y;
		float heightFade = Mth.clamp(1.0F - heightAboveSurface, 0.0F, 1.0F);
		if (heightFade <= 0.0F) {
			return;
		}

		float u0 = -x0 / 2.0F / radius + 0.5F;
		float u1 = -x1 / 2.0F / radius + 0.5F;
		float v0 = -z0 / 2.0F / radius + 0.5F;
		float v1 = -z1 / 2.0F / radius + 0.5F;

		float intensity = ConfigHandler.spiritShadowIntensityPercentage / 100.0F;
		int colour = ARGB.color(shadowPiece.alpha() * heightFade * intensity, spiritColourFrom);
		addSpiritShadowVertex(pose, buffer, colour, x0, y, z0, u0, v0);
		addSpiritShadowVertex(pose, buffer, colour, x0, y, z1, u0, v1);
		addSpiritShadowVertex(pose, buffer, colour, x1, y, z1, u1, v1);
		addSpiritShadowVertex(pose, buffer, colour, x1, y, z0, u1, v0);
	}

	private static void addSpiritShadowVertex(PoseStack.Pose pose, VertexConsumer buffer, int colour, float x, float y, float z, float u, float v) {
		Vector3f position = pose.pose().transformPosition(x, y, z, new Vector3f());
		buffer.addVertex(position.x(), position.y(), position.z(), colour, u, v, OverlayTexture.NO_OVERLAY, LightCoordsUtil.FULL_BRIGHT, 0.0F, 1.0F, 0.0F);
	}

	public static boolean isSpiritRenderState(Object state) {
		if (!(state instanceof SpiritRenderState spiritRenderState)) {
			return false;
		}

		return spiritRenderState.spiritCompanions$isSpirit();
	}

	public static int getSpiritLayerColour(Object state, int colour) {
		if (!(state instanceof SpiritRenderState spiritRenderState)) {
			return colour;
		}

		float extractionProgress = spiritRenderState.spiritCompanions$getExtractionProgress();
		if (extractionProgress >= 0.0F) {
			return ARGB.multiply(colour, ARGB.srgbLerp(extractionProgress, 0xFFFFFFFF, huskColour));
		}

		float opacity = spiritRenderState.spiritCompanions$getOpacity();
		if (spiritRenderState.spiritCompanions$isHusk()) {
			return ARGB.multiplyAlpha(ARGB.multiply(colour, huskColour), opacity);
		}

		if (!spiritRenderState.spiritCompanions$isSpirit()) {
			return colour;
		}

		int translucencyPercentage = getTranslucencyPercentage(spiritRenderState);
		return ARGB.multiplyAlpha(getSpiritTintedColour(colour, ((LivingEntityRenderState)state).ageInTicks, translucencyPercentage), opacity);
	}

	private static int getTranslucencyPercentage(SpiritRenderState spiritRenderState) {
		if (spiritRenderState.spiritCompanions$hasSpiritSaddle()) {
			return ConfigHandler.spiritTranslucencyPercentage / 2;
		}
		return ConfigHandler.spiritTranslucencyPercentage;
	}

	public static int getHorseMarkingsColour(int colour) {
		if (!Variables.isSubmittingHorseMarkings || Variables.submittingSpiritState == null) {
			return colour;
		}
		return ARGB.multiplyAlpha(getSpiritLayerColour(Variables.submittingSpiritState, colour), 0.5F);
	}

	public static int getCollarLayerColour(Object state, int colour) {
		if (state instanceof SpiritRenderState spiritRenderState && spiritRenderState.spiritCompanions$hasSpiritCollar()) {
			return ARGB.opaque(getShimmerColour(((LivingEntityRenderState)state).ageInTicks, ConfigHandler.spiritTranslucencyPercentage));
		}

		return getSpiritLayerColour(state, colour);
	}

	public static int getSpiritEquipmentColour(Object state, int colour) {
		if (!isSpiritRenderState(state)) {
			return colour;
		}

		float ageInTicks = ((LivingEntityRenderState)state).ageInTicks;
		return ARGB.opaque(getSpiritTintedColour(colour, ageInTicks, ConfigHandler.spiritTranslucencyPercentage));
	}

	public static int getSpiritTintedColour(int tintedColour, float ageInTicks, int translucencyPercentage) {
		return ARGB.multiply(tintedColour, getShimmerColour(ageInTicks, translucencyPercentage));
	}

	public static int getShimmerColour(float ageInTicks, int translucencyPercentage) {
		float wave = (Mth.sin(ageInTicks * 0.035F) + 1.0F) / 2.0F;
		int rgb = ARGB.srgbLerp(wave, spiritColourFrom, spiritColourTo);

		int baseAlpha = Math.round((100 - translucencyPercentage) * 2.55F);
		int alpha = Mth.clamp(Mth.lerpInt(wave, baseAlpha - 5, baseAlpha + 5), 0, 255);
		return ARGB.color(alpha, rgb);
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
