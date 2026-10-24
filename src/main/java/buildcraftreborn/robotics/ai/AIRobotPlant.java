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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.common.util.FakePlayer;

import buildcraftreborn.api.crops.CropManager;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.RobotUtils;

/** Plants the held seed on a block. */
public class AIRobotPlant extends AIRobot {
    @Nullable
    private BlockPos blockFound;
    private int delay = 0;

    public AIRobotPlant(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotPlant(EntityRobotBase iRobot, BlockPos iBlockFound) {
        this(iRobot);

        blockFound = iBlockFound;
    }

    @Override
    public void start() {
        if (blockFound != null) {
            robot.aimItemAt(blockFound);
        }
        robot.setItemActive(true);
    }

    @Override
    public void update() {
        if (blockFound == null || !(robot.level() instanceof ServerLevel)) {
            setSuccess(false);
            terminate();
            return;
        }

        if (delay++ > 40) {
            ItemStack held = robot.getMainHandItem();
            FakePlayer player = RobotUtils.getFakePlayer(robot, robot.blockPosition());
            if (held.isEmpty() || !CropManager.plantCrop(robot.level(), player, held, blockFound)) {
                setSuccess(false);
            }
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            if (!held.isEmpty()) {
                RobotUtils.dropItem(robot.level(), robot.position(), 6000, held);
            }
            robot.setItemInUse(ItemStack.EMPTY);
            terminate();
        }
    }

    @Override
    public void end() {
        robot.setItemActive(false);
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
