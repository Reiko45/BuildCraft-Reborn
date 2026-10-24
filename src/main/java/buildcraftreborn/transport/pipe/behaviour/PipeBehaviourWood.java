/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.behaviour;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.minecraft.core.HolderLookup;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.mj.IMjConnector;
import buildcraftreborn.api.mj.IMjRedstoneReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.api.transport.pipe.IFlowFluid;
import buildcraftreborn.api.transport.pipe.IFlowItems;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipe.ConnectedType;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.api.transport.pipe.PipeEventFluid;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeFaceTex;

import buildcraftreborn.lib.inventory.filter.StackFilter;

import buildcraftreborn.transport.BCTransportConfig;

public class PipeBehaviourWood extends PipeBehaviourDirectional implements IMjRedstoneReceiver, IDebuggable {

    private static final PipeFaceTex TEX_CLEAR = PipeFaceTex.get(0);
    private static final PipeFaceTex TEX_FILLED = PipeFaceTex.get(1);

    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);

    public PipeBehaviourWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWood(IPipe pipe, CompoundTag nbt, HolderLookup.Provider registries) {
        super(pipe, nbt, registries);
    }

    @Override
    public PipeFaceTex getTextureData(Direction face) {
        return (face != null && face == getCurrentDir()) ? TEX_FILLED : TEX_CLEAR;
    }

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWood);
    }

    @Override
    protected boolean canFaceDirection(Direction dir) {
        return dir != null && pipe.getConnectedType(dir) == ConnectedType.TILE;
    }

    @PipeEventHandler
    public void fluidSideCheck(PipeEventFluid.SideCheck sideCheck) {
        if (currentDir.face != null) {
            sideCheck.disallow(currentDir.face);
        }
    }

    protected long extract(long power, boolean simulate) {
        if (power > 0) {
            if (pipe.getFlow() instanceof IFlowItems) {
                IFlowItems flow = (IFlowItems) pipe.getFlow();
                int maxItems = (int) (power / BCTransportConfig.mjPerItem);
                if (maxItems > 0) {
                    int extracted = extractItems(flow, getCurrentDir(), maxItems, simulate);
                    if (extracted > 0) {
                        return power - extracted * BCTransportConfig.mjPerItem;
                    }
                }
            } else if (pipe.getFlow() instanceof IFlowFluid) {
                IFlowFluid flow = (IFlowFluid) pipe.getFlow();
                int maxMillibuckets = (int) (power / BCTransportConfig.mjPerMillibucket);
                if (maxMillibuckets > 0) {
                    FluidStack extracted = extractFluid(flow, getCurrentDir(), maxMillibuckets, simulate);
                    if (extracted != null && !extracted.isEmpty()) {
                        return power - extracted.getAmount() * BCTransportConfig.mjPerMillibucket;
                    }
                }
            }
        }
        return power;
    }

    protected int extractItems(IFlowItems flow, Direction dir, int count, boolean simulate) {
        return flow.tryExtractItems(count, dir, null, StackFilter.ALL, simulate);
    }

    @Nullable
    protected FluidStack extractFluid(IFlowFluid flow, Direction dir, int millibuckets, boolean simulate) {
        return flow.tryExtractFluid(millibuckets, dir, null, simulate);
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        final long power = 512 * MjAPI.MJ;
        return power - extract(power, true);
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return extract(microJoules, simulate);
    }

    @Override
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction facing) {
        return mjCaps.getCapability(capability);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("Facing = " + currentDir);
    }
}
