/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.plug;

import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.DyeColor;

import buildcraftreborn.api.facades.FacadeType;
import buildcraftreborn.api.facades.IFacade;
import buildcraftreborn.api.facades.IFacadePhasedState;

/** A whole facade: one state (a basic facade) or up to 17 (a phased facade: one per wire colour, and one for no
 * signal), solid or hollow. Immutable; it's the facade item's {@code facade} data component. */
public final class FacadeInstance implements IFacade {
    public static final Codec<FacadeInstance> CODEC = RecordCodecBuilder.<FacadeInstance> create(i -> i.group(
        FacadePhasedState.CODEC.listOf().fieldOf("states").forGetter(f -> List.of(f.phasedStates)),
        Codec.BOOL.optionalFieldOf("hollow", false).forGetter(f -> f.isHollow)
    ).apply(i, (states, hollow) -> new FacadeInstance(states.toArray(new FacadePhasedState[0]), hollow)));

    public static final StreamCodec<FriendlyByteBuf, FacadeInstance> STREAM_CODEC = StreamCodec.of(
        (buf, facade) -> facade.writeToBuffer(buf), FacadeInstance::readFromBuffer);

    public final FacadePhasedState[] phasedStates;
    public final FacadeType type;
    public final boolean isHollow;

    public FacadeInstance(FacadePhasedState[] phasedStates, boolean isHollow) {
        if (phasedStates == null) throw new NullPointerException("phasedStates");
        if (phasedStates.length == 0) {
            // (a broken item: show nothing rather than fail to load)
            phasedStates = new FacadePhasedState[] { new FacadePhasedState(FacadeStateManager.defaultState(),
                null) };
        }
        // Maximum of 17 states - 16 for each colour, 1 for no colour
        if (phasedStates.length > 17) phasedStates = Arrays.copyOf(phasedStates, 17);
        this.phasedStates = phasedStates;
        type = phasedStates.length == 1 ? FacadeType.Basic : FacadeType.Phased;
        this.isHollow = isHollow;
    }

    public static FacadeInstance createSingle(FacadeBlockStateInfo info, boolean isHollow) {
        return new FacadeInstance(new FacadePhasedState[] { new FacadePhasedState(info, null) }, isHollow);
    }

    public static FacadeInstance readFromBuffer(FriendlyByteBuf buf) {
        boolean isHollow = buf.readBoolean();
        int count = Math.min(17, buf.readVarInt());
        FacadePhasedState[] states = new FacadePhasedState[count];
        for (int i = 0; i < count; i++) {
            states[i] = FacadePhasedState.readFromBuffer(buf);
        }
        return new FacadeInstance(states, isHollow);
    }

    public void writeToBuffer(FriendlyByteBuf buf) {
        buf.writeBoolean(isHollow);
        buf.writeVarInt(phasedStates.length);
        for (FacadePhasedState phasedState : phasedStates) {
            phasedState.writeToBuffer(buf);
        }
    }

    public boolean canAddColour(@Nullable DyeColor colour) {
        for (FacadePhasedState state : phasedStates) {
            if (state.activeColour == colour) {
                return false;
            }
        }
        return true;
    }

    @Nullable
    public FacadeInstance withState(FacadePhasedState state) {
        if (canAddColour(state.activeColour)) {
            FacadePhasedState[] newStates = Arrays.copyOf(phasedStates, phasedStates.length + 1);
            newStates[newStates.length - 1] = state;
            return new FacadeInstance(newStates, isHollow);
        }
        return null;
    }

    /** @return The state an item shows: phased facades cycle through theirs every half second. */
    public FacadePhasedState getCurrentStateForStack() {
        int count = phasedStates.length;
        if (count == 1) {
            return phasedStates[0];
        }
        int now = (int) (System.currentTimeMillis() % 100_000);
        return phasedStates[(now / 500) % count];
    }

    public FacadeInstance withSwappedIsHollow() {
        return new FacadeInstance(phasedStates, !isHollow);
    }

    public boolean areAllStatesSolid(Direction side) {
        for (FacadePhasedState state : phasedStates) {
            if (!state.isSideSolid(side)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FacadeInstance other && other.isHollow == isHollow
            && Arrays.equals(other.phasedStates, phasedStates);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(phasedStates) * 31 + (isHollow ? 1 : 0);
    }

    @Override
    public String toString() {
        return "Facade" + Arrays.toString(phasedStates) + (isHollow ? " (hollow)" : "");
    }

    // IFacade

    @Override
    public FacadeType getType() {
        return type;
    }

    @Override
    public boolean isHollow() {
        return isHollow;
    }

    @Override
    public IFacadePhasedState[] getPhasedStates() {
        return phasedStates;
    }
}
