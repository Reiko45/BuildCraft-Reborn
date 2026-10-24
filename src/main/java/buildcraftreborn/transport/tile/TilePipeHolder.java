/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.tile;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.api.transport.pipe.IFlowItems;
import buildcraftreborn.api.transport.pipe.IItemPipe;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.api.transport.pipe.PipeEvent;
import buildcraftreborn.api.transport.pipe.PipeEventPlaced;
import buildcraftreborn.api.transport.pipe.PipeEventTileState;
import buildcraftreborn.api.transport.pipe.PipeFlow;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.silicon.plug.FilterEventHandler;
import buildcraftreborn.transport.client.model.key.PipeAllCutoutKey;
import buildcraftreborn.transport.client.model.key.PipeAllTranslucentKey;
import buildcraftreborn.transport.client.model.key.PipeModelKey;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.transport.block.BlockPipeHolder;
import buildcraftreborn.transport.pipe.Pipe;
import buildcraftreborn.transport.pipe.PipeEventBus;
import buildcraftreborn.transport.pipe.PluggableHolder;
import buildcraftreborn.transport.wire.WireManager;

/** The block entity of a pipe block: holds the pipe, its six pluggables and its wires.
 * <p>
 * 1.21 notes: neighbours are asked for capabilities through the level (block capabilities), and the holder invalidates
 * its own capabilities whenever its pipe, connections or pluggables change. The chunk renderer reads what to draw from
 * {@link #PROP_CUTOUT} and {@link #PROP_TRANSLUCENT} in the model data (1.12 read the tile itself, through its
 * extended block state). */
public class TilePipeHolder extends TileBC_Neptune implements IPipeHolder, ITickable, IDebuggable {
    /** The model data the pipe block model draws from. */
    public static final ModelProperty<PipeAllCutoutKey> PROP_CUTOUT = new ModelProperty<>();
    public static final ModelProperty<PipeAllTranslucentKey> PROP_TRANSLUCENT = new ModelProperty<>();

    protected static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("pipe");

    public static final int NET_UPDATE_MULTI = IDS.allocId("UPDATE_MULTI");
    public static final int NET_UPDATE_PIPE_BEHAVIOUR = getReceiverId(PipeMessageReceiver.BEHAVIOUR);
    public static final int NET_UPDATE_PIPE_FLOW = getReceiverId(PipeMessageReceiver.FLOW);
    public static final int NET_UPDATE_PLUG_DOWN = getReceiverId(PipeMessageReceiver.PLUGGABLE_DOWN);
    public static final int NET_UPDATE_PLUG_UP = getReceiverId(PipeMessageReceiver.PLUGGABLE_UP);
    public static final int NET_UPDATE_PLUG_NORTH = getReceiverId(PipeMessageReceiver.PLUGGABLE_NORTH);
    public static final int NET_UPDATE_PLUG_SOUTH = getReceiverId(PipeMessageReceiver.PLUGGABLE_SOUTH);
    public static final int NET_UPDATE_PLUG_WEST = getReceiverId(PipeMessageReceiver.PLUGGABLE_WEST);
    public static final int NET_UPDATE_PLUG_EAST = getReceiverId(PipeMessageReceiver.PLUGGABLE_EAST);
    public static final int NET_UPDATE_WIRES = getReceiverId(PipeMessageReceiver.WIRES);

    private static final ResourceLocation ADVANCEMENT_PLACE_PIPE = BuildCraftReborn.id("pipe_dream");

    static {
        for (PipeMessageReceiver rec : PipeMessageReceiver.VALUES) {
            IDS.allocId("UPDATE_" + rec);
        }
    }

    public static final int[] NET_UPDATE_PLUGS = { //
        NET_UPDATE_PLUG_DOWN, NET_UPDATE_PLUG_UP, //
        NET_UPDATE_PLUG_NORTH, NET_UPDATE_PLUG_SOUTH, //
        NET_UPDATE_PLUG_WEST, NET_UPDATE_PLUG_EAST,//
    };

