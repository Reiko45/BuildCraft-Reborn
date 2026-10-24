/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.behaviour;

import buildcraftreborn.lib.net.PacketBufferBC;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.LogicalSide;
import net.minecraft.core.HolderLookup;
import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import net.neoforged.neoforge.common.util.FakePlayer; // TODO port

import buildcraftreborn.lib.misc.FakePlayerProvider;
import buildcraftreborn.api.mj.IMjConnector;
import buildcraftreborn.api.mj.IMjRedstoneReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.pipe.IFlowItems;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.api.transport.pipe.PipeEventActionActivate;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeEventItem;
import buildcraftreborn.api.transport.pipe.PipeEventStatement;
import buildcraftreborn.api.transport.pipe.PipeFlow;
import buildcraftreborn.api.transport.pluggable.PipePluggable;

import buildcraftreborn.lib.misc.BlockUtil;
import buildcraftreborn.lib.misc.InventoryUtil;
import buildcraftreborn.lib.misc.MessageUtil;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.misc.StackUtil;

import buildcraftreborn.transport.BCTransportStatements;

public class PipeBehaviourStripes extends PipeBehaviour implements IStripesActivator, IMjRedstoneReceiver {
    private final MjBattery battery = new MjBattery(256 * MjAPI.MJ);

    @Nullable
    public Direction direction = null;
    private int progress;

    public PipeBehaviourStripes(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourStripes(IPipe pipe, CompoundTag nbt, HolderLookup.Provider registries) {
        super(pipe, nbt, registries);
        battery.deserializeNBT(nbt.getCompound("battery"));
        setDirection(NBTUtilBC.readEnum(nbt.get("direction"), Direction.class));
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = super.writeToNbt(registries);
        nbt.put("battery", battery.serializeNBT());
        nbt.put("direction", NBTUtilBC.writeEnum(direction));
        return nbt;
    }

    @Override
    public void readPayload(PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        direction = MessageUtil.readEnumOrNull(buffer, Direction.class);
    }

    @Override
    public void writePayload(PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(buffer, side);
        MessageUtil.writeEnumOrNull(buffer, direction);
    }

    // Sides

    private void setDirection(@Nullable Direction newValue) {
        if (direction != newValue) {
            direction = newValue;
            // (No level yet while the pipe loads from NBT)
            if (pipe.getHolder().getPipeWorld() != null && !pipe.getHolder().getPipeWorld().isClientSide) {
                pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
            }
        }
    }

    // Actions

    @PipeEventHandler
    public void addInternalActions(PipeEventStatement.AddActionInternal event) {
        for (Direction face : Direction.values()) {
            if (!pipe.isConnected(face)) {
                PipePluggable plug = pipe.getHolder().getPluggable(face);
                if (plug == null || !plug.isBlocking()) {
                    event.actions.add(BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]);
                }
            }
        }
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        for (Direction face : Direction.values()) {
            if (event.action == BCTransportStatements.ACTION_PIPE_DIRECTION[face.ordinal()]) {
                setDirection(face);
            }
        }
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return battery.getCapacity() - battery.getStored();
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return battery.addPowerChecking(microJoules, simulate);
    }

    // Stripes

    @Override
    public boolean canConnect(Direction face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourStripes);
    }

    @Override
    public void onTick() {
        Level level = pipe.getHolder().getPipeWorld();
        BlockPos pos = pipe.getHolder().getPipePos();
        if (level.isClientSide) {
            return;
        }
        if (direction == null || pipe.isConnected(direction)) {
            int sides = 0;
            Direction dir = null;
            for (Direction face : Direction.values()) {
                if (pipe.isConnected(face)) {
                    sides++;
                    dir = face;
                }
            }
            if (sides == 1) {
                setDirection(dir.getOpposite());
            } else {
                setDirection(null);
            }
        }
        battery.tick(level, pipe.getHolder().getPipePos());
        if (direction != null) {
            BlockPos offset = pos.relative(direction);
            long target = BlockUtil.computeBlockBreakPower(level, offset);
            if (target > 0) {
                int offsetHash = offset.hashCode();
                if (progress < target) {
                    progress += battery.extractPower(0, Math.min(target - progress, MjAPI.MJ * 10));
                    if (progress > 0) {
                        level.destroyBlockProgress(offsetHash, offset, (int) (progress * 9 / target));
                    }
                } else {
                    BlockUtil.breakBlockAndGetDrops(
                        (ServerLevel) level,
                        offset,
                        new ItemStack(Items.DIAMOND_PICKAXE),
                        pipe.getHolder().getOwner()
                    ).ifPresent(stacks -> stacks.forEach(stack -> sendItem(stack, direction)));
                    progress = 0;
                }
            }
        } else {
            progress = 0;
        }
    }

    @PipeEventHandler
    public void onDrop(PipeEventItem.Drop event) {
        if (direction == null) {
            return;
        }
        IPipeHolder holder = pipe.getHolder();
        Level level = holder.getPipeWorld();
        BlockPos pos = holder.getPipePos();
        FakePlayer player = FakePlayerProvider.INSTANCE.getFakePlayer((ServerLevel) level, holder.getOwner(), pos);
        player.getInventory().clearContent();
        // set the main hand of the fake player to the stack
        player.getInventory().setItem(player.getInventory().selected, event.getStack());
        if (PipeApi.stripeRegistry.handleItem(level, pos, direction, event.getStack(), player, this)) {
            event.setStack(ItemStack.EMPTY);
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    sendItem(stack, direction);
                }
            }
        }
    }

    @Override
    public void dropItem(ItemStack stack, Direction direction) {
        InventoryUtil.drop(pipe.getHolder().getPipeWorld(), pipe.getHolder().getPipePos(), stack);
    }

    @Override
    public boolean sendItem(ItemStack stack, Direction from) {
        PipeFlow flow = pipe.getFlow();
        if (flow instanceof IFlowItems) {
            ((IFlowItems) flow).insertItemsForce(stack, from, null, 0.02);
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction facing) {
        if (capability == MjAPI.CAP_REDSTONE_RECEIVER) {
            return (T) this;
        }
        if (capability == MjAPI.CAP_RECEIVER) {
            return (T) this;
        }
        if (capability == MjAPI.CAP_CONNECTOR) {
            return (T) this;
        }
        return super.getCapability(capability, facing);
    }
}
