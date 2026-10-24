/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.item;

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.energy.IEnergyStorage;

import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.events.RobotPlacementEvent;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.item.IItemSubtypes;
import buildcraftreborn.robotics.BCRoboticsItems;
import buildcraftreborn.robotics.entity.EntityRobot;

/** A robot as an item: its board and its stored power. Placed by using it on a robot station.
 * <p>
 * 1.21: the board and power are components ({@code buildcraftreborn:board} and {@code buildcraftreborn:robot_power},
 * in micro MJ); 1.12 kept them in NBT. Like 7.1's RF robots, a programmed robot item can be charged as an energy item
 * (FE, at the RF conversion rate) as well as in the charging table. A robot without a board stacks to 16 and can't be
 * placed. */
public class ItemRobot extends Item implements IItemSubtypes {

    public ItemRobot(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Nullable
    public EntityRobot createRobot(ItemStack stack, Level world) {
        RedstoneBoardRobotNBT robotNBT = getRobotNBT(stack);
        if (robotNBT == RedstoneBoardRegistry.instance.getEmptyRobotBoard()) {
            return null;
        }
        EntityRobot robot = new EntityRobot(world, robotNBT);
        robot.getBattery().addPower(getPower(stack), false);

        return robot;
    }

    public static RedstoneBoardRobotNBT getRobotNBT(ItemStack stack) {
        RedstoneBoardNBT<?> board = RedstoneBoardRegistry.instance.getRedstoneBoard(stack.get(BCRoboticsItems.BOARD.get()));
        if (board instanceof RedstoneBoardRobotNBT robotBoard) {
            return robotBoard;
        }
        return RedstoneBoardRegistry.instance.getEmptyRobotBoard();
    }

    public static long getPower(ItemStack stack) {
        return stack.getOrDefault(BCRoboticsItems.ROBOT_POWER.get(), 0L);
    }

    public static void setPower(ItemStack stack, long power) {
        stack.set(BCRoboticsItems.ROBOT_POWER.get(), Mth.clamp(power, 0, EntityRobotBase.MAX_POWER));
    }

    public static boolean hasBoard(ItemStack stack) {
        return getRobotNBT(stack) != RedstoneBoardRegistry.instance.getEmptyRobotBoard();
    }

    /** Adds power to a programmed robot item (the charging table and energy items).
     *
     * @return The power that didn't fit. */
    public static long receivePower(ItemStack stack, long microJoules, boolean simulate) {
        if (!hasBoard(stack)) {
            return microJoules;
        }
        long current = getPower(stack);
        long received = Math.max(0, Math.min(EntityRobotBase.MAX_POWER - current, microJoules));
        if (!simulate && received > 0) {
            setPower(stack, current + received);
        }
        return microJoules - received;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        RedstoneBoardRobotNBT boardNBT = getRobotNBT(stack);

        if (boardNBT != RedstoneBoardRegistry.instance.getEmptyRobotBoard()) {
            boardNBT.addInformation(stack, context, tooltip, flag);

            long energy = getPower(stack);
            int pct = (int) (energy * 100 / EntityRobotBase.MAX_POWER);
            Component enInfo;
            if (energy >= EntityRobotBase.MAX_POWER) {
                enInfo = Component.translatable("tip.gate.fullcharge");
            } else if (energy == 0) {
                enInfo = Component.translatable("tip.gate.nocharge");
            } else {
                enInfo = Component.literal(pct + "% ").append(Component.translatable("tip.gate.charged"));
            }
            ChatFormatting colour = pct >= 80 ? ChatFormatting.GREEN : pct >= 50 ? ChatFormatting.YELLOW
                : pct >= 30 ? ChatFormatting.GOLD : pct >= 20 ? ChatFormatting.RED : ChatFormatting.DARK_RED;
            tooltip.add(enInfo.copy().withStyle(colour));
        }
    }

    public static ItemStack createRobotStack(RedstoneBoardRobotNBT board, long power) {
        ItemStack robot = new ItemStack(BCRoboticsItems.ROBOT.get());
        if (board != RedstoneBoardRegistry.instance.getEmptyRobotBoard()) {
            robot.set(BCRoboticsItems.BOARD.get(), board.getID());
            robot.set(DataComponents.MAX_STACK_SIZE, 1);
            setPower(robot, power);
        }
        return robot;
    }

    @Override
    public void addSubtypes(Consumer<ItemStack> output) {
        output.accept(createRobotStack(RedstoneBoardRegistry.instance.getEmptyRobotBoard(), 0));

        for (RedstoneBoardNBT<?> boardNBT : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            if (boardNBT instanceof RedstoneBoardRobotNBT robotNBT) {
                output.accept(createRobotStack(robotNBT, 0));
                output.accept(createRobotStack(robotNBT, EntityRobotBase.MAX_POWER));
            }
        }
    }

    /** The energy capability of a robot item: FE converted to and from micro MJ. */
    public static IEnergyStorage createEnergyStorage(ItemStack stack) {
        return new IEnergyStorage() {
            private long mjPerRf() {
                return MjAPI.getRfConversion().mjPerRf;
            }

            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                if (!hasBoard(stack) || maxReceive <= 0) {
                    return 0;
                }
                long space = EntityRobotBase.MAX_POWER - getPower(stack);
                int rf = (int) Math.min(maxReceive, space / mjPerRf());
                if (!simulate && rf > 0) {
                    setPower(stack, getPower(stack) + rf * mjPerRf());
                }
                return Math.max(0, rf);
            }

            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                if (!hasBoard(stack) || maxExtract <= 0) {
                    return 0;
                }
                int rf = (int) Math.min(maxExtract, getPower(stack) / mjPerRf());
                if (!simulate && rf > 0) {
                    setPower(stack, getPower(stack) - rf * mjPerRf());
                }
                return Math.max(0, rf);
            }

            @Override
            public int getEnergyStored() {
                return (int) Math.min(Integer.MAX_VALUE, getPower(stack) / mjPerRf());
            }

            @Override
            public int getMaxEnergyStored() {
                return hasBoard(stack) ? (int) Math.min(Integer.MAX_VALUE, EntityRobotBase.MAX_POWER / mjPerRf()) : 0;
            }

            @Override
            public boolean canExtract() {
                return hasBoard(stack);
            }

            @Override
            public boolean canReceive() {
                return hasBoard(stack);
            }
        };
    }

    /** @return True if the player may place this robot (fires the placement events). */
    public static boolean canPlace(Player player, ItemStack stack) {
        return !NeoForge.EVENT_BUS.post(new RobotPlacementEvent(player, getRobotNBT(stack).getID())).isCanceled();
    }
}
