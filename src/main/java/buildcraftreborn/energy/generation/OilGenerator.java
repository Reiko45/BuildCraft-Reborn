/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.generation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.energy.BCEnergyConfig;
import buildcraftreborn.energy.BCEnergyTags;
import buildcraftreborn.energy.generation.OilGenStructure.GenByPredicate;
import buildcraftreborn.energy.generation.OilGenStructure.ReplaceType;
import buildcraftreborn.lib.misc.RandUtil;

/** Places oil wells. Every chunk works out which wells the chunks within {@link #MAX_CHUNK_RADIUS} would have (that
 * only depends on the world seed and the biome at the well's centre) and generates the parts of them that fall inside
 * it. Runs from {@link OilWellFeature}.
 * <p>
 * 1.21 differences: there are no oil biomes, so 1.12's biome name lists are the biome tags in {@link BCEnergyTags};
 * the biome is sampled from the biome source at sea level (so it doesn't depend on whether the well's chunk exists yet,
 * and isn't a cave biome); the deep shaft and spring of large wells go down to the bottom of the world instead of
 * y=0. */
public final class OilGenerator {
    private OilGenerator() {}

    /** Random number, used to differentiate generators */
    private static final long MAGIC_GEN_NUMBER = 0xD0_46_B4_E4_0C_7D_07_CFL;

    /** The distance that oil generation will be checked to see if their structures overlap with the currently
     * generating chunk. This should be large enough that all oil generation can fit inside this radius. If this number
     * is too big then oil generation will be slightly slower */
    private static final int MAX_CHUNK_RADIUS = 5;

    /** Set {@code -Dbuildcraftreborn.debug.oilgen=true} to log each well that generates. */
    public static final boolean DEBUG_OILGEN_BASIC = Boolean.getBoolean("buildcraftreborn.debug.oilgen");

    public enum GenType {
        LARGE,
        MEDIUM,
        LAKE,
        NONE
    }

    /** What well generation needs from the world, so tests can run it without a chunk generator. */
    public record WorldInfo(long seed, int minY, int maxY, boolean isEnd, BiomeLookup biomes) {}

    @FunctionalInterface
    public interface BiomeLookup {
        Holder<Biome> getBiome(int blockX, int blockZ);
    }

    /** Generates every part of every oil well that falls inside the given chunk. */
    public static void generate(WorldGenLevel world, WorldInfo info, ChunkPos chunk) {
        BoundingBox box = new BoundingBox(chunk.getMinBlockX(), info.minY(), chunk.getMinBlockZ(),
            chunk.getMaxBlockX(), info.maxY(), chunk.getMaxBlockZ());

        for (int cdx = -MAX_CHUNK_RADIUS; cdx <= MAX_CHUNK_RADIUS; cdx++) {
            for (int cdz = -MAX_CHUNK_RADIUS; cdz <= MAX_CHUNK_RADIUS; cdz++) {
                int cx = chunk.x + cdx;
                int cz = chunk.z + cdz;
                List<OilGenStructure> structures = getStructures(info, cx, cz, cdx == 0 && cdz == 0);
                OilGenStructure.Spring spring = null;
                for (OilGenStructure struct : structures) {
                    struct.generate(world, box);
                    if (struct instanceof OilGenStructure.Spring s) {
                        spring = s;
                    }
                }
                if (spring != null && box.isInside(spring.pos)) {
                    int count = 0;
                    for (OilGenStructure struct : structures) {
                        count += struct.countOilBlocks();
                    }
                    spring.generate(world, count);
                }
            }
        }
    }

    public static List<OilGenStructure> getStructures(WorldInfo info, int cx, int cz) {
        return getStructures(info, cx, cz, false);
    }

    /** @return The type of well the given chunk has, or {@link GenType#NONE}. */
    public static GenType getGenType(WorldInfo info, int cx, int cz) {
        Random rand = RandUtil.createRandomForChunk(info.seed(), cx, cz, MAGIC_GEN_NUMBER);
        int x = cx * 16 + 8 + rand.nextInt(16);
        int z = cz * 16 + 8 + rand.nextInt(16);
        return pickType(info, rand, x, z);
    }

