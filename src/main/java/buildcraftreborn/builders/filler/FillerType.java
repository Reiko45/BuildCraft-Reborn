/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.filler;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

import buildcraftreborn.api.filler.FillerManager;
import buildcraftreborn.api.filler.IFillerPattern;

import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.statement.StatementType;

import buildcraftreborn.builders.BCBuildersStatements;

public class FillerType extends StatementType<IFillerPattern> {
    public static final FillerType INSTANCE = new FillerType();

    private FillerType() {
        super(IFillerPattern.class, BCBuildersStatements.PATTERN_NONE);
    }

    @Override
    public IFillerPattern convertToType(Object value) {
        return value instanceof IFillerPattern ? (IFillerPattern) value : null;
    }

    @Override
    public IFillerPattern readFromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        String kind = nbt.getString("kind");
        IFillerPattern pattern = FillerManager.registry.getPattern(kind);
        if (pattern == null) {
            return defaultStatement;
        }
        return pattern;
    }

    @Override
    public CompoundTag writeToNbt(IFillerPattern slot, HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        nbt.putString("kind", slot.getUniqueTag());
        return nbt;
    }

    @Override
    public IFillerPattern readFromBuffer(PacketBufferBC buffer) {
        String kind = buffer.readUtf();
        IFillerPattern pattern = FillerManager.registry.getPattern(kind);
        if (pattern == null) {
            return defaultStatement;
        }
        return pattern;
    }

    @Override
    public void writeToBuffer(PacketBufferBC buffer, IFillerPattern slot) {
        buffer.writeUtf(slot.getUniqueTag());
    }
}
