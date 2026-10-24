/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import buildcraftreborn.api.schematics.ISchematicBlock;
import buildcraftreborn.api.schematics.ISchematicEntity;
import buildcraftreborn.api.schematics.SchematicEntityContext;
import buildcraftreborn.lib.misc.FluidUtilBC;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.lib.net.PacketBufferBC;

/** Builds a blueprint: places each block from its schematic with the items (and fluids) it needs, and spawns its
 * entities.
 * <p>
 * 1.21: a fluid taken for a block travels as a filled bucket marked with the {@code BuilderFluidStack} custom data
 * (1.12 used the bucket's NBT). */
public class BlueprintBuilder extends SnapshotBuilder<ITileForBlueprintBuilder> {
    private static final double MAX_ENTITY_DISTANCE = 0.1D;
    private static final String FLUID_STACK_KEY = "BuilderFluidStack";

    private List<ItemStack>[] remainingDisplayRequiredBlocks;
    private List<ItemStack> remainingDisplayRequiredBlocksConcat = Collections.emptyList();
    public List<ItemStack> remainingDisplayRequired = new ArrayList<>();
    private final Map<Pair<List<ItemStack>, List<FluidStack>>, Optional<List<ItemStack>>> extractRequiredCache =
        new HashMap<>();

    public BlueprintBuilder(ITileForBlueprintBuilder tile) {
        super(tile);
    }

    private ISchematicBlock getSchematicBlock(BlockPos blockPos) {
        return getBuildingInfo().box.contains(blockPos)
            ? getBuildingInfo().rotatedPalette.get(getBuildingInfo().getSnapshot().data[
                getBuildingInfo().getSnapshot().posToIndex(getBuildingInfo().fromWorld(blockPos))])
            : null;
    }

    @Override
    protected boolean isAir(BlockPos blockPos) {
        ISchematicBlock schematicBlock = getSchematicBlock(blockPos);
        return schematicBlock == null || schematicBlock.isAir();
    }

