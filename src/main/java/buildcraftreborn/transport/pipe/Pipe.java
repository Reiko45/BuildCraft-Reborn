/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe;

import java.io.IOException;
import java.util.EnumMap;
import java.util.List;


import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.api.transport.pipe.ICustomPipeConnection;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.api.transport.pipe.PipeConnectionAPI;
import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.api.transport.pipe.PipeEventConnectionChange;
import buildcraftreborn.api.transport.pipe.PipeFaceTex;
import buildcraftreborn.api.transport.pipe.PipeFlow;
import buildcraftreborn.api.transport.pluggable.PipePluggable;

import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.misc.MessageUtil;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.net.PacketBufferBC;

import buildcraftreborn.transport.client.model.key.PipeModelKey;

/** A pipe: its definition, behaviour and flow, plus which sides it connects to. Lives in a {@link IPipeHolder}. */
public final class Pipe implements IPipe, IDebuggable {
    private static final float DEFAULT_CONNECTION_DISTANCE = 0.25f;

    public final IPipeHolder holder;
    public final PipeDefinition definition;
    public final PipeBehaviour behaviour;
    public final PipeFlow flow;
    private DyeColor colour = null;
    private boolean updateMarked = true;
    private final EnumMap<Direction, Float> connected = new EnumMap<>(Direction.class);
    private final EnumMap<Direction, ConnectedType> types = new EnumMap<>(Direction.class);

    private PipeModelKey lastModel;

    public Pipe(IPipeHolder holder, PipeDefinition definition) {
        this.holder = holder;
        this.definition = definition;
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        this.flow = definition.flowType.creator.createFlow(this);
    }

    // read + write

    public Pipe(IPipeHolder holder, CompoundTag nbt, HolderLookup.Provider registries)
        throws InvalidInputDataException {
        this.holder = holder;
        this.colour = NBTUtilBC.readEnum(nbt.get("col"), DyeColor.class);
        this.definition = PipeRegistry.INSTANCE.loadDefinition(nbt.getString("def"));
        if (!definition.canBeColoured) {
            colour = null;
        }
        this.behaviour = definition.logicLoader.loadBehaviour(this, nbt.getCompound("beh"), registries);
        this.flow = definition.flowType.loader.loadFlow(this, nbt.getCompound("flow"), registries);

        int connectionData = nbt.getInt("con");
        for (Direction face : Direction.values()) {
            int data = (connectionData >>> (face.ordinal() * 2)) & 0b11;
            // The only important aspect of this is the pipe type
            // as the texture index is just used at the client (which is updated in the first tick)
            // and the distance is only used on the server for item pipe travel times.
            // (which is minor enough that it doesn't really matter)
            if (data == 0b01) {
                connected.put(face, DEFAULT_CONNECTION_DISTANCE);
                types.put(face, ConnectedType.PIPE);
            } else if (data == 0b10) {
                connected.put(face, DEFAULT_CONNECTION_DISTANCE);
                types.put(face, ConnectedType.TILE);
            }
        }
    }

    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        nbt.put("col", NBTUtilBC.writeEnum(colour));
        nbt.putString("def", definition.identifier.toString());
        nbt.put("beh", behaviour.writeToNbt(registries));
        nbt.put("flow", flow.writeToNbt(registries));

