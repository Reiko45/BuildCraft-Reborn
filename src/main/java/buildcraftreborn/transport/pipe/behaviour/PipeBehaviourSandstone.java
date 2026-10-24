/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.behaviour;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;

import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeEventItem;

public class PipeBehaviourSandstone extends PipeBehaviour {
    private static final double SPEED_DELTA = PipeBehaviourStone.SPEED_DELTA;
    private static final double SPEED_TARGET = PipeBehaviourStone.SPEED_TARGET;

    public PipeBehaviourSandstone(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourSandstone(IPipe pipe, CompoundTag nbt, HolderLookup.Provider registries) {
        super(pipe, nbt, registries);
    }

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        return true;
    }

    @Override
    public boolean canConnect(Direction face, BlockEntity oTile) {
        return false;
    }

    @PipeEventHandler
    public static void modifySpeed(PipeEventItem.ModifySpeed event) {
        event.modifyTo(SPEED_TARGET, SPEED_DELTA);
    }
}
