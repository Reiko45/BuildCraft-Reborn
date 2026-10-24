/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.station;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.render.EnumRenderLayer;
import buildcraftreborn.api.events.RobotEvent;
import buildcraftreborn.api.mj.IMjConnector;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.IDockingStationProvider;
import buildcraftreborn.api.robots.IRobotRegistry;
import buildcraftreborn.api.robots.RobotManager;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeEventTileState;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;
import buildcraftreborn.lib.misc.SoundUtil;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.robotics.BCRoboticsItems;
import buildcraftreborn.robotics.entity.EntityRobot;
import buildcraftreborn.robotics.item.ItemRobot;

/** The robot station (1.12: docking station) pluggable. The server keeps a {@link DockingStationPipe} for it in the
 * level's robot registry; robots are placed by using a robot on it, and a docked robot charges from a kinesis pipe.
 * <p>
 * 1.21: the station registers itself on its first tick (1.12: when the pipe validated it) and leaves the registry
 * when it is removed or its pipe is. It takes power from the pipe as an {@link IMjReceiver}. Placing a robot on a
 * station is done here rather than in the robot item, as the pipe handles clicks on its pluggables. */
public class PluggableRobotStation extends PipePluggable implements IDockingStationProvider, IDebuggable {
    public enum EnumRobotStationState {
        NONE,
        AVAILABLE,
        RESERVED,
        LINKED;

        public static final EnumRobotStationState[] VALUES = values();

        public String getTextureSuffix() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private static final AABB[] BOXES = new AABB[6];

    static {
        // 7.1: a plate a quarter of a block out from the pipe; 8.0's model adds a stem out to the block's face
        AABB west = new AABB(0, 0.25, 0.25, 0.25, 0.75, 0.75);
        BOXES[Direction.WEST.ordinal()] = west;
        BOXES[Direction.EAST.ordinal()] = new AABB(0.75, 0.25, 0.25, 1, 0.75, 0.75);
        BOXES[Direction.DOWN.ordinal()] = new AABB(0.25, 0, 0.25, 0.75, 0.25, 0.75);
        BOXES[Direction.UP.ordinal()] = new AABB(0.25, 0.75, 0.25, 0.75, 1, 0.75);
        BOXES[Direction.NORTH.ordinal()] = new AABB(0.25, 0.25, 0, 0.75, 0.75, 0.25);
        BOXES[Direction.SOUTH.ordinal()] = new AABB(0.25, 0.25, 0.75, 0.75, 0.75, 1);
    }

    private EnumRobotStationState renderState = EnumRobotStationState.NONE;
    @Nullable
    private DockingStationPipe station;
    private boolean isValid = false;
    private boolean stateDirty = false;

    private final IMjReceiver receiver = new IMjReceiver() {
        @Override
        public boolean canConnect(IMjConnector other) {
            return true;
        }

        @Override
        public long getPowerRequested() {
            EntityRobot robot = getDockedRobot();
            return robot == null ? 0 : EntityRobotBase.MAX_POWER - robot.getBattery().getStored();
        }

        @Override
        public long receivePower(long microJoules, boolean simulate) {
            EntityRobot robot = getDockedRobot();
            if (robot == null) {
                return microJoules;
            }
            return robot.receivePower(microJoules, simulate);
        }

        @Override
        public boolean canReceive() {
            return getDockedRobot() != null;
        }
    };

    public PluggableRobotStation(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    public PluggableRobotStation(PluggableDefinition definition, IPipeHolder holder, Direction side, CompoundTag nbt,
        HolderLookup.Provider registries) {
        super(definition, holder, side);
    }

    public PluggableRobotStation(PluggableDefinition definition, IPipeHolder holder, Direction side,
        PacketBufferBC buffer) {
        super(definition, holder, side);
        readState(buffer);
    }

    // Networking

    @Override
    public void writeCreationPayload(PacketBufferBC buffer) {
        super.writeCreationPayload(buffer);
        refreshRenderState();
        buffer.writeByte(renderState.ordinal());
    }

    @Override
    public void writePayload(PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(buffer, side);
        if (side == LogicalSide.SERVER) {
            refreshRenderState();
            buffer.writeByte(renderState.ordinal());
        }
    }

    @Override
    public void readPayload(PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            EnumRobotStationState old = renderState;
            readState(buffer);
            if (old != renderState) {
                holder.scheduleRenderUpdate();
            }
        }
    }

    private void readState(PacketBufferBC buffer) {
        int num = buffer.readByte();
        renderState = EnumRobotStationState.VALUES[(num % 4 + 4) % 4];
    }

    // Station

    @Override
    @Nullable
    public DockingStation getStation() {
        return station;
    }