    private static int getReceiverId(PipeMessageReceiver type) {
        return NET_UPDATE_MULTI + 1 + type.ordinal();
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private int[] redstoneValues = new int[6];
    private int[] oldRedstoneValues = new int[] { -1, -1, -1, -1, -1, -1 };

    public final WireManager wireManager = new WireManager(this);
    public final PipeEventBus eventBus = new PipeEventBus();
    private final Map<Direction, PluggableHolder> pluggables = new EnumMap<>(Direction.class);
    @Nullable
    private Pipe pipe;
    private boolean scheduleRenderUpdate = true;
    private boolean capsChanged = false;
    private final Set<PipeMessageReceiver> networkUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    private final Set<PipeMessageReceiver> networkGuiUpdates = EnumSet.noneOf(PipeMessageReceiver.class);
    @Nullable
    private CompoundTag unknownData;

    /** The last shape computed by {@link BlockPipeHolder}, and the parts it was computed from. */
    public Object shapeKey;
    public VoxelShape shape;

    public TilePipeHolder(BlockPos pos, BlockState state) {
        super(BCTransportBlocks.PIPE_HOLDER_TILE.get(), pos, state);
        for (Direction side : Direction.values()) {
            pluggables.put(side, new PluggableHolder(this, side));
        }
        caps.addCapabilityInstance(PipeApi.CAP_PIPE_HOLDER, this, EnumPipePart.VALUES);
        caps.addCapability(PipeApi.CAP_PIPE, this::getPipe, EnumPipePart.VALUES);
        caps.addCapability(PipeApi.CAP_PLUG, this::getPluggable, EnumPipePart.FACES);
    }

    // Read + write

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        if (pipe != null) {
            nbt.put("pipe", pipe.writeToNbt(registries));
        } else if (unknownData != null && unknownData.contains("pipe")) {
            // Keep a pipe we couldn't load (for example from a removed addon)
            nbt.put("pipe", unknownData.getCompound("pipe"));
        }
        CompoundTag plugs = new CompoundTag();
        for (Direction face : Direction.values()) {
            CompoundTag plugTag = pluggables.get(face).writeToNbt(registries);
            if (!plugTag.isEmpty()) {
                plugs.put(face.getName(), plugTag);
            }
        }
        if (!plugs.isEmpty()) {
            nbt.put("plugs", plugs);
        }
        nbt.put("wireManager", wireManager.writeToNbt());
        nbt.putIntArray("redstone", redstoneValues);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        if (pipe != null) {
            unregisterPipe();
        }
        pipe = null;
        unknownData = null;
        if (nbt.contains("pipe")) {
            try {
                pipe = new Pipe(this, nbt.getCompound("pipe"), registries);
                registerPipe();
            } catch (InvalidInputDataException e) {
                // We can't throw an exception because then this tile won't persist
                BuildCraftReborn.LOGGER.warn("[transport.pipe] Couldn't load the pipe at {}", worldPosition, e);
                unknownData = nbt.copy();
            }
        }
        CompoundTag plugs = nbt.getCompound("plugs");
        for (Direction face : Direction.values()) {
            pluggables.get(face).readFromNbt(plugs.getCompound(face.getName()), registries);
        }
        wireManager.readFromNbt(nbt.getCompound("wireManager"));
        if (nbt.contains("redstone")) {
            int[] temp = nbt.getIntArray("redstone");
            if (temp.length == 6) {
                redstoneValues = temp;
            }
        }
        capsChanged = true;
    }

    private void registerPipe() {
        eventBus.registerHandler(pipe.behaviour);
        eventBus.registerHandler(pipe.flow);
        if (pipe.flow instanceof IFlowItems) {
            eventBus.registerHandler(FilterEventHandler.class);
        }
    }

    private void unregisterPipe() {
        eventBus.unregisterHandler(pipe.behaviour);
        eventBus.unregisterHandler(pipe.flow);
        eventBus.unregisterHandler(FilterEventHandler.class);
    }

    // Misc

    @Override
    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (stack.getItem() instanceof IItemPipe itemPipe) {
            PipeDefinition definition = itemPipe.getDefinition();
            this.pipe = new Pipe(this, definition);
            registerPipe();
            DyeColor colour = stack.get(BCItemComponents.COLOUR.get());
            if (colour != null) {
                pipe.setColour(colour);
            }
            eventBus.fireEvent(new PipeEventPlaced(this, placer, stack));
            capsChanged = true;
        }
        scheduleRenderUpdate();

