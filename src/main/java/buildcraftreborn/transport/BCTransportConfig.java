/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport;

import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.transport.pipe.EnumPipeColourType;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraftreborn.api.transport.pipe.PipeApi.RedstoneFluxTransferInfo;
import buildcraftreborn.api.transport.pipe.PipeDefinition;

/** Pipe settings from 1.12's {@code BCTransportConfig}, in {@code config/buildcraftreborn-transport.toml}.
 * <p>
 * 1.21 differences: the RF pipes (which carry FE) can't be turned off, and 1.12's "old MJ texture" option is gone.
 * Transfer rates are applied when the config loads or reloads. */
public final class BCTransportConfig {
    public enum PowerLossMode {
        LOSSLESS,
        PERCENTAGE,
        ABSOLUTE;

        public static final PowerLossMode DEFAULT = LOSSLESS;
    }

    private static final long MJ_REQ_MILLIBUCKET_MIN = 100;
    private static final long MJ_REQ_ITEM_MIN = 50_000;

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.IntValue MJ_PER_MILLIBUCKET;
    private static final ModConfigSpec.IntValue MJ_PER_ITEM;
    private static final ModConfigSpec.IntValue BASE_FLOW_RATE;
    private static final ModConfigSpec.IntValue BASE_POWER_RATE;
    private static final ModConfigSpec.IntValue BASE_RF_RATE;
    private static final ModConfigSpec.BooleanValue FLUID_PIPE_COLOUR_BORDER;
    private static final ModConfigSpec.EnumValue<PowerLossMode> LOSS_MODE;

