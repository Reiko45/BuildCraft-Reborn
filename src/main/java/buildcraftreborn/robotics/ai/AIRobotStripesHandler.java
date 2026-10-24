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
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.common.util.FakePlayer;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.robotics.RobotUtils;

/** Uses the held item at a spot as a stripes pipe would. */
public class AIRobotStripesHandler extends AIRobot implements IStripesActivator {
    @Nullable
    private BlockPos useToBlock;
    private int useCycles = 0;

    public AIRobotStripesHandler(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotStripesHandler(EntityRobotBase iRobot, BlockPos index) {
        this(iRobot);

        useToBlock = index;
    }

    @Override
    public void start() {
        if (useToBlock != null) {
            robot.aimItemAt(useToBlock);
        }
        robot.setItemActive(true);
    }

    @Override
    public void update() {
        if (useToBlock == null || !(robot.level() instanceof ServerLevel)) {
            setSuccess(false);
            terminate();
            return;
        }

        useCycles++;

        if (useCycles > 60) {
            ItemStack stack = robot.getMainHandItem();

            Direction direction = Direction.NORTH;

            FakePlayer player = RobotUtils.getFakePlayer(robot, useToBlock);
            player.setXRot(0);
            player.setYRot(180);
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);

            boolean handled = PipeApi.stripeRegistry != null && !stack.isEmpty()
                && PipeApi.stripeRegistry.handleItem(robot.level(), useToBlock, direction, stack, player, this);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            if (handled) {
                robot.setItemInUse(ItemStack.EMPTY);
            }
            terminate();
        }
    }

    @Override
    public void end() {
        robot.setItemActive(false);
    }

    @Override
    public long getPowerCost() {
        return 15 * MjAPI.MJ / 10;
    }

    @Override
    public boolean sendItem(ItemStack stack, Direction direction) {
        RobotUtils.dropItem(robot.level(), robot.position(), 6000, stack);
        return true;
    }

    @Override
    public void dropItem(ItemStack stack, Direction direction) {
        sendItem(stack, direction);
    }
}