    @Override
    protected Blueprint.BuildingInfo getBuildingInfo() {
        return tile.getBlueprintBuildingInfo();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void updateSnapshot() {
        super.updateSnapshot();
        remainingDisplayRequiredBlocks = (List<ItemStack>[]) new List<?>[getBuildingInfo().getSnapshot().getDataSize()];
        Arrays.fill(remainingDisplayRequiredBlocks, Collections.emptyList());
    }

    @Override
    public void resourcesChanged() {
        super.resourcesChanged();
        extractRequiredCache.clear();
    }

    @Override
    public void cancel() {
        super.cancel();
        remainingDisplayRequiredBlocks = null;
    }

    private Stream<ItemStack> getDisplayRequired(List<ItemStack> requiredItems, List<FluidStack> requiredFluids) {
        return Stream.concat(
            requiredItems == null ? Stream.empty() : requiredItems.stream(),
            requiredFluids == null ? Stream.empty() : requiredFluids.stream().map(FluidUtil::getFilledBucket));
    }

    private Optional<List<ItemStack>> tryExtractRequired(List<ItemStack> requiredItems,
        List<FluidStack> requiredFluids, boolean simulate) {
        List<ItemStack> items = requiredItems == null ? Collections.emptyList() : requiredItems;
        List<FluidStack> fluids = requiredFluids == null ? Collections.emptyList() : requiredFluids;
        Supplier<Optional<List<ItemStack>>> function = () -> {
            boolean hasAll = StackUtil.mergeSameItems(items).stream()
                .noneMatch(stack -> tile.getInvResources().extract(
                    extracted -> StackUtil.canMerge(stack, extracted), stack.getCount(), stack.getCount(), true)
                    .isEmpty())
                && FluidUtilBC.mergeSameFluids(fluids).stream()
                    .allMatch(stack -> FluidStack.matches(stack, tile.getTankManager().drain(stack,
                        FluidAction.SIMULATE)));
            if (!hasAll) {
                return Optional.empty();
            }
            List<ItemStack> taken = new ArrayList<>();
            for (ItemStack stack : items) {
                taken.add(tile.getInvResources().extract(extracted -> StackUtil.canMerge(stack, extracted),
                    stack.getCount(), stack.getCount(), simulate));
            }
            for (FluidStack fluidStack : FluidUtilBC.mergeSameFluids(fluids)) {
                FluidStack drained = tile.getTankManager().drain(fluidStack,
                    simulate ? FluidAction.SIMULATE : FluidAction.EXECUTE);
                ItemStack bucket = FluidUtil.getFilledBucket(drained);
                CompoundTag data = new CompoundTag();
                data.put(FLUID_STACK_KEY, drained.save(tile.getWorldBC().registryAccess()));
                CustomData.update(DataComponents.CUSTOM_DATA, bucket, tag -> tag.merge(data));
                taken.add(bucket);
            }
            return Optional.of(StackUtil.mergeSameItems(taken));
        };
        if (!simulate) {
            return function.get();
        }
        return extractRequiredCache.computeIfAbsent(Pair.of(items, fluids), pair -> function.get());
    }

    @Override
    protected boolean canPlace(BlockPos blockPos) {
        return !isAir(blockPos) && getSchematicBlock(blockPos).canBuild(tile.getWorldBC(), blockPos);
    }

    @Override
    protected boolean isReadyToPlace(BlockPos blockPos) {
        ISchematicBlock schematicBlock = getSchematicBlock(blockPos);
        return schematicBlock.getRequiredBlockOffsets().stream()
            .map(blockPos::offset)
            .allMatch(pos -> getSchematicBlock(pos) == null || checkResults[posToIndex(pos)] == CHECK_RESULT_CORRECT)
            && schematicBlock.isReadyToBuild(tile.getWorldBC(), blockPos);
    }

    @Override
    protected boolean hasEnoughToPlaceItems(BlockPos blockPos) {
        return tryExtractRequired(getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
            getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)], true).isPresent();
    }

    @Override
    protected List<ItemStack> getToPlaceItems(BlockPos blockPos) {
        return tryExtractRequired(getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
            getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)], false).orElse(null);
    }

    private static boolean isFluidBucket(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.contains(FLUID_STACK_KEY);
    }

    @Override
    protected void cancelPlaceTask(PlaceTask placeTask) {
        super.cancelPlaceTask(placeTask);
        for (ItemStack stack : placeTask.items) {
            if (!isFluidBucket(stack)) {
                tile.getInvResources().insert(stack, false, false);
            } else {
                CompoundTag tag = stack.get(DataComponents.CUSTOM_DATA).copyTag().getCompound(FLUID_STACK_KEY);
                FluidStack fluidStack = FluidStack.parseOptional(tile.getWorldBC().registryAccess(), tag);
                if (!fluidStack.isEmpty()) {
                    fluidStack.setAmount(fluidStack.getAmount() * stack.getCount());
                    tile.getTankManager().fill(fluidStack, FluidAction.EXECUTE);
                }
            }
        }
    }

    @Override
    protected boolean isBlockCorrect(BlockPos blockPos) {
        ISchematicBlock schematicBlock = getBuildingInfo() == null ? null : getSchematicBlock(blockPos);
        return schematicBlock != null && schematicBlock.isBuilt(tile.getWorldBC(), blockPos);
    }

    @Override
    protected boolean doPlaceTask(PlaceTask placeTask) {
        ISchematicBlock schematicBlock = getBuildingInfo() == null ? null : getSchematicBlock(placeTask.pos);
        return schematicBlock != null && schematicBlock.build(tile.getWorldBC(), placeTask.pos);
    }

    @Override
    public boolean tick() {
        Level level = tile.getWorldBC();
        if (level.isClientSide) {
            return super.tick();
        }
        level.getProfiler().push("entitiesWithinBox");
        List<Entity> entitiesWithinBox = level.getEntitiesOfClass(Entity.class,
            getBuildingInfo().box.getBoundingBox(), Objects::nonNull);
        level.getProfiler().pop();
        Vec3 offset = Vec3.atLowerCornerOf(getBuildingInfo().offsetPos);
        List<ISchematicEntity> toSpawn = getBuildingInfo().entities.stream()
            .filter(schematicEntity -> entitiesWithinBox.stream()
                .map(Entity::position)
                .map(schematicEntity.getPos().add(offset)::distanceTo)
                .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE))
            .collect(Collectors.toList());
        // Compute needed stacks
        remainingDisplayRequired.clear();
        remainingDisplayRequired.addAll(StackUtil.mergeSameItems(Stream.concat(
            remainingDisplayRequiredBlocksConcat.stream(),
            toSpawn.stream().flatMap(schematicEntity -> getDisplayRequired(
                getBuildingInfo().entitiesRequiredItems.get(schematicEntity),
                getBuildingInfo().entitiesRequiredFluids.get(schematicEntity))))
            .collect(Collectors.toList())));
        // Kill not needed entities
        List<Entity> toKill = entitiesWithinBox.stream()
            .filter(entity -> entity != null
                && getBuildingInfo().entities.stream()
                    .map(ISchematicEntity::getPos)
                    .map(offset::add)
                    .map(entity.position()::distanceTo)
                    .noneMatch(distance -> distance < MAX_ENTITY_DISTANCE)
                && SchematicEntityManager.getSchematicEntity(new SchematicEntityContext(level, BlockPos.ZERO,
                    entity)) != null)
            .collect(Collectors.toList());
        if (!toKill.isEmpty()) {
            if (!tile.getBattery().isFull()) {
                return false;
            } else {
                toKill.forEach(Entity::discard);
            }
        }
        // Call superclass method
        if (super.tick()) {
            // Spawn needed entities
            if (!toSpawn.isEmpty()) {
                if (!tile.getBattery().isFull()) {
                    return false;
                } else {
                    for (ISchematicEntity schematicEntity : toSpawn) {
                        List<ItemStack> items = getBuildingInfo().entitiesRequiredItems.get(schematicEntity);
                        List<FluidStack> fluids = getBuildingInfo().entitiesRequiredFluids.get(schematicEntity);
                        if (tryExtractRequired(items, fluids, true).isPresent()
                            && schematicEntity.build(level, getBuildingInfo().offsetPos) != null) {
                            tryExtractRequired(items, fluids, false);
                        }
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean check(BlockPos blockPos) {
        if (super.check(blockPos)) {
            remainingDisplayRequiredBlocks[posToIndex(blockPos)] =
                checkResults[posToIndex(blockPos)] != CHECK_RESULT_CORRECT
                    ? getDisplayRequired(getBuildingInfo().toPlaceRequiredItems[posToIndex(blockPos)],
                        getBuildingInfo().toPlaceRequiredFluids[posToIndex(blockPos)]).collect(Collectors.toList())
                    : Collections.emptyList();
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void afterChecks() {
        remainingDisplayRequiredBlocksConcat = StackUtil.mergeSameItems(Arrays.stream(remainingDisplayRequiredBlocks)
            .flatMap(Collection::stream)
            .collect(Collectors.toList()));
    }

    @Override
    public void writeToByteBuf(PacketBufferBC buffer) {
        super.writeToByteBuf(buffer);
        buffer.writeInt(remainingDisplayRequired.size());
        remainingDisplayRequired.forEach(stack -> {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, stack.copyWithCount(1));
            buffer.writeInt(stack.getCount());
        });
    }

    @Override
    public void readFromByteBuf(PacketBufferBC buffer) {
        super.readFromByteBuf(buffer);
        remainingDisplayRequired.clear();
        int count = buffer.readInt();
        for (int i = 0; i < count; i++) {
            ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            stack.setCount(buffer.readInt());
            remainingDisplayRequired.add(stack);
        }
    }
}
