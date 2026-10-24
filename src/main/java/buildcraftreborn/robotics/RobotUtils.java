/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.common.util.FakePlayer;

import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.IDockingStationProvider;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.misc.FakePlayerProvider;
import buildcraftreborn.robotics.item.ItemRobot;

public final class RobotUtils {
    private RobotUtils() {

    }

    /** @return The stations of a block entity: its own, or those on a pipe's sides. */
    public static List<DockingStation> getStations(@Nullable Object tile) {
        List<DockingStation> stations = new ArrayList<>();

        if (tile instanceof IDockingStationProvider provider) {
            DockingStation station = provider.getStation();
            if (station != null) {
                stations.add(station);
            }
        }

        if (tile instanceof IPipeHolder pipeTile) {
            for (Direction d : Direction.values()) {
                if (pipeTile.getPluggable(d) instanceof IDockingStationProvider pluggable) {
                    DockingStation station = pluggable.getStation();

                    if (station != null) {
                        stations.add(station);
                    }
                }
            }
        }

        return stations;
    }

    /** @return True if the pipe has a station on one of its sides (on the client too, where stations aren't kept). */
    public static boolean hasStation(@Nullable Object tile) {
        if (tile instanceof IDockingStationProvider) {
            return true;
        }
        if (tile instanceof IPipeHolder pipeTile) {
            for (Direction d : Direction.values()) {
                if (pipeTile.getPluggable(d) instanceof IDockingStationProvider) {
                    return true;
                }
            }
        }
        return false;
    }

    /** @return The board after (or before) the one of the robot stack, or the first (last) for anything else. */
    @Nullable
    public static RedstoneBoardRobotNBT getNextBoard(ItemStack stack, boolean reverse) {
        List<RedstoneBoardRobotNBT> boards = new ArrayList<>();
        for (RedstoneBoardNBT<?> board : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            if (board instanceof RedstoneBoardRobotNBT robotBoard) {
                boards.add(robotBoard);
            }
        }
        if (boards.isEmpty()) {
            return null;
        }
        if (stack.isEmpty() || !(stack.getItem() instanceof ItemRobot)) {
            return reverse ? boards.get(boards.size() - 1) : boards.get(0);
        }
        int index = boards.indexOf(ItemRobot.getRobotNBT(stack));
        if (index < 0) {
            return null;
        }
        int next = index + (reverse ? -1 : 1);
        return next >= 0 && next < boards.size() ? boards.get(next) : null;
    }

    /** @return The fake player robots act as, standing at the position. */
    public static FakePlayer getFakePlayer(EntityRobotBase robot, BlockPos pos) {
        return FakePlayerProvider.INSTANCE.getFakePlayer((ServerLevel) robot.level(), FakePlayerProvider.NULL_PROFILE,
            pos);
    }

    /** Drops a stack where the robot is, lasting the given number of ticks (7.1's {@code BlockUtils.dropItem}). */
    public static void dropItem(Level world, Vec3 pos, int lifespan, ItemStack stack) {
        if (stack.isEmpty() || world.isClientSide) {
            return;
        }
        ItemEntity entity = new ItemEntity(world, pos.x, pos.y, pos.z, stack.copy());
        entity.lifespan = lifespan;
        entity.setDefaultPickUpDelay();
        world.addFreshEntity(entity);
    }

    /** Uses the stack on the top of the block, as the robot's fake player (7.1's {@code BlockUtils.useItemOnBlock}).
     *
     * @return True if the item did something. */
    public static boolean useItemOnBlock(EntityRobotBase robot, ItemStack stack, BlockPos pos, Direction side) {
        if (stack.isEmpty() || !(robot.level() instanceof ServerLevel)) {
            return false;
        }
        FakePlayer player = getFakePlayer(robot, robot.blockPosition());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        try {
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos).relative(side, 0.5), side, pos, false);
            return stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction();
        } finally {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }
}
