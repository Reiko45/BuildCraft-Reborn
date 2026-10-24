/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;

import buildcraftreborn.api.lists.ListMatchHandler;

/** Fluid containers: "same type" matches the same container whatever it holds, "same material" matches anything
 * holding the same fluid. */
public class ListMatchHandlerFluid extends ListMatchHandler {
    private static final List<ItemStack> clientExampleHolders = new ArrayList<>();
    private static boolean isBuilt = false;

    private static void buildClientExampleList() {
        if (isBuilt) {
            return;
        }
        isBuilt = true;
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack toTry = new ItemStack(item);
            Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(toTry);
            if (fluidHandler.isPresent() && fluidHandler.get().drain(1, FluidAction.SIMULATE).isEmpty()) {
                clientExampleHolders.add(toTry);
            }
        }
    }

    private static ItemStack emptied(ItemStack stack) {
        Optional<IFluidHandlerItem> handler = FluidUtil.getFluidHandler(stack.copyWithCount(1));
        if (handler.isEmpty()) {
            return ItemStack.EMPTY;
        }
        handler.get().drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
        return handler.get().getContainer();
    }

    @Override
    public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            if (FluidUtil.getFluidHandler(stack).isPresent() && FluidUtil.getFluidHandler(target).isPresent()) {
                // check to make sure that both of the stacks can contain fluid
                ItemStack emptyStack = emptied(stack);
                ItemStack emptyTarget = emptied(target);
                return !emptyStack.isEmpty() && ItemStack.isSameItemSameComponents(emptyStack, emptyTarget);
            }
        } else if (type == Type.MATERIAL) {
            Optional<FluidStack> fStack = FluidUtil.getFluidContained(stack);
            Optional<FluidStack> fTarget = FluidUtil.getFluidContained(target);
            if (fStack.isPresent() && fTarget.isPresent()) {
                return FluidStack.isSameFluidSameComponents(fStack.get(), fTarget.get());
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, ItemStack stack) {
        if (type == Type.TYPE) {
            return FluidUtil.getFluidHandler(stack).isPresent();
        } else if (type == Type.MATERIAL) {
            return FluidUtil.getFluidContained(stack).isPresent();
        }
        return false;
    }

    @Override
    @Nullable
    public NonNullList<ItemStack> getClientExamples(Type type, ItemStack stack) {
        buildClientExampleList();
        if (type == Type.MATERIAL) {
            Optional<FluidStack> fStack = FluidUtil.getFluidContained(stack);
            if (fStack.isPresent()) {
                NonNullList<ItemStack> examples = NonNullList.create();
                for (ItemStack potentialHolder : clientExampleHolders) {
                    Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(potentialHolder.copy());
                    if (fluidHandler.isPresent()
                        && fluidHandler.get().fill(fStack.get().copy(), FluidAction.EXECUTE) > 0) {
                        examples.add(fluidHandler.get().getContainer());
                    }
                }
                return examples;
            }
        } else if (type == Type.TYPE) {
            Optional<IFluidHandlerItem> fluidHandler = FluidUtil.getFluidHandler(stack.copyWithCount(1));
            if (fluidHandler.isPresent()) {
                NonNullList<ItemStack> examples = NonNullList.create();
                examples.add(stack);
                FluidStack contained = fluidHandler.get().drain(Integer.MAX_VALUE, FluidAction.EXECUTE);
                if (!contained.isEmpty()) {
                    ItemStack empty = fluidHandler.get().getContainer();
                    examples.add(empty);
                    for (ItemStack potential : clientExampleHolders) {
                        if (!ItemStack.isSameItem(potential, empty)) {
                            continue;
                        }
                        Optional<IFluidHandlerItem> potentialHolder = FluidUtil.getFluidHandler(potential.copy());
                        if (potentialHolder.isPresent()
                            && potentialHolder.get().fill(contained.copy(), FluidAction.EXECUTE) > 0) {
                            examples.add(potentialHolder.get().getContainer());
                        }
                    }
                }
                return examples;
            }
        }
        return null;
    }
}
