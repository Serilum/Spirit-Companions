package com.serilum.spiritcompanions.config;

import com.natamus.collective.config.DuskConfig;
import com.serilum.spiritcompanions.util.Reference;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConfigHandler extends DuskConfig {
	public static HashMap<String, List<String>> configMetaData = new HashMap<>();

	@Entry(min = 1, max = 100) public static int maxSpiritsPerPlayer = 5;
	@Entry(min = 0, max = 256) public static int spiritWorldDistance = 64;
	@Entry(min = 1, max = 30) public static int spiritExtractionSeconds = 5;
	@Entry(min = 0, max = 3600) public static int huskWitherSeconds = 30;
	@Entry(min = 1, max = 60) public static int collarSpiritReturnSeconds = 10;
	@Entry(min = 0, max = 100) public static int spiritTranslucencyPercentage = 50;
	@Entry(min = 0, max = 100) public static int spiritOriginalColourPercentage = 30;
	@Entry(min = 0, max = 100) public static int spiritShadowIntensityPercentage = 50;
	@Entry(min = 1.5, max = 8.0) public static double spiritOrbitDistance = 4.0;
	@Entry(min = 0, max = 3600) public static int averageSecondsBetweenSpontaneousFlights = 40;
	@Entry(min = 1, max = 30) public static int overlayMessageSeconds = 5;
	@Entry public static boolean showSpiritParticleTrail = true;
	@Entry(min = 0, max = 10.0) public static double spiritParticleTrailAmountModifier = 0.5;

	public static void initConfig() {
		configMetaData.put("maxSpiritsPerPlayer", Arrays.asList(
			"The maximum amount of spirit companions that can follow a player at the same time."
		));
		configMetaData.put("spiritWorldDistance", Arrays.asList(
			"How many blocks a player can be away from their spirit companions before the spirits go to the spirit world. 0 only sends them there when the player logs out."
		));
		configMetaData.put("spiritExtractionSeconds", Arrays.asList(
			"How many seconds the Spirit Lantern has to be held on a mob to extract its spirit."
		));
		configMetaData.put("huskWitherSeconds", Arrays.asList(
			"How many seconds a soulless husk takes to fade away after its spirit was extracted. 0 keeps husks until their chunk is unloaded."
		));
		configMetaData.put("collarSpiritReturnSeconds", Arrays.asList(
			"How many seconds the spirit of a pet with a Spirit Collar takes to fully form after the pet died."
		));
		configMetaData.put("spiritTranslucencyPercentage", Arrays.asList(
			"How see-through spirit companions are, in percent. 0 is fully solid, 100 is invisible."
		));
		configMetaData.put("spiritOriginalColourPercentage", Arrays.asList(
			"How much of a mob's own colours its spirit keeps, in percent. 0 makes spirits fully ice blue."
		));
		configMetaData.put("spiritShadowIntensityPercentage", Arrays.asList(
			"The strength of the blue light below spirit companions, in percent. 0 hides it."
		));
		configMetaData.put("spiritOrbitDistance", Arrays.asList(
			"How many blocks away from their player spirit companions circle while flying."
		));
		configMetaData.put("averageSecondsBetweenSpontaneousFlights", Arrays.asList(
			"How many seconds on average before a walking spirit companion takes off for a short flight by itself. 0 disables these flights."
		));
		configMetaData.put("overlayMessageSeconds", Arrays.asList(
			"How many seconds the mod's messages stay visible above the hotbar. Vanilla uses 3 seconds."
		));
		configMetaData.put("showSpiritParticleTrail", Arrays.asList(
			"Whether spirit companions leave a trail of translucent particles behind them."
		));
		configMetaData.put("spiritParticleTrailAmountModifier", Arrays.asList(
			"Modifies the amount of trail particles spirit companions leave behind. 2.0 doubles it."
		));

		DuskConfig.init(Reference.NAME, Reference.MOD_ID, ConfigHandler.class);
	}
}
