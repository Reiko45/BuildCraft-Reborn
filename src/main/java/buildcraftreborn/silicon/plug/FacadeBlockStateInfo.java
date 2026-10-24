/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.plug;

import java.util.Objects;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import buildcraftreborn.api.facades.IFacadeState;

/** A block state that can be a facade, with the item that makes it ({@link ItemStack#EMPTY} if no item does, in which
 * case the facade can't be made in survival) and the properties that differ between states made by the same item
 * (a log's axis, say). */
public final class FacadeBlockStateInfo implements IFacadeState {
    public final BlockState state;
    public final ItemStack requiredStack;
    public final ImmutableSet<Property<?>> varyingProperties;
    public final boolean isTransparent;
    public final boolean isVisible;
    public final boolean[] isSideSolid = new boolean[6];

    public FacadeBlockStateInfo(BlockState state, ItemStack requiredStack, ImmutableSet<Property<?>> varyingProperties) {
        this.state = Objects.requireNonNull(state, "state must not be null!");
        this.requiredStack = requiredStack;
        this.varyingProperties = varyingProperties;
        this.isTransparent = !state.canOcclude();
        this.isVisible = !requiredStack.isEmpty();
        for (Direction side : Direction.values()) {
            boolean solid;
            try {
                solid = state.isFaceSturdy(EmptyBlockGetter.INSTANCE, BlockPos.ZERO, side);
            } catch (RuntimeException e) {
                // (some blocks need a real level)
                solid = false;
            }
            isSideSolid[side.ordinal()] = solid;
        }
    }

    public FacadePhasedState createPhased(DyeColor activeColour) {
        return new FacadePhasedState(this, activeColour);
    }

    @Override
    public String toString() {
        return "StateInfo [block = " + state.getBlock() + ", state = " + state + "]";
    }

    // IFacadeState

    @Override
    public BlockState getBlockState() {
        return state;
    }

    @Override
    public boolean isTransparent() {
        return isTransparent;
    }

    @Override
    public ItemStack getRequiredStack() {
        return requiredStack;
    }
}
