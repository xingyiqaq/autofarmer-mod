package com.xingyi.autofarmer.block;

import com.xingyi.autofarmer.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class AutoFarmerBlock extends BaseEntityBlock {

    public AutoFarmerBlock(Properties p) {
        super(p);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return ModBlockEntities.AUTOFARMER.get().create(pos, state);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof AutoFarmerBlockEntity afbe) {
            int occupied = 0;
            for (int i = 0; i < afbe.inventory.length; i++) {
                if (!afbe.inventory[i].isEmpty()) occupied++;
            }
            return Math.round(occupied * 15.0f / afbe.inventory.length);
        }
        return 0;
    }
}
