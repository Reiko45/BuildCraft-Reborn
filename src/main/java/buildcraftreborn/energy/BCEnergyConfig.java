/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.common.ModConfigSpec;

/** Energy settings from 1.12's {@code BCEnergyConfig}, in {@code config/buildcraftreborn-energy.toml}.
 * <p>
 * 1.21 differences: there are no oil biomes (oil wells generate in normal biomes), and 1.12's biome name lists are
 * biome tags instead: {@code #buildcraftreborn:oil/excessive}, {@code #buildcraftreborn:oil/surface_deposit} and
 * {@code #buildcraftreborn:oil/excluded}. Dimensions are listed by name ({@code minecraft:the_nether}) instead of by
 * number. The RF engine and MJ dynamo are always registered (so clients and servers agree on the registries); when
 * they are disabled (the default, as in 1.12) they have no recipe and are left out of the creative tab. */
public final class BCEnergyConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLE_OIL_GENERATION;
    private static final ModConfigSpec.DoubleValue OIL_WELL_GENERATION_RATE;
    private static final ModConfigSpec.BooleanValue ENABLE_OIL_BURN;
    private static final ModConfigSpec.BooleanValue OIL_IS_STICKY;
    private static final ModConfigSpec.BooleanValue ENABLE_RF_ENGINE;
    private static final ModConfigSpec.BooleanValue ENABLE_MJ_DYNAMO;
    private static final ModConfigSpec.DoubleValue SMALL_OIL_GEN_PROB;
    private static final ModConfigSpec.DoubleValue MEDIUM_OIL_GEN_PROB;
    private static final ModConfigSpec.DoubleValue LARGE_OIL_GEN_PROB;
    private static final ModConfigSpec.BooleanValue ENABLE_OIL_SPOUTS;
    private static final ModConfigSpec.IntValue SMALL_SPOUT_MIN_HEIGHT;
    private static final ModConfigSpec.IntValue SMALL_SPOUT_MAX_HEIGHT;
    private static final ModConfigSpec.IntValue LARGE_SPOUT_MIN_HEIGHT;
    private static final ModConfigSpec.IntValue LARGE_SPOUT_MAX_HEIGHT;
    private static final ModConfigSpec.BooleanValue EXCLUDED_BIOMES_IS_BLACKLIST;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> EXCLUDED_DIMENSIONS;
    private static final ModConfigSpec.BooleanValue EXCLUDED_DIMENSIONS_IS_BLACKLIST;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("general");
        OIL_IS_STICKY = b.comment("Should oil be dense and drag entities down?").define("oilIsDense", false);
        ENABLE_RF_ENGINE = b.comment("Enable the RF to MJ engine? (Its recipe and creative tab entry; needs a world"
            + " reload to change.)").define("enableRfEngine", false);
        ENABLE_MJ_DYNAMO = b.comment("Enable the MJ to RF dynamo / engine / generator? (Its recipe and creative tab"
            + " entry; needs a world reload to change.)").define("enableMjDynamo", false);
        b.pop();

        b.push("worldgen").push("oil");
        ENABLE_OIL_GENERATION = b.comment("Should any oil sprouts or lakes be generated at all?").define("enable", true);
        ENABLE_OIL_BURN = b.comment("Can oil blocks burn?").define("can_burn", true);
        OIL_WELL_GENERATION_RATE = b.comment("The rate of occurrence of oil wells.")
            .defineInRange("generationRate", 1.0, 0.0, 100.0);
        b.push("spawn_probability");
        SMALL_OIL_GEN_PROB = b.comment("The percentage probability of a small oil spawn")
            .defineInRange("small", 2.0, 0.0, 100.0);
        MEDIUM_OIL_GEN_PROB = b.comment("The percentage probability of a medium oil spawn")
            .defineInRange("medium", 0.1, 0.0, 100.0);
        LARGE_OIL_GEN_PROB = b.comment("The percentage probability of a large oil spawn")
            .defineInRange("large", 0.04, 0.0, 100.0);
        b.pop();
        b.push("spouts");
        ENABLE_OIL_SPOUTS = b.comment("Whether oil spouts are generated or not. The oil spring at the bottom of large"
            + " lakes will still exist.").define("enable", true);
        SMALL_SPOUT_MIN_HEIGHT = b.comment("The minimum height for small oil spouts")
            .defineInRange("small_min_height", 6, 0, 256);
        SMALL_SPOUT_MAX_HEIGHT = b.comment("The maximum height for small oil spouts")
            .defineInRange("small_max_height", 12, 0, 256);
        LARGE_SPOUT_MIN_HEIGHT = b.comment("The minimum height for large oil spouts")
            .defineInRange("large_min_height", 10, 0, 256);
        LARGE_SPOUT_MAX_HEIGHT = b.comment("The maximum height for large oil spouts")
            .defineInRange("large_max_height", 20, 0, 256);
        b.pop();
        EXCLUDED_BIOMES_IS_BLACKLIST = b.comment("Biomes get more or less oil from the biome tags"
            + " #buildcraftreborn:oil/excessive (30 times more), #buildcraftreborn:oil/surface_deposit (3 times more,"
            + " plus surface lakes) and #buildcraftreborn:oil/excluded (none), which a datapack can change.",
            "If true then the excluded biomes tag will be treated as a blacklist, otherwise it will be treated as a"
                + " whitelist.").define("excludedBiomesIsBlacklist", true);
        EXCLUDED_DIMENSIONS = b.comment("Dimension names (e.g. 'minecraft:the_nether','minecraft:the_end') of"
            + " dimensions that should never generate oil.").defineListAllowEmpty("excludedDimensions",
                List.of("minecraft:the_nether", "minecraft:the_end"), () -> "minecraft:overworld",
                o -> o instanceof String str && ResourceLocation.tryParse(str) != null);
        EXCLUDED_DIMENSIONS_IS_BLACKLIST = b.comment("If true then the excluded dimensions list will be treated as a"
            + " blacklist, otherwise it will be treated as a whitelist.").define("excludedDimensionsIsBlacklist", true);
        b.pop().pop();
        SPEC = b.build();
    }

    private BCEnergyConfig() {}

    private static boolean loaded() {
        return SPEC.isLoaded();
    }

    public static boolean enableOilGeneration() {
        return !loaded() || ENABLE_OIL_GENERATION.get();
    }

    public static double oilWellGenerationRate() {
        return loaded() ? OIL_WELL_GENERATION_RATE.get() : 1.0;
    }

    public static boolean enableOilBurn() {
        return !loaded() || ENABLE_OIL_BURN.get();
    }

    public static boolean enableRfEngine() {
        return loaded() && ENABLE_RF_ENGINE.get();
    }

    public static boolean enableMjDynamo() {
        return loaded() && ENABLE_MJ_DYNAMO.get();
    }

    public static boolean oilIsSticky() {
        return loaded() && OIL_IS_STICKY.get();
    }

    public static double smallOilGenProb() {
        return (loaded() ? SMALL_OIL_GEN_PROB.get() : 2.0) / 100;
    }

    public static double mediumOilGenProb() {
        return (loaded() ? MEDIUM_OIL_GEN_PROB.get() : 0.1) / 100;
    }

    public static double largeOilGenProb() {
        return (loaded() ? LARGE_OIL_GEN_PROB.get() : 0.04) / 100;
    }

    public static boolean enableOilSpouts() {
        return !loaded() || ENABLE_OIL_SPOUTS.get();
    }

    public static int smallSpoutMinHeight() {
        return loaded() ? SMALL_SPOUT_MIN_HEIGHT.get() : 6;
    }

    public static int smallSpoutMaxHeight() {
        return loaded() ? Math.max(SMALL_SPOUT_MIN_HEIGHT.get(), SMALL_SPOUT_MAX_HEIGHT.get()) : 12;
    }

    public static int largeSpoutMinHeight() {
        return loaded() ? LARGE_SPOUT_MIN_HEIGHT.get() : 10;
    }

    public static boolean excludedBiomesIsBlacklist() {
        return !loaded() || EXCLUDED_BIOMES_IS_BLACKLIST.get();
    }

    /** @return true if oil shouldn't generate in the given dimension. */
    public static boolean isDimensionExcluded(ResourceLocation dimension) {
        List<? extends String> list = loaded() ? EXCLUDED_DIMENSIONS.get()
            : List.of("minecraft:the_nether", "minecraft:the_end");
        boolean listed = list.contains(dimension.toString());
        boolean blacklist = !loaded() || EXCLUDED_DIMENSIONS_IS_BLACKLIST.get();
        return listed == blacklist;
    }

    public static int largeSpoutMaxHeight() {
        return loaded() ? Math.max(LARGE_SPOUT_MIN_HEIGHT.get(), LARGE_SPOUT_MAX_HEIGHT.get()) : 20;
    }
}
