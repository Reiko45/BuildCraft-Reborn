/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.robots;

import java.util.Arrays;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.statements.StatementSlot;
import buildcraftreborn.api.transport.IInjectable;

/** A place a robot can dock at: one side of a block (in BuildCraft, a robot station on a pipe). A robot links to one
 * station as its "main" (home) station, and can take other stations for a while to load or unload at them.
 * <p>
 * 1.21: positions are {@link BlockPos} and {@link Direction}, item input is an {@link IItemHandler}. */
public abstract class DockingStation {
    private static final Logger LOGGER = LogUtils.getLogger();

    public Direction side;
    public Level world;

    private long robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
    private EntityRobotBase robotTaking;

    private boolean linkIsMain = false;

    private BlockPos pos;

    public DockingStation(BlockPos iIndex, Direction iSide) {
        pos = iIndex;
        side = iSide;
    }

    public DockingStation() {}

    public boolean isMainStation() {
        return linkIsMain;
    }

    public BlockPos getPos() {
        return pos;
    }

    public Direction side() {
        return side;
    }

    @Nullable
    public EntityRobotBase robotTaking() {
        if (robotTakingId == EntityRobotBase.NULL_ROBOT_ID) {
            return null;
        } else if (robotTaking == null) {
            robotTaking = RobotManager.registryProvider.getRegistry(world).getLoadedRobot(robotTakingId);
        }

        return robotTaking;
    }

    public void invalidateRobotTakingEntity() {
        robotTaking = null;
    }

    public long linkedId() {
        return robotTakingId;
    }

    public boolean takeAsMain(EntityRobotBase robot) {
        if (robotTakingId == EntityRobotBase.NULL_ROBOT_ID) {
            IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
            linkIsMain = true;
            robotTaking = robot;
            robotTakingId = robot.getRobotId();
            registry.registryMarkDirty();
            robot.setMainStation(this);
            registry.take(this, robot.getRobotId());

            return true;
        } else {
            return robotTakingId == robot.getRobotId();
        }
    }

    public boolean take(EntityRobotBase robot) {
        if (robotTaking == null) {
            IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
            linkIsMain = false;
            robotTaking = robot;
            robotTakingId = robot.getRobotId();
            registry.registryMarkDirty();
            registry.take(this, robot.getRobotId());

            return true;
        } else {
            return robot.getRobotId() == robotTakingId;
        }
    }

    public void release(EntityRobotBase robot) {
        if (robotTaking == robot && !linkIsMain) {
            IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
            unsafeRelease(robot);
            registry.registryMarkDirty();
            registry.release(this, robot.getRobotId());
        }
    }

    /** Same a release but doesn't clear the registry (presumably called from the registry). */
    public void unsafeRelease(EntityRobotBase robot) {
        if (robotTaking == robot) {
            linkIsMain = false;
            robotTaking = null;
            robotTakingId = EntityRobotBase.NULL_ROBOT_ID;
        }
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.putIntArray("pos", new int[] { getPos().getX(), getPos().getY(), getPos().getZ() });
        nbt.putByte("side", (byte) side.ordinal());
        nbt.putBoolean("isMain", linkIsMain);
        nbt.putLong("robotId", robotTakingId);
    }

    public void readFromNBT(CompoundTag nbt) {
        int[] array = nbt.getIntArray("pos");
        if (array.length == 3) {
            pos = new BlockPos(array[0], array[1], array[2]);
        } else if (array.length != 0) {
            LOGGER.warn("Found an integer array that was not the right length! ({})", Arrays.toString(array));
        } else {
            LOGGER.warn("Did not find any integer positions! This is a bug!");
        }

        side = Direction.from3DDataValue(nbt.getByte("side"));
        linkIsMain = nbt.getBoolean("isMain");
        robotTakingId = nbt.getLong("robotId");
    }

    public boolean isTaken() {
        return robotTakingId != EntityRobotBase.NULL_ROBOT_ID;
    }

    public long robotIdTaking() {
        return robotTakingId;
    }

    public BlockPos index() {
        return pos;
    }

    @Override
    public String toString() {
        return "{" + pos + ", " + side + " :" + robotTakingId + "}";
    }

    public boolean linkIsDocked() {
        if (robotTaking() != null) {
            return robotTaking().getDockingStation() == this;
        } else {
            return false;
        }
    }

    public boolean canRelease() {
        return !isMainStation() && !linkIsDocked();
    }

    public boolean isInitialized() {
        return true;
    }

    /** @return The actions of the gates that control this station. */
    public abstract Iterable<StatementSlot> getActiveActions();

    /** @return Where robots unload items into, or null if they can't. */
    @Nullable
    public IInjectable getItemOutput() {
        return null;
    }

    public EnumPipePart getItemOutputSide() {
        return EnumPipePart.CENTER;
    }

    /** @return Where robots load items from, or null if they can't. */
    @Nullable
    public IItemHandler getItemInput() {
        return null;
    }

    public EnumPipePart getItemInputSide() {
        return EnumPipePart.CENTER;
    }

    @Nullable
    public IFluidHandler getFluidOutput() {
        return null;
    }

    public EnumPipePart getFluidOutputSide() {
        return EnumPipePart.CENTER;
    }

    @Nullable
    public IFluidHandler getFluidInput() {
        return null;
    }

    public EnumPipePart getFluidInputSide() {
        return EnumPipePart.CENTER;
    }

    public boolean providesPower() {
        return false;
    }

    @Nullable
    public IRequestProvider getRequestProvider() {
        return null;
    }

    public void onChunkUnload() {

    }
}
