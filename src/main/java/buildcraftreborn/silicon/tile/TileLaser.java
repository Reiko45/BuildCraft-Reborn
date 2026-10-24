/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.mj.ILaserTarget;
import buildcraftreborn.api.mj.ILaserTargetBlock;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.lib.block.ILocalBlockUpdateSubscriber;
import buildcraftreborn.lib.block.LocalBlockUpdateNotifier;
import buildcraftreborn.lib.client.render.DetachedRenderer.IDetachedRenderer;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.misc.VolumeUtil;
import buildcraftreborn.lib.misc.data.AverageLong;
import buildcraftreborn.lib.mj.MjBatteryReceiver;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.block.BlockLaser;
import buildcraftreborn.silicon.client.render.AdvDebuggerLaser;

/** The laser: takes MJ and beams it at laser tables in a cone in front of it (6 blocks), picking a table that needs
 * power at random every so often. */
public class TileLaser extends TileBC_Neptune implements ITickable, IDebuggable, ILocalBlockUpdateSubscriber {
    public static final int TARGETING_RANGE = 6;
    /** How often (in ticks) the client moves the beam's end, and the server picks a new target. */
    private static final int CLIENT_MOVE_INTERVAL = 5, SERVER_MOVE_INTERVAL = 10;

    private final List<BlockPos> targetPositions = new ArrayList<>();
    private BlockPos targetPos;
    /** Client only: where the beam ends. */
    public Vec3 laserPos;
    private boolean worldHasUpdated = true;
    private int clientMoveTimer, serverMoveTimer;

    private final AverageLong avgPower = new AverageLong(100);
    private long averageClient;
    private final MjBattery battery;

