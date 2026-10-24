/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.plug;

import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.facades.IFacadePhasedState;
import buildcraftreborn.api.facades.IFacadeState;

/** One state of a facade, shown while the pipe's wires carry the given colour (or always, with no colour). */
public final class FacadePhasedState implements IFacadePhasedState {
    public static final Codec<FacadePhasedState> CODEC = RecordCodecBuilder.create(i -> i.group(
        BlockState.CODEC.fieldOf("state").forGetter(s -> s.stateInfo.state),
        DyeColor.CODEC.optionalFieldOf("active_colour").forGetter(s -> Optional.ofNullable(s.activeColour))
    ).apply(i, (state, colour) -> new FacadePhasedState(FacadeStateManager.getInfo(state), colour.orElse(null))));

    public final FacadeBlockStateInfo stateInfo;

    @Nullable
    public final DyeColor activeColour;

    public FacadePhasedState(FacadeBlockStateInfo stateInfo, @Nullable DyeColor activeColour) {
        this.stateInfo = stateInfo;
        this.activeColour = activeColour;
    }

    public static FacadePhasedState readFromBuffer(FriendlyByteBuf buf) {
        BlockState state = Block.stateById(buf.readVarInt());
        int colour = buf.readByte();
        return new FacadePhasedState(FacadeStateManager.getInfo(state),
            colour < 0 ? null : DyeColor.byId(colour));
    }

    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeVarInt(Block.getId(stateInfo.state));
        buf.writeByte(activeColour == null ? -1 : activeColour.getId());
    }

    public FacadePhasedState withColour(@Nullable DyeColor colour) {
        return new FacadePhasedState(stateInfo, colour);
    }

    public boolean isSideSolid(Direction side) {
        return stateInfo.isSideSolid[side.ordinal()];
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FacadePhasedState other && other.stateInfo.state == stateInfo.state
            && other.activeColour == activeColour;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stateInfo.state, activeColour);
    }

    @Override
    public String toString() {
        return (activeColour == null ? "" : activeColour + " ") + stateInfo.state;
    }

    // IFacadePhasedState

    @Override
    public IFacadeState getState() {
        return stateInfo;
    }

    @Override
    @Nullable
    public DyeColor getActiveColor() {
        return activeColour;
    }
}
