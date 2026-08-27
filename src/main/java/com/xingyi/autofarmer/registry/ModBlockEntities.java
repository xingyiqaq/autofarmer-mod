package com.xingyi.autofarmer.registry;

import com.xingyi.autofarmer.AutoFarmerMod;
import com.xingyi.autofarmer.block.AutoFarmerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AutoFarmerMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<AutoFarmerBlockEntity>> AUTOFARMER =
        BLOCK_ENTITIES.register("autofarmer",
            () -> BlockEntityType.Builder.of(
                AutoFarmerBlockEntity::new,
                ModBlocks.AUTOFARMER.get()
            ).build(null));
}
