/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.IAreaProvider;
import buildcraftreborn.api.enums.EnumSnapshotType;
import buildcraftreborn.api.schematics.ISchematicBlock;
import buildcraftreborn.api.schematics.ISchematicEntity;
import buildcraftreborn.api.schematics.SchematicBlockContext;
import buildcraftreborn.api.schematics.SchematicEntityContext;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.block.BlockArchitectTable;
import buildcraftreborn.builders.client.ClientArchitectTables;
import buildcraftreborn.builders.container.ContainerArchitectTable;
import buildcraftreborn.builders.item.ItemSnapshot;
import buildcraftreborn.builders.snapshot.Blueprint;
import buildcraftreborn.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraftreborn.builders.snapshot.SchematicBlockManager;
import buildcraftreborn.builders.snapshot.SchematicEntityManager;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.builders.snapshot.Template;
import buildcraftreborn.core.marker.volume.Lock;
import buildcraftreborn.core.marker.volume.VolumeBox;
import buildcraftreborn.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraftreborn.lib.delta.DeltaInt;
import buildcraftreborn.lib.delta.DeltaManager;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.misc.data.Box;
import buildcraftreborn.lib.misc.data.BoxIterator;
import buildcraftreborn.lib.misc.data.EnumAxisOrder;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** The architect table: scans the area of the markers or volume box behind it into a blank blueprint or template.
 * <p>
 * 1.21: the scanned positions (drawn as a fading flash on the client) are sent once per tick in one message, and the
 * render data only once per tick, instead of two messages for every block scanned. */