        if (level != null && !level.isClientSide && hasOwner()) {
            AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT_PLACE_PIPE);
        }
    }

    /** Places a pipe directly (used by stripes pipes extending themselves, and tests). */
    public void setPipe(@Nullable Pipe newPipe) {
        if (pipe != null) {
            unregisterPipe();
        }
        pipe = newPipe;
        if (pipe != null) {
            registerPipe();
        }
        capsChanged = true;
        scheduleRenderUpdate();
        if (level != null && !level.isClientSide) {
            sendNetworkUpdate(NET_RENDER_DATA);
        }
    }

    /** 1.21 also calls {@link #setRemoved()} when the chunk unloads (right after {@link #onChunkUnloaded()}); 1.12
     * only invalidated tiles that were really removed. Rebuilding the wire systems then would load the neighbouring
     * chunks again (and hang the server while it saves the world on shutdown). */
    private boolean chunkUnloaded = false;

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (chunkUnloaded) {
            return;
        }
        eventBus.fireEvent(new PipeEventTileState.Invalidate(this));
        wireManager.invalidate();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        chunkUnloaded = false;
        eventBus.fireEvent(new PipeEventTileState.Validate(this));
        wireManager.validate();
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        chunkUnloaded = true;
        eventBus.fireEvent(new PipeEventTileState.ChunkUnload(this));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (pipe != null) {
            pipe.onLoad();
        }
        wireManager.validate();
        capsChanged = true;
    }

    @Override
    public void onNeighbourBlockChanged(Block block, BlockPos neighbour) {
        super.onNeighbourBlockChanged(block, neighbour);
        if (level == null || level.isClientSide) {
            return;
        }
        if (pipe != null) {
            pipe.markForUpdate();
        }
    }

    /** Tells NeoForge that the capabilities this pipe offers may have changed (next tick, on the server). */
    public void markCapabilitiesChanged() {
        capsChanged = true;
    }

    // ITickable

    @Override
    public void update() {
        if (level == null) {
            return;
        }
        redstoneValues = new int[6];
        // Tick objects
        if (pipe != null) {
            pipe.onTick();
        }
        for (Direction face : Direction.values()) {
            pluggables.get(face).onTick();
        }
        if (pipe != null) {
            pipe.postPluggableTick();
        }

        // Send network updates
        if (!networkUpdates.isEmpty()) {
            Set<PipeMessageReceiver> parts = EnumSet.copyOf(networkUpdates);
            for (PipeMessageReceiver part : parts) {
                sendNetworkUpdate(getReceiverId(part));
            }
        }
        // No need to send gui updates to specific players if we just sent off messages to all players.
        networkGuiUpdates.removeAll(networkUpdates);
        networkUpdates.clear();

        if (!networkGuiUpdates.isEmpty()) {
            Set<PipeMessageReceiver> parts = EnumSet.copyOf(networkGuiUpdates);
            for (PipeMessageReceiver part : parts) {
                sendNetworkGuiUpdate(getReceiverId(part));
            }
        }
        networkGuiUpdates.clear();

        if (scheduleRenderUpdate) {
            scheduleRenderUpdate = false;
            redrawBlock();
        }

        if (capsChanged && !level.isClientSide) {
            capsChanged = false;
            level.invalidateCapabilities(worldPosition);
        }

        wireManager.tick();

        if (!level.isClientSide && !Arrays.equals(redstoneValues, oldRedstoneValues)) {
            Block block = getBlockState().getBlock();
            level.updateNeighborsAt(worldPosition, block);
            for (int i = 0; i < 6; i++) {
                Direction face = Direction.values()[i];
                if (oldRedstoneValues[i] != redstoneValues[i]) {
                    level.updateNeighborsAt(worldPosition.relative(face), block);
                }
            }
            oldRedstoneValues = redstoneValues;
        }

        /* It's difficult to check to see if we actually have changed at all. So let's just always mark the chunk as
         * dirty instead of making every component do it individually. */
        markChunkDirty();
    }

    // Network

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writeCreationPayload(buffer);
                }
                for (Direction face : Direction.values()) {
                    pluggables.get(face).writeCreationPayload(buffer);
                }
                wireManager.writePayload(buffer, side);
            } else if (id == NET_UPDATE_PIPE_BEHAVIOUR) {
                if (pipe == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    pipe.writePayload(buffer, side);
                }
            } else if (id == NET_UPDATE_WIRES) {
                wireManager.writePayload(buffer, side);
            }
        }
        if (id == NET_UPDATE_PIPE_FLOW) {
            if (pipe == null || pipe.flow == null) {
                buffer.writeBoolean(false);
            } else {
                buffer.writeBoolean(true);
                pipe.flow.writePayload(PipeFlow.NET_ID_UPDATE, buffer, side);
            }
        } else if (id == NET_UPDATE_PLUG_DOWN) pluggables.get(Direction.DOWN).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_UP) pluggables.get(Direction.UP).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_NORTH) pluggables.get(Direction.NORTH).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_SOUTH) pluggables.get(Direction.SOUTH).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_WEST) pluggables.get(Direction.WEST).writePayload(buffer, side);
        else if (id == NET_UPDATE_PLUG_EAST) pluggables.get(Direction.EAST).writePayload(buffer, side);
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                if (buffer.readBoolean()) {
                    if (pipe != null) {
                        unregisterPipe();
                    }
                    pipe = new Pipe(this, buffer, ctx);
                    registerPipe();
                } else if (pipe != null) {
                    unregisterPipe();
                    pipe = null;
                }
                for (Direction face : Direction.values()) {
                    pluggables.get(face).readCreationPayload(buffer);
                }
                wireManager.readPayload(buffer, side, ctx);
                scheduleRenderUpdate();
            } else if (id == NET_UPDATE_MULTI) {
                int total = buffer.readUnsignedByte();
                for (PipeMessageReceiver type : PipeMessageReceiver.VALUES) {
                    if (((total >> type.ordinal()) & 1) == 1) {
                        readPayload(getReceiverId(type), buffer, side, ctx);
                    }
                }
            } else if (id == NET_UPDATE_PIPE_BEHAVIOUR) {
                if (buffer.readBoolean()) {
                    if (pipe == null) {
                        throw new IllegalStateException("Pipe was null when it shouldn't have been!");
                    }
                    pipe.readPayload(buffer, side, ctx);
                }
            } else if (id == NET_UPDATE_WIRES) {
                wireManager.readPayload(buffer, side, ctx);
                scheduleRenderUpdate();
            }
        }
        if (id == NET_UPDATE_PIPE_FLOW) {
            if (buffer.readBoolean()) {
                if (pipe == null) {
                    throw new IllegalStateException("Pipe was null when it shouldn't have been!");
                }
                int fId = buffer.readShort();
                pipe.flow.readPayload(fId, buffer, side);
            }
        } else if (id == NET_UPDATE_PLUG_DOWN) readPlug(Direction.DOWN, buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_UP) readPlug(Direction.UP, buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_NORTH) readPlug(Direction.NORTH, buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_SOUTH) readPlug(Direction.SOUTH, buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_WEST) readPlug(Direction.WEST, buffer, side, ctx);
        else if (id == NET_UPDATE_PLUG_EAST) readPlug(Direction.EAST, buffer, side, ctx);
    }

    private void readPlug(Direction face, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        pluggables.get(face).readPayload(buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            scheduleRenderUpdate();
        }
    }

    // Rendering

    @Override
    public ModelData getModelData() {
        // Called on the client's main thread: the chunk is drawn on other threads, so the model is built from these
        // keys rather than from the tile.
        if (pipe == null) {
            return ModelData.EMPTY;
        }
        PipeModelKey key = pipe.getModel();
        return ModelData.builder().with(PROP_CUTOUT, new PipeAllCutoutKey(key, this))
            .with(PROP_TRANSLUCENT, new PipeAllTranslucentKey(key, this)).build();
    }

    // IPipeHolder

    @Override
    public Level getPipeWorld() {
        return getLevel();
    }

    @Override
    public BlockPos getPipePos() {
        return getBlockPos();
    }

    @Override
    public BlockEntity getPipeTile() {
        return this;
    }

    @Override
    @Nullable
    public Pipe getPipe() {
        return pipe;
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return canInteractWith(player);
    }

    @Override
    @Nullable
    public PipePluggable getPluggable(@Nullable Direction side) {
        if (side == null) return null;
        return pluggables.get(side).pluggable;
    }

    @Nullable
    public PipePluggable replacePluggable(Direction side, @Nullable PipePluggable with) {
        redstoneValues = new int[6];
        PluggableHolder holder = pluggables.get(side);
        PipePluggable old = holder.pluggable;
        holder.pluggable = with;

        eventBus.unregisterHandler(old);
        eventBus.registerHandler(with);

        if (pipe != null) {
            pipe.markForUpdate();
        }
        if (level != null && !level.isClientSide) {
            if (old != with) {
                wireManager.getWireSystems().rebuildWireSystemsAround(this);
            }
            holder.sendNewPluggableData();
            capsChanged = true;
        }
        scheduleRenderUpdate();
        if (level != null) {
            level.neighborChanged(worldPosition.relative(side), getBlockState().getBlock(), worldPosition);
        }
        return old;
    }

    @Override
    @Nullable
    public IPipe getNeighbourPipe(Direction side) {
        if (level == null) {
            return null;
        }
        BlockPos other = worldPosition.relative(side);
        if (!level.isLoaded(other)) {
            return null;
        }
        return level.getCapability(PipeApi.CAP_PIPE, other, side.getOpposite());
    }

    @Override
    @Nullable
    public <T> T getCapabilityFromPipe(Direction side, BlockCapability<T, @Nullable Direction> capability) {
        PipePluggable plug = getPluggable(side);
        if (plug != null) {
            T t = plug.getInternalCapability(capability);
            if (t != null) {
                return t;
            }
            if (plug.isBlocking()) {
                return null;
            }
        }
        if (pipe != null && pipe.isConnected(side) && level != null) {
            BlockPos other = worldPosition.relative(side);
            if (level.isLoaded(other)) {
                return level.getCapability(capability, other, side.getOpposite());
            }
        }
        return null;
    }

    @Override
    public void scheduleRenderUpdate() {
        scheduleRenderUpdate = true;
    }

    @Override
    public void scheduleNetworkUpdate(PipeMessageReceiver... parts) {
        Collections.addAll(networkUpdates, parts);
    }

    @Override
    public void scheduleNetworkGuiUpdate(PipeMessageReceiver... parts) {
        Collections.addAll(networkGuiUpdates, parts);
    }

    @Override
    public void sendMessage(PipeMessageReceiver to, IWriter writer) {
        createAndSendMessage(getReceiverId(to), writer::write);
    }

    @Override
    public void sendGuiMessage(PipeMessageReceiver to, IWriter writer) {
        createAndSendGuiMessage(getReceiverId(to), writer::write);
    }

    @Override
    public WireManager getWireManager() {
        return wireManager;
    }

    @Override
    public boolean fireEvent(PipeEvent event) {
        return eventBus.fireEvent(event);
    }

    @Override
    public int getRedstoneInput(@Nullable Direction side) {
        if (level == null) {
            return 0;
        }
        if (side == null) {
            return level.getBestNeighborSignal(worldPosition);
        }
        return level.getSignal(worldPosition.relative(side), side);
    }

    @Override
    public boolean setRedstoneOutput(@Nullable Direction side, int value) {
        if (side == null) {
            for (Direction facing : Direction.values()) {
                redstoneValues[facing.ordinal()] = value;
            }
        } else {
            redstoneValues[side.ordinal()] = value;
        }
        return true;
    }

    public int getRedstoneOutput(Direction side) {
        return redstoneValues[side.ordinal()];
    }

    // Caps

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction facing) {
        if (facing != null) {
            PipePluggable plug = getPluggable(facing);
            if (plug != null) {
                T cap = plug.getCapability(capability);
                if (cap != null) return cap;
                if (plug.isBlocking()) return null;
            }
        }
        if (pipe != null) {
            T val = pipe.getCapability(capability, facing);
            if (val != null) {
                return val;
            }
        }
        return super.getCapability(capability, facing);
    }

    // Debug

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        if (pipe == null) {
            left.add("Pipe = null");
        } else {
            left.add("Pipe:");
            pipe.getDebugInfo(left, right, side);
        }
        left.add("Parts:");
        wireManager.parts
            .forEach((part, color) -> left.add(" - " + part + " = " + color + " = " + wireManager.isPowered(part)));
        left.add("All wire systems in level count = "
            + (level == null || level.isClientSide ? 0 : wireManager.getWireSystems().wireSystems.size()));
        if (unknownData != null) {
            left.add(unknownData.toString());
        }
    }
}
