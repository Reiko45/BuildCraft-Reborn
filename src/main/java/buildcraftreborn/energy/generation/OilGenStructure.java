/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.generation;

import java.util.function.Predicate;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Fluid;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.energy.block.BlockSpringOil;
import buildcraftreborn.energy.tile.TileSpringOil;

/** One part of an oil well (a lake pattern, a sphere, a tube, a spout or the spring). Every chunk asks each structure
 * near it to generate only the blocks inside that chunk, so a structure is the same whichever chunk generates first.
 * <p>
 * 1.21 differences: blocks are only placed inside the chunk being generated (1.12 populated a 16x16 area offset by
 * 8 blocks; spouts, which are only placed from the chunk at their centre, can still reach
 * one block into the next chunk), the world is taller (structures that went to y=0 or y=255 now use the world's real
 * bottom and top), and oil next to air gets a fluid tick so it can flow, which 1.12 got from
 * {@code onBlockAdded}. */
public abstract class OilGenStructure {
    /** Flags for worldgen block changes: tell clients, don't update neighbours. */
    private static final int FLAGS = Block.UPDATE_CLIENTS;

    public final BoundingBox box;
    public final ReplaceType replaceType;

    public OilGenStructure(BoundingBox containingBox, ReplaceType replaceType) {
        this.box = containingBox;
        this.replaceType = replaceType;
    }

    public final void generate(WorldGenLevel world, BoundingBox within) {
        BoundingBox intersect = intersect(box, within);
        if (intersect != null) {
            generateWithin(world, intersect);
        }
    }

    /** Generates this structure in the world, but only between the given coordinates. */
    protected abstract void generateWithin(WorldGenLevel world, BoundingBox intersect);

    /** @return The number of oil blocks that this structure will set. Note that this is called *after*
     *         {@link #generateWithin(WorldGenLevel, BoundingBox)}, by the Spring type, so this can store the number
     *         set. */
    public abstract int countOilBlocks();

    public void setOilIfCanReplace(WorldGenLevel world, BlockPos pos) {
        if (canReplaceForOil(world, pos)) {
            setOil(world, pos);
        }
    }

    public boolean canReplaceForOil(WorldGenLevel world, BlockPos pos) {
        return replaceType.canReplace(world, pos);
    }

    public static void setOil(WorldGenLevel world, BlockPos pos) {
        if (world.isOutsideBuildHeight(pos)) {
            return;
        }
        BlockState oil = BCEnergyFluids.crudeOil[0].getBlockOrNull().defaultBlockState();
        world.setBlock(pos, oil, FLAGS);
        // Only oil that can go somewhere needs to flow (it floats on water, so only air and plants count).
        // Neighbours are at most one block outside the chunk being generated, which worldgen is allowed to read.
        Fluid fluid = oil.getFluidState().getType();
        BlockPos.MutableBlockPos next = new BlockPos.MutableBlockPos();
        for (Direction dir : Direction.values()) {
            if (dir == Direction.UP) {
                continue;
            }
            next.setWithOffset(pos, dir);
            if (world.isOutsideBuildHeight(next)) {
                continue;
            }
            BlockState state = world.getBlockState(next);
            if (state.isAir() || (state.getFluidState().isEmpty() && state.canBeReplaced(fluid))) {
                world.scheduleTick(pos, fluid, fluid.getTickDelay(world));
                return;
            }
        }
    }

    public static void setAir(WorldGenLevel world, BlockPos pos) {
        if (!world.isOutsideBuildHeight(pos)) {
            world.setBlock(pos, Blocks.AIR.defaultBlockState(), FLAGS);
        }
    }

    @Nullable
    public static BoundingBox intersect(BoundingBox a, BoundingBox b) {
        if (!a.intersects(b)) {
            return null;
        }
        return new BoundingBox(Math.max(a.minX(), b.minX()), Math.max(a.minY(), b.minY()),
            Math.max(a.minZ(), b.minZ()), Math.min(a.maxX(), b.maxX()), Math.min(a.maxY(), b.maxY()),
            Math.min(a.maxZ(), b.maxZ()));
    }

    public static BlockPos withValue(BlockPos pos, Axis axis, int value) {
        return switch (axis) {
            case X -> new BlockPos(value, pos.getY(), pos.getZ());
            case Y -> new BlockPos(pos.getX(), value, pos.getZ());
            case Z -> new BlockPos(pos.getX(), pos.getY(), value);
        };
    }

