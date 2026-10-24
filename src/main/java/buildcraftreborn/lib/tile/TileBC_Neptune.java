/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.tile;

import java.io.IOException;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.Set;

import com.mojang.authlib.GameProfile;
import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.IPlayerOwned;
import buildcraftreborn.lib.cap.CapabilityHelper;
import buildcraftreborn.lib.client.render.DetachedRenderer.IDetachedRenderer;
import buildcraftreborn.lib.debug.BCAdvDebugging;
import buildcraftreborn.lib.debug.IAdvDebugTarget;
import buildcraftreborn.lib.delta.DeltaManager.EnumDeltaMessage;
import buildcraftreborn.lib.delta.DeltaManager;
import buildcraftreborn.lib.fluid.TankManager;
import buildcraftreborn.lib.misc.FakePlayerProvider;
import buildcraftreborn.lib.misc.MessageUtil;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.IPayloadReceiver;
import buildcraftreborn.lib.net.IPayloadWriter;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.net.TileUpdatePayload;
import buildcraftreborn.lib.tile.item.ItemHandlerManager;

/** The base of every BuildCraft block entity.
 * <p>
 * It keeps 1.12's design: tiles send compact custom payloads keyed by message ID ({@link #NET_RENDER_DATA},
 * {@link #NET_GUI_DATA}, ...) written by {@link #writePayload} and read by {@link #readPayload}, and add capabilities
 * per instance through {@link #caps}. Port notes:
 * <ul>
 * <li>NBT methods take a {@link HolderLookup.Provider} ({@code saveAdditional}/{@code loadAdditional}).
 * <li>The chunk and neighbour caches were dropped; NeoForge's {@code BlockCapabilityCache} covers capability lookups.
 * <li>Advanced debugging (the debugger item) sends {@link #NET_ADV_DEBUG} every tick to the client watching; tiles
 * add what they need to {@link #writePayload} for it and return a renderer from {@link #getDebugRenderer()}.
 * <li>Permissions were a stub in 1.12 too: only the 8-block reach check applies.
 * </ul> */
public abstract class TileBC_Neptune extends BlockEntity implements IPayloadReceiver, IPlayerOwned, IAdvDebugTarget {
    public static final boolean DEBUG = Boolean.getBoolean("buildcraftreborn.debug.tile");

    protected static final IdAllocator IDS = new IdAllocator("tile");

    /** Used for sending all data used for rendering the tile on a client. This does not include items, power, stages,
     * etc (Unless some are shown in the world) */
    public static final int NET_RENDER_DATA = IDS.allocId("RENDER_DATA");
    /** Used for sending all data in the GUI. Basically what has been omitted from {@link #NET_RENDER_DATA} that is
     * shown in the GUI. */
    public static final int NET_GUI_DATA = IDS.allocId("GUI_DATA");
    /** Used for sending the data that would normally be sent with {@code broadcastChanges()}. Note that if no bytes are
     * written then the update message won't be sent. You should detect if any changes have been made to the gui since
     * the last tick, so you don't resend duplicate information if nothing has changed by the next tick. */
    public static final int NET_GUI_TICK = IDS.allocId("GUI_TICK");

    public static final int NET_REN_DELTA_SINGLE = IDS.allocId("REN_DELTA_SINGLE");
    public static final int NET_REN_DELTA_CLEAR = IDS.allocId("REN_DELTA_CLEAR");
    public static final int NET_GUI_DELTA_SINGLE = IDS.allocId("GUI_DELTA_SINGLE");
    public static final int NET_GUI_DELTA_CLEAR = IDS.allocId("GUI_DELTA_CLEAR");

    /** Sent every tick while this is the advanced debug target (see {@link BCAdvDebugging}). */
    public static final int NET_ADV_DEBUG = IDS.allocId("DEBUG_DATA");
    public static final int NET_ADV_DEBUG_DISABLE = IDS.allocId("DEBUG_DISABLE");

    /** Used to tell the client to redraw the block. */
    public static final int NET_REDRAW = IDS.allocId("REDRAW");

    /** Players further away than this can't use the tile's GUI. */
    private static final double MAX_INTERACT_DISTANCE_SQ = 8 * 8;

