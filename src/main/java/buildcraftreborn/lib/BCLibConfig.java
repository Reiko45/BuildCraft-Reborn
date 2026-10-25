/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.lib;

import net.minecraft.core.Direction;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Settings ported from 1.12's {@code BCLibConfig} and {@code BCCoreConfig}, now on {@link ModConfigSpec}. Only the
 * options whose systems exist so far are here; the rest arrive with their modules.
 */
public final class BCLibConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final ModConfigSpec CLIENT_SPEC;

    // Common (both sides)
    public static final ModConfigSpec.IntValue ITEM_LIFESPAN;
    public static final ModConfigSpec.EnumValue<ChunkLoaderLevel> CHUNK_LOAD_LEVEL;
    public static final ModConfigSpec.IntValue MARKER_MAX_DISTANCE;
    public static final ModConfigSpec.BooleanValue MINE_PLAYER_PROTECTED;
    public static final ModConfigSpec.BooleanValue PUMPS_CONSUME_WATER;
    public static final ModConfigSpec.IntValue PUMP_MAX_DISTANCE;
    public static final ModConfigSpec.IntValue NETWORK_UPDATE_RATE;
    public static final ModConfigSpec.DoubleValue MINING_MULTIPLIER;
    public static final ModConfigSpec.IntValue MINING_MAX_DEPTH;

    // Client display
    public static final ModConfigSpec.BooleanValue USE_COLOURED_LABELS;
    public static final ModConfigSpec.BooleanValue USE_HIGH_CONTRAST_LABEL_COLOURS;
    public static final ModConfigSpec.BooleanValue COLOUR_BLIND_MODE;
    public static final ModConfigSpec.BooleanValue USE_BUCKETS_STATIC;
    public static final ModConfigSpec.BooleanValue USE_BUCKETS_FLOW;
    public static final ModConfigSpec.BooleanValue USE_LONG_LOCALIZED_NAME;
    public static final ModConfigSpec.EnumValue<RenderRotation> ITEM_RENDER_ROTATION;
    public static final ModConfigSpec.BooleanValue RENDER_LASER_BEAMS;
    public static final ModConfigSpec.BooleanValue GUIDE_SHOW_DETAIL;
    public static final ModConfigSpec.IntValue GUIDE_ITEM_SEARCH_LIMIT;
    public static final ModConfigSpec.IntValue MAX_GUIDE_SEARCH_RESULTS;

    static {
        ModConfigSpec.Builder common = new ModConfigSpec.Builder();
        ITEM_LIFESPAN = common
            .comment("How long, in seconds, items dropped by quarries, builders and other machines stay on the ground.",
                "Vanilla = 300, default = 60.")
            .defineInRange("itemLifespan", 60, 5, 600);
        CHUNK_LOAD_LEVEL = common
            .comment("Which machines keep their chunks loaded.")
            .defineEnum("chunkLoadLevel", ChunkLoaderLevel.SELF_TILES);
        MARKER_MAX_DISTANCE = common
            .comment("How far, in minecraft blocks, should markers (volume and path) reach?")
            .defineInRange("markerMaxDistance", 64, 16, 256);
        MINE_PLAYER_PROTECTED = common
            .comment("Should mining machines break blocks that other mods protect from the machine's owner?")
            .define("miningBreaksPlayerProtectedBlocks", false);
        PUMPS_CONSUME_WATER = common
            .comment("Should pumps consume water? Enabling this will disable minor optimisations, but work properly",
                "with finite water mods.")
            .define("pumpsConsumeWater", false);
        PUMP_MAX_DISTANCE = common
            .comment("How far, in minecraft blocks, should pumps reach in fluids?")
            .defineInRange("pumpMaxDistance", 64, 16, 128);
        NETWORK_UPDATE_RATE = common
            .comment("How often, in ticks, should network update packets be sent? Increasing this might help network",
                "performance.")
            .defineInRange("updateFactor", 10, 1, 100);
        MINING_MULTIPLIER = common
            .comment("How much power should be required for all mining machines?")
            .defineInRange("miningMultiplier", 1.0, 1.0, 200.0);
        MINING_MAX_DEPTH = common
            .comment("How much further down can miners (like the quarry or the mining well) dig?")
            .defineInRange("miningMaxDepth", 512, 32, 4096);
        COMMON_SPEC = common.build();

        ModConfigSpec.Builder client = new ModConfigSpec.Builder();
        USE_COLOURED_LABELS = client
            .comment("Show dye-coloured items (pipes, paintbrushes, pipe wire) with their colour in the item name.")
            .define("useColouredLabels", true);
        USE_HIGH_CONTRAST_LABEL_COLOURS = client
            .comment("With coloured labels on, only use colours that strongly contrast with the background.")
            .define("useHighContrastLabelColours", false);
        COLOUR_BLIND_MODE = client
            .comment("Display applicable visual elements in a more colour-blind friendly way.")
            .define("colourBlindMode", false);
        USE_BUCKETS_STATIC = client
            .comment("Show fluid amounts as buckets (\"4B Water\") instead of millibuckets (\"4000mB of Water\").")
            .define("useBucketsStatic", true);
        USE_BUCKETS_FLOW = client
            .comment("Show fluid flow rates as buckets per second (\"4B/s\") instead of millibuckets per tick.")
            .define("useBucketsFlow", true);
        USE_LONG_LOCALIZED_NAME = client
            .comment("Use long unit names, for example \"1.2 Buckets per second\" instead of \"60mB/t\".")
            .define("useLongLocalizedName", false);
        ITEM_RENDER_ROTATION = client
            .comment("Which way items travelling through pipes face: DISABLED (always east), HORIZONTALS_ONLY or",
                "ENABLED (the way they travel).")
            .defineEnum("itemRenderRotation", RenderRotation.ENABLED);
        RENDER_LASER_BEAMS = client
            .comment("When false laser beams will not be visible while transmitting power, unless wearing robot "
                + "goggles.")
            .define("renderLaserBeams", true);
        GUIDE_SHOW_DETAIL = client
            .comment("Show the detailed numbers (flow rates, power use...) in the guide book.")
            .define("guideBookEnableDetail", false);
        GUIDE_ITEM_SEARCH_LIMIT = client
            .comment("The maximum number of items that the guide book will index.")
            .defineInRange("guideItemSearchLimit", 10_000, 1_500, 5_000_000);
        MAX_GUIDE_SEARCH_RESULTS = client
            .comment("The maximum number of search results to display in the guide book.")
            .defineInRange("maxGuideSearchResults", 1200, 500, 5000);
        CLIENT_SPEC = client.build();
    }

    private BCLibConfig() {}

    /** How far apart markers can be and still connect (1.12's {@code markerMaxDistance}). */
    public static int markerMaxDistance() {
        return COMMON_SPEC.isLoaded() ? MARKER_MAX_DISTANCE.get() : 64;
    }

    /** 1.12's {@code minePlayerProtected}: if false, machines only break blocks their owner could break. */
    public static boolean minePlayerProtected() {
        return COMMON_SPEC.isLoaded() && MINE_PLAYER_PROTECTED.get();
    }

    public static boolean pumpsConsumeWater() {
        return COMMON_SPEC.isLoaded() && PUMPS_CONSUME_WATER.get();
    }

    public static int pumpMaxDistance() {
        return COMMON_SPEC.isLoaded() ? PUMP_MAX_DISTANCE.get() : 64;
    }

    /** 1.12's {@code networkUpdateRate} ("updateFactor"): ticks between repeated network updates. */
    public static int networkUpdateRate() {
        return COMMON_SPEC.isLoaded() ? NETWORK_UPDATE_RATE.get() : 10;
    }

    public static double miningMultiplier() {
        return COMMON_SPEC.isLoaded() ? MINING_MULTIPLIER.get() : 1;
    }

    public static int miningMaxDepth() {
        return COMMON_SPEC.isLoaded() ? MINING_MAX_DEPTH.get() : 512;
    }

    public static boolean guideShowDetail() {
        return CLIENT_SPEC.isLoaded() && GUIDE_SHOW_DETAIL.get();
    }

    public static int guideItemSearchLimit() {
        return CLIENT_SPEC.isLoaded() ? GUIDE_ITEM_SEARCH_LIMIT.get() : 10_000;
    }

    public static int maxGuideSearchResults() {
        return CLIENT_SPEC.isLoaded() ? MAX_GUIDE_SEARCH_RESULTS.get() : 1200;
    }

    public static boolean colourBlindMode() {
        return CLIENT_SPEC.isLoaded() && COLOUR_BLIND_MODE.get();
    }

    public static boolean renderLaserBeams() {
        return !CLIENT_SPEC.isLoaded() || RENDER_LASER_BEAMS.get();
    }

    public static RenderRotation itemRenderRotation() {
        return CLIENT_SPEC.isLoaded() ? ITEM_RENDER_ROTATION.get() : RenderRotation.ENABLED;
    }

    /** How items travelling through pipes are turned. */
    public enum RenderRotation {
        DISABLED {
            @Override
            public Direction changeFacing(Direction dir) {
                return Direction.EAST;
            }
        },
        HORIZONTALS_ONLY {
            @Override
            public Direction changeFacing(Direction dir) {
                return dir.getAxis() == Direction.Axis.Y ? Direction.EAST : dir;
            }
        },
        ENABLED {
            @Override
            public Direction changeFacing(Direction dir) {
                return dir;
            }
        };

        public abstract Direction changeFacing(Direction dir);
    }

    /** Which block entities may force their chunks to stay loaded. Same values as 1.12. */
    public enum ChunkLoaderLevel {
        /** No automatic chunk loading. */
        NONE,
        /** Only machines that must stay loaded to work correctly, such as the quarry. */
        STRICT_TILES,
        /** Every BuildCraft machine that asks to be chunk loaded. */
        SELF_TILES,
        /** Every block entity in the world. */
        ALL_TILES;

        public boolean canLoad(buildcraftreborn.lib.chunkload.IChunkLoadingTile.LoadType loadType) {
            return switch (this) {
                case NONE -> false;
                case STRICT_TILES -> loadType == buildcraftreborn.lib.chunkload.IChunkLoadingTile.LoadType.HARD;
                case SELF_TILES, ALL_TILES -> true;
            };
        }
    }

    public static ChunkLoaderLevel chunkLoadLevel() {
        return COMMON_SPEC.isLoaded() ? CHUNK_LOAD_LEVEL.get() : ChunkLoaderLevel.SELF_TILES;
    }
}