    public static long mjPerMillibucket = 1_000;
    public static long mjPerItem = MjAPI.MJ;
    public static int baseFlowRate = 10;
    public static int basePowerRate = 4;
    public static int baseRfRate = 40;
    public static boolean fluidPipeColourBorder = true;
    public static PowerLossMode lossMode = PowerLossMode.DEFAULT;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("general");
        MJ_PER_MILLIBUCKET = b.comment("How much power (in micro MJ) the wooden fluid pipe uses per millibucket.")
            .defineInRange("pipes.mjPerMillibucket", 1_000, (int) MJ_REQ_MILLIBUCKET_MIN, Integer.MAX_VALUE);
        MJ_PER_ITEM = b.comment("How much power (in micro MJ) the wooden item pipe uses per item.")
            .defineInRange("pipes.mjPerItem", (int) MjAPI.MJ, (int) MJ_REQ_ITEM_MIN, Integer.MAX_VALUE);
        BASE_FLOW_RATE = b.comment("The fluid transfer rate of the slowest (cobblestone) fluid pipe, in mB per tick.")
            .defineInRange("pipes.baseFluidRate", 10, 1, 40);
        BASE_POWER_RATE = b.comment("The MJ transfer rate of the slowest (cobblestone) kinesis pipe, in MJ per tick.")
            .defineInRange("pipes.basePowerRate", 4, 1, 40);
        BASE_RF_RATE = b.comment("The FE transfer rate of the slowest (cobblestone) energy pipe, in FE per tick.")
            .defineInRange("pipes.baseRfRate", 40, 10, 4000);
        b.pop();
        b.push("display");
        FLUID_PIPE_COLOUR_BORDER = b.comment("Painted fluid pipes show their colour as a border (instead of a tint).")
            .define("pipes.fluidColourIsBorder", true);
        b.pop();
        b.push("experimental");
        LOSS_MODE = b.comment("How kinesis pipes lose power.").defineEnum("kinesisLossMode", PowerLossMode.DEFAULT);
        b.pop();
        SPEC = b.build();
    }

    private BCTransportConfig() {}

    /** Listener for config loading and reloading (on the mod bus). */
    public static void onConfigLoad(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC) {
            reloadConfig();
        }
    }

    /** Applies the current values (or the defaults if the config isn't loaded yet). */
    public static void reloadConfig() {
        boolean loaded = SPEC.isLoaded();
        mjPerMillibucket = Math.max(MJ_REQ_MILLIBUCKET_MIN, loaded ? MJ_PER_MILLIBUCKET.get() : 1_000);
        mjPerItem = Math.max(MJ_REQ_ITEM_MIN, loaded ? MJ_PER_ITEM.get() : MjAPI.MJ);
        baseFlowRate = loaded ? BASE_FLOW_RATE.get() : 10;
        basePowerRate = loaded ? BASE_POWER_RATE.get() : 4;
        baseRfRate = loaded ? BASE_RF_RATE.get() : 40;
        fluidPipeColourBorder = !loaded || FLUID_PIPE_COLOUR_BORDER.get();
        lossMode = loaded ? LOSS_MODE.get() : PowerLossMode.DEFAULT;

        PipeApi.flowFluids.fallbackColourType =
            fluidPipeColourBorder ? EnumPipeColourType.BORDER_INNER : EnumPipeColourType.TRANSLUCENT;

        fluidTransfer(BCTransportPipes.cobbleFluid, baseFlowRate, 10);
        fluidTransfer(BCTransportPipes.woodFluid, baseFlowRate, 10);

        fluidTransfer(BCTransportPipes.stoneFluid, baseFlowRate * 2, 10);
        fluidTransfer(BCTransportPipes.sandstoneFluid, baseFlowRate * 2, 10);

        fluidTransfer(BCTransportPipes.clayFluid, baseFlowRate * 4, 10);
        fluidTransfer(BCTransportPipes.ironFluid, baseFlowRate * 4, 10);
        fluidTransfer(BCTransportPipes.quartzFluid, baseFlowRate * 4, 10);

        fluidTransfer(BCTransportPipes.diamondFluid, baseFlowRate * 8, 10);
        fluidTransfer(BCTransportPipes.diaWoodFluid, baseFlowRate * 8, 10);
        fluidTransfer(BCTransportPipes.goldFluid, baseFlowRate * 8, 2);
        fluidTransfer(BCTransportPipes.voidFluid, baseFlowRate * 8, 10);

        powerTransfer(BCTransportPipes.cobblePower, basePowerRate, 16, false);
        powerTransfer(BCTransportPipes.stonePower, basePowerRate * 2, 32, false);
        powerTransfer(BCTransportPipes.woodPower, basePowerRate * 4, 128, true);
        powerTransfer(BCTransportPipes.sandstonePower, basePowerRate * 4, 32, false);
        powerTransfer(BCTransportPipes.quartzPower, basePowerRate * 8, 32, false);
        powerTransfer(BCTransportPipes.ironPower, basePowerRate * 8, 32, false);
        powerTransfer(BCTransportPipes.goldPower, basePowerRate * 32, 32, false);
        powerTransfer(BCTransportPipes.diamondPower, basePowerRate * 64, 32, false);
        powerTransfer(BCTransportPipes.diaWoodPower, basePowerRate * 64, 32, true);

        rfTransfer(BCTransportPipes.cobbleRf, baseRfRate, false);
        rfTransfer(BCTransportPipes.stoneRf, baseRfRate * 2, false);
        rfTransfer(BCTransportPipes.woodRf, baseRfRate * 4, true);
        rfTransfer(BCTransportPipes.sandstoneRf, baseRfRate * 4, false);
        rfTransfer(BCTransportPipes.quartzRf, baseRfRate * 8, false);
        rfTransfer(BCTransportPipes.ironRf, baseRfRate * 8, false);
        rfTransfer(BCTransportPipes.goldRf, baseRfRate * 32, false);
        rfTransfer(BCTransportPipes.diamondRf, baseRfRate * 64, false);
        rfTransfer(BCTransportPipes.diaWoodRf, baseRfRate * 64, true);
    }

    private static void fluidTransfer(PipeDefinition def, int rate, int delay) {
        PipeApi.fluidTransferData.put(def, new PipeApi.FluidTransferInfo(rate, delay));
    }

    private static void powerTransfer(PipeDefinition def, int transferMultiplier, int resistanceDivisor, boolean recv) {
        long transfer = MjAPI.MJ * transferMultiplier;
        long resistance = MjAPI.MJ / resistanceDivisor;
        PipeApi.powerTransferData.put(def, PowerTransferInfo.createFromResistance(transfer, resistance, recv));
    }

    private static void rfTransfer(PipeDefinition def, int maxTransfer, boolean recv) {
        PipeApi.rfTransferData.put(def, new RedstoneFluxTransferInfo(maxTransfer, recv));
    }
}
