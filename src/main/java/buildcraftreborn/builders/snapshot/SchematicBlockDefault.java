/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.schematics.ISchematicBlock;
import buildcraftreborn.api.schematics.SchematicBlockContext;
import buildcraftreborn.lib.misc.BlockUtil;
import buildcraftreborn.lib.misc.NBTUtilBC;

/** Copies a block state (and its block entity's NBT), adjusted by the schematic rules for its block.
 * <p>
 * 1.21: block entities are saved with {@code saveWithFullMetadata} and loaded into the block entity the placed block
 * creates. They can no longer be rotated (1.12's {@code TileEntity.rotate}); blocks that face somewhere keep that in
 * their block state, which is rotated. */
public class SchematicBlockDefault implements ISchematicBlock {
    protected final Set<BlockPos> requiredBlockOffsets = new HashSet<>();
    protected BlockState blockState;
    protected final List<Property<?>> ignoredProperties = new ArrayList<>();
    @Nullable
    protected CompoundTag tileNbt;
    protected Rotation tileRotation = Rotation.NONE;
    protected Block placeBlock;
    protected final Set<BlockPos> updateBlockOffsets = new HashSet<>();
    protected final Set<Block> canBeReplacedWithBlocks = new HashSet<>();

    @Nullable
    protected static CompoundTag getTileNbt(SchematicBlockContext context) {
        if (!context.blockState.hasBlockEntity()) {
            return null;
        }
        BlockEntity tile = context.level.getBlockEntity(context.pos);
        return tile == null ? null : tile.saveWithFullMetadata(context.level.registryAccess());
    }

    public static boolean predicate(SchematicBlockContext context) {
        if (context.blockState.isAir()) {
            return false;
        }
        ResourceLocation registryName = BuiltInRegistries.BLOCK.getKey(context.block);
        return RulesLoader.READ_DOMAINS.contains(registryName.getNamespace())
            && RulesLoader.getRules(context.blockState, getTileNbt(context)).stream().noneMatch(rule -> rule.ignore);
    }

    protected void setRequiredBlockOffsets(SchematicBlockContext context, Set<JsonRule> rules) {
        requiredBlockOffsets.clear();
        rules.stream()
            .map(rule -> rule.requiredBlockOffsets)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .forEach(requiredBlockOffsets::add);
        if (context.block instanceof FallingBlock) {
            requiredBlockOffsets.add(new BlockPos(0, -1, 0));
        }
    }

    protected void setBlockState(SchematicBlockContext context, Set<JsonRule> rules) {
        blockState = context.blockState;
    }

    protected void setIgnoredProperties(SchematicBlockContext context, Set<JsonRule> rules) {
        ignoredProperties.clear();
        rules.stream()
            .map(rule -> rule.ignoredProperties)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .flatMap(propertyName -> context.blockState.getProperties().stream()
                .filter(property -> property.getName().equals(propertyName)))
            .forEach(ignoredProperties::add);
    }

    protected void setTileNbt(SchematicBlockContext context, Set<JsonRule> rules) {
        tileNbt = getTileNbt(context);
    }

    protected void setPlaceBlock(SchematicBlockContext context, Set<JsonRule> rules) {
        placeBlock = rules.stream()
            .map(rule -> rule.placeBlock)
            .filter(Objects::nonNull)
            .findFirst()
            .map(SchematicBlockDefault::blockByName)
            .orElse(context.block);
    }

    protected void setUpdateBlockOffsets(SchematicBlockContext context, Set<JsonRule> rules) {
        updateBlockOffsets.clear();
        if (rules.stream().map(rule -> rule.updateBlockOffsets).anyMatch(Objects::nonNull)) {
            rules.stream()
                .map(rule -> rule.updateBlockOffsets)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(updateBlockOffsets::add);
        } else {
            for (Direction face : Direction.values()) {
                updateBlockOffsets.add(BlockPos.ZERO.relative(face));
            }
            updateBlockOffsets.add(BlockPos.ZERO);
        }
    }

    protected void setCanBeReplacedWithBlocks(SchematicBlockContext context, Set<JsonRule> rules) {
        canBeReplacedWithBlocks.clear();
        rules.stream()
            .map(rule -> rule.canBeReplacedWithBlocks)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .map(SchematicBlockDefault::blockByName)
            .forEach(canBeReplacedWithBlocks::add);
        canBeReplacedWithBlocks.add(context.block);
        canBeReplacedWithBlocks.add(placeBlock);
    }

    private static Block blockByName(String name) {
        ResourceLocation id = ResourceLocation.tryParse(name);
        return id == null ? Blocks.AIR : BuiltInRegistries.BLOCK.get(id);
    }

