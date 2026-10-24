/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.behaviour;

import net.minecraft.core.HolderLookup;
import java.util.Arrays;

import net.minecraft.nbt.CompoundTag;

import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.api.transport.pipe.PipeEventFluid;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeEventItem;

public class PipeBehaviourVoid extends PipeBehaviour {
    public PipeBehaviourVoid(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourVoid(IPipe pipe, CompoundTag nbt, HolderLookup.Provider registries) {
        super(pipe, nbt, registries);
    }

    @PipeEventHandler
    public static void reachCentre(PipeEventItem.ReachCenter reachCenter) {
        reachCenter.getStack().setCount(0);
    }

    @PipeEventHandler
    public static void moveFluidToCentre(PipeEventFluid.OnMoveToCentre move) {
        /*
        int removed = 0;
        for (int i = 0; i < move.fluidEnteringCentre.length; i++) {
            removed += move.fluidEnteringCentre[i];
        }
        */
        Arrays.fill(move.fluidEnteringCentre, 0);
        /*
        Level level = move.holder.getPipeWorld();
        BlockPos pos = move.holder.getPipePos();
        if (removed > 0 && (level.getGameTime() + pos.asLong()) % 23 == 0) {
            SoundType soundType = SoundType.SLIME;
            final SoundEvent soundEvent;
            Fluid f = move.fluid.getFluid();
            if (f == FluidRegistry.LAVA) {
                soundEvent = SoundEvents.ITEM_BUCKET_EMPTY_LAVA;
            } else if (f == FluidRegistry.WATER) {
                soundEvent = SoundEvents.ITEM_BUCKET_EMPTY;
            } else {
                soundEvent = SoundEvents.ITEM_BOTTLE_FILL_DRAGONBREATH;
            }
            float volume = (soundType.getVolume() + 1.0F) / 5.0F;
            float pitch = soundType.getPitch() * 0.1F;
            level.playSound(null, pos, soundEvent, SoundSource.BLOCKS, volume, pitch);
        }
        */
    }
}
