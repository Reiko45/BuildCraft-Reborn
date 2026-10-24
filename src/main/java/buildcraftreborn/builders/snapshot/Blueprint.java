/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.enums.EnumSnapshotType;
import buildcraftreborn.api.schematics.ISchematicBlock;
import buildcraftreborn.api.schematics.ISchematicEntity;


public class Blueprint extends Snapshot {
    public final List<ISchematicBlock> palette = new ArrayList<>();
    public int[] data;
    public final List<ISchematicEntity> entities = new ArrayList<>();

    @Override
    public Blueprint copy() {
        Blueprint blueprint = new Blueprint();
        blueprint.size = size;
        blueprint.facing = facing;
        blueprint.offset = offset;
        blueprint.palette.addAll(palette);
        blueprint.data = data.clone();
        blueprint.entities.addAll(entities);
        blueprint.computeKey();
        return blueprint;
    }

    public void replace(ISchematicBlock from, ISchematicBlock to) {
        Collections.replaceAll(palette, from, to);
        // TODO: reallocate IDs
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = super.serializeNBT();
        ListTag paletteList = new ListTag();
        palette.forEach(schematicBlock -> paletteList.add(SchematicBlockManager.writeToNBT(schematicBlock)));
        nbt.put("palette", paletteList);
        // 1.21: written as an int array (1.12 wrote a list of ints, which is still read)
        nbt.putIntArray("data", data.clone());
        ListTag entityList = new ListTag();
        entities.forEach(schematicEntity -> entityList.add(SchematicEntityManager.writeToNBT(schematicEntity)));
        nbt.put("entities", entityList);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        super.deserializeNBT(nbt);
        palette.clear();
        ListTag paletteList = nbt.getList("palette", Tag.TAG_COMPOUND);
        for (int i = 0; i < paletteList.size(); i++) {
            // TODO (1.12): allow reading blueprints partially - invalid elements should be replaced with air
            palette.add(SchematicBlockManager.readFromNBT(paletteList.getCompound(i)));
        }
        data = new int[Snapshot.getDataSize(size)];
        ListTag serializedDataList = nbt.contains("data", Tag.TAG_LIST) ? nbt.getList("data", Tag.TAG_INT) : null;
        int[] serializedDataIntArray = nbt.contains("data", Tag.TAG_INT_ARRAY) ? nbt.getIntArray("data") : null;
        if (serializedDataIntArray == null && serializedDataList == null) {
            throw new InvalidInputDataException("Can't read a blueprint with no data!");
        }
        int serializedDataLength = serializedDataList == null ? serializedDataIntArray.length : serializedDataList.size();
        if (serializedDataLength != getDataSize()) {
            throw new InvalidInputDataException("Serialized data has length of " + serializedDataLength
                + ", but we expected " + getDataSize() + " (" + size + ")");
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = serializedDataList == null ? serializedDataIntArray[i] : serializedDataList.getInt(i);
            if (data[i] < 0 || data[i] >= palette.size()) {
                throw new InvalidInputDataException("Palette index " + data[i] + " out of range");
            }
        }
        entities.clear();
        ListTag entityList = nbt.getList("entities", Tag.TAG_COMPOUND);
        for (int i = 0; i < entityList.size(); i++) {
            entities.add(SchematicEntityManager.readFromNBT(entityList.getCompound(i)));
        }
    }

    @Override
    public EnumSnapshotType getType() {
        return EnumSnapshotType.BLUEPRINT;
    }

    @SuppressWarnings("WeakerAccess")
    public class BuildingInfo extends Snapshot.BuildingInfo {
        public final List<ItemStack>[] toPlaceRequiredItems;
        public final List<FluidStack>[] toPlaceRequiredFluids;
        public final List<ISchematicBlock> rotatedPalette;
        public final Set<ISchematicEntity> entities;
        public final Map<ISchematicEntity, List<ItemStack>> entitiesRequiredItems;
        public final Map<ISchematicEntity, List<FluidStack>> entitiesRequiredFluids;

        public BuildingInfo(BlockPos basePos, Rotation rotation) {
            super(basePos, rotation);
            // noinspection unchecked
            toPlaceRequiredItems = (List<ItemStack>[]) new List<?>[getDataSize()];
            // noinspection unchecked
            toPlaceRequiredFluids = (List<FluidStack>[]) new List<?>[getDataSize()];
            rotatedPalette = ImmutableList.copyOf(
                palette.stream()
                    .map(schematicBlock -> schematicBlock.getRotated(rotation))
                    .collect(Collectors.toList())
            );
            for (int z = 0; z < getSnapshot().size.getZ(); z++) {
                for (int y = 0; y < getSnapshot().size.getY(); y++) {
                    for (int x = 0; x < getSnapshot().size.getX(); x++) {
                        ISchematicBlock schematicBlock = rotatedPalette.get(data[posToIndex(x, y, z)]);
                        if (!schematicBlock.isAir()) {
                            toPlaceRequiredItems[posToIndex(x, y, z)] = schematicBlock.computeRequiredItems();
                            toPlaceRequiredFluids[posToIndex(x, y, z)] = schematicBlock.computeRequiredFluids();
                        }
                    }
                }
            }
            ImmutableSet.Builder<ISchematicEntity> entitiesBuilder = ImmutableSet.builder();
            ImmutableMap.Builder<ISchematicEntity, List<ItemStack>> entitiesRequiredItemsBuilder =
                ImmutableMap.builder();
            ImmutableMap.Builder<ISchematicEntity, List<FluidStack>> entitiesRequiredFluidsBuilder =
                ImmutableMap.builder();
            for (ISchematicEntity schematicEntity : getSnapshot().entities) {
                ISchematicEntity rotatedSchematicEntity = schematicEntity.getRotated(rotation);
                entitiesBuilder.add(rotatedSchematicEntity);
                entitiesRequiredItemsBuilder.put(rotatedSchematicEntity, schematicEntity.computeRequiredItems());
                entitiesRequiredFluidsBuilder.put(rotatedSchematicEntity, schematicEntity.computeRequiredFluids());
            }
            entities = entitiesBuilder.build();
            entitiesRequiredItems = entitiesRequiredItemsBuilder.build();
            entitiesRequiredFluids = entitiesRequiredFluidsBuilder.build();
        }

        @Override
        public Blueprint getSnapshot() {
            return Blueprint.this;
        }
    }
}