        int connectionData = 0;
        for (Direction face : Direction.values()) {
            ConnectedType type = types.get(face);
            if (type != null) {
                int data = type == ConnectedType.PIPE ? 0b01 : 0b10;
                connectionData |= data << (face.ordinal() * 2);
            }
        }
        nbt.putInt("con", connectionData);
        return nbt;
    }

    // network

    public Pipe(IPipeHolder holder, PacketBufferBC buffer, @Nullable IPayloadContext ctx) throws IOException {
        this.holder = holder;
        this.definition = PipeRegistry.INSTANCE.loadDefinition(buffer.readUtf(256));
        this.behaviour = definition.logicConstructor.createBehaviour(this);
        readPayload(buffer, LogicalSide.CLIENT, ctx);
        this.flow = definition.flowType.creator.createFlow(this);
        this.flow.readPayload(PipeFlow.NET_ID_FULL_STATE, buffer, LogicalSide.CLIENT);
    }

    public void writeCreationPayload(PacketBufferBC buffer) {
        buffer.writeUtf(definition.identifier.toString());
        writePayload(buffer, LogicalSide.SERVER);
        flow.writePayload(PipeFlow.NET_ID_FULL_STATE, buffer, LogicalSide.SERVER);
    }

    public void writePayload(PacketBufferBC buffer, LogicalSide side) {
        if (side == LogicalSide.SERVER) {
            buffer.writeByte(colour == null ? 0 : colour.getId() + 1);
            for (Direction face : Direction.values()) {
                Float con = connected.get(face);
                if (con != null) {
                    buffer.writeBoolean(true);
                    buffer.writeFloat(con);
                    MessageUtil.writeEnumOrNull(buffer, types.get(face));
                } else {
                    buffer.writeBoolean(false);
                }
            }
            behaviour.writePayload(buffer, side);
        }
    }

    public void readPayload(PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        if (side == LogicalSide.CLIENT) {
            connected.clear();
            types.clear();

            int nColour = buffer.readUnsignedByte();
            colour = nColour == 0 ? null : DyeColor.byId(nColour - 1);

            for (Direction face : Direction.values()) {
                if (buffer.readBoolean()) {
                    float dist = buffer.readFloat();

                    connected.put(face, dist);

                    ConnectedType type = MessageUtil.readEnumOrNull(buffer, ConnectedType.class);
                    types.put(face, type);
                }
            }

            behaviour.readPayload(buffer, side, ctx);

            PipeModelKey model = getModel();
            if (!model.equals(lastModel)) {
                lastModel = model;
                getHolder().scheduleRenderUpdate();
            }
        }
    }

    // IPipe

    @Override
    public IPipeHolder getHolder() {
        return holder;
    }

    @Override
    public PipeDefinition getDefinition() {
        return definition;
    }

    @Override
    public PipeBehaviour getBehaviour() {
        return behaviour;
    }

    @Override
    public PipeFlow getFlow() {
        return flow;
    }

    @Override
    public DyeColor getColour() {
        return this.colour;
    }

    @Override
    public void setColour(DyeColor colour) {
        if (definition.canBeColoured) {
            this.colour = colour;
            markForUpdate();
        }
    }

    // Caps

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction facing) {
        T val = behaviour.getCapability(capability, facing);
        if (val != null) return val;
        return flow.getCapability(capability, facing);
    }

    // misc

    public void onLoad() {
        markForUpdate();
    }

    public void onTick() {
        if (updateMarked) {
            // Ensure that the behaviour and flow *always* get valid connection data
            // (for example if we just read from disk)
            updateConnections();
        }
        behaviour.onTick();
        flow.onTick();
        if (updateMarked) {
            updateConnections();
        }
    }

    public void postPluggableTick() {
        flow.postPluggableTick();
    }

    private void updateConnections() {
        if (holder.getPipeWorld().isClientSide) {
            return;
        }
        updateMarked = false;

        EnumMap<Direction, Float> old = connected.clone();

        connected.clear();
        types.clear();

        for (Direction facing : Direction.values()) {
            PipePluggable plug = getHolder().getPluggable(facing);
            if (plug != null && plug.isBlocking()) {
                continue;
            }
            BlockEntity oTile = getHolder().getNeighbourTile(facing);
            if (oTile == null) {
                continue;
            }
            IPipe oPipe = getHolder().getNeighbourPipe(facing);
            if (oPipe != null) {
                PipeBehaviour oBehaviour = oPipe.getBehaviour();
                if (oBehaviour == null) {
                    continue;
                }
                PipePluggable oPlug = holder.getPipeWorld().getCapability(PipeApi.CAP_PLUG,
                    oTile.getBlockPos(), facing.getOpposite());
                if (oPlug == null || !oPlug.isBlocking()) {
                    if (canPipesConnect(facing, this, oPipe)) {
                        connected.put(facing, DEFAULT_CONNECTION_DISTANCE);
                        types.put(facing, ConnectedType.PIPE);
                    }
                    continue;
                }
            }

            BlockPos nPos = holder.getPipePos().relative(facing);
            BlockState neighbour = holder.getPipeWorld().getBlockState(nPos);

            ICustomPipeConnection cust = PipeConnectionAPI.getCustomConnection(neighbour.getBlock());
            if (cust == null) {
                cust = DefaultPipeConnection.INSTANCE;
            }
            float ext = DEFAULT_CONNECTION_DISTANCE
                + cust.getExtension(holder.getPipeWorld(), nPos, facing.getOpposite(), neighbour);

            if (behaviour.shouldForceConnection(facing, oTile) || flow.shouldForceConnection(facing, oTile)
                || (behaviour.canConnect(facing, oTile) && flow.canConnect(facing, oTile))) {
                connected.put(facing, ext);
                types.put(facing, ConnectedType.TILE);
            }
        }
        if (!old.equals(connected)) {
            for (Direction face : Direction.values()) {
                boolean o = old.containsKey(face);
                boolean n = connected.containsKey(face);
                if (o != n) {
                    IPipe oPipe = getHolder().getNeighbourPipe(face);
                    if (oPipe != null) {
                        oPipe.markForUpdate();
                    }
                    holder.fireEvent(new PipeEventConnectionChange(holder, face));
                }
            }
        }
        getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
    }

    public void addDrops(NonNullList<ItemStack> toDrop) {
        toDrop.add(createItemStack());
        flow.addDrops(toDrop);
        behaviour.addDrops(toDrop);
    }

    /** @return An item of this pipe, with this pipe's colour. */
    public ItemStack createItemStack() {
        if (PipeApi.pipeRegistry.getItemForPipe(definition) instanceof Item item) {
            ItemStack stack = new ItemStack(item);
            if (colour != null) {
                stack.set(BCItemComponents.COLOUR.get(), colour);
            }
            return stack;
        }
        return ItemStack.EMPTY;
    }

    public static boolean canPipesConnect(Direction to, IPipe one, IPipe two) {
        return canColoursConnect(one.getColour(), two.getColour())//
        && canBehavioursConnect(to, one.getBehaviour(), two.getBehaviour())//
        && canFlowsConnect(to, one.getFlow(), two.getFlow());
    }

    public static boolean canColoursConnect(DyeColor one, DyeColor two) {
        return one == null || two == null || one == two;
    }

    public static boolean canBehavioursConnect(Direction to, PipeBehaviour one, PipeBehaviour two) {
        return one.canConnect(to, two) && two.canConnect(to.getOpposite(), one);
    }

    public static boolean canFlowsConnect(Direction to, PipeFlow one, PipeFlow two) {
        return one.canConnect(to, two) && two.canConnect(to.getOpposite(), one);
    }

    @Override
    public void markForUpdate() {
        updateMarked = true;
    }

    public PipeModelKey getModel() {
        PipeFaceTex[] sides = new PipeFaceTex[6];
        float[] mc = new float[6];
        for (Direction face : Direction.values()) {
            int i = face.ordinal();
            sides[i] = behaviour.getTextureData(face);
            mc[i] = getConnectedDist(face);
        }
        return new PipeModelKey(definition, behaviour.getTextureData(null), sides, mc, colour);
    }

    @Override
    public BlockEntity getConnectedTile(Direction side) {
        if (connected.containsKey(side)) {
            BlockEntity offset = getHolder().getNeighbourTile(side);
            if (offset == null && !getHolder().getPipeWorld().isClientSide) {
                markForUpdate();
            } else {
                return offset;
            }
        }
        return null;
    }

    @Override
    public IPipe getConnectedPipe(Direction side) {
        if (connected.containsKey(side) && getConnectedType(side) == ConnectedType.PIPE) {
            IPipe offset = getHolder().getNeighbourPipe(side);
            if (offset == null && !getHolder().getPipeWorld().isClientSide) {
                markForUpdate();
            } else {
                return offset;
            }
        }
        return null;
    }

    @Override
    public ConnectedType getConnectedType(Direction side) {
        return types.get(side);
    }

    @Override
    public boolean isConnected(Direction side) {
        return connected.containsKey(side);
    }

    public float getConnectedDist(Direction face) {
        Float custom = connected.get(face);
        return custom == null ? 0 : custom;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("Colour = " + colour);
        left.add("Definition = " + definition.identifier);
        if (behaviour instanceof IDebuggable) {
            left.add("Behaviour:");
            ((IDebuggable) behaviour).getDebugInfo(left, right, side);
            left.add("");
        } else {
            left.add("Behaviour = " + behaviour.getClass());
        }

        if (flow instanceof IDebuggable) {
            left.add("Flow:");
            ((IDebuggable) flow).getDebugInfo(left, right, side);
            left.add("");
        } else {
            left.add("Flow = " + flow.getClass());
        }
        for (Direction face : Direction.values()) {
            right.add(face + " = " + types.get(face) + ", " + getConnectedDist(face));
        }
    }
}
