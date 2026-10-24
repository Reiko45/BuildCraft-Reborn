/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.statement;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import buildcraftreborn.api.statements.IGuiSlot;

import buildcraftreborn.lib.net.PacketBufferBC;

public abstract class StatementType<S extends IGuiSlot> {

    public final Class<S> clazz;
    public final S defaultStatement;

    public StatementType(Class<S> clazz, S defaultStatement) {
        this.clazz = clazz;
        this.defaultStatement = defaultStatement;
    }

    /** Reads a {@link StatementWrapper} from the given {@link CompoundTag}. The tag compound will be equal to the
     * one returned by {@link #writeToNbt(IGuiSlot, HolderLookup.Provider)} */
    public abstract S readFromNbt(CompoundTag nbt, HolderLookup.Provider registries);

    public abstract CompoundTag writeToNbt(S slot, HolderLookup.Provider registries);

    /** Reads a {@link StatementWrapper} from the given {@link PacketBufferBC}. The buffer will return the data written
     * to a different buffer by {@link #writeToBuffer(PacketBufferBC, IGuiSlot)}. */
    public abstract S readFromBuffer(PacketBufferBC buffer) throws IOException;

    public abstract void writeToBuffer(PacketBufferBC buffer, S slot);

    @Nullable
    public abstract S convertToType(Object value);
}
