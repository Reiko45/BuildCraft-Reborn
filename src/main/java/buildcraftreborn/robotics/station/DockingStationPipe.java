/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.station;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.IRequestProvider;
import buildcraftreborn.api.robots.RobotManager;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.StatementSlot;
import buildcraftreborn.api.transport.IInjectable;
import buildcraftreborn.api.transport.pipe.IFlowFluid;
import buildcraftreborn.api.transport.pipe.IFlowItems;
import buildcraftreborn.api.transport.pipe.IFlowPower;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.robotics.BCRoboticsStatements;
import buildcraftreborn.silicon.plug.PluggableGate;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourWood;

/** A docking station on one side of a pipe. The pipe decides what robots can do there: unload items into an item
 * pipe, load items from the inventory a wooden item pipe extracts from, load or unload fluids likewise, and recharge
 * on a kinesis pipe. The gates on the pipe control it with robot and station actions.
 * <p>
 * 1.21: robots unload items straight into the pipe (from the station's side); requests are read from the gates as in
 * 7.1. */
public class DockingStationPipe extends DockingStation implements IRequestProvider {

    private static final double ITEM_SPEED = 0.08;

    private final IInjectable injectablePipe = new IInjectable() {
        @Override
        public boolean canInjectItems(Direction from) {
            return true;
        }

        @Override
        public ItemStack injectItem(ItemStack stack, boolean doAdd, Direction from, @Nullable DyeColor color,
            double speed) {
            IPipeHolder holder = getPipe();
            if (holder == null || !(holder.getPipe().getFlow() instanceof IFlowItems flow)) {
                return stack;
            }
            if (doAdd) {
                flow.insertItemsForce(stack.copy(), side(), color, speed <= 0 ? ITEM_SPEED : speed);
            }
            return ItemStack.EMPTY;
        }
    };

    @Nullable
    private IPipeHolder pipe;

    public DockingStationPipe() {
        // Loading later from NBT - DO NOT TOUCH!
    }

    public DockingStationPipe(IPipeHolder iPipe, Direction side) {
        super(iPipe.getPipePos(), side);
        pipe = iPipe;
        world = iPipe.getPipeWorld();
    }

    public void setPipe(IPipeHolder holder) {
        this.pipe = holder;
        this.world = holder.getPipeWorld();
    }

    @Nullable
    public IPipeHolder getPipe() {
        if (pipe == null && world != null && world.isLoaded(getPos())) {
            BlockEntity tile = world.getBlockEntity(getPos());
            if (tile instanceof IPipeHolder holder) {
                pipe = holder;
            }
        }

        if (pipe != null && (pipe.getPipeTile().isRemoved() || pipe.getPipe() == null)) {
            pipe = null;
        }

        return pipe;
    }

    @Nullable
    private IPipe getPipeLogic() {
        IPipeHolder holder = getPipe();
        return holder == null ? null : holder.getPipe();
    }

    @Override
    public Iterable<StatementSlot> getActiveActions() {
        List<StatementSlot> actions = new ArrayList<>();
        IPipeHolder holder = getPipe();
        if (holder == null) {
            return actions;
        }
        for (Direction dir : Direction.values()) {
            if (holder.getPluggable(dir) instanceof PluggableGate gate) {
                actions.addAll(gate.logic.getActiveActions());
            }
        }
        return actions;
    }

    @Override
    @Nullable
    public IInjectable getItemOutput() {
        IPipe logic = getPipeLogic();
        if (logic == null || !(logic.getFlow() instanceof IFlowItems)) {
            return null;
        }

        return injectablePipe;
    }

    @Override
    public EnumPipePart getItemOutputSide() {
        return EnumPipePart.fromFacing(side().getOpposite());
    }

    @Nullable
    private Direction getWoodDirection(Class<?> flowClass) {
        IPipe logic = getPipeLogic();
        if (logic == null || !flowClass.isInstance(logic.getFlow())) {
            return null;
        }
        if (!(logic.getBehaviour() instanceof PipeBehaviourWood wood)) {
            return null;
        }
        return wood.getCurrentDir();
    }

    @Override
    @Nullable
    public IItemHandler getItemInput() {
        Direction dir = getWoodDirection(IFlowItems.class);
        if (dir == null) {
            return null;
        }
        BlockPos other = getPos().relative(dir);
        if (!world.isLoaded(other)) {
            return null;
        }
        return world.getCapability(Capabilities.ItemHandler.BLOCK, other, dir.getOpposite());
    }

