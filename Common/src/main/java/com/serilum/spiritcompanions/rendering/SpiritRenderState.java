package com.serilum.spiritcompanions.rendering;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

import java.util.List;

public interface SpiritRenderState {
	boolean spiritCompanions$isSpirit();

	void spiritCompanions$setSpirit(boolean isSpirit);

	boolean spiritCompanions$isHusk();

	void spiritCompanions$setHusk(boolean isHusk);

	boolean spiritCompanions$hasSpiritCollar();

	void spiritCompanions$setSpiritCollar(boolean hasSpiritCollar);

	boolean spiritCompanions$isBeingExtracted();

	void spiritCompanions$setBeingExtracted(boolean isBeingExtracted);

	float spiritCompanions$getExtractionProgress();

	void spiritCompanions$setExtractionProgress(float extractionProgress);

	float spiritCompanions$getOpacity();

	void spiritCompanions$setOpacity(float opacity);

	boolean spiritCompanions$hasSpiritSaddle();

	void spiritCompanions$setSpiritSaddle(boolean hasSpiritSaddle);

	List<EntityRenderState.ShadowPiece> spiritCompanions$getShadowPieces();
}
