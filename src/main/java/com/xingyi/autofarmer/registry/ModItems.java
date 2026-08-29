package com.xingyi.autofarmer.registry;

import com.xingyi.autofarmer.AutoFarmerMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, AutoFarmerMod.MOD_ID);

    public static final RegistryObject<Item> AUTOFARMER_ITEM = ITEMS.register("autofarmer_block",
        () -> new BlockItem(ModBlocks.AUTOFARMER.get(), new Item.Properties()));
}
