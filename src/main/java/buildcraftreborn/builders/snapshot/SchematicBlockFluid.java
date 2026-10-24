/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.schematics.ISchematicBlock;
import buildcraftreborn.api.schematics.SchematicBlockContext;
import buildcraftreborn.lib.misc.BlockUtil;

/** A fluid source (or flowing fluid, which is skipped when building, as it flows back by itself). */
public class SchematicBlockFluid implements ISchematicBlock {
    private BlockState blockState;
    private boolean isFlowing;

    public static boolean predicate(SchematicBlockContext context) {
        return BlockUtil.getFluidWithFlowing(context.level, context.pos) != null
            && (BlockUtil.getFluid(context.level, context.pos) == null
                || BlockUtil.getFluidWithoutFlowing(context.level.getBlockState(context.pos)) != null);
    }

    @Override
    public void init(SchematicBlockContext context) {
        blockState = context.blockState;
        isFlowing = BlockUtil.getFluid(context.level, context.pos) == null;
    }

    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        Set<BlockPos> offsets = new HashSet<>();
        for (Direction face : Direction.Plane.HORIZONTAL) {
            offsets.add(BlockPos.ZERO.relative(face));
        }
        offsets.add(BlockPos.ZERO.below());
        return offsets;
    }

    @Override
    public List<FluidStack> computeRequiredFluids() {
        Fluid fluid = BlockUtil.getFluidWithoutFlowing(blockState);
        return fluid == null ? Collections.emptyList()
            : Collections.singletonList(new FluidStack(fluid, FluidType.BUCKET_VOLUME));
    }

    @Override
    public SchematicBlockFluid getRotated(Rotation rotation) {
        SchematicBlockFluid schematicBlock = SchematicBlockManager.createCleanCopy(this);
        schematicBlock.blockState = blockState;
        schematicBlock.isFlowing = isFlowing;
        return schematicBlock;
    }

    @Override
    public boolean canBuild(Level level, BlockPos blockPos) {
        return level.isEmptyBlock(blockPos)
            || BlockUtil.getFluidWithFlowing(level, blockPos) == BlockUtil.getFluidWithFlowing(blockState)
                && BlockUtil.getFluid(level, blockPos) == null;
    }

    @Override
    public boolean build(Level level, BlockPos blockPos) {
        if (isFlowing) {
            return true;
        }
        if (level.setBlock(blockPos, blockState, Block.UPDATE_ALL_IMMEDIATE)) {
            for (Direction face : Direction.values()) {
                level.updateNeighborsAt(blockPos.relative(face), blockState.getBlock());
            }
            level.updateNeighborsAt(blockPos, blockState.getBlock());
            return true;
        }
        return false;
    }

    @Override
    public boolean buildWithoutChecks(Level level, BlockPos blockPos) {
        return level.setBlock(blockPos, blockState, 0);
    }

    @Override
    public boolean isBuilt(Level level, BlockPos blockPos) {
        return isFlowing || BlockUtil.blockStatesEqual(blockState, level.getBlockState(blockPos));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("blockState", NbtUtils.writeBlockState(blockState));
        nbt.putBoolean("isFlowing", isFlowing);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("blockState"));
        isFlowing = nbt.getBoolean("isFlowing");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SchematicBlockFluid that = (SchematicBlockFluid) o;
        return isFlowing == that.isFlowing && blockState.equals(that.blockState);
    }

    @Override
    public int hashCode() {
        int result = blockState.hashCode();
        result = 31 * result + (isFlowing ? 1 : 0);
        return result;
    }
}
