/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.robots;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.core.IFluidHandlerAdv;
import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;

/** A robot: a small flying entity that runs the program on its redstone board, docks at stations on pipes and
 * recharges there. Its inventory and tank are offered through {@link IItemHandler} and {@link IFluidHandlerAdv}.
 * <p>
 * 1.21: a {@link Mob}, and power is in micro MJ. A full robot holds 10,000 MJ, the 100,000 RF it held in 7.1 (at 10 RF
 * to the MJ). */
public abstract class EntityRobotBase extends Mob implements IItemHandler, IFluidHandlerAdv {

    public static final long MAX_POWER = 10_000 * MjAPI.MJ;
    public static final long SAFETY_POWER = MAX_POWER / 5;
    public static final long SHUTDOWN_POWER = 0;
    public static final long NULL_ROBOT_ID = Long.MAX_VALUE;

    protected EntityRobotBase(EntityType<? extends EntityRobotBase> type, Level level) {
        super(type, level);
    }

    public abstract void setItemInUse(ItemStack stack);

    public abstract void setItemActive(boolean b);

    public abstract boolean isMoving();

    @Nullable
    public abstract DockingStation getLinkedStation();

    public abstract RedstoneBoardRobot getBoard();

    public abstract void aimItemAt(float yaw, float pitch);

    public abstract void aimItemAt(BlockPos pos);

    public abstract float getAimYaw();

    public abstract float getAimPitch();

    public long getPower() {
        return getBattery().getStored();
    }

    public abstract MjBattery getBattery();

    @Nullable
    public abstract DockingStation getDockingStation();

    public abstract void dock(DockingStation station);

    public abstract void undock();

    @Nullable
    public abstract IZone getZoneToWork();

    @Nullable
    public abstract IZone getZoneToLoadUnload();

    public abstract boolean containsItems();

    public abstract boolean hasFreeSlot();

    public abstract void unreachableEntityDetected(Entity entity);

    public abstract boolean isKnownUnreachable(Entity entity);

    public abstract long getRobotId();

    public abstract IRobotRegistry getRegistry();

    public abstract void releaseResources();

    public abstract void onChunkUnload();

    /** Offers a stack from a block entity (usually a pipe) to the robot's current AI.
     *
     * @return What is left over. */
    public abstract ItemStack receiveItem(BlockEntity tile, ItemStack stack);

    public abstract void setMainStation(DockingStation station);
}
