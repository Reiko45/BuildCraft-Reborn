/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.tile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import com.google.common.primitives.Bytes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.container.ContainerElectronicLibrary;
import buildcraftreborn.builders.item.ItemSnapshot;
import buildcraftreborn.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.lib.delta.DeltaInt;
import buildcraftreborn.lib.delta.DeltaManager;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.nbt.NbtSquisher;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.lib.tile.item.StackInsertionFunction;

/** The electronic library: copies blueprints and templates between the server and the player's own snapshot
 * library (on their computer).
 * <p>
 * How networking works here:
 * <ul>
 * <li>down: the server sends {@link #NET_DOWN} with the snapshot to the players using the GUI, and they add it to
 * their local library.</li>
 * <li>up: the server sends an empty {@link #NET_UP}; the client that selected a snapshot sends it back in pieces, and
 * the server adds it to its library.</li>
 * </ul>
 * 1.21: the uploading player is the one the message came from (1.12 trusted a player id the client wrote), and an
 * upload is limited in size. */
public class TileElectronicLibrary extends TileBC_Neptune implements ITickable, IDebuggable, MenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("library");
    public static final int NET_DOWN = IDS.allocId("DOWN");
    public static final int NET_UP = IDS.allocId("UP");

    /** The largest snapshot (compressed) that a client may upload. */
    private static final int MAX_UPLOAD_BYTES = 16 * 1024 * 1024;
    private static final int UPLOAD_PART_BYTES = 4 * 1024;

    public final ItemHandlerSimple invDownIn = itemManager.addInvHandler("downIn", 1,
        (slot, stack) -> ItemSnapshot.isUsed(stack), StackInsertionFunction.getInsertionFunction(1),
        EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invDownOut = itemManager.addInvHandler("downOut", 1,
        StackInsertionFunction.getInsertionFunction(1), EnumAccess.EXTRACT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invUpIn = itemManager.addInvHandler("upIn", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSnapshot, StackInsertionFunction.getInsertionFunction(1),
        EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invUpOut = itemManager.addInvHandler("upOut", 1,
        StackInsertionFunction.getInsertionFunction(1), EnumAccess.EXTRACT, EnumPipePart.VALUES);
    @Nullable
    public Snapshot.Key selected = null;
    private int progressDown = -1;
    private int progressUp = -1;
    public final DeltaInt deltaProgressDown =
        deltaManager.addDelta("progressDown", DeltaManager.EnumNetworkVisibility.GUI_ONLY);
    public final DeltaInt deltaProgressUp =
        deltaManager.addDelta("progressUp", DeltaManager.EnumNetworkVisibility.GUI_ONLY);
    private final Map<Pair<UUID, Snapshot.Key>, List<byte[]>> upSnapshotsParts = new HashMap<>();

    public TileElectronicLibrary(BlockPos pos, BlockState state) {
        super(BCBuildersBlocks.LIBRARY_TILE.get(), pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (handler == invDownIn) {
            if (progressDown > 0) {
                progressDown = -1;
                deltaProgressDown.setValue(0);
            }
        }
        if (handler == invUpIn) {
            if (progressUp > 0) {
                progressUp = -1;
                deltaProgressUp.setValue(0);
            }
        }
    }

    @Override
    public void update() {
        deltaManager.tick();

        if (level.isClientSide) {
            return;
        }

        if (!invDownIn.getStackInSlot(0).isEmpty() && invDownOut.getStackInSlot(0).isEmpty()) {
            if (progressDown == -1) {
                progressDown = 0;
                deltaProgressDown.addDelta(0, 50, 1);
                deltaProgressDown.addDelta(50, 55, -1);
            }
            if (progressDown >= 50) {
                sendNetworkGuiUpdate(NET_DOWN);
                invDownOut.setStackInSlot(0, invDownIn.getStackInSlot(0));
                invDownIn.setStackInSlot(0, ItemStack.EMPTY);
                progressDown = -1;
            } else {
                progressDown++;
            }
        } else if (progressDown != -1) {
            progressDown = -1;
            deltaProgressDown.setValue(0);
        }

        if (selected != null && !invUpIn.getStackInSlot(0).isEmpty() && invUpOut.getStackInSlot(0).isEmpty()) {
            if (progressUp == -1) {
                progressUp = 0;
                deltaProgressUp.addDelta(0, 50, 1);
                deltaProgressUp.addDelta(50, 55, -1);
            }
            if (progressUp >= 50) {
                sendNetworkGuiUpdate(NET_UP);
                progressUp = -1;
            } else {
                progressUp++;
            }
        } else if (progressUp != -1) {
            progressUp = -1;
            deltaProgressUp.setValue(0);
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(selected != null);
                if (selected != null) {
                    selected.writeToByteBuf(buffer);
                }
            }
            if (id == NET_DOWN) {
                Snapshot.Header header = ItemSnapshot.getHeader(invDownIn.getStackInSlot(0));
                Snapshot snapshot = header == null ? null : GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
                byte[] data = null;
                if (snapshot != null) {
                    snapshot = snapshot.copy();
                    snapshot.key = new Snapshot.Key(snapshot.key, header);
                    try {
                        ByteArrayOutputStream out = new ByteArrayOutputStream();
                        NbtSquisher.squishVanilla(Snapshot.writeToNBT(snapshot), out);
                        data = out.toByteArray();
                    } catch (IOException e) {
                        BuildCraftReborn.LOGGER.warn("[builders.library] Failed to write a snapshot", e);
                    }
                }
                buffer.writeBoolean(data != null);
                if (data != null) {
                    buffer.writeByteArray(data);
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                selected = buffer.readBoolean() ? new Snapshot.Key(buffer) : null;
            }
            if (id == NET_DOWN) {
                if (buffer.readBoolean()) {
                    byte[] data = buffer.readByteArray();
                    Snapshot snapshot = Snapshot.readFromNBT(NbtSquisher.expand(new ByteArrayInputStream(data)));
                    snapshot.computeKey();
                    GlobalSavedDataSnapshots.get(level).addSnapshot(snapshot);
                }
            }
            if (id == NET_UP) {
                sendSelectedUp();
            }
        }
        if (side == LogicalSide.SERVER) {
            if (id == NET_UP && ctx != null) {
                readUploadPart(buffer, ctx.player());
            }
        }
    }

    /** Client side: sends the selected snapshot to the server, in pieces small enough for a client message. */
    private void sendSelectedUp() throws IOException {
        Snapshot.Key key = selected;
        if (key == null) {
            return;
        }
        Snapshot snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(key);
        if (snapshot == null) {
            return;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NbtSquisher.squishVanilla(Snapshot.writeToNBT(snapshot), out);
        byte[] data = out.toByteArray();
        for (int start = 0; start < data.length || start == 0; start += UPLOAD_PART_BYTES) {
            int end = Math.min(data.length, start + UPLOAD_PART_BYTES);
            byte[] part = Arrays.copyOfRange(data, start, end);
            boolean last = end >= data.length;
            PacketDistributor.sendToServer(createMessage(NET_UP, localBuffer -> {
                key.writeToByteBuf(localBuffer);
                localBuffer.writeBoolean(last);
                localBuffer.writeByteArray(part);
            }));
            if (last) {
                break;
            }
        }
    }

    private void readUploadPart(PacketBufferBC buffer, Player player) {
        Snapshot.Key key = new Snapshot.Key(buffer);
        boolean last = buffer.readBoolean();
        byte[] part = buffer.readByteArray(UPLOAD_PART_BYTES);
        Pair<UUID, Snapshot.Key> pair = Pair.of(player.getUUID(), key);
        List<byte[]> parts = upSnapshotsParts.computeIfAbsent(pair, p -> new ArrayList<>());
        parts.add(part);
        if (parts.size() * (long) UPLOAD_PART_BYTES > MAX_UPLOAD_BYTES) {
            BuildCraftReborn.LOGGER.warn("[builders.library] {} tried to upload a snapshot that was too big",
                player.getName().getString());
            upSnapshotsParts.remove(pair);
            return;
        }
        if (!last) {
            return;
        }
        upSnapshotsParts.remove(pair);
        if (!getUsingPlayers().contains(player) || invUpIn.getStackInSlot(0).isEmpty()
            || !invUpOut.getStackInSlot(0).isEmpty()) {
            return;
        }
        try {
            byte[] data = Bytes.concat(parts.toArray(new byte[0][]));
            Snapshot snapshot = Snapshot.readFromNBT(NbtSquisher.expand(new ByteArrayInputStream(data),
                NbtAccounter.create(64L * 1024 * 1024)));
            Snapshot.Header header = snapshot.key.header;
            if (header == null) {
                return;
            }
            snapshot = snapshot.copy();
            snapshot.key = new Snapshot.Key(snapshot.key, (Snapshot.Header) null);
            snapshot.computeKey();
            GlobalSavedDataSnapshots.get(level).addSnapshot(snapshot);
            Snapshot.Header newHeader = new Snapshot.Header(snapshot.key, header.owner, header.created, header.name);
            invUpOut.setStackInSlot(0, ItemSnapshot.getUsed(snapshot.getType(), newHeader));
            invUpIn.setStackInSlot(0, ItemStack.EMPTY);
        } catch (IOException | RuntimeException e) {
            BuildCraftReborn.LOGGER.warn("[builders.library] Couldn't read a snapshot sent by {}",
                player.getName().getString(), e);
        }
    }

    // GUI

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (!level.isClientSide) {
            openGui(player);
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerElectronicLibrary(windowId, player, this);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("selected = " + selected);
        left.add("progressDown = " + progressDown);
        left.add("progressUp = " + progressUp);
    }
}