    @Override
    public void init(SchematicBlockContext context) {
        Set<JsonRule> rules = RulesLoader.getRules(context.blockState, getTileNbt(context));
        setRequiredBlockOffsets(context, rules);
        setBlockState(context, rules);
        setIgnoredProperties(context, rules);
        setTileNbt(context, rules);
        setPlaceBlock(context, rules);
        setUpdateBlockOffsets(context, rules);
        setCanBeReplacedWithBlocks(context, rules);
    }

    /** The block this places, for previews. */
    public BlockState getBlockState() {
        return blockState;
    }

    @Override
    public Set<BlockPos> getRequiredBlockOffsets() {
        return requiredBlockOffsets;
    }

    @Override
    public List<ItemStack> computeRequiredItems() {
        Set<JsonRule> rules = RulesLoader.getRules(blockState, tileNbt);
        List<List<RequiredExtractor>> collect = rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        Stream<RequiredExtractor> extractors = collect.isEmpty()
            ? Stream.of(new RequiredExtractorItemFromBlock())
            : collect.stream().flatMap(Collection::stream);
        return extractors
            .flatMap(requiredExtractor -> requiredExtractor.extractItemsFromBlock(blockState, tileNbt).stream())
            .filter(stack -> !stack.isEmpty())
            .collect(Collectors.toList());
    }

    @Override
    public List<FluidStack> computeRequiredFluids() {
        Set<JsonRule> rules = RulesLoader.getRules(blockState, tileNbt);
        return rules.stream()
            .map(rule -> rule.requiredExtractors)
            .filter(Objects::nonNull)
            .flatMap(Collection::stream)
            .flatMap(requiredExtractor -> requiredExtractor.extractFluidsFromBlock(blockState, tileNbt).stream())
            .filter(fluid -> fluid != null && !fluid.isEmpty())
            .collect(Collectors.toList());
    }

    @Override
    public SchematicBlockDefault getRotated(Rotation rotation) {
        SchematicBlockDefault schematicBlock = SchematicBlockManager.createCleanCopy(this);
        requiredBlockOffsets.stream()
            .map(blockPos -> blockPos.rotate(rotation))
            .forEach(schematicBlock.requiredBlockOffsets::add);
        schematicBlock.blockState = blockState.rotate(rotation);
        schematicBlock.ignoredProperties.addAll(ignoredProperties);
        schematicBlock.tileNbt = tileNbt;
        schematicBlock.tileRotation = tileRotation.getRotated(rotation);
        schematicBlock.placeBlock = placeBlock;
        updateBlockOffsets.stream()
            .map(blockPos -> blockPos.rotate(rotation))
            .forEach(schematicBlock.updateBlockOffsets::add);
        schematicBlock.canBeReplacedWithBlocks.addAll(canBeReplacedWithBlocks);
        return schematicBlock;
    }

    @Override
    public boolean canBuild(Level level, BlockPos blockPos) {
        return level.isEmptyBlock(blockPos);
    }

    @Override
    public boolean build(Level level, BlockPos blockPos) {
        if (placeBlock == Blocks.AIR) {
            return true;
        }
        BlockState newBlockState = blockState;
        if (placeBlock != blockState.getBlock()) {
            newBlockState = placeBlock.defaultBlockState();
            for (Property<?> property : blockState.getProperties()) {
                if (newBlockState.hasProperty(property)) {
                    newBlockState = BlockUtil.copyProperty(property, newBlockState, blockState);
                }
            }
        }
        for (Property<?> property : ignoredProperties) {
            newBlockState = BlockUtil.copyProperty(property, newBlockState, placeBlock.defaultBlockState());
        }
        if (level.setBlock(blockPos, newBlockState, Block.UPDATE_ALL_IMMEDIATE)) {
            for (BlockPos offset : updateBlockOffsets) {
                level.updateNeighborsAt(blockPos.offset(offset), placeBlock);
            }
            if (tileNbt != null && newBlockState.hasBlockEntity()) {
                CompoundTag replaceNbt = RulesLoader.getRules(blockState, tileNbt).stream()
                    .map(rule -> rule.replaceNbt)
                    .filter(Objects::nonNull)
                    .map(Tag.class::cast)
                    .reduce(NBTUtilBC::merge)
                    .map(CompoundTag.class::cast)
                    .orElse(null);
                loadTile(level, blockPos, replaceNbt);
            }
            return true;
        }
        return false;
    }

    /** Loads the saved block entity NBT into the block entity at the position. */
    protected void loadTile(Level level, BlockPos blockPos, @Nullable CompoundTag replaceNbt) {
        BlockEntity tileEntity = level.getBlockEntity(blockPos);
        if (tileEntity == null || tileNbt == null) {
            return;
        }
        CompoundTag newTileNbt = tileNbt.copy();
        newTileNbt.putInt("x", blockPos.getX());
        newTileNbt.putInt("y", blockPos.getY());
        newTileNbt.putInt("z", blockPos.getZ());
        if (replaceNbt != null) {
            newTileNbt = (CompoundTag) NBTUtilBC.merge(newTileNbt, replaceNbt);
        }
        tileEntity.loadWithComponents(newTileNbt, level.registryAccess());
        tileEntity.setChanged();
        BlockState state = level.getBlockState(blockPos);
        level.sendBlockUpdated(blockPos, state, state, Block.UPDATE_ALL);
    }