    public TileLaser(BlockPos pos, BlockState state) {
        super(BCSiliconBlocks.LASER_TILE.get(), pos, state);
        battery = new MjBattery(1024 * MjAPI.MJ);
        caps.addMj(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    // ILocalBlockUpdateSubscriber

    @Override
    public int getUpdateRange() {
        return TARGETING_RANGE;
    }

    @Override
    public BlockPos getSubscriberPos() {
        return getBlockPos();
    }

    @Override
    public void setWorldUpdated(Level level, BlockPos eventPos, BlockState newState) {
        this.worldHasUpdated = true;
    }

    // Targeting

    private void findPossibleTargets() {
        targetPositions.clear();
        BlockState state = level.getBlockState(worldPosition);
        if (!(state.getBlock() instanceof BlockLaser laser)) {
            return;
        }
        Direction face = state.getValue(laser.getFacingProperty());
        VolumeUtil.iterateCone(level, worldPosition, face, TARGETING_RANGE, true, (w, s, p, visible) -> {
            if (visible && level.getBlockState(p).getBlock() instanceof ILaserTargetBlock
                && level.getBlockEntity(p) instanceof ILaserTarget) {
                targetPositions.add(p);
            }
        });
    }

    private void randomlyChooseTargetPos() {
        List<BlockPos> targetsNeedingPower = new ArrayList<>();
        for (BlockPos position : targetPositions) {
            if (isPowerNeededAt(position)) {
                targetsNeedingPower.add(position);
            }
        }
        if (targetsNeedingPower.isEmpty()) {
            targetPos = null;
            return;
        }
        targetPos = targetsNeedingPower.get(level.random.nextInt(targetsNeedingPower.size()));
    }

    private boolean isPowerNeededAt(@Nullable BlockPos position) {
        return position != null && level.getBlockEntity(position) instanceof ILaserTarget target
            && target.getRequiredLaserPower() > 0;
    }

    @Nullable
    private ILaserTarget getTarget() {
        return targetPos != null && level.getBlockEntity(targetPos) instanceof ILaserTarget target ? target : null;
    }

    private void updateLaser() {
        if (targetPos != null) {
            laserPos = Vec3.atLowerCornerOf(targetPos).add((5 + level.random.nextInt(6) + 0.5) / 16D, 9 / 16D,
                (5 + level.random.nextInt(6) + 0.5) / 16D);
        } else {
            laserPos = null;
        }
    }

    @Nullable
    public BlockPos getTargetPos() {
        return targetPos;
    }

    public long getAverageClient() {
        return averageClient;
    }

    public long getMaxPowerPerTick() {
        return 4 * MjAPI.MJ;
    }

    @Override
    public void update() {
        if (level.isClientSide) {
            // Move the end of the beam around the table now and then
            if (++clientMoveTimer >= CLIENT_MOVE_INTERVAL || laserPos == null) {
                clientMoveTimer = 0;
                updateLaser();
            }
            return;
        }

        avgPower.tick();

        if (worldHasUpdated) {
            findPossibleTargets();
            worldHasUpdated = false;
        }

        if (!isPowerNeededAt(targetPos)) {
            targetPos = null;
        }

        if (++serverMoveTimer >= SERVER_MOVE_INTERVAL || !isPowerNeededAt(targetPos)) {
            serverMoveTimer = 0;
            randomlyChooseTargetPos();
        }

        ILaserTarget target = getTarget();
        if (target != null) {
            long max = getMaxPowerPerTick();
            max *= battery.getStored() + max;
            max /= battery.getCapacity() / 2;
            max = Math.min(Math.min(max, getMaxPowerPerTick()), target.getRequiredLaserPower());
            long power = battery.extractPower(0, max);
            long excess = target.receiveLaserPower(power);
            if (excess > 0) {
                battery.addPowerChecking(excess, false);
            }
            avgPower.push(power - excess);
        } else {
            avgPower.clear();
        }

        // (1.12 sent this every tick too)
        sendNetworkUpdate(NET_RENDER_DATA);
        setChanged();
    }

    // Saving

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("battery", battery.serializeNBT());
        if (targetPos != null) {
            nbt.put("target_pos", NBTUtilBC.writeBlockPos(targetPos));
        }
        avgPower.writeToNbt(nbt, "average_power");
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        battery.deserializeNBT(nbt.getCompound("battery"));
        targetPos = NBTUtilBC.readBlockPos(nbt.get("target_pos"));
        avgPower.readFromNbt(nbt, "average_power");
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER && id == NET_RENDER_DATA) {
            battery.writeToBuffer(buffer);
            buffer.writeBoolean(targetPos != null);
            if (targetPos != null) {
                buffer.writeBlockPos(targetPos);
            }
            buffer.writeLong((long) avgPower.getAverage());
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT && id == NET_RENDER_DATA) {
            battery.readFromBuffer(buffer);
            BlockPos newTarget = buffer.readBoolean() ? buffer.readBlockPos() : null;
            if (newTarget == null || !newTarget.equals(targetPos)) {
                targetPos = newTarget;
                updateLaser();
            }
            averageClient = buffer.readLong();
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("target = " + targetPos);
        left.add("laser = " + laserPos);
        left.add("average = " + LocaleUtil.localizeMjFlow(averageClient == 0 ? (long) avgPower.getAverage()
            : averageClient).getString());
    }

    // Lifecycle

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            LocalBlockUpdateNotifier.instance(level).registerSubscriberForUpdateNotifications(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            LocalBlockUpdateNotifier.instance(level).removeSubscriberFromUpdateNotifications(this);
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            LocalBlockUpdateNotifier.instance(level).removeSubscriberFromUpdateNotifications(this);
        }
    }

    /** Large enough for the beam to reach any target. */
    public AABB getRenderBoundingBox() {
        return new AABB(worldPosition).inflate(TARGETING_RANGE + 1);
    }

    @SuppressWarnings("unused")
    private static boolean isLaserTarget(BlockEntity tile) {
        return tile instanceof ILaserTarget;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public IDetachedRenderer getDebugRenderer() {
        return new AdvDebuggerLaser(this);
    }
}