    private static GenType pickType(WorldInfo info, Random rand, int x, int z) {
        Holder<Biome> biome = info.biomes().getBiome(x, z);

        // Do not generate oil in excluded biomes
        boolean isExcludedBiome = biome.is(BCEnergyTags.OIL_EXCLUDED);
        if (isExcludedBiome == BCEnergyConfig.excludedBiomesIsBlacklist()) {
            return GenType.NONE;
        }

        // Not within 1200 blocks of the ender dragon fight. (1.12 used "||" here, which excluded two long strips
        // through the whole End instead of a square around the main island.)
        if (info.isEnd() && Math.abs(x) < 1200 && Math.abs(z) < 1200) {
            return GenType.NONE;
        }

        boolean oilBiome = biome.is(BCEnergyTags.OIL_SURFACE_DEPOSIT);

        double bonus = oilBiome ? 3.0 : 1.0;
        bonus *= BCEnergyConfig.oilWellGenerationRate();
        if (biome.is(BCEnergyTags.OIL_EXCESSIVE)) {
            bonus *= 30.0;
        }

        if (rand.nextDouble() <= BCEnergyConfig.largeOilGenProb() * bonus) {
            // 0.04%
            return GenType.LARGE;
        } else if (rand.nextDouble() <= BCEnergyConfig.mediumOilGenProb() * bonus) {
            // 0.1%
            return GenType.MEDIUM;
        } else if (oilBiome && rand.nextDouble() <= BCEnergyConfig.smallOilGenProb() * bonus) {
            // 2%
            return GenType.LAKE;
        } else {
            return GenType.NONE;
        }
    }

    private static List<OilGenStructure> getStructures(WorldInfo info, int cx, int cz, boolean log) {
        Random rand = RandUtil.createRandomForChunk(info.seed(), cx, cz, MAGIC_GEN_NUMBER);

        // shift to world coordinates
        int x = cx * 16 + 8 + rand.nextInt(16);
        int z = cz * 16 + 8 + rand.nextInt(16);

        final GenType type = pickType(info, rand, x, z);
        if (type == GenType.NONE) {
            return ImmutableList.of();
        }
        if (DEBUG_OILGEN_BASIC & log) {
            BuildCraftReborn.LOGGER.info("[energy.oilgen] Generating an oil well ({}) in chunk {}, {} at {}, {}",
                type.name().toLowerCase(Locale.ROOT), cx, cz, x, z);
        }

        List<OilGenStructure> structures = new ArrayList<>();
        int lakeRadius;
        int tendrilRadius;
        if (type == GenType.LARGE) {
            lakeRadius = 4;
            tendrilRadius = 25 + rand.nextInt(20);
        } else if (type == GenType.LAKE) {
            lakeRadius = 6;
            tendrilRadius = 25 + rand.nextInt(20);
        } else {
            lakeRadius = 2;
            tendrilRadius = 5 + rand.nextInt(10);
        }
        structures.add(createTendril(info, new BlockPos(x, 62, z), lakeRadius, tendrilRadius, rand));

        if (type != GenType.LAKE) {
            // Generate a spherical cave deposit
            int wellY = 20 + rand.nextInt(10);

            int radius;
            if (type == GenType.LARGE) {
                radius = 8 + rand.nextInt(9);
            } else {
                radius = 4 + rand.nextInt(4);
            }

            structures.add(createSphere(new BlockPos(x, wellY, z), radius));

            // Generate a spout
            if (BCEnergyConfig.enableOilSpouts()) {
                int maxHeight, minHeight;

                if (type == GenType.LARGE) {
                    minHeight = BCEnergyConfig.largeSpoutMinHeight();
                    maxHeight = BCEnergyConfig.largeSpoutMaxHeight();
                    radius = 1;
                } else {
                    minHeight = BCEnergyConfig.smallSpoutMinHeight();
                    maxHeight = BCEnergyConfig.smallSpoutMaxHeight();
                    radius = 0;
                }
                final int height;
                if (maxHeight == minHeight) {
                    height = maxHeight;
                } else {
                    if (maxHeight < minHeight) {
                        int t = maxHeight;
                        maxHeight = minHeight;
                        minHeight = t;
                    }
                    height = minHeight + rand.nextInt(maxHeight - minHeight);
                }
                structures.add(createSpout(info, new BlockPos(x, wellY, z), height, radius));
            }

            // Generate a spring at the very bottom
            if (type == GenType.LARGE) {
                // 1.12 reused the spout's radius (1) here, so with spouts turned off the shaft became as wide as the
                // sphere. The shaft is always 1 here.
                int bottom = info.minY();
                structures.add(createTube(new BlockPos(x, bottom + 1, z), wellY - bottom, 1, Axis.Y));
                structures.add(createSpring(new BlockPos(x, bottom, z)));
            }
        }
        return structures;
    }

