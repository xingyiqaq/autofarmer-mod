package com.xingyi.autofarmer.block;

import com.xingyi.autofarmer.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class AutoFarmerBlock extends BaseEntityBlock {

    public AutoFarmerBlock(Properties p) {
        super(p);
    }

    @Override
    public RenderShape getRenderShape(BlockState p) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos,
                                  Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AutoFarmerBlockEntity) {
                player.openMenu((AutoFarmerBlockEntity) be);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level,
                                                                   BlockState state,
                                                                   BlockEntityType<T> type) {
        if (!level.isClientSide() && type == ModBlockEntities.AUTOFARMER.get()) {
            return (l, p, s, be) -> {
                if (be instanceof AutoFarmerBlockEntity entity) {
                    entity.tick(l, p);
                }
            };
        }
        return null;
    }
}
