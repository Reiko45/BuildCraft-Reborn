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

/** Mines ores with a pickaxe.
 * <p>
 * 1.21: an ore counts if the held pickaxe can mine it for drops (7.1 compared harvest levels). */
public class BoardRobotMiner extends BoardRobotGenericBreakBlock {

    public BoardRobotMiner(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("miner");
    }

    @Override
    public boolean isExpectedTool(ItemStack stack) {
        return stack.canPerformAction(ItemAbilities.PICKAXE_DIG);
    }

    @Override
    public boolean isExpectedBlock(Level world, BlockPos pos) {
        if (!BuildCraftAPI.getWorldProperty("ore").get(world, pos)) {
            return false;
        }
        ItemStack tool = robot.getMainHandItem();
        return !tool.isEmpty() && tool.isCorrectToolForDrops(world.getBlockState(pos));
    }
}
