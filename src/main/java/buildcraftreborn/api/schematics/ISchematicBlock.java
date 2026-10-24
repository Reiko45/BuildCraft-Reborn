/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.schematics;

import java.util.Collections;
import java.util.List;
import java.util.Set;


import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.core.InvalidInputDataException;

public interface ISchematicBlock {
    void init(SchematicBlockContext context);

    default boolean isAir() {
        return false;
    }

    
    default Set<BlockPos> getRequiredBlockOffsets() {
        return Collections.emptySet();
    }

    
    default List<ItemStack> computeRequiredItems() {
        return Collections.emptyList();
    }

    
    default List<FluidStack> computeRequiredFluids() {
        return Collections.emptyList();
    }

    ISchematicBlock getRotated(Rotation rotation);

    boolean canBuild(Level level, BlockPos blockPos);

    default boolean isReadyToBuild(Level level, BlockPos blockPos) {
        return true;
    }

    boolean build(Level level, BlockPos blockPos);

    boolean buildWithoutChecks(Level level, BlockPos blockPos);

    boolean isBuilt(Level level, BlockPos blockPos);

    CompoundTag serializeNBT();

    /** @throws InvalidInputDataException If the input data wasn't correct or didn't make sense. */
    void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException;
}
