/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import java.util.IdentityHashMap;
import java.util.Map;


import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.capabilities.BlockCapability;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.transport.IInjectable;
import buildcraftreborn.api.transport.IStripesRegistry;
import buildcraftreborn.api.transport.pluggable.IPluggableRegistry;
import buildcraftreborn.api.transport.pluggable.PipePluggable;

/** The central holding class for all pipe related registers and methods. */
public final class PipeApi {
    public static IPipeRegistry pipeRegistry;
    public static IPluggableRegistry pluggableRegistry;
    public static IStripesRegistry stripeRegistry;
    public static IPipeExtensionManager extensionManager;
    public static PipeFlowType flowStructure;
    public static PipeFlowType flowItems;
    public static PipeFlowType flowFluids;
    public static PipeFlowType flowPower;
    public static PipeFlowType flowRf;

    /** The default transfer information used if a pipe definition has not been registered. Note that this is replaced
     * by BuildCraft Transport to config-defined values. */
    public static FluidTransferInfo fluidInfoDefault = new FluidTransferInfo(20, 10);

    /** The default transfer information used if a pipe definition has not been registered. Note that this is replaced
     * by BuildCraft Transport to config-defined values. */
    public static PowerTransferInfo powerInfoDefault = PowerTransferInfo.createFromResistance(8 * MjAPI.MJ, MjAPI.MJ / 32, false);

    /** The default transfer information used if a pipe definition has not been registered. Note that this is replaced
     * by BuildCraft Transport to config-defined values. */
    public static RedstoneFluxTransferInfo rfInfoDefault = new RedstoneFluxTransferInfo(80, false);

    public static final Map<PipeDefinition, FluidTransferInfo> fluidTransferData = new IdentityHashMap<>();
    public static final Map<PipeDefinition, PowerTransferInfo> powerTransferData = new IdentityHashMap<>();
    public static final Map<PipeDefinition, RedstoneFluxTransferInfo> rfTransferData = new IdentityHashMap<>();

    // 1.21: NeoForge block capabilities. A pipe holder answers these for itself (side = null) and per side.
    public static final BlockCapability<IPipeHolder, @Nullable Direction> CAP_PIPE_HOLDER =
        BlockCapability.createSided(id("pipe_holder"), IPipeHolder.class);

    public static final BlockCapability<IPipe, @Nullable Direction> CAP_PIPE =
        BlockCapability.createSided(id("pipe"), IPipe.class);

    public static final BlockCapability<PipePluggable, @Nullable Direction> CAP_PLUG =
        BlockCapability.createSided(id("pipe_pluggable"), PipePluggable.class);

    public static final BlockCapability<IInjectable, @Nullable Direction> CAP_INJECTABLE =
        BlockCapability.createSided(id("injectable"), IInjectable.class);

    public static FluidTransferInfo getFluidTransferInfo(PipeDefinition def) {
        FluidTransferInfo info = fluidTransferData.get(def);
        if (info == null) {
            return fluidInfoDefault;
        } else {
            return info;
        }
    }

    public static PowerTransferInfo getPowerTransferInfo(PipeDefinition def) {
        PowerTransferInfo info = powerTransferData.get(def);
        if (info == null) {
            return powerInfoDefault;
        } else {
            return info;
        }
    }

    public static RedstoneFluxTransferInfo getRfTransferInfo(PipeDefinition def) {
        RedstoneFluxTransferInfo info = rfTransferData.get(def);
        if (info == null) {
            return rfInfoDefault;
        } else {
            return info;
        }
    }

    public static class FluidTransferInfo {
        /** Controls the maximum amount of fluid that can be transferred around and out of a pipe per tick. Note that
         * this does not affect the flow rate coming into the pipe. */
        public final int transferPerTick;

        /** Controls how long the pipe should delay incoming fluids by. Minimum value is 1, because of the way that
         * fluids are handled internally. This value is multiplied by the fluids viscosity, and divided by 100 to give
         * the actual delay. */
        public final double transferDelayMultiplier;

        public FluidTransferInfo(int transferPerTick, int transferDelay) {
            this.transferPerTick = transferPerTick;
            if (transferDelay <= 0) {
                transferDelay = 1;
            }
            this.transferDelayMultiplier = transferDelay;
        }
    }

    public static class PowerTransferInfo {
        public final long transferPerTick;
        public final long lossPerTick;
        /** The percentage resistance per tick. Should be a value between 0 and {@link MjAPI#MJ} */
        public final long resistancePerTick;
        public final boolean isReceiver;

        /** Sets resistancePerTick to be equal to lossPerTick when full power is being transferred, scaling down to 0.
         * 
         * @param transferPerTick
         * @param lossPerTick
         * @param isReceiver */
        public static PowerTransferInfo createFromLoss(long transferPerTick, long lossPerTick, boolean isReceiver) {
            return new PowerTransferInfo(transferPerTick, lossPerTick, lossPerTick * MjAPI.MJ / transferPerTick, isReceiver);
        }

        /** Sets lossPerTick to be equal to resistancePerTick when full power is being transferred.
         * 
         * @param transferPerTick
         * @param resistancePerTick
         * @param isReceiver */
        public static PowerTransferInfo createFromResistance(long transferPerTick, long resistancePerTick, boolean isReceiver) {
            return new PowerTransferInfo(transferPerTick, resistancePerTick, resistancePerTick * transferPerTick / MjAPI.MJ, isReceiver);
        }

        public PowerTransferInfo(long transferPerTick, long lossPerTick, long resistancePerTick, boolean isReceiver) {
            if (transferPerTick < 10) {
                transferPerTick = 10;
            }
            this.transferPerTick = transferPerTick;
            this.lossPerTick = lossPerTick;
            this.resistancePerTick = resistancePerTick;
            this.isReceiver = isReceiver;
        }
    }

    public static class RedstoneFluxTransferInfo {
        public final int transferPerTick;
        public final boolean isReceiver;

        public RedstoneFluxTransferInfo(int transferPerTick, boolean isReceiver) {
            this.transferPerTick = transferPerTick;
            this.isReceiver = isReceiver;
        }
    }

    // Internals

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("buildcraftreborn", path);
    }
}
