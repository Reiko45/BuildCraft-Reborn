/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.item;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.lib.item.IItemSubtypes;
import buildcraftreborn.robotics.BCRoboticsItems;

/** A redstone board: blank ("clean", from crafting) or programmed in the programming table.
 * <p>
 * 1.21: the board id is the {@code buildcraftreborn:board} component (1.12: the {@code id} NBT tag); a blank board
 * has none. Programmed boards don't stack, blank ones stack to 16. */
public class ItemRedstoneBoard extends Item implements IItemSubtypes {
    public ItemRedstoneBoard(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public Component getName(ItemStack stack) {
        RedstoneBoardNBT<?> board = getBoardNBT(stack);
        return Component.translatable(getDescriptionId(stack)).append(" (").append(board.getDisplayName())
            .append(")");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        // The board's name is already in the item's name
        RedstoneBoardNBT<?> board = getBoardNBT(stack);
        List<Component> lines = new ArrayList<>();
        board.addInformation(stack, context, lines, flag);
        String name = board.getDisplayName().getString();
        lines.removeIf(line -> line.getString().equals(name));
        tooltip.addAll(lines);
    }

    @Override
    public void addSubtypes(Consumer<ItemStack> output) {
        output.accept(createStack(RedstoneBoardRegistry.instance.getEmptyRobotBoard()));
        for (RedstoneBoardNBT<?> boardNBT : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            output.accept(createStack(boardNBT));
        }
    }

    public static ItemStack createStack(RedstoneBoardNBT<?> boardNBT) {
        ItemStack stack = new ItemStack(BCRoboticsItems.REDSTONE_BOARD.get());
        if (boardNBT != RedstoneBoardRegistry.instance.getEmptyRobotBoard()) {
            stack.set(BCRoboticsItems.BOARD.get(), boardNBT.getID());
            stack.set(DataComponents.MAX_STACK_SIZE, 1);
        }
        return stack;
    }

    public static RedstoneBoardNBT<?> getBoardNBT(ItemStack stack) {
        return RedstoneBoardRegistry.instance.getRedstoneBoard(stack.get(BCRoboticsItems.BOARD.get()));
    }

    public static boolean isClean(ItemStack stack) {
        return getBoardNBT(stack) == RedstoneBoardRegistry.instance.getEmptyRobotBoard();
    }
}
