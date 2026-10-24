/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.recipe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.api.recipes.IProgrammingRecipe;
import buildcraftreborn.robotics.item.ItemRedstoneBoard;

/** Programs a redstone board into any board, cheapest first. */
public class BoardProgrammingRecipe implements IProgrammingRecipe {

    @Override
    public String getId() {
        return "buildcraft:redstone_board";
    }

    @Override
    public List<ItemStack> getOptions(int width, int height) {
        List<ItemStack> options = new ArrayList<>(width * height);
        for (RedstoneBoardNBT<?> nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            options.add(ItemRedstoneBoard.createStack(nbt));
        }
        options.sort(Comparator.comparingLong(this::getPowerCost)
            .thenComparing(stack -> ItemRedstoneBoard.getBoardNBT(stack).getID()));
        return options;
    }

    @Override
    public long getPowerCost(ItemStack option) {
        return RedstoneBoardRegistry.instance.getPowerCost(ItemRedstoneBoard.getBoardNBT(option));
    }

    @Override
    public boolean canCraft(ItemStack input) {
        return input.getItem() instanceof ItemRedstoneBoard;
    }

    @Override
    public ItemStack craft(ItemStack input, ItemStack option) {
        return option.copy();
    }
}
