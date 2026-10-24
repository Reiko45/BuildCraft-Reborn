/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/** Quarry settings from 1.12's {@code BCBuildersConfig}, in {@code config/buildcraftreborn-builders.toml}.
 * <p>
 * 1.21: the options 1.12 read but never used ({@code bptStoreExternalThreshold}, {@code quarryFrameMoveBoth}) and
 * the architect table's stencil options (the stencil drawing isn't ported) are gone. */
public final class BCBuildersConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue QUARRY_FRAME_MIN_HEIGHT;
    private static final ModConfigSpec.IntValue QUARRY_MAX_TASKS_PER_TICK;
    private static final ModConfigSpec.IntValue QUARRY_POWER_DIVISOR;
    private static final ModConfigSpec.DoubleValue QUARRY_MAX_FRAME_SPEED;
    private static final ModConfigSpec.DoubleValue QUARRY_MAX_BLOCK_MINE_RATE;

    /** The minimum height that all quarry frames must be. */
    public static int quarryFrameMinHeight = 4;
    public static int quarryMaxTasksPerTick = 4;
    public static int quarryTaskPowerDivisor = 2;
    public static double quarryMaxFrameMoveSpeed = 0;
    public static double quarryMaxBlockMineRate = 0;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("general");
        QUARRY_FRAME_MIN_HEIGHT = b.comment("The minimum height that all quarry frames must be. A value of 1 will look"
            + " strange when it drills the uppermost layer.").defineInRange("quarryFrameMinHeight", 4, 1, 256);
        QUARRY_MAX_TASKS_PER_TICK = b.comment("The maximum number of tasks that the quarry will do per tick.",
            "(Where a task is either breaking a block, or moving the frame)")
            .defineInRange("quarryMaxTasksPerTick", 4, 1, 20);
        QUARRY_POWER_DIVISOR = b.comment("1 divided by this value is added to the power cost for each additional task"
            + " done per tick.", "A value of 0 disables this behaviour.").defineInRange("quarryPowerDivisor", 2, 0, 100);
        QUARRY_MAX_FRAME_SPEED = b.comment("The maximum number of blocks that a quarry is allowed to move, per second.",
            "A value of 0 means no limit.").defineInRange("quarryMaxFrameSpeed", 0.0, 0.0, 5120.0);
        QUARRY_MAX_BLOCK_MINE_RATE = b.comment("The maximum number of blocks that the quarry is allowed to mine each"
            + " second.", "A value of 0 means no limit, and a value of 0.5 will mine up to half a block per second.")
            .defineInRange("quarryMaxBlockMineRate", 0.0, 0.0, 1000.0);
        b.pop();
        SPEC = b.build();
    }

    private BCBuildersConfig() {}

    /** Listener for config loading and reloading (on the mod bus). */
    public static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            reloadConfig();
        }
    }

    public static void reloadConfig() {
        if (!SPEC.isLoaded()) {
            return;
        }
        quarryFrameMinHeight = QUARRY_FRAME_MIN_HEIGHT.get();
        quarryMaxTasksPerTick = QUARRY_MAX_TASKS_PER_TICK.get();
        quarryTaskPowerDivisor = QUARRY_POWER_DIVISOR.get();
        quarryMaxFrameMoveSpeed = QUARRY_MAX_FRAME_SPEED.get();
        quarryMaxBlockMineRate = QUARRY_MAX_BLOCK_MINE_RATE.get();
    }
}