    @Override
    public boolean buildWithoutChecks(Level level, BlockPos blockPos) {
        if (level.setBlock(blockPos, blockState, 0)) {
            if (tileNbt != null && blockState.hasBlockEntity()) {
                loadTile(level, blockPos, null);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBuilt(Level level, BlockPos blockPos) {
        BlockState inWorld = level.getBlockState(blockPos);
        return blockState != null && canBeReplacedWithBlocks.contains(inWorld.getBlock())
            && BlockUtil.blockStatesWithoutBlockEqual(blockState, inWorld, ignoredProperties);
    }

    private static ListTag writePosList(Set<BlockPos> positions) {
        ListTag list = new ListTag();
        positions.forEach(pos -> list.add(NBTUtilBC.writeBlockPos(pos)));
        return list;
    }

    private static void readPosList(Tag tag, Set<BlockPos> to) {
        if (tag instanceof ListTag list) {
            for (Tag element : list) {
                BlockPos pos = NBTUtilBC.readBlockPos(element);
                if (pos != null) {
                    to.add(pos);
                }
            }
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.put("requiredBlockOffsets", writePosList(requiredBlockOffsets));
        nbt.put("blockState", NbtUtils.writeBlockState(blockState));
        ListTag ignored = new ListTag();
        ignoredProperties.forEach(property -> ignored.add(StringTag.valueOf(property.getName())));
        nbt.put("ignoredProperties", ignored);
        if (tileNbt != null) {
            nbt.put("tileNbt", tileNbt);
        }
        nbt.put("tileRotation", NBTUtilBC.writeEnum(tileRotation));
        nbt.putString("placeBlock", BuiltInRegistries.BLOCK.getKey(placeBlock).toString());
        nbt.put("updateBlockOffsets", writePosList(updateBlockOffsets));
        ListTag replaceable = new ListTag();
        canBeReplacedWithBlocks.forEach(block ->
            replaceable.add(StringTag.valueOf(BuiltInRegistries.BLOCK.getKey(block).toString())));
        nbt.put("canBeReplacedWithBlocks", replaceable);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) throws InvalidInputDataException {
        readPosList(nbt.get("requiredBlockOffsets"), requiredBlockOffsets);
        blockState = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), nbt.getCompound("blockState"));
        ListTag ignored = nbt.getList("ignoredProperties", Tag.TAG_STRING);
        for (int i = 0; i < ignored.size(); i++) {
            Property<?> property = blockState.getBlock().getStateDefinition().getProperty(ignored.getString(i));
            if (property != null) {
                ignoredProperties.add(property);
            }
        }
        if (nbt.contains("tileNbt")) {
            tileNbt = nbt.getCompound("tileNbt");
        }
        Rotation rotation = NBTUtilBC.readEnum(nbt.get("tileRotation"), Rotation.class);
        tileRotation = rotation == null ? Rotation.NONE : rotation;
        placeBlock = blockByName(nbt.getString("placeBlock"));
        readPosList(nbt.get("updateBlockOffsets"), updateBlockOffsets);
        ListTag replaceable = nbt.getList("canBeReplacedWithBlocks", Tag.TAG_STRING);
        for (int i = 0; i < replaceable.size(); i++) {
            canBeReplacedWithBlocks.add(blockByName(replaceable.getString(i)));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SchematicBlockDefault that = (SchematicBlockDefault) o;
        return requiredBlockOffsets.equals(that.requiredBlockOffsets)
            && blockState.equals(that.blockState)
            && ignoredProperties.equals(that.ignoredProperties)
            && Objects.equals(tileNbt, that.tileNbt)
            && tileRotation == that.tileRotation
            && placeBlock.equals(that.placeBlock)
            && updateBlockOffsets.equals(that.updateBlockOffsets)
            && canBeReplacedWithBlocks.equals(that.canBeReplacedWithBlocks);
    }

    @Override
    public int hashCode() {
        int result = requiredBlockOffsets.hashCode();
        result = 31 * result + blockState.hashCode();
        result = 31 * result + ignoredProperties.hashCode();
        result = 31 * result + (tileNbt != null ? tileNbt.hashCode() : 0);
        result = 31 * result + tileRotation.hashCode();
        result = 31 * result + placeBlock.hashCode();
        result = 31 * result + updateBlockOffsets.hashCode();
        result = 31 * result + canBeReplacedWithBlocks.hashCode();
        return result;
    }
}
