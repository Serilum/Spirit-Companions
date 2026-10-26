package com.serilum.spiritcompanions.mixin;

import com.serilum.spiritcompanions.rendering.SpiritRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = LivingEntityRenderState.class, priority = 1001)
public class LivingEntityRenderStateMixin implements SpiritRenderState {
	@Unique private boolean spiritCompanions$isSpirit = false;
	@Unique private boolean spiritCompanions$isHusk = false;
	@Unique private boolean spiritCompanions$hasSpiritCollar = false;
	@Unique private boolean spiritCompanions$isBeingExtracted = false;
	@Unique private float spiritCompanions$extractionProgress = -1.0F;
	@Unique private float spiritCompanions$opacity = 1.0F;
	@Unique private boolean spiritCompanions$hasSpiritSaddle = false;
	@Unique private final List<EntityRenderState.ShadowPiece> spiritCompanions$shadowPieces = new ArrayList<>();

	@Override
	public boolean spiritCompanions$isSpirit() {
		return spiritCompanions$isSpirit;
	}

	@Override
	public void spiritCompanions$setSpirit(boolean isSpirit) {
		spiritCompanions$isSpirit = isSpirit;
	}

	@Override
	public boolean spiritCompanions$isHusk() {
		return spiritCompanions$isHusk;
	}

	@Override
	public void spiritCompanions$setHusk(boolean isHusk) {
		spiritCompanions$isHusk = isHusk;
	}

	@Override
	public boolean spiritCompanions$hasSpiritCollar() {
		return spiritCompanions$hasSpiritCollar;
	}

	@Override
	public void spiritCompanions$setSpiritCollar(boolean hasSpiritCollar) {
		spiritCompanions$hasSpiritCollar = hasSpiritCollar;
	}

	@Override
	public boolean spiritCompanions$isBeingExtracted() {
		return spiritCompanions$isBeingExtracted;
	}

	@Override
	public void spiritCompanions$setBeingExtracted(boolean isBeingExtracted) {
		spiritCompanions$isBeingExtracted = isBeingExtracted;
	}

	@Override
	public float spiritCompanions$getExtractionProgress() {
		return spiritCompanions$extractionProgress;
	}

	@Override
	public void spiritCompanions$setExtractionProgress(float extractionProgress) {
		spiritCompanions$extractionProgress = extractionProgress;
	}

	@Override
	public float spiritCompanions$getOpacity() {
		return spiritCompanions$opacity;
	}

	@Override
	public void spiritCompanions$setOpacity(float opacity) {
		spiritCompanions$opacity = opacity;
	}

	@Override
	public boolean spiritCompanions$hasSpiritSaddle() {
		return spiritCompanions$hasSpiritSaddle;
	}

	@Override
	public void spiritCompanions$setSpiritSaddle(boolean hasSpiritSaddle) {
		spiritCompanions$hasSpiritSaddle = hasSpiritSaddle;
	}

	@Override
	public List<EntityRenderState.ShadowPiece> spiritCompanions$getShadowPieces() {
		return spiritCompanions$shadowPieces;
	}
}