    protected final CapabilityHelper caps = new CapabilityHelper();
    protected final ItemHandlerManager itemManager = new ItemHandlerManager(this::onSlotChange);
    protected final TankManager tankManager = new TankManager();

    /** Handles all of the players that are currently using this tile (have a GUI open) */
    private final Set<Player> usingPlayers = Collections.newSetFromMap(new IdentityHashMap<>());
    @Nullable
    private GameProfile owner;

    protected final DeltaManager deltaManager = new DeltaManager((gui, type, writer) -> {
        final int id;
        if (type == EnumDeltaMessage.ADD_SINGLE) {
            id = gui ? NET_GUI_DELTA_SINGLE : NET_REN_DELTA_SINGLE;
        } else if (type == EnumDeltaMessage.SET_VALUE) {
            id = gui ? NET_GUI_DELTA_CLEAR : NET_REN_DELTA_CLEAR;
        } else {
            throw new IllegalArgumentException("Unknown delta message type " + type);
        }
        if (gui) {
            createAndSendGuiMessage(id, writer);
        } else {
            createAndSendMessage(id, writer);
        }
    });

    public TileBC_Neptune(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        caps.addProvider(itemManager);
    }

    // ##################################################
    //
    // Local blockstate + tile entity getters
    //
    // ##################################################

    public final BlockState getCurrentState() {
        return level == null ? getBlockState() : level.getBlockState(worldPosition);
    }

    @Nullable
    public final BlockState getCurrentStateForBlock(Block expectedBlock) {
        BlockState state = getCurrentState();
        return state.getBlock() == expectedBlock ? state : null;
    }

    public final BlockState getNeighbourState(Direction offset) {
        return getLocalState(worldPosition.relative(offset));
    }

    /** @param pos The <i>absolute</i> position of the {@link BlockState}. */
    public final BlockState getLocalState(BlockPos pos) {
        if (DEBUG && !level.isLoaded(pos)) {
            BuildCraftReborn.LOGGER.warn("[lib.tile] Ghost-loading block at {} (from {})", pos, worldPosition);
        }
        return level.getBlockState(pos);
    }

    @Nullable
    public final BlockEntity getNeighbourTile(Direction offset) {
        return getLocalTile(worldPosition.relative(offset));
    }

    /** @param pos The <i>absolute</i> position of the {@link BlockEntity}. */
    @Nullable
    public final BlockEntity getLocalTile(BlockPos pos) {
        if (DEBUG && !level.isLoaded(pos)) {
            BuildCraftReborn.LOGGER.warn("[lib.tile] Ghost-loading tile at {} (from {})", pos, worldPosition);
        }
        return level.getBlockEntity(pos);
    }

    // ##################
    //
    // Misc overridables
    //
    // ##################

    /** @return The {@link IdAllocator} that allocates all ID's for this class, and its parent classes. All subclasses
     *         should override this if they allocate their own ids after calling {@link IdAllocator#makeChild(String)} */
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    /** Checks to see if this tile can update. The base implementation only checks to see if it has a world. */
    public boolean cannotUpdate() {
        return !hasLevel();
    }

    /** Called whenever the block is removed (broken or replaced). Drops the tile's contents. */
    public void onRemove() {
        NonNullList<ItemStack> toDrop = NonNullList.create();
        addDrops(toDrop);
        if (level != null) {
            Containers.dropContents(level, worldPosition, toDrop);
        }
    }

