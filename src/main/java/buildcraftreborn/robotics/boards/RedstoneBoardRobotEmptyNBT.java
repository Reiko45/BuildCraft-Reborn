/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.robots.EntityRobotBase;

/** The blank ("clean") board: a robot with it does nothing but sleep. */
public class RedstoneBoardRobotEmptyNBT extends RedstoneBoardRobotNBT {

    public static final RedstoneBoardRobotEmptyNBT instance = new RedstoneBoardRobotEmptyNBT();

    public static final ResourceLocation ROBOT_BASE = BuildCraftReborn.id("textures/entity/robot/base.png");

    @Override
    public RedstoneBoardRobot create(EntityRobotBase robot) {
        return new BoardRobotEmpty(robot);
    }

    @Override
    public ResourceLocation getRobotTexture() {
        return ROBOT_BASE;
    }

    @Override
    public String getID() {
        return BuildCraftReborn.id("empty").toString();
    }

    @Override
    public void addInformation(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
        TooltipFlag flag) {}

    @Override
    public ResourceLocation getItemModelLocation() {
        return BuildCraftReborn.id("item/redstone_board/clean");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("buildcraftreborn.board.clean");
    }
}
