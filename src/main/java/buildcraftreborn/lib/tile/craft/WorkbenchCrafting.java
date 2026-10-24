/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.tile.craft;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.lib.inventory.filter.ArrayStackFilter;
import buildcraftreborn.lib.misc.InventoryUtil;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** Crafts a vanilla crafting recipe from a "blueprint" (the pattern, in phantom slots) using items from a materials
 * inventory, putting the result into a result inventory.
 * <p>
 * 1.21: a {@link TransientCraftingContainer} with a menu that ignores changes (1.12 extended
 * {@code InventoryCrafting}). Recipes are looked up with a {@link CraftingInput} made from the blueprint, and the
 * remaining items (empty buckets and the like) are mapped back to grid slots through the input's position, as the
 * vanilla result slot does. */
public class WorkbenchCrafting extends TransientCraftingContainer {
    enum EnumRecipeType {
        INGREDIENTS,
        EXACT_STACKS;
    }

    private final TileBC_Neptune tile;
    private final ItemHandlerSimple invBlueprint;
    private final ItemHandlerSimple invMaterials;
    private final ItemHandlerSimple invResult;
    private boolean isBlueprintDirty = true;
    private boolean areMaterialsDirty = true;
    private boolean cachedHasRequirements = false;

    @Nullable
    private RecipeHolder<CraftingRecipe> currentRecipe;
    private ItemStack assumedResult = ItemStack.EMPTY;

    private EnumRecipeType recipeType = null;

    public WorkbenchCrafting(int width, int height, TileBC_Neptune tile, ItemHandlerSimple invBlueprint,
        ItemHandlerSimple invMaterials, ItemHandlerSimple invResult) {
        super(NullMenu.INSTANCE, width, height);
        this.tile = tile;
        this.invBlueprint = invBlueprint;
        if (invBlueprint.getSlots() < this.getContainerSize()) {
            throw new IllegalArgumentException("Passed blueprint has a smaller size than width * height! ( expected "
                + getContainerSize() + ", got " + invBlueprint.getSlots() + ")");
        }
        this.invMaterials = invMaterials;
        this.invResult = invResult;
    }

    public ItemStack getAssumedResult() {
        return assumedResult;
    }

    public void onInventoryChange(IItemHandler inv) {
        if (inv == invBlueprint) {
            isBlueprintDirty = true;
        } else if (inv == invMaterials) {
            areMaterialsDirty = true;
        }
    }

    private CraftingInput blueprintInput() {
        List<ItemStack> items = new ArrayList<>(getContainerSize());
        for (int s = 0; s < getContainerSize(); s++) {
            items.add(invBlueprint.getStackInSlot(s));
        }
        return CraftingInput.of(getWidth(), getHeight(), items);
    }

    private Level level() {
        return tile.getLevel();
    }