    @Nullable
    private EntityRobot getDockedRobot() {
        if (station != null && station.robotTaking() instanceof EntityRobot robot
            && robot.getDockingStation() == station) {
            return robot;
        }
        return null;
    }

    private void validate() {
        Level world = holder.getPipeWorld();
        if (!isValid && !world.isClientSide) {
            IRobotRegistry registry = RobotManager.registryProvider.getRegistry(world);
            DockingStation existing = registry.getStation(holder.getPipePos(), side);

            if (existing instanceof DockingStationPipe pipeStation) {
                station = pipeStation;
                station.setPipe(holder);
            } else {
                if (existing != null) {
                    registry.removeStation(existing);
                }
                station = new DockingStationPipe(holder, side);
                registry.registerStation(station);
            }

            isValid = true;
            stateDirty = true;
        }
    }

    private void invalidate() {
        if (station != null && !holder.getPipeWorld().isClientSide) {
            station.removeFromRegistry();
            station = null;
        }
        isValid = false;
    }

    @Override
    public void onTick() {
        if (holder.getPipeWorld().isClientSide) {
            return;
        }
        if (!isValid) {
            validate();
        }
        EnumRobotStationState old = renderState;
        refreshRenderState();
        if (stateDirty || old != renderState) {
            stateDirty = false;
            scheduleNetworkUpdate();
        }
    }

    /** Called by the station when a robot takes or releases it. */
    void markStateDirty() {
        stateDirty = true;
    }

    @Override
    public void onRemove() {
        invalidate();
    }

    @PipeEventHandler
    public void onInvalidate(PipeEventTileState.Invalidate event) {
        invalidate();
    }

    @PipeEventHandler
    public void onChunkUnload(PipeEventTileState.ChunkUnload event) {
        if (station != null) {
            station.onChunkUnload();
        }
        // The station stays in the registry (so robots stay linked); it is found again when the chunk loads.
        station = null;
        isValid = false;
    }

    private void refreshRenderState() {
        if (station == null) {
            return;
        }
        renderState = station.isTaken() ? (station.isMainStation() ? EnumRobotStationState.LINKED
            : EnumRobotStationState.RESERVED) : EnumRobotStationState.AVAILABLE;
    }

    public EnumRobotStationState getRenderState() {
        return renderState;
    }

    // PipePluggable

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCRoboticsItems.ROBOT_STATION.get());
    }

    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getInternalCapability(BlockCapability<T, @Nullable Direction> cap) {
        if (cap == MjAPI.CAP_RECEIVER || cap == MjAPI.CAP_CONNECTOR) {
            return (T) receiver;
        }
        return null;
    }

    @Override
    @Nullable
    public PluggableModelKey getModelRenderKey(EnumRenderLayer layer) {
        if (layer == EnumRenderLayer.CUTOUT) {
            // (client only: named in full, as this class also loads on servers)
            return buildcraftreborn.robotics.client.model.KeyPlugRobotStation.create(side, renderState);
        }
        return null;
    }

    @Override
    public boolean onPluggableActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ) {
        InteractionHand hand = player.getMainHandItem().getItem() instanceof ItemRobot ? InteractionHand.MAIN_HAND
            : player.getOffhandItem().getItem() instanceof ItemRobot ? InteractionHand.OFF_HAND : null;
        if (hand == null) {
            return false;
        }
        Level world = holder.getPipeWorld();
        if (world.isClientSide) {
            return true;
        }
        ItemStack held = player.getItemInHand(hand);
        if (station == null) {
            validate();
        }
        if (station == null || station.isTaken() || !ItemRobot.hasBoard(held) || !ItemRobot.canPlace(player, held)) {
            return true;
        }
        EntityRobot robot = ((ItemRobot) held.getItem()).createRobot(held, world);
        if (robot == null) {
            return true;
        }
        if (NeoForge.EVENT_BUS.post(new RobotEvent.Place(robot, player)).isCanceled()) {
            return true;
        }

        robot.setUniqueRobotId(robot.getRegistry().getNextRobotId());

        Vec3 pos = EntityRobot.dockedPosition(station);
        robot.setPos(pos.x, pos.y, pos.z);
        station.takeAsMain(robot);
        robot.dock(robot.getLinkedStation());
        world.addFreshEntity(robot);
        SoundUtil.playBlockPlace(world, holder.getPipePos());

        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        return true;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("State: " + getRenderState());
        if (station == null) {
            left.add("PluggableRobotStation: No station found!");
        } else {
            left.add("Docking Station (side " + this.side.name() + ", " + renderState.name() + ")");
            if (station.robotTaking() instanceof IDebuggable debuggable) {
                debuggable.getDebugInfo(left, right, side);
            }
        }
    }
}