    /** Called by {@link #onRemove()} (by default). */
    public void addDrops(NonNullList<ItemStack> toDrop) {
        itemManager.addDrops(toDrop);
        tankManager.addDrops(toDrop);
    }

    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        if (level != null && !level.isClientSide && placer instanceof Player player) {
            owner = player.getGameProfile();
        }
    }

    public void onPlayerOpen(Player player) {
        if (owner == null || owner == FakePlayerProvider.NULL_PROFILE) {
            owner = player.getGameProfile();
        }
        sendNetworkUpdate(NET_GUI_DATA, player);
        usingPlayers.add(player);
    }

    public void onPlayerClose(Player player) {
        usingPlayers.remove(player);
    }

    /** Called on both sides when a player right-clicks the block (with or without an item). As in 1.12 the tile
     * decides what happens: return true if the click was handled, or false to let the held item act instead (a
     * wrench rotates the block, a bucket places its fluid). Tiles with a GUI open it from here with
     * {@link #openGui(Player)}.
     * <p>
     * The default lets buckets and other fluid containers empty into (or fill from) this tile's tanks. Unlike 1.12,
     * a tile without tanks returns false, so a held bucket still works normally against it. */
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held, BlockHitResult hit) {
        return onActivated(player, hand, held);
    }

    /** Shorter form of {@link #onActivated(Player, InteractionHand, ItemStack, BlockHitResult)} for tiles that don't
     * care where they were clicked. */
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (tankManager.isEmpty()) {
            return false;
        }
        return tankManager.onActivated(player, getBlockPos(), hand);
    }

    /** Opens this tile's menu on the server (does nothing on the client, or if this tile isn't a
     * {@link MenuProvider}). The block position is sent so the client menu can find the tile. */
    public void openGui(Player player) {
        if (this instanceof MenuProvider provider && player instanceof ServerPlayer serverPlayer) {
            BlockPos pos = getBlockPos();
            serverPlayer.openMenu(provider, buf -> buf.writeBlockPos(pos));
        }
    }

    public void onNeighbourBlockChanged(Block block, BlockPos neighbour) {}

    /** Answers a capability query for this tile. Registered for every BuildCraft tile type by
     * {@code BCCapabilities}. */
    @Nullable
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        return caps.getCapability(capability, side);
    }

    // Item caps
    protected void onSlotChange(net.neoforged.neoforge.items.IItemHandlerModifiable handler, int slot,
        ItemStack before, ItemStack after) {
        if (level != null && level.isLoaded(worldPosition)) {
            if (getCurrentState().hasAnalogOutputSignal()) {
                setChanged();
            } else {
                markChunkDirty();
            }
        }
    }

    /** Cheaper version of {@link #setChanged()} that doesn't update nearby comparators, so all it will do is ensure that
     * the current chunk is saved after the last tick. */
    public void markChunkDirty() {
        if (level != null) {
            level.blockEntityChanged(worldPosition);
        }
    }

    // ##################
    //
    // Permission related
    //
    // ##################

    protected boolean hasOwner() {
        return owner != null;
    }

    @Override
    public GameProfile getOwner() {
        if (owner == null) {
            BuildCraftReborn.LOGGER.warn("[lib.tile] Unknown owner for {} at {}", getClass(), worldPosition);
            owner = FakePlayerProvider.NULL_PROFILE;
        }
        return owner;
    }

    public boolean canPlayerEdit(Player player) {
        return player.distanceToSqr(worldPosition.getCenter()) <= MAX_INTERACT_DISTANCE_SQ;
    }

    public boolean canInteractWith(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this) {
            return false;
        }
        // edit rather than view because you can normally change the contents from gui interaction
        return canPlayerEdit(player);
    }

    public Set<Player> getUsingPlayers() {
        return Collections.unmodifiableSet(usingPlayers);
    }

    // ##################
    //
    // Network helpers
    //
    // ##################

    /** Tells MC to redraw this block. On the server this sends the {@link #NET_REDRAW} message. */
    public final void redrawBlock() {
        if (hasLevel()) {
            if (level.isClientSide) {
                BlockState state = level.getBlockState(worldPosition);
                level.sendBlockUpdated(worldPosition, state, state, 0);
                requestModelDataUpdate();
            } else {
                sendNetworkUpdate(NET_REDRAW);
            }
        }
    }

    /** Sends a network update of the specified ID: to the server from a client, or to every watching player. */
    public final void sendNetworkUpdate(int id) {
        if (hasLevel()) {
            TileUpdatePayload message = createNetworkUpdate(id);
            if (level.isClientSide) {
                PacketDistributor.sendToServer(message);
            } else {
                MessageUtil.sendToAllWatching(level, worldPosition, message);
            }
        }
    }

    public final void sendNetworkGuiTick(Player player) {
        if (hasLevel() && !level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            TileUpdatePayload message = createNetworkUpdate(NET_GUI_TICK);
            if (message.data().length <= Short.BYTES) {
                // Only the message ID was written: nothing changed
                return;
            }
            PacketDistributor.sendToPlayer(serverPlayer, message);
        }
    }

    public final void sendNetworkGuiUpdate(int id) {
        if (hasLevel()) {
            for (Player player : usingPlayers) {
                sendNetworkUpdate(id, player);
            }
        }
    }

    public final void sendNetworkUpdate(int id, Player target) {
        if (hasLevel() && target instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, createNetworkUpdate(id));
        }
    }

    public final TileUpdatePayload createNetworkUpdate(final int id) {
        final LogicalSide side = level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
        return createMessage(id, (buffer) -> writePayload(id, buffer, side));
    }

    public final void createAndSendMessage(int id, IPayloadWriter writer) {
        if (hasLevel()) {
            TileUpdatePayload message = createMessage(id, writer);
            if (level.isClientSide) {
                PacketDistributor.sendToServer(message);
            } else {
                MessageUtil.sendToAllWatching(level, worldPosition, message);
            }
        }
    }

    public final void createAndSendGuiMessage(int id, IPayloadWriter writer) {
        if (hasLevel()) {
            TileUpdatePayload message = createMessage(id, writer);
            if (level.isClientSide) {
                PacketDistributor.sendToServer(message);
            } else {
                MessageUtil.sendToPlayers(usingPlayers, message);
            }
        }
    }

    public final void createAndSendMessage(int id, ServerPlayer player, IPayloadWriter writer) {
        if (hasLevel()) {
            PacketDistributor.sendToPlayer(player, createMessage(id, writer));
        }
    }

    public final void createAndSendGuiMessage(int id, ServerPlayer player, IPayloadWriter writer) {
        if (usingPlayers.contains(player)) {
            createAndSendMessage(id, player, writer);
        }
    }

    public final TileUpdatePayload createMessage(int id, IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer(), level.registryAccess());
        try {
            buffer.writeShort(id);
            writer.write(buffer);
            return new TileUpdatePayload(worldPosition, buffer.toByteArray());
        } finally {
            buffer.release();
        }
    }

    // Vanilla chunk sync: the client gets NET_RENDER_DATA when the chunk is sent.

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        if (level == null) {
            return nbt;
        }
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer(), level.registryAccess());
        try {
            buffer.writeShort(NET_RENDER_DATA);
            writePayload(NET_RENDER_DATA, buffer, level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER);
            nbt.putByteArray("d", buffer.toByteArray());
        } finally {
            buffer.release();
        }
        return nbt;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        handleUpdateTag(pkt.getTag(), registries);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        // Explicitly don't read the (server) data from NBT
        if (!tag.contains("d", Tag.TAG_BYTE_ARRAY) || level == null) {
            // A bit odd, but ok - this was probably sent by something else
            return;
        }
        byte[] bytes = tag.getByteArray("d");
        if (bytes.length < 2) {
            BuildCraftReborn.LOGGER.warn("[lib.tile] Received an update tag that didn't have any data! ({})", tag);
            return;
        }
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.wrappedBuffer(bytes), level.registryAccess());
        try {
            int id = buffer.readUnsignedShort();
            readPayload(id, buffer, level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER, null);
            // Make sure that we actually read the entire message rather than just discarding it
            MessageUtil.ensureEmpty(buffer, level.isClientSide, getClass() + ", id = " + getIdAllocator().getNameFor(id));
        } catch (IOException e) {
            throw new RuntimeException("Received an update tag that failed to read correctly!", e);
        } finally {
            buffer.release();
        }
    }

    @Override
    public final void receivePayload(IPayloadContext ctx, PacketBufferBC buffer) throws IOException {
        int id = buffer.readUnsignedShort();
        LogicalSide side = ctx.flow().getReceptionSide();
        readPayload(id, buffer, side, ctx);
        // Make sure that we actually read the entire message rather than just discarding it
        MessageUtil.ensureEmpty(buffer, level.isClientSide, getClass() + ", id = " + getIdAllocator().getNameFor(id));
    }

    // ######################
    //
    // Network overridables
    //
    // ######################

    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        // write render data with gui data
        if (id == NET_GUI_DATA) {
            writePayload(NET_RENDER_DATA, buffer, side);
            if (side == LogicalSide.SERVER) {
                MessageUtil.writeGameProfile(buffer, owner);
            }
        }
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                deltaManager.writeDeltaState(false, buffer);
            } else if (id == NET_GUI_DATA) {
                deltaManager.writeDeltaState(true, buffer);
            }
        }
    }

    /** @param ctx The context. Will be null if this is a generic update payload (chunk data)
     * @throws IOException if something went wrong */
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        // read render data with gui data
        if (id == NET_GUI_DATA) {
            readPayload(NET_RENDER_DATA, buffer, side, ctx);
            if (side == LogicalSide.CLIENT) {
                owner = MessageUtil.readGameProfile(buffer);
            }
        }
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) deltaManager.receiveDeltaData(false, EnumDeltaMessage.CURRENT_STATE, buffer);
            else if (id == NET_GUI_DATA) deltaManager.receiveDeltaData(true, EnumDeltaMessage.CURRENT_STATE, buffer);
            else if (id == NET_REN_DELTA_SINGLE) deltaManager.receiveDeltaData(false, EnumDeltaMessage.ADD_SINGLE, buffer);
            else if (id == NET_GUI_DELTA_SINGLE) deltaManager.receiveDeltaData(true, EnumDeltaMessage.ADD_SINGLE, buffer);
            else if (id == NET_REN_DELTA_CLEAR) deltaManager.receiveDeltaData(false, EnumDeltaMessage.SET_VALUE, buffer);
            else if (id == NET_GUI_DELTA_CLEAR) deltaManager.receiveDeltaData(true, EnumDeltaMessage.SET_VALUE, buffer);
            else if (id == NET_REDRAW) redrawBlock();
            else if (id == NET_ADV_DEBUG) BCAdvDebugging.setClientDebugTarget(this);
            else if (id == NET_ADV_DEBUG_DISABLE) BCAdvDebugging.clearClientDebugTarget(this);
        }
    }

    // ######################
    //
    // NBT handling
    //
    // ######################

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        deltaManager.readFromNBT(nbt.getCompound("deltas"));
        if (nbt.contains("owner", Tag.TAG_COMPOUND)) {
            owner = ExtraCodecs.GAME_PROFILE.parse(NbtOps.INSTANCE, nbt.get("owner")).result().orElse(null);
        }
        if (nbt.contains("items", Tag.TAG_COMPOUND)) {
            itemManager.deserializeNBT(registries, nbt.getCompound("items"));
        }
        if (nbt.contains("tanks", Tag.TAG_COMPOUND)) {
            tankManager.deserializeNBT(registries, nbt.getCompound("tanks"));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("deltas", deltaManager.writeToNBT());
        if (owner != null && owner != FakePlayerProvider.NULL_PROFILE) {
            Optional<Tag> encoded = ExtraCodecs.GAME_PROFILE.encodeStart(NbtOps.INSTANCE, owner).result();
            encoded.ifPresent(tag -> nbt.put("owner", tag));
        }
        CompoundTag items = itemManager.serializeNBT(registries);
        if (!items.isEmpty()) {
            nbt.put("items", items);
        }
        CompoundTag tanks = tankManager.serializeNBT(registries);
        if (!tanks.isEmpty()) {
            nbt.put("tanks", tanks);
        }
    }

    // ##################
    //
    // Advanced debugging
    //
    // ##################

    public boolean isBeingDebugged() {
        return BCAdvDebugging.isBeingDebugged(this);
    }

    public void enableDebugging() {
        if (level == null || level.isClientSide) {
            return;
        }
        BCAdvDebugging.setCurrentDebugTarget(this);
    }

    @Override
    public void disableDebugging() {
        sendNetworkUpdate(NET_ADV_DEBUG_DISABLE);
    }

    @Override
    public boolean doesExistInWorld() {
        return hasLevel() && !isRemoved() && level.getBlockEntity(worldPosition) == this;
    }

    @Override
    public void sendDebugState() {
        sendNetworkUpdate(NET_ADV_DEBUG);
    }

    @Override
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public IDetachedRenderer getDebugRenderer() {
        return null;
    }
}
