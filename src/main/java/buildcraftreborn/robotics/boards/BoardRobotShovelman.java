/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.ItemAbilities;

import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.core.BuildCraftAPI;
import buildcraftreborn.api.robots.EntityRobotBase;

/** Digs dirt, sand, gravel and the like with a shovel. */
public class BoardRobotShovelman extends BoardRobotGenericBreakBlock {

    public BoardRobotShovelman(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("shovelman");
    }

    @Override
    public boolean isExpectedTool(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.SHOVEL_DIG);
    }

    @Override
    public boolean isExpectedBlock(Level world, BlockPos pos) {
        return BuildCraftAPI.getWorldProperty("shoveled").get(world, pos);
    }
}