    private static OilGenStructure createSpout(WorldInfo info, BlockPos start, int height, int radius) {
        return new OilGenStructure.Spout(start, info.maxY(), ReplaceType.ALWAYS, radius, height);
    }

    public static OilGenStructure createTubeY(BlockPos base, int height, int radius) {
        return createTube(base, height, radius, Axis.Y);
    }

    public static OilGenStructure.Spring createSpring(BlockPos at) {
        return new OilGenStructure.Spring(at);
    }

    public static OilGenStructure createTube(BlockPos center, int length, int radius, Axis axis) {
        length = Math.max(0, length);
        int valForAxis = center.get(axis);
        BlockPos min = OilGenStructure.withValue(center.offset(-radius, -radius, -radius), axis, valForAxis);
        BlockPos max = OilGenStructure.withValue(center.offset(radius, radius, radius), axis, valForAxis + length);
        double radiusSq = radius * radius;
        int toReplace = valForAxis;
        Predicate<BlockPos> tester = p -> OilGenStructure.withValue(p, axis, toReplace).distSqr(center) <= radiusSq;
        return new GenByPredicate(OilGenStructure.box(min, max), ReplaceType.ALWAYS, tester);
    }

    public static OilGenStructure createSphere(BlockPos center, int radius) {
        BoundingBox box = OilGenStructure.box(center.offset(-radius, -radius, -radius),
            center.offset(radius, radius, radius));
        double radiusSq = radius * radius + 0.01;
        Predicate<BlockPos> tester = p -> p.distSqr(center) <= radiusSq;
        return new GenByPredicate(box, ReplaceType.ALWAYS, tester);
    }

    public static OilGenStructure createTendril(WorldInfo info, BlockPos center, int lakeRadius, int radius,
        Random rand) {
        BlockPos start = center.offset(-radius, 0, -radius);
        int diameter = radius * 2 + 1;
        boolean[][] pattern = new boolean[diameter][diameter];

        int x = radius;
        int z = radius;
        for (int dx = -lakeRadius; dx <= lakeRadius; dx++) {
            for (int dz = -lakeRadius; dz <= lakeRadius; dz++) {
                pattern[x + dx][z + dz] = dx * dx + dz * dz <= lakeRadius * lakeRadius;
            }
        }

        for (int w = 1; w < radius; w++) {
            float proba = (float) (radius - w + 4) / (float) (radius + 4);

            fillPatternIfProba(rand, proba, x, z + w, pattern);
            fillPatternIfProba(rand, proba, x, z - w, pattern);
            fillPatternIfProba(rand, proba, x + w, z, pattern);
            fillPatternIfProba(rand, proba, x - w, z, pattern);

            for (int i = 1; i <= w; i++) {
                fillPatternIfProba(rand, proba, x + i, z + w, pattern);
                fillPatternIfProba(rand, proba, x + i, z - w, pattern);
                fillPatternIfProba(rand, proba, x + w, z + i, pattern);
                fillPatternIfProba(rand, proba, x - w, z + i, pattern);

                fillPatternIfProba(rand, proba, x - i, z + w, pattern);
                fillPatternIfProba(rand, proba, x - i, z - w, pattern);
                fillPatternIfProba(rand, proba, x + w, z - i, pattern);
                fillPatternIfProba(rand, proba, x - w, z - i, pattern);
            }
        }

        int depth = rand.nextDouble() < 0.5 ? 1 : 2;
        return OilGenStructure.PatternTerrainHeight.create(start, info.minY(), info.maxY(), ReplaceType.IS_FOR_LAKE,
            pattern, depth);
    }

    private static void fillPatternIfProba(Random rand, float proba, int x, int z, boolean[][] pattern) {
        if (rand.nextFloat() <= proba) {
            pattern[x][z] = isSet(pattern, x, z - 1) | isSet(pattern, x, z + 1) //
                | isSet(pattern, x - 1, z) | isSet(pattern, x + 1, z);
        }
    }

    private static boolean isSet(boolean[][] pattern, int x, int z) {
        if (x < 0 || x >= pattern.length) return false;
        if (z < 0 || z >= pattern[x].length) return false;
        return pattern[x][z];
    }
}
