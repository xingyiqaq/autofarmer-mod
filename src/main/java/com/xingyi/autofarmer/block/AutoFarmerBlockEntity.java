package com.xingyi.autofarmer.block;

import com.xingyi.autofarmer.AutoFarmerConfig;
import com.xingyi.autofarmer.menu.AutoFarmerMenu;
import com.xingyi.autofarmer.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;



import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AutoFarmerBlockEntity extends BlockEntity implements MenuProvider {

    private static Block regLookup(String path) {
        return ForgeRegistries.BLOCKS.getValue(ResourceLocation.parse(path));
    }

    private static final Block netherWartsBlock = regLookup("minecraft:nether_warts");

    private static final int SLOT_OUTPUT   = 5;
    private static final int TOTAL_SLOTS   = 6;

    // Track last planted position for tree auto-harvest
    private int lastSaplingX = 0, lastSaplingY = 0, lastSaplingZ = 0;
    private String plantedSaplingItem = null;

    public final net.minecraft.world.SimpleContainer inventory = new net.minecraft.world.SimpleContainer(TOTAL_SLOTS);
    private final ContainerData data = new ContainerData() {
        @Override public int get(int idx)  { return cooldown; }
        @Override public void set(int idx, int val) { cooldown = val; }
        @Override public int getCount()    { return 1; }
    };
    private int cooldown = 0;

    // Crop blocks with maturity threshold
    private static final Map<Block, Integer> CROP_AGE_MAP = new HashMap<>();
    static {
        CROP_AGE_MAP.put(Blocks.WHEAT,            7);
        CROP_AGE_MAP.put(Blocks.BEETROOTS,        3);
        CROP_AGE_MAP.put(Blocks.POTATOES,         7);
        CROP_AGE_MAP.put(Blocks.CARROTS,          7);
        CROP_AGE_MAP.put(Blocks.BAMBOO,           15);
        CROP_AGE_MAP.put(Blocks.SUGAR_CANE,       15);
        CROP_AGE_MAP.put(regLookup("minecraft:nether_warts"),     3);
        CROP_AGE_MAP.put(Blocks.CACTUS,           255);
        CROP_AGE_MAP.put(Blocks.MELON_STEM,       7);
        CROP_AGE_MAP.put(Blocks.PUMPKIN_STEM,     7);
        CROP_AGE_MAP.put(Blocks.SWEET_BERRY_BUSH, 3);
    }

    private static final Set<Block> VANILLA_SAPLING_BLOCKS = Set.of(
        Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING,
        Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING,
        regLookup("minecraft:mangrove_sapling"), regLookup("minecraft:cherry_sapling"),
        Blocks.AZALEA, Blocks.FLOWERING_AZALEA,
        Blocks.CRIMSON_FUNGUS, Blocks.WARPED_FUNGUS
    );

    private static final Set<Block> VANILLA_LOG_BLOCKS = new HashSet<>();
    static {
        VANILLA_LOG_BLOCKS.add(Blocks.OAK_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.SPRUCE_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.BIRCH_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.JUNGLE_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.ACACIA_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.DARK_OAK_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.MANGROVE_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.CHERRY_LOG);
        VANILLA_LOG_BLOCKS.add(regLookup("minecraft:azalea_stem"));
        VANILLA_LOG_BLOCKS.add(regLookup("minecraft:flowering_azalea_stem"));
        VANILLA_LOG_BLOCKS.add(Blocks.CRIMSON_STEM);
        VANILLA_LOG_BLOCKS.add(Blocks.WARPED_STEM);
        VANILLA_LOG_BLOCKS.add(Blocks.STRIPPED_OAK_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.STRIPPED_SPRUCE_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.STRIPPED_BIRCH_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.STRIPPED_JUNGLE_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.STRIPPED_ACACIA_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.STRIPPED_DARK_OAK_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.STRIPPED_MANGROVE_LOG);
        VANILLA_LOG_BLOCKS.add(Blocks.STRIPPED_CHERRY_LOG);
        VANILLA_LOG_BLOCKS.add(regLookup("minecraft:stripped_azalea_stem"));
        VANILLA_LOG_BLOCKS.add(regLookup("minecraft:stripped_flowering_azalea_stem"));
        VANILLA_LOG_BLOCKS.add(Blocks.STRIPPED_CRIMSON_STEM);
        VANILLA_LOG_BLOCKS.add(Blocks.STRIPPED_WARPED_STEM);
    }

    private static final Set<Block> FRUIT_BLOCKS = Set.of(Blocks.MELON, Blocks.PUMPKIN);

    public AutoFarmerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.AUTOFARMER.get(), pos, state);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.autofarmer.autofarmer_block");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new AutoFarmerMenu(syncId, inv, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("Cooldown", cooldown);
        tag.putInt("LastSapX", lastSaplingX);
        tag.putInt("LastSapY", lastSaplingY);
        tag.putInt("LastSapZ", lastSaplingZ);
        if (plantedSaplingItem != null) {
            tag.putString("PlantedSapling", plantedSaplingItem);
        }
        saveInventory(tag);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        cooldown = tag.getInt("Cooldown");
        lastSaplingX = tag.getInt("LastSapX");
        lastSaplingY = tag.getInt("LastSapY");
        lastSaplingZ = tag.getInt("LastSapZ");
        if (tag.contains("PlantedSapling")) {
            plantedSaplingItem = tag.getString("PlantedSapling");
        }
        loadInventory(tag);
    }

    private int getCooldownTicks() {
        return AutoFarmerConfig.cooldownTicks.get();
    }

    private int getTreeSearchRadius() {
        return AutoFarmerConfig.treeSearchRadius.get();
    }

    private int getTreeHarvestRadius() {
        return AutoFarmerConfig.treeHarvestRadius.get();
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TICK
    // ══════════════════════════════════════════════════════════════════════════

    public void tick(Level level, BlockPos pos) {
        if (cooldown > 0) { cooldown--; return; }

        List<BlockPos> targets = new ArrayList<>();
        for (Direction d : Direction.Plane.HORIZONTAL) targets.add(pos.relative(d));
        targets.add(pos.above());

        // ── Phase 1: Tree auto-harvest ─────────────────────────────────────
        BlockPos sapPos = new BlockPos(lastSaplingX, lastSaplingY, lastSaplingZ);
        if (lastSaplingX != 0 || lastSaplingY != 0 || lastSaplingZ != 0) {
            if (level.isLoaded(sapPos)) {
                BlockState bs = level.getBlockState(sapPos);
                if (isLogLike(bs.getBlock())) {
                    harvestTree(level, sapPos);
                    cooldown = getCooldownTicks();
                    setChanged();
                    return;
                }
                if (!isSaplingLike(bs.getBlock())) {
                    // Sapling disappeared — search wider for grown tree
                    harvestTreeSearch(level, sapPos);
                    return;
                }
            } else {
                // Chunk unloaded, reset
                lastSaplingX = 0; lastSaplingY = 0; lastSaplingZ = 0;
                plantedSaplingItem = null;
            }
        }

        // ── Phase 2: Crop / fruit harvest ─────────────────────────────────
        for (BlockPos tp : targets) {
            if (!level.isLoaded(tp)) continue;
            BlockState s = level.getBlockState(tp);

            if (FRUIT_BLOCKS.contains(s.getBlock())) {
                ItemStack drop = harvest(level, tp, s);
                if (!drop.isEmpty()) addToOutputOrDrop(drop, level, pos);
                cooldown = getCooldownTicks();
                setChanged();
                return;
            }

            ItemStack cropDrop = tryHarvestCrop(level, tp, s);
            if (!cropDrop.isEmpty()) {
                addToOutputOrDrop(cropDrop, level, pos);
                level.playSound(null, tp, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 0.5f, 1f);
                cooldown = getCooldownTicks();
                setChanged();
                return;
            }

            if (s.is(Blocks.COCOA)) {
                if (s.getValue(BlockStateProperties.AGE_2) >= 2) {
                    ItemStack cocoaDrop = harvest(level, tp, s);
                    if (!cocoaDrop.isEmpty()) addToOutputOrDrop(cocoaDrop, level, pos);
                    cooldown = getCooldownTicks();
                    setChanged();
                    return;
                }
            }
        }

        // ── Phase 3: Plant ────────────────────────────────────────────────
        for (BlockPos tp : targets) {
            if (!level.isLoaded(tp)) continue;
            if (tryPlantAt(level, tp)) {
                level.playSound(null, tp, SoundEvents.BONE_MEAL_USE, SoundSource.BLOCKS, 0.3f, 1.2f);
                cooldown = getCooldownTicks();
                setChanged();
                return;
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  CROP HARVEST
    // ══════════════════════════════════════════════════════════════════════════

    private ItemStack tryHarvestCrop(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (!CROP_AGE_MAP.containsKey(block)) return ItemStack.EMPTY;

        int maxAge = CROP_AGE_MAP.get(block);

        if (block == Blocks.CACTUS) {
            int height = 0;
            BlockPos check = pos;
            while (height < 4 && level.isLoaded(check) && level.getBlockState(check).is(Blocks.CACTUS)) {
                height++;
                check = check.above();
            }
            return height >= 3 ? harvest(level, pos, state) : ItemStack.EMPTY;
        }

        if (block == Blocks.SUGAR_CANE) {
            int height = 0;
            BlockPos check = pos;
            while (height < 5 && level.isLoaded(check) && level.getBlockState(check).is(Blocks.SUGAR_CANE)) {
                height++;
                check = check.above();
            }
            if (height >= 3) return harvest(level, pos, state);
        }

        if (hasProperty(state, BlockStateProperties.AGE_7)
                && state.getValue(BlockStateProperties.AGE_7) >= maxAge)
            return harvest(level, pos, state);
        if (hasProperty(state, BlockStateProperties.AGE_3)
                && state.getValue(BlockStateProperties.AGE_3) >= maxAge)
            return harvest(level, pos, state);
        if (hasProperty(state, BlockStateProperties.AGE_15)
                && state.getValue(BlockStateProperties.AGE_15) >= maxAge)
            return harvest(level, pos, state);

        return ItemStack.EMPTY;
    }

    private boolean hasProperty(BlockState s, net.minecraft.world.level.block.state.properties.Property<?> p) {
        return s.hasProperty(p);
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  TREE HARVEST (dynamic — works with any mod)
    // ══════════════════════════════════════════════════════════════════════════

    /**
     * Search a wider area around the sapling position for grown tree logs.
     * Some tree mods grow trees offset from the sapling position.
     */
    private void harvestTreeSearch(Level level, BlockPos center) {
        int radius = getTreeSearchRadius();
        BlockPos foundPos = null;

        for (int dx = -radius; dx <= radius && foundPos == null; dx++) {
            for (int dy = -radius; dy <= radius && foundPos == null; dy++) {
                for (int dz = -radius; dz <= radius && foundPos == null; dz++) {
                    BlockPos check = center.offset(dx, dy, dz);
                    if (level.isLoaded(check) && isLogLike(level.getBlockState(check).getBlock())) {
                        foundPos = check;
                    }
                }
            }
        }

        if (foundPos != null) {
            harvestTree(level, foundPos);
            cooldown = getCooldownTicks();
            setChanged();
        } else {
            // Nothing found — reset tracking
            lastSaplingX = 0; lastSaplingY = 0; lastSaplingZ = 0;
            plantedSaplingItem = null;
        }
    }

    private void harvestTree(Level level, BlockPos plantedPos) {
        Set<BlockPos> logPositions = new LinkedHashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        queue.add(plantedPos);
        Set<BlockPos> visited = new HashSet<>();

        while (!queue.isEmpty()) {
            BlockPos bp = queue.poll();
            if (visited.contains(bp) || !level.isLoaded(bp)) continue;
            visited.add(bp);

            Block block = level.getBlockState(bp).getBlock();
            if (isLogLike(block)) {
                logPositions.add(bp);
                for (Direction d : Direction.values()) {
                    queue.add(bp.relative(d));
                }
            }
        }

        if (logPositions.isEmpty()) {
            // No logs at planted pos — tree may have grown at offset
            // Try BFS from a wider starting set
            Set<BlockPos> seedPositions = new LinkedHashSet<>();
            int radius = getTreeSearchRadius();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        BlockPos check = plantedPos.offset(dx, dy, dz);
                        if (level.isLoaded(check) && isLogLike(level.getBlockState(check).getBlock())) {
                            seedPositions.add(check);
                        }
                    }
                }
            }
            if (!seedPositions.isEmpty()) {
                for (BlockPos sp : seedPositions) queue.add(sp);
                while (!queue.isEmpty()) {
                    BlockPos bp = queue.poll();
                    if (visited.contains(bp) || !level.isLoaded(bp)) continue;
                    visited.add(bp);
                    if (isLogLike(level.getBlockState(bp).getBlock())) {
                        logPositions.add(bp);
                        for (Direction d : Direction.values()) {
                            queue.add(bp.relative(d));
                        }
                    }
                }
            }
        }

        if (logPositions.isEmpty()) {
            lastSaplingX = 0; lastSaplingY = 0; lastSaplingZ = 0;
            plantedSaplingItem = null;
            return;
        }

        // Harvest all logs
        List<ItemStack> allDrops = new ArrayList<>();
        for (BlockPos lp : logPositions) {
            if (!level.isLoaded(lp)) continue;
            ItemStack drop = harvest(level, lp, level.getBlockState(lp));
            if (!drop.isEmpty()) allDrops.add(drop);
        }

        // Harvest adjacent leaves within harvest radius
        int leafRadius = getTreeHarvestRadius();
        Set<BlockPos> leafCandidates = new LinkedHashSet<>();
        for (BlockPos lp : logPositions) {
            for (int dx = -leafRadius; dx <= leafRadius; dx++) {
                for (int dy = -leafRadius; dy <= leafRadius; dy++) {
                    for (int dz = -leafRadius; dz <= leafRadius; dz++) {
                        BlockPos adj = lp.offset(dx, dy, dz);
                        if (!visited.contains(adj) && level.isLoaded(adj)) {
                            leafCandidates.add(adj);
                        }
                    }
                }
            }
        }
        for (BlockPos leafPos : leafCandidates) {
            if (level.isLoaded(leafPos)) {
                if (isLeafLike(level.getBlockState(leafPos).getBlock())) {
                    ItemStack drop = harvest(level, leafPos, level.getBlockState(leafPos));
                    if (!drop.isEmpty()) allDrops.add(drop);
                }
            }
        }

        // Collect all to output
        for (ItemStack drop : allDrops) {
            addToOutputOrDrop(drop, level, getBlockPos());
        }

        // Auto-replant sapling
        if (plantedSaplingItem != null && !allDrops.isEmpty()) {
            ItemStack sapling = new ItemStack(ForgeRegistries.ITEMS.getValue(
                    new ResourceLocation(plantedSaplingItem)));
            addToInputOrDrop(sapling, level, getBlockPos());
        }

        // Reset tracking
        lastSaplingX = 0; lastSaplingY = 0; lastSaplingZ = 0;
        plantedSaplingItem = null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  HARVEST (loot-table)
    // ══════════════════════════════════════════════════════════════════════════

    private ItemStack harvest(Level level, BlockPos pos, BlockState state) {
        // Simple harvest: return the block item directly
        net.minecraft.world.item.Item item = net.minecraft.world.item.BlockItem.getItemFor(state.getBlock());
        if (item == null) return net.minecraft.world.item.ItemStack.EMPTY;
        // Handle crops specially
        ItemStack result = getHarvestItem(state);
        if (result.isEmpty()) return net.minecraft.world.item.ItemStack.EMPTY;
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        return result;
    }

    private ItemStack getHarvestItem(BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.WHEAT) return new ItemStack(Items.WHEAT, 1);
        if (block == Blocks.CARROTS) return new ItemStack(Items.CARROTS, 2);
        if (block == Blocks.POTATOES) return new ItemStack(Items.POTATOES, 2);
        if (block == Blocks.BEETROOTS) return new ItemStack(Items.BEETROOT, 1);
        if (block == Blocks.BEETROOT) return new ItemStack(Items.BEETROOT, 1);
        if (block == Blocks.SWEET_BERRIES) return new ItemStack(Items.SWEET_BERRIES, 2);
        if (block == Blocks.BAMBOO) return new ItemStack(Items.BAMBOO, 1);
        if (block == Blocks.SUGAR_CANE) return new ItemStack(Items.SUGAR_CANE, 1);
        if (block == Blocks.CACTUS) return new ItemStack(Blocks.CACTUS, 1);
        if (block == Blocks.COCOA_BLOCK) return new ItemStack(Items.COCOA_BEANS, 2);
        if (block == Blocks.MELON_STEM || block == Blocks.PUMPKIN_STEM) return ItemStack.EMPTY; // stems handled elsewhere
        if (block == Blocks.MELON) return new ItemStack(Items.MELON_SLICE, 2);
        if (block == Blocks.PUMPKIN) return new ItemStack(Blocks.PUMPKIN, 1);
        if (block == Blocks.NETHER_WART) return new ItemStack(Items.NETHER_WART, 2);
        if (block == Blocks.CREEPER_HEAD) return new ItemStack(Items.CREEPER_HEAD, 1);
        if (block == Blocks.SKELETON_SKULL) return new ItemStack(Items.SKELETON_SKULL, 1);
        if (block == Blocks.ZOMBIE_HEAD) return new ItemStack(Items.ZOMBIE_HEAD, 1);
        if (block == Blocks.PLAYER_HEAD) return new ItemStack(Items.PLAYER_HEAD, 1);
        if (block == Blocks.WITHER_SKELETON_SKULL) return new ItemStack(Items.WITHER_SKELETON_SKULL, 1);
        if (block == Blocks.DRIED_KELP_BLOCK) return new ItemStack(Items.DRIED_KELP, 3);
        if (block == Blocks.HAY_BLOCK) return new ItemStack(Items.WHEAT, 3);
        if (block == Blocks.TNT) return new ItemStack(Blocks.TNT, 1);
        if (block == Blocks.OAK_LOG) return new ItemStack(Blocks.OAK_LOG, 1);
        if (block == Blocks.SPRUCE_LOG) return new ItemStack(Blocks.SPRUCE_LOG, 1);
        if (block == Blocks.BIRCH_LOG) return new ItemStack(Blocks.BIRCH_LOG, 1);
        if (block == Blocks.JUNGLE_LOG) return new ItemStack(Blocks.JUNGLE_LOG, 1);
        if (block == Blocks.ACACIA_LOG) return new ItemStack(Blocks.ACACIA_LOG, 1);
        if (block == Blocks.DARK_OAK_LOG) return new ItemStack(Blocks.DARK_OAK_LOG, 1);
        if (block == Blocks.MANGROVE_LOG) return new ItemStack(Blocks.MANGROVE_LOG, 1);
        if (block == Blocks.CHERRY_LOG) return new ItemStack(Blocks.CHERRY_LOG, 1);
        if (block == Blocks.OAK_LEAVES) return new ItemStack(Blocks.OAK_LEAVES, 1);
        if (block == Blocks.SPRUCE_LEAVES) return new ItemStack(Blocks.SPRUCE_LEAVES, 1);
        if (block == Blocks.BIRCH_LEAVES) return new ItemStack(Blocks.BIRCH_LEAVES, 1);
        if (block == Blocks.JUNGLE_LEAVES) return new ItemStack(Blocks.JUNGLE_LEAVES, 1);
        if (block == Blocks.ACACIA_LEAVES) return new ItemStack(Blocks.ACACIA_LEAVES, 1);
        if (block == Blocks.DARK_OAK_LEAVES) return new ItemStack(Blocks.DARK_OAK_LEAVES, 1);
        if (block == Blocks.MANGROVE_LEAVES) return new ItemStack(Blocks.MANGROVE_LEAVES, 1);
        if (block == Blocks.CHERRY_LEAVES) return new ItemStack(Blocks.CHERRY_LEAVES, 1);
        // Default: return the block item
        Item item = BlockItem.getItemFor(block);
        if (item == null) return ItemStack.EMPTY;
        return new ItemStack(item, 1);
    }


    // ══════════════════════════════════════════════════════════════════════════
    //  PLANT (dynamic — accepts any BlockItem via IPlantable / SaplingBlock)
    // ══════════════════════════════════════════════════════════════════════════

    private boolean tryPlantAt(Level level, BlockPos pos) {
        if (!level.getBlockState(pos).isAir()) return false;
        BlockState below = level.getBlockState(pos.below());
        Block belowBlock = below.getBlock();

        for (int i = 0; i < SLOT_OUTPUT; i++) {
            ItemStack s = inventory.getItem(i);
            if (s.isEmpty()) continue;
            if (!(s.getItem() instanceof BlockItem blockItem)) continue;
            Block plantBlock = blockItem.getBlock();

            if (!canPlantHere(plantBlock, belowBlock, level, pos)) continue;

            // Don't plant if there's already a growing crop at this position
            BlockState existing = level.getBlockState(pos);
            if (CROP_AGE_MAP.containsKey(existing.getBlock())) continue;
            if (existing.is(Blocks.CACTUS) || existing.is(Blocks.SUGAR_CANE)) continue;
            if (FRUIT_BLOCKS.contains(existing.getBlock())) continue;

            consumeSlot(i, 1);

            if (plantBlock == Blocks.BAMBOO) {
                level.setBlock(pos, Blocks.BAMBOO.defaultBlockState()
                        .setValue(BlockStateProperties.AGE_15, 0), 3);
            } else if (plantBlock == netherWartsBlock) {
                level.setBlock(pos, netherWartsBlock.defaultBlockState()
                        .setValue(BlockStateProperties.AGE_3, 0), 3);
            } else if (plantBlock == Blocks.SWEET_BERRY_BUSH) {
                level.setBlock(pos, Blocks.SWEET_BERRY_BUSH.defaultBlockState()
                        .setValue(BlockStateProperties.AGE_3, 0), 3);
            } else {
                level.setBlock(pos, plantBlock.defaultBlockState(), 3);
            }

            if (isSaplingLike(plantBlock)) {
                lastSaplingX = pos.getX();
                lastSaplingY = pos.getY();
                lastSaplingZ = pos.getZ();
                plantedSaplingItem = ForgeRegistries.ITEMS.getKey(s.getItem()) != null
                        ? ForgeRegistries.ITEMS.getKey(s.getItem()).toString() : null;
            }

            return true;
        }
        return false;
    }

    private boolean canPlantHere(Block plantBlock, Block belowBlock, Level level, BlockPos pos) {
        if (plantBlock == Blocks.WHEAT || plantBlock == Blocks.BEETROOTS
                || plantBlock == Blocks.POTATOES || plantBlock == Blocks.CARROTS
                || plantBlock == Blocks.MELON_STEM || plantBlock == Blocks.PUMPKIN_STEM) {
            return belowBlock == Blocks.FARMLAND || isDirtLike(belowBlock);
        }

        if (plantBlock == netherWartsBlock) {
            return belowBlock == Blocks.SOUL_SAND;
        }

        if (plantBlock == Blocks.BAMBOO) {
            return belowBlock == Blocks.GRASS_BLOCK || belowBlock == Blocks.DIRT
                    || belowBlock == Blocks.SAND || belowBlock == Blocks.CLAY
                    || belowBlock == Blocks.PODZOL;
        }

        if (plantBlock == Blocks.SUGAR_CANE) {
            return belowBlock == Blocks.SAND && hasAdjacentWater(level, pos);
        }

        if (plantBlock == Blocks.CACTUS) {
            return belowBlock == Blocks.SAND;
        }

        if (plantBlock == Blocks.SWEET_BERRY_BUSH) {
            return belowBlock == Blocks.GRASS_BLOCK
                    || belowBlock == Blocks.PODZOL
                    || belowBlock == Blocks.MYCELIUM;
        }

        if (plantBlock == Blocks.CRIMSON_FUNGUS || plantBlock == Blocks.WARPED_FUNGUS) {
            return belowBlock == Blocks.NETHERRACK
                    || belowBlock == Blocks.CRIMSON_NYLIUM
                    || belowBlock == Blocks.WARPED_NYLIUM;
        }

        if (isSaplingLike(plantBlock)) {
            return belowBlock == Blocks.GRASS_BLOCK || belowBlock == Blocks.DIRT
                    || belowBlock == Blocks.COARSE_DIRT || belowBlock == Blocks.ROOTED_DIRT
                    || belowBlock == Blocks.PODZOL;
        }

        // Default permissive
        return isDirtLike(belowBlock);
    }

    private boolean hasAdjacentWater(Level level, BlockPos pos) {
        for (Direction d : Direction.Plane.HORIZONTAL) {
            if (level.getBlockState(pos.relative(d)).is(Blocks.WATER)) return true;
        }
        return false;
    }

    private boolean isDirtLike(Block block) {
        return block == Blocks.DIRT || block == Blocks.GRASS_BLOCK
                || block == Blocks.COARSE_DIRT || block == Blocks.ROOTED_DIRT
                || block == Blocks.PODZOL || block == Blocks.SOUL_SOIL
                || block == Blocks.SAND;
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  DYNAMIC BLOCK TYPE DETECTION
    // ══════════════════════════════════════════════════════════════════════════

    private boolean isSaplingLike(Block block) {
        if (block instanceof SaplingBlock) return true;
        if (VANILLA_SAPLING_BLOCKS.contains(block)) return true;
        ResourceLocation reg = ForgeRegistries.BLOCKS.getKey(block);
        if (reg == null) return false;
        String name = reg.getPath().toLowerCase();
        return name.contains("sapling") || name.contains("fungus")
                || name.contains("mushroom");
    }

    private boolean isLogLike(Block block) {
        if (VANILLA_LOG_BLOCKS.contains(block)) return true;
        ResourceLocation reg = ForgeRegistries.BLOCKS.getKey(block);
        if (reg == null) return false;
        String name = reg.getPath().toLowerCase();
        return name.contains("log") || name.contains("stem")
                || name.contains("wood") || name.contains("trunk");
    }

    private boolean isLeafLike(Block block) {
        ResourceLocation reg = ForgeRegistries.BLOCKS.getKey(block);
        if (reg == null) return false;
        String name = reg.getPath().toLowerCase();
        return name.contains("leaf") || name.contains("leaves") || name.contains("foliage");
    }

    // ══════════════════════════════════════════════════════════════════════════
    //  INVENTORY HELPERS
    // ══════════════════════════════════════════════════════════════════════════

    private ItemStack addToOutput(ItemStack toAdd) {
        if (inventory.getItem(SLOT_OUTPUT).isEmpty()) {
            inventory.setItem(SLOT_OUTPUT, toAdd.copy());
            return ItemStack.EMPTY;
        }
        if (!ItemStack.isSameItemSameTags(inventory.getItem(SLOT_OUTPUT), toAdd)) {
            return toAdd;
        }
        int space = inventory.getItem(SLOT_OUTPUT).getMaxStackSize() - inventory.getItem(SLOT_OUTPUT).getCount();
        if (space >= toAdd.getCount()) {
            inventory.getItem(SLOT_OUTPUT).grow(toAdd.getCount());
            return ItemStack.EMPTY;
        }
        inventory.getItem(SLOT_OUTPUT).setCount(inventory.getItem(SLOT_OUTPUT).getMaxStackSize());
        ItemStack rem = toAdd.copy();
        rem.shrink(space);
        return rem;
    }

    private void addToInput(ItemStack toAdd) {
        for (int i = 0; i < SLOT_OUTPUT; i++) {
            if (inventory.getItem(i).isEmpty()) {
                inventory.setItem(i, toAdd.copy());
                return;
            }
            if (ItemStack.isSameItemSameTags(inventory.getItem(i), toAdd)) {
                int space = inventory.getItem(i).getMaxStackSize() - inventory.getItem(i).getCount();
                if (space > 0) {
                    int take = Math.min(space, toAdd.getCount());
                    inventory.getItem(i).grow(take);
                    if (take < toAdd.getCount()) {
                        ItemStack rem = toAdd.copy();
                        rem.shrink(take);
                        addToInput(rem);
                    }
                    return;
                }
            }
        }
    }

    /**
     * Add to output slot; if full, drop on ground.
     */
    private void addToOutputOrDrop(ItemStack toAdd, Level level, BlockPos machinePos) {
        ItemStack leftover = addToOutput(toAdd);
        if (!leftover.isEmpty()) {
            dropOnGround(level, machinePos, leftover);
        }
    }

    /**
     * Add to input slots; if full, drop on ground.
     */
    private void addToInputOrDrop(ItemStack toAdd, Level level, BlockPos machinePos) {
        addToInput(toAdd);
        // Check if the item was actually added
        boolean added = false;
        for (int i = 0; i < SLOT_OUTPUT; i++) {
            if (!inventory.getItem(i).isEmpty() && ItemStack.isSameItemSameTags(inventory.getItem(i), toAdd)) {
                added = true;
                break;
            }
        }
        if (!added) {
            dropOnGround(level, machinePos, toAdd);
        }
    }

    private void dropOnGround(Level level, BlockPos pos, ItemStack stack) {
        if (level.isClientSide()) return;
        Vec3 dropPos = Vec3.atBottomCenterOf(pos).add(0.5, 1.0, 0.5);
        ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, stack);
        entity.setDeltaMovement(
                (level.random.nextGaussian() * 0.02) / 1000.0,
                0.20000000298023224,
                (level.random.nextGaussian() * 0.02) / 1000.0);
        level.addFreshEntity(entity);
    }

    private void consumeSlot(int slot, int count) {
        ItemStack s = inventory.getItem(slot);
        if (s.isEmpty()) return;
        int rem = s.getCount() - count;
        if (rem <= 0) inventory.setItem(slot, ItemStack.EMPTY);
        else s.shrink(count);
    }

    private void saveInventory(CompoundTag tag) {
        CompoundTag items = new CompoundTag();
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (!inventory.getItem(i).isEmpty())
                items.put("s" + i, inventory.getItem(i).save(new CompoundTag()));
        }
        tag.put("Items", items);
    }

    private void loadInventory(CompoundTag tag) {
        CompoundTag items = tag.getCompound("Items");
        for (int i = 0; i < TOTAL_SLOTS; i++) {
            if (items.contains("s" + i))
                inventory.setItem(i, ItemStack.of(items.getCompound("s" + i)));
        }
    }
}
