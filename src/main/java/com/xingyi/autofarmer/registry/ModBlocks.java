package com.xingyi.autofarmer.registry;

import com.xingyi.autofarmer.AutoFarmerMod;
import com.xingyi.autofarmer.block.AutoFarmerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
        DeferredRegister.create(ForgeRegistries.BLOCKS, AutoFarmerMod.MOD_ID);

    public static final RegistryObject<Block> AUTOFARMER = BLOCKS.register("autofarmer_block",
        () -> new AutoFarmerBlock(
            Block.Properties.of().mapColor(MapColor.COLOR_RED).strength(2.0f).noOcclusion()));
}