    /** @return True if anything changed, false otherwise */
    public boolean tick() {
        if (level().isClientSide) {
            throw new IllegalStateException("Never call this on the client side!");
        }
        if (isBlueprintDirty) {
            CraftingInput input = blueprintInput();
            Optional<RecipeHolder<CraftingRecipe>> found =
                level().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level());
            currentRecipe = found.orElse(null);
            if (currentRecipe == null) {
                assumedResult = ItemStack.EMPTY;
                recipeType = null;
            } else {
                assumedResult = currentRecipe.value().assemble(input, level().registryAccess());
                NonNullList<Ingredient> ingredients = currentRecipe.value().getIngredients();
                if (ingredients.isEmpty()) {
                    recipeType = EnumRecipeType.EXACT_STACKS;
                } else {
                    recipeType = EnumRecipeType.INGREDIENTS;
                }
            }
            isBlueprintDirty = false;
            return true;
        }
        return false;
    }

    /** @return True if {@link #craft()} might return true, or false if {@link #craft()} will definitely return
     *         false. */
    public boolean canCraft() {
        if (currentRecipe == null || isBlueprintDirty) {
            return false;
        }
        if (!invResult.canFullyAccept(assumedResult)) {
            return false;
        }
        if (areMaterialsDirty) {
            areMaterialsDirty = false;
            switch (recipeType) {
                case INGREDIENTS:
                    // cachedHasRequirements = hasIngredients();
                    // break;
                case EXACT_STACKS: {
                    cachedHasRequirements = hasExactStacks();
                    break;
                }
                default: {
                    throw new IllegalStateException("Unknown recipe type " + recipeType);
                }
            }
        }
        return cachedHasRequirements;
    }

    /** Attempts to craft a single item. Assumes that {@link #canCraft()} has been called in the same tick, without
     * any modifications happening to the inventories in between.
     *
     * @return True if the crafting happened, false otherwise. */
    public boolean craft() throws IllegalStateException {
        if (isBlueprintDirty) {
            return false;
        }

        switch (recipeType) {
            case INGREDIENTS:
                // return craftByIngredients();
            case EXACT_STACKS: {
                return craftExact();
            }
            default: {
                throw new IllegalStateException("Unknown recipe type " + recipeType);
            }
        }
    }

    private boolean hasExactStacks() {
        // Count how many of each (single-item) stack the blueprint needs.
        Map<ItemStackKey, Integer> required = new LinkedHashMap<>(getContainerSize());
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack req = invBlueprint.getStackInSlot(s);
            if (!req.isEmpty()) {
                int count = req.getCount();
                ItemStackKey key = new ItemStackKey(req.copyWithCount(1));
                required.merge(key, count, Integer::sum);
            }
        }
        for (Map.Entry<ItemStackKey, Integer> entry : required.entrySet()) {
            int count = entry.getValue();
            ArrayStackFilter filter = new ArrayStackFilter(entry.getKey().stack);
            ItemStack inInventory = invMaterials.extract(filter, count, count, true);
            if (inInventory.isEmpty() || inInventory.getCount() != count) {
                return false;
            }
        }
        return true;
    }

    /** Implementation of {@link #craft()}, assuming nothing about the current recipe. */
    private boolean craftExact() {
        // 4 steps:
        // - Move everything out of this inventory (Just to check: state correction operation)
        // - Attempt to move every exact item from invMaterials to this inventory
        // - Call normal crafting stuffs
        // - Move everything from the inventory back to materials

        // Step 1
        clearInventory();

        // Step 2
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack bpt = invBlueprint.getStackInSlot(s);
            if (!bpt.isEmpty()) {
                ItemStack stack = invMaterials.extract(new ArrayStackFilter(bpt), 1, 1, false);
                if (stack.isEmpty()) {
                    clearInventory();
                    return false;
                }
                setItem(s, stack);
            }
        }

        // Step 3
        // Some recipes (for example vanilla fireworks) require calling
        // matches before calling getCraftingResult, as they store the
        // result of matches for getCraftingResult and getResult.
        CraftingInput.Positioned positioned = asPositionedCraftInput();
        CraftingInput input = positioned.input();
        if (!currentRecipe.value().matches(input, level())) {
            return false;
        }
        ItemStack result = currentRecipe.value().assemble(input, level().registryAccess());
        if (result.isEmpty()) {
            // what?
            clearInventory();
            return false;
        }
        ItemStack leftover = invResult.insert(result, false, false);
        if (!leftover.isEmpty()) {
            InventoryUtil.addToBestAcceptor(level(), tile.getBlockPos(), null, leftover);
        }
        NonNullList<ItemStack> remainingStacks = currentRecipe.value().getRemainingItems(input);
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                int s = x + positioned.left() + (y + positioned.top()) * getWidth();
                ItemStack inSlot = getItem(s);
                ItemStack remaining = remainingStacks.get(x + y * input.width());

                if (!inSlot.isEmpty()) {
                    removeItem(s, 1);
                    inSlot = getItem(s);
                }

                if (!remaining.isEmpty()) {
                    if (inSlot.isEmpty()) {
                        setItem(s, remaining);
                    } else if (ItemStack.isSameItemSameComponents(inSlot, remaining)) {
                        remaining.grow(inSlot.getCount());
                        setItem(s, remaining);
                    } else {
                        leftover = invMaterials.insert(remaining, false, false);
                        if (!leftover.isEmpty()) {
                            InventoryUtil.addToBestAcceptor(level(), tile.getBlockPos(), null, leftover);
                        }
                    }
                }
            }
        }

        // Step 4
        // Some ingredients really need to be removed (like empty buckets)
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack inSlot = removeItemNoUpdate(s);
            if (!inSlot.isEmpty()) {
                leftover = invMaterials.insert(inSlot, false, false);
                if (!leftover.isEmpty()) {
                    InventoryUtil.addToBestAcceptor(level(), tile.getBlockPos(), null, leftover);
                }
            }
        }
        return true;
    }

    /** @return True if this inventory is now clear, false otherwise. */
    private boolean clearInventory() {
        for (int s = 0; s < getContainerSize(); s++) {
            ItemStack inSlot = getItem(s);
            if (!inSlot.isEmpty()) {
                ItemStack leftover = invMaterials.insert(inSlot, false, false);
                removeItem(s, inSlot.getCount() - (leftover.isEmpty() ? 0 : leftover.getCount()));
                if (!leftover.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /** An item stack compared by item and components (1.12's {@code ItemStackKey}). */
    private record ItemStackKey(ItemStack stack) {
        @Override
        public boolean equals(Object obj) {
            return obj instanceof ItemStackKey other && ItemStack.isSameItemSameComponents(stack, other.stack);
        }

        @Override
        public int hashCode() {
            return ItemStack.hashItemAndComponents(stack);
        }
    }

    /** A menu that ignores the changes the crafting grid reports (1.12's {@code ContainerNullEventHandler}). */
    static final class NullMenu extends AbstractContainerMenu {
        static final NullMenu INSTANCE = new NullMenu();

        private NullMenu() {
            super(null, -1);
        }

        @Override
        public ItemStack quickMoveStack(Player player, int index) {
            return ItemStack.EMPTY;
        }

        @Override
        public boolean stillValid(Player player) {
            return false;
        }

        @Override
        public void slotsChanged(net.minecraft.world.Container container) {
            // NO-OP
        }
    }
}
