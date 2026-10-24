/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.schematics.ISchematicBlock;
import buildcraftreborn.api.schematics.SchematicBlockContext;

public final class SchematicBlockAir implements ISchematicBlock {
    @SuppressWarnings("unused")
    public static boolean predicate(SchematicBlockContext context) {
        return true;
    }

    @Override
    public void init(SchematicBlockContext context) {
    }

    @Override
    public boolean isAir() {
        return true;
    }

    @Override
    public SchematicBlockAir getRotated(Rotation rotation) {
        return SchematicBlockManager.createCleanCopy(this);
    }

    @Override
    public boolean canBuild(Level level, BlockPos blockPos) {
        return false;
    }

    @Override
    public boolean build(Level level, BlockPos blockPos) {
        return true;
    }

    @Override
    public boolean buildWithoutChecks(Level level, BlockPos blockPos) {
        return true;
    }

    @Override
    public boolean isBuilt(Level level, BlockPos blockPos) {
        return true;
    }

    @Override
    public CompoundTag serializeNBT() {
        return new CompoundTag();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
    }

    @Override
    public boolean equals(Object o) {
        return this == o || !(o == null || getClass() != o.getClass());
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