    public static BoundingBox box(BlockPos a, BlockPos b) {
        return BoundingBox.fromCorners(a, b);
    }

    public enum ReplaceType {
        ALWAYS {
            @Override
            public boolean canReplace(WorldGenLevel world, BlockPos pos) {
                return true;
            }
        },
        IS_FOR_LAKE {
            @Override
            public boolean canReplace(WorldGenLevel world, BlockPos pos) {
                return ALWAYS.canReplace(world, pos);
            }
        };

        public abstract boolean canReplace(WorldGenLevel world, BlockPos pos);
    }

    public static class GenByPredicate extends OilGenStructure {
        public final Predicate<BlockPos> predicate;

        public GenByPredicate(BoundingBox containingBox, ReplaceType replaceType, Predicate<BlockPos> predicate) {
            super(containingBox, replaceType);
            this.predicate = predicate;
        }

        @Override
        protected void generateWithin(WorldGenLevel world, BoundingBox intersect) {
            for (BlockPos pos : BlockPos.betweenClosed(intersect.minX(), intersect.minY(), intersect.minZ(),
                intersect.maxX(), intersect.maxY(), intersect.maxZ())) {
                if (predicate.test(pos)) {
                    setOilIfCanReplace(world, pos);
                }
            }
        }

        @Override
        public int countOilBlocks() {
            int count = 0;
            for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(),
                box.maxZ())) {
                if (predicate.test(pos)) {
                    count++;
                }
            }
            return count;
        }
    }

    public static class FlatPattern extends OilGenStructure {
        private final boolean[][] pattern;
        private final int depth;

        private FlatPattern(BoundingBox containingBox, ReplaceType replaceType, boolean[][] pattern, int depth) {
            super(containingBox, replaceType);
            this.pattern = pattern;
            this.depth = depth;
        }

        public static FlatPattern create(BlockPos start, ReplaceType replaceType, boolean[][] pattern, int depth) {
            BlockPos min = start.offset(0, 1 - depth, 0);
            BlockPos max = start.offset(pattern.length - 1, 0, pattern.length == 0 ? 0 : pattern[0].length - 1);
            return new FlatPattern(box(min, max), replaceType, pattern, depth);
        }

        @Override
        protected void generateWithin(WorldGenLevel world, BoundingBox intersect) {
            for (BlockPos pos : BlockPos.betweenClosed(intersect.minX(), intersect.minY(), intersect.minZ(),
                intersect.maxX(), intersect.maxY(), intersect.maxZ())) {
                int x = pos.getX() - box.minX();
                int z = pos.getZ() - box.minZ();
                if (pattern[x][z]) {
                    setOilIfCanReplace(world, pos);
                }
            }
        }

        @Override
        public int countOilBlocks() {
            int count = 0;
            for (boolean[] row : pattern) {
                for (boolean set : row) {
                    if (set) {
                        count++;
                    }
                }
            }
            return count * depth;
        }
    }

    /** A flat pattern that follows the terrain: at each set point the top block and up to 4 above it are cleared, and
     * the top {@code depth} blocks become oil. */
    public static class PatternTerrainHeight extends OilGenStructure {
        private final boolean[][] pattern;
        private final int depth;

        private PatternTerrainHeight(BoundingBox containingBox, ReplaceType replaceType, boolean[][] pattern,
            int depth) {
            super(containingBox, replaceType);
            this.pattern = pattern;
            this.depth = depth;
        }

        public static PatternTerrainHeight create(BlockPos start, int minY, int maxY, ReplaceType replaceType,
            boolean[][] pattern, int depth) {
            BlockPos min = withValue(start, Axis.Y, minY);
            BlockPos max = new BlockPos(min.getX() + pattern.length - 1, maxY,
                min.getZ() + (pattern.length == 0 ? 0 : pattern[0].length - 1));
            return new PatternTerrainHeight(box(min, max), replaceType, pattern, depth);
        }

        @Override
        protected void generateWithin(WorldGenLevel world, BoundingBox intersect) {
            for (int x = intersect.minX(); x <= intersect.maxX(); x++) {
                int px = x - box.minX();

                for (int z = intersect.minZ(); z <= intersect.maxZ(); z++) {
                    int pz = z - box.minZ();

                    if (pattern[px][pz]) {
                        // 1.12's World.getHeight counted every block that stops light, including water and leaves.
                        BlockPos upper = new BlockPos(x, world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z) - 1, z);
                        if (upper.getY() < world.getMinBuildHeight()) {
                            continue;
                        }
                        if (canReplaceForOil(world, upper)) {
                            for (int y = 0; y < 5; y++) {
                                setAir(world, upper.above(y));
                            }
                            for (int y = 0; y < depth; y++) {
                                setOilIfCanReplace(world, upper.below(y));
                            }
                        }
                    }
                }
            }
        }

        @Override
        public int countOilBlocks() {
            int count = 0;
            for (boolean[] row : pattern) {
                for (boolean set : row) {
                    if (set) {
                        count++;
                    }
                }
            }
            return count * depth;
        }
    }

    /** A tube of oil from the well up to the surface, then a column of oil above the surface that gets thinner. */
    public static class Spout extends OilGenStructure {
        public final BlockPos start;
        public final int radius;
        public final int height;
        private int count = 0;

        public Spout(BlockPos start, int maxY, ReplaceType replaceType, int radius, int height) {
            super(createBox(start, maxY), replaceType);
            this.start = start;
            this.radius = radius;
            this.height = height;
        }

        private static BoundingBox createBox(BlockPos start, int maxY) {
            // Only a single column -- that way we are only called once.
            return box(start, withValue(start, Axis.Y, Math.max(start.getY(), maxY)));
        }

        @Override
        protected void generateWithin(WorldGenLevel world, BoundingBox intersect) {
            count = 0;
            // Find the first block from the top that is a fluid or that blocks movement.
            int top = world.getHeight(Heightmap.Types.WORLD_SURFACE, start.getX(), start.getZ()) - 1;
            BlockPos worldTop = start;
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int y = top; y >= start.getY(); y--) {
                pos.set(start.getX(), y, start.getZ());
                BlockState state = world.getBlockState(pos);
                if (state.isAir()) {
                    continue;
                }
                if (!state.getFluidState().isEmpty() || state.blocksMotion()) {
                    worldTop = pos.immutable();
                    break;
                }
            }
            if (OilGenerator.DEBUG_OILGEN_BASIC) {
                BuildCraftReborn.LOGGER.info("[energy.oilgen] Spout at {}, {} from y {} to {} (+{} x {})", start.getX(),
                    start.getZ(), start.getY(), worldTop.getY(), height, radius + 1);
            }
            OilGenStructure tubeY = OilGenerator.createTube(start, worldTop.getY() - start.getY(), radius, Axis.Y);
            tubeY.generate(world, tubeY.box);
            count += tubeY.countOilBlocks();
            BlockPos base = worldTop;
            for (int r = radius; r >= 0; r--) {
                OilGenStructure struct = OilGenerator.createTube(base, height, r, Axis.Y);
                struct.generate(world, struct.box);
                base = base.offset(0, height, 0);
                count += struct.countOilBlocks();
            }
        }

        @Override
        public int countOilBlocks() {
            if (count == 0) {
                throw new IllegalStateException("Called countOilBlocks before calling generateWithin!");
            }
            return count;
        }
    }

    /** The oil spring under a large well. It only knows how much oil the well has once the rest of the well is
     * generated, so the generator places it separately with {@link #generate(WorldGenLevel, int)}. */
    public static class Spring extends OilGenStructure {
        public final BlockPos pos;

        public Spring(BlockPos pos) {
            super(box(pos, pos), ReplaceType.ALWAYS);
            this.pos = pos;
        }

        @Override
        protected void generateWithin(WorldGenLevel world, BoundingBox intersect) {
            // NO-OP (this one is called separately)
        }

        @Override
        public int countOilBlocks() {
            return 0;
        }

        public void generate(WorldGenLevel world, int count) {
            BlockSpringOil block = BCEnergyBlocks.OIL_SPRING.get();
            world.setBlock(pos, block.defaultBlockState(), FLAGS);
            world.scheduleTick(pos, block, block.type.tickRate);
            // In a chunk that is still generating this creates the tile from the block.
            BlockEntity tile = world.getBlockEntity(pos);
            if (tile instanceof TileSpringOil spring) {
                spring.totalSources = count;
                spring.setChanged();
            } else {
                BuildCraftReborn.LOGGER.warn("[energy.gen.oil] Setting the blockstate didn't also set the tile at {}",
                    pos);
            }
        }
    }
}