public class TileArchitectTable extends TileBC_Neptune implements ITickable, IDebuggable, MenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("architect");
    public static final int NET_BOX = IDS.allocId("BOX");
    public static final int NET_SCAN = IDS.allocId("SCAN");
    private static final ResourceLocation ADVANCEMENT = BuildCraftReborn.id("architect");

    public final ItemHandlerSimple invSnapshotIn = itemManager.addInvHandler("in", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSnapshot, EnumAccess.INSERT, EnumPipePart.VALUES);
    public final ItemHandlerSimple invSnapshotOut =
        itemManager.addInvHandler("out", 1, EnumAccess.EXTRACT, EnumPipePart.VALUES);

    private EnumSnapshotType snapshotType = EnumSnapshotType.BLUEPRINT;
    public final Box box = new Box();
    public boolean markerBox = false;
    private BitSet templateScannedBlocks;
    private final List<ISchematicBlock> blueprintScannedPalette = new ArrayList<>();
    private int[] blueprintScannedData;
    private final List<ISchematicEntity> blueprintScannedEntities = new ArrayList<>();
    private BoxIterator boxIterator;
    private boolean isValid = false;
    private boolean scanning = false;
    public String name = "<unnamed>";
    public final DeltaInt deltaProgress = deltaManager.addDelta("progress",
        DeltaManager.EnumNetworkVisibility.GUI_ONLY);
    private final List<BlockPos> scannedThisTick = new ArrayList<>();

    public TileArchitectTable(BlockPos pos, BlockState state) {
        super(BCBuildersBlocks.ARCHITECT_TILE.get(), pos, state);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (level.isClientSide) {
            return;
        }
        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
        BlockState blockState = level.getBlockState(worldPosition);
        BlockPos offsetPos = worldPosition.relative(blockState.getValue(BlockArchitectTable.PROP_FACING).getOpposite());
        VolumeBox volumeBox = volumeBoxes.getVolumeBoxAt(offsetPos);
        BlockEntity tile = level.getBlockEntity(offsetPos);
        if (volumeBox != null) {
            box.reset();
            box.setMin(volumeBox.box.min());
            box.setMax(volumeBox.box.max());
            isValid = true;
            volumeBox.locks.add(new Lock(new Lock.Cause.CauseBlock(worldPosition, blockState.getBlock()),
                new Lock.Target.TargetRemove(), new Lock.Target.TargetResize(),
                new Lock.Target.TargetUsedByMachine(Lock.Target.TargetUsedByMachine.EnumType.STRIPES_READ)));
            volumeBoxes.setDirty();
            sendNetworkUpdate(NET_BOX);
        } else if (tile instanceof IAreaProvider provider) {
            box.reset();
            box.setMin(provider.min());
            box.setMax(provider.max());
            markerBox = true;
            isValid = true;
            provider.removeFromWorld();
        } else {
            isValid = false;
            level.setBlockAndUpdate(worldPosition, blockState.setValue(BlockArchitectTable.PROP_VALID, Boolean.FALSE));
        }
        setChanged();
    }

    @Override
    public void update() {
        deltaManager.tick();

        if (level.isClientSide) {
            if (box.isInitialized()) {
                ClientArchitectTables.BOXES.put(box.getBoundingBox(), ClientArchitectTables.START_BOX_VALUE);
            }
            return;
        }

        ItemStack in = invSnapshotIn.getStackInSlot(0);
        if (!in.isEmpty() && invSnapshotOut.getStackInSlot(0).isEmpty() && isValid
            && in.getItem() instanceof ItemSnapshot snapshotItem) {
            if (!scanning) {
                snapshotType = snapshotItem.snapshotType;
                int size = box.size().getX() * box.size().getY() * box.size().getZ();
                size /= snapshotType.maxPerTick;
                deltaProgress.addDelta(0, size, 1);
                deltaProgress.addDelta(size, size + 10, -1);
                scanning = true;
            }
        } else {
            scanning = false;
        }

        if (scanning) {
            scanMultipleBlocks();
            if (!scannedThisTick.isEmpty()) {
                List<BlockPos> scanned = List.copyOf(scannedThisTick);
                scannedThisTick.clear();
                createAndSendMessage(NET_SCAN, buffer -> {
                    buffer.writeVarInt(scanned.size());
                    scanned.forEach(buffer::writeBlockPos);
                });
                sendNetworkUpdate(NET_RENDER_DATA);
            }
            if (!scanning) {
                if (snapshotType == EnumSnapshotType.BLUEPRINT) {
                    scanEntities();
                }
                finishScanning();
            }
            setChanged();
        }
    }

    private void scanMultipleBlocks() {
        for (int i = snapshotType.maxPerTick; i > 0; i--) {
            scanSingleBlock();
            if (!scanning) {
                break;
            }
        }
    }

    private void scanSingleBlock() {
        BlockPos size = box.size();
        if (templateScannedBlocks == null || blueprintScannedData == null || boxIterator == null) {
            boxIterator = new BoxIterator(box, EnumAxisOrder.XZY.getMinToMaxOrder(), true);
            templateScannedBlocks = new BitSet(Snapshot.getDataSize(size));
            blueprintScannedData = new int[Snapshot.getDataSize(size)];
            blueprintScannedPalette.clear();
        }

        // Read from world
        BlockPos worldScanPos = boxIterator.getCurrent();
        BlockPos schematicPos = worldScanPos.subtract(box.min());
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            templateScannedBlocks.set(Snapshot.posToIndex(box.size(), schematicPos), !level.isEmptyBlock(worldScanPos));
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            ISchematicBlock schematicBlock = readSchematicBlock(worldScanPos);
            int index = blueprintScannedPalette.indexOf(schematicBlock);
            if (index == -1) {
                index = blueprintScannedPalette.size();
                blueprintScannedPalette.add(schematicBlock);
            }
            blueprintScannedData[Snapshot.posToIndex(box.size(), schematicPos)] = index;
        }
        scannedThisTick.add(worldScanPos);

        // Move scanPos along
        boxIterator.advance();

        if (boxIterator.hasFinished()) {
            scanning = false;
            boxIterator = null;
        }
    }

    private ISchematicBlock readSchematicBlock(BlockPos worldScanPos) {
        BlockState state = level.getBlockState(worldScanPos);
        return SchematicBlockManager.getSchematicBlock(
            new SchematicBlockContext(level, box.min(), worldScanPos, state, state.getBlock()));
    }

    private void scanEntities() {
        level.getEntitiesOfClass(Entity.class, box.getBoundingBox()).stream()
            .map(entity -> SchematicEntityManager.getSchematicEntity(
                new SchematicEntityContext(level, box.min(), entity)))
            .filter(Objects::nonNull)
            .forEach(blueprintScannedEntities::add);
    }

    private void finishScanning() {
        BlockState thisState = getCurrentStateForBlock(BCBuildersBlocks.ARCHITECT.get());
        if (thisState == null) {
            return;
        }

        Direction facing = thisState.getValue(BlockArchitectTable.PROP_FACING);
        Snapshot snapshot = Snapshot.create(snapshotType);
        snapshot.size = box.size();
        snapshot.facing = facing;
        snapshot.offset = box.min().subtract(worldPosition.relative(facing.getOpposite()));
        if (snapshot instanceof Template template) {
            template.data = templateScannedBlocks;
        }
        if (snapshot instanceof Blueprint blueprint) {
            blueprint.palette.addAll(blueprintScannedPalette);
            blueprint.data = blueprintScannedData;
            blueprint.entities.addAll(blueprintScannedEntities);
        }
        snapshot.computeKey();
        GlobalSavedDataSnapshots.get(level).addSnapshot(snapshot);
        ItemStack stackIn = invSnapshotIn.getStackInSlot(0).copy();
        stackIn.shrink(1);
        invSnapshotIn.setStackInSlot(0, stackIn.isEmpty() ? ItemStack.EMPTY : stackIn);
        invSnapshotOut.setStackInSlot(0, ItemSnapshot.getUsed(snapshotType,
            new Snapshot.Header(snapshot.key, getOwner().getId(), new Date(), name)));
        templateScannedBlocks = null;
        blueprintScannedData = null;
        blueprintScannedPalette.clear();
        blueprintScannedEntities.clear();
        boxIterator = null;
        sendNetworkUpdate(NET_RENDER_DATA);
        AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT);
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_BOX, buffer, side);
                buffer.writeUtf(name);
            } else if (id == NET_BOX) {
                box.writeData(buffer);
                buffer.writeBoolean(markerBox);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_BOX, buffer, side, ctx);
                name = buffer.readUtf();
            } else if (id == NET_BOX) {
                box.readData(buffer);
                markerBox = buffer.readBoolean();
            } else if (id == NET_SCAN) {
                int count = buffer.readVarInt();
                for (int i = 0; i < count; i++) {
                    ClientArchitectTables.SCANNED_BLOCKS.put(buffer.readBlockPos(),
                        ClientArchitectTables.START_SCANNED_BLOCK_VALUE);
                }
            }
        }
    }

    // Saving

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("box", box.writeToNBT());
        nbt.putBoolean("markerBox", markerBox);
        nbt.putBoolean("scanning", scanning);
        nbt.put("snapshotType", NBTUtilBC.writeEnum(snapshotType));
        nbt.putBoolean("isValid", isValid);
        nbt.putString("name", name);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        box.initialize(nbt.getCompound("box"));
        markerBox = nbt.getBoolean("markerBox");
        // (1.12 saved the scan position too, but not what it had scanned, so a half done scan starts again.)
        boxIterator = null;
        scanning = nbt.getBoolean("scanning");
        EnumSnapshotType type = NBTUtilBC.readEnum(nbt.get("snapshotType"), EnumSnapshotType.class);
        snapshotType = type == null ? EnumSnapshotType.BLUEPRINT : type;
        isValid = nbt.getBoolean("isValid");
        name = nbt.getString("name");
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
        return new ContainerArchitectTable(windowId, player, this);
    }

    /** Server side: the name typed into the GUI. */
    public void setName(String name) {
        this.name = name.length() > 64 ? name.substring(0, 64) : name;
        setChanged();
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    public boolean isScanning() {
        return scanning;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("box:");
        left.add(" - min = " + box.min());
        left.add(" - max = " + box.max());
        left.add("scanning = " + scanning);
        left.add("current = " + (boxIterator == null ? null : boxIterator.getCurrent()));
    }
}
