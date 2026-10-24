/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.BuildCraftAPI;
import buildcraftreborn.api.crops.CropManager;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.RobotUtils;

/** Harvests a mature crop, dropping what it gives where the robot is. */
public class AIRobotHarvest extends AIRobot {

    @Nullable
    private BlockPos blockFound;
    private int delay = 0;

    public AIRobotHarvest(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotHarvest(EntityRobotBase iRobot, BlockPos iBlockFound) {
        super(iRobot);
        blockFound = iBlockFound;
    }

    @Override
    public void update() {
        if (blockFound == null) {
            setSuccess(false);
            terminate();
            return;
        }

        if (delay++ > 20) {
            if (!BuildCraftAPI.getWorldProperty("harvestable").get(robot.level(), blockFound)) {
                setSuccess(false);
                terminate();
                return;
            }
            NonNullList<ItemStack> drops = NonNullList.create();
            if (!CropManager.harvestCrop(robot.level(), blockFound, drops)) {
                setSuccess(false);
                terminate();
                return;
            }
            for (ItemStack stack : drops) {
                RobotUtils.dropItem(robot.level(), robot.position(), 6000, stack);
            }
            // 1.21: 7.1 waited for the next update to find the crop gone (and so failed)
            terminate();
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.put("blockFound", NBTUtilBC.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("blockFound")) {
            blockFound = NBTUtilBC.readBlockPos(nbt.get("blockFound"));
        }
    }
}
