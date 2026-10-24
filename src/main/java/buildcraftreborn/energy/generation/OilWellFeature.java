/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.energy.generation;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import buildcraftreborn.energy.BCEnergyConfig;

/** The oil well feature, {@code buildcraftreborn:oil_wells}. It replaces 1.12's {@code PopulateChunkEvent.Pre}
 * handler and runs once per chunk, in the {@code lakes} step, in every biome (see the biome modifier
 * {@code data/buildcraftreborn/neoforge/biome_modifier/oil_wells.json}); the biome at each well's centre decides
 * whether that well exists. */
public class OilWellFeature extends Feature<NoneFeatureConfiguration> {
    public OilWellFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        if (!BCEnergyConfig.enableOilGeneration()) {
            return false;
        }
        WorldGenLevel level = context.level();
        ChunkGenerator generator = context.chunkGenerator();
        if (generator instanceof FlatLevelSource) {
            // 1.12 didn't generate oil in superflat worlds either.
            return false;
        }
        ResourceKey<Level> dimension = level.getLevel().dimension();
        if (BCEnergyConfig.isDimensionExcluded(dimension.location())) {
            return false;
        }
        BiomeSource biomes = generator.getBiomeSource();
        Climate.Sampler sampler = level.getLevel().getChunkSource().randomState().sampler();
        int sampleY = QuartPos.fromBlock(generator.getSeaLevel());
        Cache<Long, Holder<Biome>> cache = BIOME_CACHES.getUnchecked(level.getLevel());
        OilGenerator.WorldInfo info = new OilGenerator.WorldInfo(level.getSeed(), level.getMinBuildHeight(),
            level.getMaxBuildHeight() - 1, dimension == Level.END, (x, z) -> {
                // Every chunk looks at the wells of the 121 chunks around it, so the (slow) biome noise at each
                // well centre is remembered.
                long key = ((long) x << 32) | (z & 0xFFFFFFFFL);
                Holder<Biome> biome = cache.getIfPresent(key);
                if (biome == null) {
                    biome = biomes.getNoiseBiome(QuartPos.fromBlock(x), sampleY, QuartPos.fromBlock(z), sampler);
                    cache.put(key, biome);
                }
                return biome;
            });
        OilGenerator.generate(level, info, new ChunkPos(context.origin()));
        return true;
    }

    /** Biomes at well centres, per level. Weak keys, so a closed world's cache goes with it. */
    private static final LoadingCache<ServerLevel, Cache<Long, Holder<Biome>>> BIOME_CACHES = CacheBuilder
        .newBuilder().weakKeys().build(CacheLoader.from(() -> CacheBuilder.newBuilder().maximumSize(16384).build()));
}
