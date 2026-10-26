package com.serilum.spiritcompanions;

import com.natamus.collective.functions.CreativeModeTabFunctions;
import com.natamus.collective.services.Services;
import com.natamus.collective.translations.ServerTranslationPack;
import com.serilum.spiritcompanions.config.ConfigHandler;
import com.serilum.spiritcompanions.data.SpiritItems;
import com.serilum.spiritcompanions.item.EmptySpiritLanternItem;
import com.serilum.spiritcompanions.item.SpiritBellItem;
import com.serilum.spiritcompanions.item.SpiritCanisterItem;
import com.serilum.spiritcompanions.item.SpiritCollarItem;
import com.serilum.spiritcompanions.item.SpiritLanternItem;
import com.serilum.spiritcompanions.item.SpiritSaddleItem;
import com.serilum.spiritcompanions.util.Reference;
import com.serilum.spiritcompanions.util.UtilClient;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;

public class ModCommon {

	public static void init() {
		ServerTranslationPack.requireClientTranslations(Reference.NAME);
		ConfigHandler.initConfig();
		load();
	}

	private static void load() {

	}

	public static void registerAssets(Object modEventBusObject) {
		ResourceKey<CreativeModeTab> tabKey = CreativeModeTabFunctions.getCreativeModeTabResourceKey("tools_and_utilities");

		Services.REGISTERITEM.registerItem(modEventBusObject, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit_lantern"), () -> new SpiritLanternItem(new Item.Properties().stacksTo(1)), tabKey);
		Services.REGISTERITEM.registerItem(modEventBusObject, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "empty_spirit_lantern"), () -> new EmptySpiritLanternItem(new Item.Properties().stacksTo(1)), tabKey);
		Services.REGISTERITEM.registerItem(modEventBusObject, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit_bell"), () -> new SpiritBellItem(new Item.Properties().stacksTo(1)), tabKey);
		Services.REGISTERITEM.registerItem(modEventBusObject, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit_canister"), () -> new SpiritCanisterItem(new Item.Properties().stacksTo(16)), tabKey);
		Services.REGISTERITEM.registerItem(modEventBusObject, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit_saddle"), () -> new SpiritSaddleItem(new Item.Properties().stacksTo(1)), tabKey);
		Services.REGISTERITEM.registerItem(modEventBusObject, ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit_collar"), () -> new SpiritCollarItem(new Item.Properties().stacksTo(16)), tabKey, true);
	}

	public static void setAssets() {
		SpiritItems.SPIRIT_LANTERN = Services.REGISTERITEM.getRegisteredItem(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit_lantern"));
		SpiritItems.EMPTY_SPIRIT_LANTERN = Services.REGISTERITEM.getRegisteredItem(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "empty_spirit_lantern"));
		SpiritItems.SPIRIT_BELL = Services.REGISTERITEM.getRegisteredItem(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit_bell"));
		SpiritItems.SPIRIT_CANISTER = Services.REGISTERITEM.getRegisteredItem(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit_canister"));
		SpiritItems.SPIRIT_SADDLE = Services.REGISTERITEM.getRegisteredItem(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit_saddle"));
		SpiritItems.SPIRIT_COLLAR = Services.REGISTERITEM.getRegisteredItem(ResourceLocation.fromNamespaceAndPath(Reference.MOD_ID, "spirit_collar"));

		if (Services.MODLOADER.isClientSide()) {
			UtilClient.registerItemProperties();
		}
	}
}
