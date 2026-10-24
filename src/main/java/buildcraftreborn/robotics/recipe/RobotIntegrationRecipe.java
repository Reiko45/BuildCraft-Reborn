/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.recipe;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.IngredientStack;
import buildcraftreborn.api.recipes.IntegrationRecipe;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.BCRoboticsItems;
import buildcraftreborn.robotics.item.ItemRedstoneBoard;
import buildcraftreborn.robotics.item.ItemRobot;

/** Puts a redstone board into a robot in the integration table (replacing the board it had). A robot with no power
 * gets a fifth of a charge, as in 7.1.
 * <p>
 * 1.21: registered in code, as it works out its output from the board; it costs 5,000 MJ (7.1: 50,000 RF). */
public class RobotIntegrationRecipe extends IntegrationRecipe {
    private static final long POWER = 5_000 * MjAPI.MJ;

    public RobotIntegrationRecipe() {
        super(BuildCraftReborn.id("robot_board"));
    }

    private static ItemStack findBoard(NonNullList<ItemStack> toIntegrate) {
        ItemStack found = ItemStack.EMPTY;
        for (ItemStack stack : toIntegrate) {
            if (stack.isEmpty()) {
                continue;
            }
            if (!(stack.getItem() instanceof ItemRedstoneBoard) || !found.isEmpty()) {
                return ItemStack.EMPTY;
            }
            found = stack;
        }
        return found;
    }

    @Override
    public ItemStack getOutput(ItemStack target, NonNullList<ItemStack> toIntegrate) {
        if (!(target.getItem() instanceof ItemRobot)) {
            return ItemStack.EMPTY;
        }
        ItemStack board = findBoard(toIntegrate);
        if (board.isEmpty()) {
            return ItemStack.EMPTY;
        }
        RedstoneBoardNBT<?> boardNBT = ItemRedstoneBoard.getBoardNBT(board);
        if (!(boardNBT instanceof RedstoneBoardRobotNBT robotBoard)) {
            return ItemStack.EMPTY;
        }

        long energy = ItemRobot.getPower(target);
        if (energy == 0) {
            energy = EntityRobotBase.SAFETY_POWER;
        }
        return ItemRobot.createRobotStack(robotBoard, energy);
    }

    @Override
    public ImmutableList<IngredientStack> getRequirements(ItemStack output) {
        return ImmutableList.of(IngredientStack.of(BCRoboticsItems.REDSTONE_BOARD.get()));
    }

    @Override
    public long getRequiredMicroJoules(ItemStack output) {
        return POWER;
    }

    @Override
    public IngredientStack getCenterStack() {
        return new IngredientStack(Ingredient.of(BCRoboticsItems.ROBOT.get()), 1);
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return new ItemStack(BCRoboticsItems.ROBOT.get());
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        throw new UnsupportedOperationException("The robot integration recipe is only ever registered in code");
    }
}
