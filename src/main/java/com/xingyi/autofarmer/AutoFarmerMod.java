package com.xingyi.autofarmer;

import com.xingyi.autofarmer.registry.ModBlockEntities;
import com.xingyi.autofarmer.registry.ModBlocks;
import com.xingyi.autofarmer.registry.ModItems;
import com.xingyi.autofarmer.registry.ModMenus;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AutoFarmerMod.MOD_ID)
public class AutoFarmerMod {

    public static final String MOD_ID = "autofarmer";

    public AutoFarmerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AutoFarmerConfig.spec);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // DeferredRegister handles registration automatically
        });
    }
}
