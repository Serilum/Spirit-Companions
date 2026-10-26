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
import com.serilum.spiritcompanions.networking.PacketRegistration;
import com.serilum.spiritcompanions.util.Reference;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.item.equipment.Equippable;

public class ModCommon {

	public static void init() {
		ServerTranslationPack.requireClientTranslations(Reference.NAME);
		ConfigHandler.initConfig();
		load();
	}

	private static void load() {
		new PacketRegistration().init();
	}

	public static void registerAssets(Object modEventBusObject) {
		ResourceKey<CreativeModeTab> tabKey = CreativeModeTabFunctions.getCreativeModeTabResourceKey("tools_and_utilities");

		Services.REGISTERITEM.registerItem(modEventBusObject, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_lantern"), (properties) -> new SpiritLanternItem(properties), new Item.Properties().stacksTo(1), tabKey);
		Services.REGISTERITEM.registerItem(modEventBusObject, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "empty_spirit_lantern"), (properties) -> new EmptySpiritLanternItem(properties), new Item.Properties().stacksTo(1), tabKey);
		Services.REGISTERITEM.registerItem(modEventBusObject, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_bell"), (properties) -> new SpiritBellItem(properties), new Item.Properties().stacksTo(1), tabKey);
		Services.REGISTERITEM.registerItem(modEventBusObject, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_canister"), (properties) -> new SpiritCanisterItem(properties), new Item.Properties().stacksTo(16), tabKey);
		Services.REGISTERITEM.registerItem(modEventBusObject, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_saddle"), (properties) -> new Item(properties), new Item.Properties().stacksTo(1).component(DataComponents.EQUIPPABLE, getSpiritSaddleEquippable()), tabKey);
		Services.REGISTERITEM.registerItem(modEventBusObject, Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_collar"), (properties) -> new SpiritCollarItem(properties), new Item.Properties().stacksTo(16), tabKey, true);
	}

	private static Equippable getSpiritSaddleEquippable() {
		return Equippable.builder(EquipmentSlot.SADDLE)
			.setEquipSound(SoundEvents.HORSE_SADDLE)
			.setAsset(EquipmentAssets.SADDLE)
			.setEquipOnInteract(true)
			.setCanBeSheared(true)
			.setShearingSound(SoundEvents.SADDLE_UNEQUIP)
			.build();
	}

	public static void setAssets() {
		SpiritItems.SPIRIT_LANTERN = Services.REGISTERITEM.getRegisteredItem(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_lantern"));
		SpiritItems.EMPTY_SPIRIT_LANTERN = Services.REGISTERITEM.getRegisteredItem(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "empty_spirit_lantern"));
		SpiritItems.SPIRIT_BELL = Services.REGISTERITEM.getRegisteredItem(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_bell"));
		SpiritItems.SPIRIT_CANISTER = Services.REGISTERITEM.getRegisteredItem(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_canister"));
		SpiritItems.SPIRIT_SADDLE = Services.REGISTERITEM.getRegisteredItem(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_saddle"));
		SpiritItems.SPIRIT_COLLAR = Services.REGISTERITEM.getRegisteredItem(Identifier.fromNamespaceAndPath(Reference.MOD_ID, "spirit_collar"));
	}
}