    @Override
    public EnumPipePart getItemInputSide() {
        Direction dir = getWoodDirection(IFlowItems.class);
        return dir == null ? EnumPipePart.CENTER : EnumPipePart.fromFacing(dir.getOpposite());
    }

    @Override
    @Nullable
    public IFluidHandler getFluidInput() {
        Direction dir = getWoodDirection(IFlowFluid.class);
        if (dir == null) {
            return null;
        }
        BlockPos other = getPos().relative(dir);
        if (!world.isLoaded(other)) {
            return null;
        }
        return world.getCapability(Capabilities.FluidHandler.BLOCK, other, dir.getOpposite());
    }

    @Override
    public EnumPipePart getFluidInputSide() {
        Direction dir = getWoodDirection(IFlowFluid.class);
        return dir == null ? EnumPipePart.CENTER : EnumPipePart.fromFacing(dir.getOpposite());
    }

    @Override
    @Nullable
    public IFluidHandler getFluidOutput() {
        IPipe logic = getPipeLogic();
        if (logic == null || !(logic.getFlow() instanceof IFlowFluid)) {
            return null;
        }

        return logic.getFlow().getCapability(Capabilities.FluidHandler.BLOCK, null);
    }

    @Override
    public EnumPipePart getFluidOutputSide() {
        return EnumPipePart.CENTER;
    }

    @Override
    public boolean providesPower() {
        IPipe logic = getPipeLogic();
        return logic != null && logic.getFlow() instanceof IFlowPower;
    }

    @Override
    public IRequestProvider getRequestProvider() {
        IPipeHolder holder = getPipe();
        if (holder != null) {
            for (Direction dir : Direction.values()) {
                BlockEntity nearbyTile = holder.getNeighbourTile(dir);
                if (nearbyTile instanceof IRequestProvider provider) {
                    return provider;
                }
            }
        }
        return this;
    }

    @Override
    public boolean isInitialized() {
        return getPipeLogic() != null;
    }

    @Override
    public boolean take(EntityRobotBase robot) {
        boolean result = super.take(robot);
        if (result) {
            updateRender();
        }
        return result;
    }

    @Override
    public boolean takeAsMain(EntityRobotBase robot) {
        boolean result = super.takeAsMain(robot);
        if (result) {
            updateRender();
        }
        return result;
    }

    @Override
    public void unsafeRelease(EntityRobotBase robot) {
        super.unsafeRelease(robot);
        if (robotTaking() == null) {
            updateRender();
        }
    }

    /** Lets the station's pluggable know it should send its new state. */
    private void updateRender() {
        IPipeHolder holder = getPipe();
        if (holder != null && holder.getPluggable(side()) instanceof PluggableRobotStation station) {
            station.markStateDirty();
        }
    }

    @Override
    public void onChunkUnload() {
        pipe = null;
    }

    /** Removes this station from the registry (its pipe is gone). */
    public void removeFromRegistry() {
        if (world != null) {
            RobotManager.registryProvider.getRegistry(world).removeStation(this);
        }
    }

    // IRequestProvider: the station's own requests are the items in its gates' "request items" actions

    @Override
    public int getRequestsCount() {
        return 127;
    }

    @Override
    public ItemStack getRequest(int slot) {
        int facing = (slot & 0x70) >> 4;
        int action = (slot & 0xc) >> 2;
        int param = slot & 0x3;

        if (facing >= 6) {
            return ItemStack.EMPTY;
        }

        IPipeHolder holder = getPipe();
        if (holder == null) {
            return ItemStack.EMPTY;
        }
        PipePluggable plug = holder.getPluggable(Direction.from3DDataValue(facing));
        if (!(plug instanceof PluggableGate gate)) {
            return ItemStack.EMPTY;
        }

        List<IStatement> actions = gate.logic.getActions();
        if (actions.size() <= action) {
            return ItemStack.EMPTY;
        }

        if (actions.get(action) != BCRoboticsStatements.ACTION_STATION_REQUEST_ITEMS) {
            return ItemStack.EMPTY;
        }

        StatementSlot slotStmt = null;
        for (StatementSlot stmt : gate.logic.getActiveActions()) {
            if (stmt.statement == actions.get(action)) {
                slotStmt = stmt;
                break;
            }
        }
        if (slotStmt == null || slotStmt.parameters.length <= param || slotStmt.parameters[param] == null) {
            return ItemStack.EMPTY;
        }

        return slotStmt.parameters[param].getItemStack();
    }

    @Override
    public ItemStack offerItem(int slot, ItemStack stack) {
        return injectablePipe.injectItem(stack, true, side.getOpposite(), null, ITEM_SPEED);
    }
}
