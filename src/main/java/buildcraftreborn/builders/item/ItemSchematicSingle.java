/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.item;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.schematics.ISchematicBlock;
import buildcraftreborn.api.schematics.SchematicBlockContext;
import buildcraftreborn.builders.BCBuildersItems;
import buildcraftreborn.builders.snapshot.Blueprint;
import buildcraftreborn.builders.snapshot.SchematicBlockManager;
import buildcraftreborn.lib.inventory.InventoryWrapper;
import buildcraftreborn.lib.misc.SoundUtil;
import buildcraftreborn.lib.misc.StackUtil;

/** A single block schematic: click a block to copy it, then click to place copies of it (using items from your
 * inventory). Sneak-click to clear it.
 * <p>
 * 1.21: the copied block is kept in the {@code schematic} component (1.12 used item damage 1 and NBT). A used one
 * stacks to 1, a blank one to 16. */
public class ItemSchematicSingle extends Item {
    public ItemSchematicSingle(Properties properties) {
        super(properties.stacksTo(16));
    }

    public static boolean isUsed(ItemStack stack) {
        return stack.has(BCBuildersItems.SCHEMATIC.get());
    }

    private static void clear(ItemStack stack) {
        stack.remove(BCBuildersItems.SCHEMATIC.get());
        // Setting the item's own value drops the override (removing it would leave the stack unstackable)
        stack.set(DataComponents.MAX_STACK_SIZE, stack.getItem().getDefaultMaxStackSize());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.pass(stack);
        }
        if (player.isShiftKeyDown()) {
            clear(stack);
            return InteractionResultHolder.success(stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide || player == null) {
            return InteractionResult.PASS;
        }
        BlockPos pos = context.getClickedPos();
        if (player.isShiftKeyDown()) {
            clear(stack);
            return InteractionResult.SUCCESS;
        }
        if (!isUsed(stack)) {
            BlockState state = level.getBlockState(pos);
            ISchematicBlock schematicBlock = SchematicBlockManager.getSchematicBlock(
                new SchematicBlockContext(level, pos, pos, state, state.getBlock()));
            if (schematicBlock.isAir()) {
                return InteractionResult.FAIL;
            }
            if (stack.getCount() > 1) {
                // Only one of a stack of blank schematics gets used.
                ItemStack used = stack.split(1);
                used.set(BCBuildersItems.SCHEMATIC.get(), SchematicBlockManager.writeToNBT(schematicBlock));
                used.set(DataComponents.MAX_STACK_SIZE, 1);
                if (!player.getInventory().add(used)) {
                    player.drop(used, false);
                }
            } else {
                stack.set(BCBuildersItems.SCHEMATIC.get(), SchematicBlockManager.writeToNBT(schematicBlock));
                stack.set(DataComponents.MAX_STACK_SIZE, 1);
            }
            return InteractionResult.SUCCESS;
        }
        BlockPos placePos = pos;
        boolean replaceable = level.getBlockState(pos).canBeReplaced();
        if (!replaceable) {
            placePos = placePos.relative(context.getClickedFace());
        }
        if (!level.getBlockState(placePos).canBeReplaced() || !level.mayInteract(player, placePos)
            || !player.mayUseItemAt(placePos, context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }
        if (replaceable && !level.isEmptyBlock(placePos)) {
            level.removeBlock(placePos, false);
        }
        try {
            ISchematicBlock schematicBlock = getSchematic(stack);
            if (schematicBlock != null && !schematicBlock.isBuilt(level, placePos)
                && schematicBlock.canBuild(level, placePos)) {
                List<FluidStack> requiredFluids = schematicBlock.computeRequiredFluids();
                List<ItemStack> requiredItems = StackUtil.mergeSameItems(schematicBlock.computeRequiredItems());
                if (!requiredFluids.isEmpty()) {
                    player.displayClientMessage(Component.literal("Schematic requires fluids"), true);
                    return InteractionResult.FAIL;
                }
                InventoryWrapper itemTransactor = new InventoryWrapper(player.getInventory());
                if (requiredItems.stream().noneMatch(s -> itemTransactor
                    .extract(extracted -> StackUtil.canMerge(s, extracted), s.getCount(), s.getCount(), true)
                    .isEmpty())) {
                    if (schematicBlock.build(level, placePos)) {
                        requiredItems.forEach(s -> itemTransactor.extract(
                            extracted -> StackUtil.canMerge(s, extracted), s.getCount(), s.getCount(), false));
                        SoundUtil.playBlockPlace(level, placePos);
                        player.swing(context.getHand(), true);
                        return InteractionResult.SUCCESS;
                    }
                } else {
                    player.displayClientMessage(Component.literal("Not enough items. Total needed: " + requiredItems
                        .stream().map(s -> s.getHoverName().getString() + " x " + s.getCount())
                        .collect(Collectors.joining(", "))), true);
                }
            }
        } catch (InvalidInputDataException e) {
            player.displayClientMessage(Component.literal("Invalid schematic: " + e.getMessage()), true);
            BuildCraftReborn.LOGGER.warn("[builders.schematic] Invalid schematic", e);
        }
        return InteractionResult.FAIL;
    }

    /** A preview of the copied block (a 1x1x1 blueprint, as in 1.12). */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        ISchematicBlock schematicBlock = getSchematicSafe(stack);
        if (schematicBlock == null) {
            return Optional.empty();
        }
        Blueprint blueprint = new Blueprint();
        blueprint.size = new BlockPos(1, 1, 1);
        blueprint.offset = BlockPos.ZERO;
        blueprint.facing = net.minecraft.core.Direction.NORTH;
        blueprint.data = new int[] { 0 };
        blueprint.palette.add(schematicBlock);
        blueprint.computeKey();
        return Optional.of(new SnapshotTooltip(null, blueprint));
    }

    @Nullable
    public static ISchematicBlock getSchematic(ItemStack stack) throws InvalidInputDataException {
        if (stack.getItem() instanceof ItemSchematicSingle) {
            CompoundTag nbt = stack.get(BCBuildersItems.SCHEMATIC.get());
            return nbt == null ? null : SchematicBlockManager.readFromNBT(nbt);
        }
        return null;
    }

    @Nullable
    public static ISchematicBlock getSchematicSafe(ItemStack stack) {
        try {
            return getSchematic(stack);
        } catch (InvalidInputDataException e) {
            BuildCraftReborn.LOGGER.warn("Invalid schematic " + e.getMessage());
            return null;
        }
    }
}
