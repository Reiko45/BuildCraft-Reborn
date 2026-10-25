/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.silicon.tile;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.lib.misc.InventoryUtil;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.container.ContainerStampingTable;
import buildcraftreborn.silicon.item.ItemPackage;

/** The stamping table: lasers power it to craft what's in it. A package is crafted as the crafting grid it holds; any
 * other item is crafted on its own, as a one-item recipe (a log into planks, for example). What can't be crafted moves
 * to the output slot unchanged. What a recipe leaves behind (an empty bucket, say) goes in the three small slots, then
 * into an inventory beside the table, then on the ground.
 * <p>
 * 7.1's cost, 400 RF for each item in the package, is 40 MJ here (10 RF to the MJ). 7.1 used the power up even when
 * there was nowhere for the result to go; here the table waits instead. */
public class TileStampingTable extends TileLaserTableBase implements MenuProvider {
    public static final long POWER_PER_ITEM = 40 * MjAPI.MJ;
    /** The result goes in the first output slot; leftovers go in the other three. */
    public static final int OUTPUT_SLOTS = 4;

    public final ItemHandlerSimple invInput = itemManager.addInvHandler("input", 1, EnumAccess.INSERT,
        EnumPipePart.VALUES);
    public final ItemHandlerSimple invOutput = itemManager.addInvHandler("output", OUTPUT_SLOTS,
        EnumAccess.EXTRACT, EnumPipePart.VALUES);

    public TileStampingTable(BlockPos pos, BlockState state) {
        super(BCSiliconPackaging.STAMPING_TABLE_TILE.get(), pos, state);
    }

    @Override
    public long getTarget() {
        ItemStack input = invInput.getStackInSlot(0);
        if (input.isEmpty()) {
            return 0;
        }
        ItemStack output = invOutput.getStackInSlot(0);
        if (!output.isEmpty() && output.getCount() >= output.getMaxStackSize()) {
            return 0;
        }
        if (input.getItem() instanceof ItemPackage) {
            // (7.1 left an empty package in the table for good; here it costs as much as one item to pass on)
            return POWER_PER_ITEM * Math.max(1, ItemPackage.countFilledSlots(input));
        }
        return POWER_PER_ITEM;
    }

    @Override
    public void update() {
        super.update();
        if (level.isClientSide) {
            return;
        }
        long target = getTarget();
        if (target > 0 && power >= target && stamp()) {
            power -= target;
        }
    }

    /** @return The crafting grid for the input: the package's contents, or the item alone in the first slot. */
    private static NonNullList<ItemStack> getGrid(ItemStack input) {
        if (input.getItem() instanceof ItemPackage) {
            return ItemPackage.getContents(input);
        }
        NonNullList<ItemStack> grid = NonNullList.withSize(ItemPackage.SLOTS, ItemStack.EMPTY);
        grid.set(0, input.copyWithCount(1));
        return grid;
    }

    @Nullable
    private RecipeHolder<CraftingRecipe> findRecipe(CraftingInput input) {
        Optional<RecipeHolder<CraftingRecipe>> recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
            input, level);
        return recipe.orElse(null);
    }

    /** Crafts the input, or moves it on if it can't be crafted.
     *
     * @return True if something happened (and the power should be used). */
    private boolean stamp() {
        ItemStack input = invInput.getStackInSlot(0);
        CraftingInput grid = CraftingInput.of(3, 3, getGrid(input));
        RecipeHolder<CraftingRecipe> recipe = findRecipe(grid);
        ItemStack result = recipe == null ? ItemStack.EMPTY : recipe.value().assemble(grid, level.registryAccess());
        if (result.isEmpty()) {
            // Nothing to craft: the input goes on to the output, if that's free
            if (!invOutput.getStackInSlot(0).isEmpty()) {
                return false;
            }
            invOutput.setStackInSlot(0, input);
            invInput.setStackInSlot(0, ItemStack.EMPTY);
            return true;
        }
        for (ItemStack left : recipe.value().getRemainingItems(grid)) {
            if (!left.isEmpty()) {
                ItemStack rest = addToSlots(left, 1);
                if (!rest.isEmpty()) {
                    InventoryUtil.drop(level, worldPosition.above(),
                        InventoryUtil.addToRandomInventory(level, worldPosition, rest));
                }
            }
        }
        ItemStack rest = addToSlots(result, 0);
        if (!rest.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 0.7,
                worldPosition.getZ() + 0.5, rest));
        }
        invInput.setStackInSlot(0, input.copyWithCount(input.getCount() - 1));
        return true;
    }

    /** Puts the stack into the output slots from {@code first} on, merging with what's there.
     *
     * @return What didn't fit. */
    private ItemStack addToSlots(ItemStack stack, int first) {
        stack = stack.copy();
        for (int i = first; i < OUTPUT_SLOTS && !stack.isEmpty(); i++) {
            ItemStack inside = invOutput.getStackInSlot(i);
            if (inside.isEmpty()) {
                invOutput.setStackInSlot(i, stack);
                return ItemStack.EMPTY;
            }
            if (ItemStack.isSameItemSameComponents(inside, stack)) {
                int moved = Math.min(stack.getCount(), inside.getMaxStackSize() - inside.getCount());
                if (moved > 0) {
                    invOutput.setStackInSlot(i, inside.copyWithCount(inside.getCount() + moved));
                    stack.shrink(moved);
                }
            }
        }
        return stack;
    }

    // GUI

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (!level.isClientSide) {
            openGui(player);
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerStampingTable(windowId, player, this);
    }
}
