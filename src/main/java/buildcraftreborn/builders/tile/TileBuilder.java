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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.IPathProvider;
import buildcraftreborn.api.enums.EnumOptionalSnapshotType;
import buildcraftreborn.api.enums.EnumSnapshotType;
import buildcraftreborn.api.inventory.IItemTransactor;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.block.BlockBuilder;
import buildcraftreborn.builders.container.ContainerBuilder;
import buildcraftreborn.builders.item.ItemSnapshot;
import buildcraftreborn.builders.snapshot.Blueprint;
import buildcraftreborn.builders.snapshot.BlueprintBuilder;
import buildcraftreborn.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraftreborn.builders.snapshot.ITileForBlueprintBuilder;
import buildcraftreborn.builders.snapshot.ITileForTemplateBuilder;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.builders.snapshot.SnapshotBuilder;
import buildcraftreborn.builders.snapshot.Template;
import buildcraftreborn.builders.snapshot.TemplateBuilder;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.fluid.TankManager;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.misc.PositionUtil;
import buildcraftreborn.lib.misc.data.Box;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.mj.MjBatteryReceiver;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** The builder: builds the blueprint or template in its snapshot slot behind itself, or at each step along a path of
 * path markers, using the items and fluids it holds.
 * <p>
 * 1.21: the snapshot is looked up again when the tile loads, and the build position along the path is saved (1.12 lost
 * both, so a builder stopped after a world reload until its snapshot was put back). The {@code snapshot_type} block
 * state is set by the tile instead of being read when drawing. */
public class TileBuilder extends TileBC_Neptune
    implements ITickable, IDebuggable, ITileForTemplateBuilder, ITileForBlueprintBuilder, MenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("builder");
    public static final int NET_CAN_EXCAVATE = IDS.allocId("CAN_EXCAVATE");
    public static final int NET_SNAPSHOT_TYPE = IDS.allocId("SNAPSHOT_TYPE");
    private static final ResourceLocation ADVANCEMENT = BuildCraftReborn.id("paving_the_way");

    public final ItemHandlerSimple invSnapshot = itemManager.addInvHandler("snapshot", 1,
        (slot, stack) -> ItemSnapshot.isUsed(stack), EnumAccess.BOTH, EnumPipePart.VALUES);
    public final ItemHandlerSimple invResources =
        itemManager.addInvHandler("resources", 27, EnumAccess.BOTH, EnumPipePart.VALUES);

    private final MjBattery battery = new MjBattery(16000 * MjAPI.MJ);
    private boolean canExcavate = true;

    /** Stores the real path - just a few block positions. */
    @Nullable
    public List<BlockPos> path = null;
    /** Stores the real path plus all possible block positions inbetween. */
    private List<BlockPos> basePoses = new ArrayList<>();
    private int currentBasePosIndex = 0;
    @Nullable
    private Snapshot snapshot = null;
    @Nullable
    public EnumSnapshotType snapshotType = null;
    private Template.BuildingInfo templateBuildingInfo = null;
    private Blueprint.BuildingInfo blueprintBuildingInfo = null;
    public final TemplateBuilder templateBuilder = new TemplateBuilder(this);
    public final BlueprintBuilder blueprintBuilder = new BlueprintBuilder(this);
    private Box currentBox = new Box();
    @Nullable
    private Rotation rotation = null;

    private boolean isDone = false;
    /** Read from NBT before the tile had a level: applied in {@link #onLoad()}. */
    @Nullable
    private CompoundTag pendingBuilderNbt;

    public TileBuilder(BlockPos pos, BlockState state) {
        super(BCBuildersBlocks.BUILDER_TILE.get(), pos, state);
        for (int i = 1; i <= 4; i++) {
            tankManager.add(new Tank("tank" + i, 1000 * 8, this) {
                @Override
                protected void onContentsChanged() {
                    super.onContentsChanged();
                    Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
                }
            });
        }
        caps.addMj(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
        caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, tankManager, EnumPipePart.VALUES);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        if (level != null && !level.isClientSide) {
            if (handler == invSnapshot) {
                currentBasePosIndex = 0;
                readSnapshot(after);
                updateSnapshot(true);
                sendNetworkUpdate(NET_SNAPSHOT_TYPE);
            }
            if (handler == invResources) {
                Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
            }
        }
        super.onSlotChange(handler, slot, before, after);
    }

    private void readSnapshot(ItemStack stack) {
        snapshot = null;
        Snapshot.Header header = ItemSnapshot.getHeader(stack);
        if (header != null) {
            snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        templateBuilder.validate();
        blueprintBuilder.validate();
        if (!level.isClientSide) {
            if (basePoses.isEmpty()) {
                updateBasePoses();
            }
            readSnapshot(invSnapshot.getStackInSlot(0));
            updateSnapshot(rotation == null);
            if (pendingBuilderNbt != null) {
                CompoundTag builderNbt = pendingBuilderNbt;
                pendingBuilderNbt = null;
                Optional.ofNullable(getBuilder()).ifPresent(b -> b.deserializeNBT(builderNbt, level.registryAccess()));
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            templateBuilder.invalidate();
            blueprintBuilder.invalidate();
        }
    }

    private void updateSnapshot(boolean canGetFacing) {
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::cancel);
        if (snapshot != null && getCurrentBasePos() != null) {
            snapshotType = snapshot.getType();
            if (canGetFacing || rotation == null) {
                Direction facing = level.getBlockState(worldPosition).getValue(BlockBCBase_Neptune.PROP_FACING);
                rotation = Arrays.stream(Rotation.values()).filter(r -> r.rotate(snapshot.facing) == facing)
                    .findFirst().orElse(Rotation.NONE);
            }
            templateBuildingInfo = null;
            blueprintBuildingInfo = null;
            if (snapshot instanceof Template template) {
                templateBuildingInfo = template.new BuildingInfo(getCurrentBasePos(), rotation);
            }
            if (snapshot instanceof Blueprint blueprint) {
                blueprintBuildingInfo = blueprint.new BuildingInfo(getCurrentBasePos(), rotation);
            }
            currentBox = Optional.ofNullable(getBuildingInfo()).map(buildingInfo -> buildingInfo.box).orElse(null);
            Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::updateSnapshot);
        } else {
            snapshotType = null;
            rotation = null;
            templateBuildingInfo = null;
            blueprintBuildingInfo = null;
            currentBox = null;
        }
        if (currentBox == null) {
            currentBox = new Box();
        }
        updateBlockState();
    }

    /** Shows what's in the snapshot slot on the block. */
    private void updateBlockState() {
        BlockState state = level.getBlockState(worldPosition);
        if (state.getBlock() instanceof BlockBuilder) {
            EnumOptionalSnapshotType wanted = EnumOptionalSnapshotType.fromNullable(snapshotType);
            if (state.getValue(BlockBuilder.SNAPSHOT_TYPE) != wanted) {
                level.setBlock(worldPosition, state.setValue(BlockBuilder.SNAPSHOT_TYPE, wanted), Block.UPDATE_CLIENTS);
            }
        }
    }

    private void updateBasePoses() {
        basePoses.clear();
        if (path != null) {
            int max = path.size() - 1;
            // Create a list of all the possible block positions on the path that could be used
            basePoses.add(path.get(0));
            for (int i = 1; i <= max; i++) {
                basePoses.addAll(PositionUtil.getAllOnPath(path.get(i - 1), path.get(i)));
            }
        } else {
            BlockState state = level.getBlockState(worldPosition);
            Direction facing = state.hasProperty(BlockBCBase_Neptune.PROP_FACING)
                ? state.getValue(BlockBCBase_Neptune.PROP_FACING) : Direction.NORTH;
            basePoses.add(worldPosition.relative(facing.getOpposite()));
        }
    }

    @Nullable
    private BlockPos getCurrentBasePos() {
        return currentBasePosIndex < basePoses.size() ? basePoses.get(currentBasePosIndex) : null;
    }

    @Override
    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        Direction facing = level.getBlockState(worldPosition).getValue(BlockBCBase_Neptune.PROP_FACING);
        BlockEntity inFront = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
        if (inFront instanceof IPathProvider provider) {
            ImmutableList<BlockPos> copiedPath = ImmutableList.copyOf(provider.getPath());
            if (copiedPath.size() >= 2) {
                path = copiedPath;
                provider.removeFromWorld();
            }
        }
        updateBasePoses();
        setChanged();
    }

    @Override
    public void update() {
        level.getProfiler().push("main");
        level.getProfiler().push("power");
        battery.tick(level, worldPosition);
        level.getProfiler().popPush("builder");
        SnapshotBuilder<?> builder = getBuilder();
        if (builder != null) {
            isDone = builder.tick();
            if (isDone && !level.isClientSide) {
                if (currentBasePosIndex < basePoses.size() - 1) {
                    currentBasePosIndex++;
                    if (currentBasePosIndex == basePoses.size() - 1 && currentBasePosIndex > 1) {
                        // (1.12 checked for an index it could never reach, so this was never given.)
                        AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT);
                    }
                    updateSnapshot(true);
                    setChanged();
                }
            }
        }
        level.getProfiler().popPush("net_update");
        if (!level.isClientSide) {
            sendNetworkUpdate(NET_RENDER_DATA);
        }
        level.getProfiler().pop();
        level.getProfiler().pop();
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeInt(path == null ? 0 : path.size());
                if (path != null) {
                    path.forEach(buffer::writeBlockPos);
                }
                SnapshotBuilder<?> builder = getBuilder();
                buffer.writeBoolean(builder != null);
                if (builder != null) {
                    buffer.writeEnum(snapshotType);
                    builder.writeToByteBuf(buffer);
                }
                currentBox.writeData(buffer);
                writePayload(NET_CAN_EXCAVATE, buffer, side);
                writePayload(NET_SNAPSHOT_TYPE, buffer, side);
            }
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.writeData(buffer);
            }
            if (id == NET_CAN_EXCAVATE) {
                buffer.writeBoolean(canExcavate);
            }
            if (id == NET_SNAPSHOT_TYPE) {
                buffer.writeEnum(EnumOptionalSnapshotType.fromNullable(snapshotType));
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                int pathSize = buffer.readInt();
                if (pathSize != 0) {
                    path = new ArrayList<>();
                    for (int i = 0; i < pathSize; i++) {
                        path.add(buffer.readBlockPos());
                    }
                } else {
                    path = null;
                }
                updateBasePoses();
                if (buffer.readBoolean()) {
                    snapshotType = buffer.readEnum(EnumSnapshotType.class);
                    getBuilder().readFromByteBuf(buffer);
                } else {
                    snapshotType = null;
                }
                currentBox.readData(buffer);
                readPayload(NET_CAN_EXCAVATE, buffer, side, ctx);
                readPayload(NET_SNAPSHOT_TYPE, buffer, side, ctx);
            }
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.readData(buffer);
            }
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
            }
            if (id == NET_SNAPSHOT_TYPE) {
                snapshotType = buffer.readEnum(EnumOptionalSnapshotType.class).type;
            }
        }
        if (side == LogicalSide.SERVER) {
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
                setChanged();
                sendNetworkUpdate(NET_CAN_EXCAVATE);
            }
        }
    }

    /** Client side: asks the server to change the "can excavate" setting. */
    public void sendCanExcavate(boolean newValue) {
        PacketDistributor.sendToServer(createMessage(NET_CAN_EXCAVATE, buffer -> buffer.writeBoolean(newValue)));
    }

    // Saving

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        if (path != null) {
            nbt.put("path", writePosList(path));
        }
        nbt.put("basePoses", writePosList(basePoses));
        nbt.putInt("currentBasePosIndex", currentBasePosIndex);
        nbt.putBoolean("canExcavate", canExcavate);
        if (rotation != null) {
            nbt.put("rotation", NBTUtilBC.writeEnum(rotation));
        }
        nbt.put("battery", battery.serializeNBT());
        if (pendingBuilderNbt != null) {
            nbt.put("builder", pendingBuilderNbt);
        } else {
            Optional.ofNullable(getBuilder()).ifPresent(builder -> nbt.put("builder", builder.serializeNBT(registries)));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        path = nbt.contains("path") ? readPosList(nbt.getList("path", Tag.TAG_INT_ARRAY)) : null;
        basePoses = readPosList(nbt.getList("basePoses", Tag.TAG_INT_ARRAY));
        currentBasePosIndex = nbt.getInt("currentBasePosIndex");
        canExcavate = nbt.getBoolean("canExcavate");
        rotation = NBTUtilBC.readEnum(nbt.get("rotation"), Rotation.class);
        battery.deserializeNBT(nbt.getCompound("battery"));
        pendingBuilderNbt = nbt.contains("builder") ? nbt.getCompound("builder") : null;
    }

    private static ListTag writePosList(List<BlockPos> list) {
        ListTag tag = new ListTag();
        for (BlockPos pos : list) {
            tag.add(NBTUtilBC.writeBlockPos(pos));
        }
        return tag;
    }

    private static List<BlockPos> readPosList(ListTag tag) {
        List<BlockPos> list = new ArrayList<>();
        for (Tag element : tag) {
            BlockPos pos = NBTUtilBC.readBlockPos(element);
            if (pos != null) {
                list.add(pos);
            }
        }
        return list;
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
        return new ContainerBuilder(windowId, player, this);
    }

    // Rendering

    public Box getBox() {
        return currentBox;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("basePoses = " + (basePoses == null ? "null" : basePoses.size()));
        left.add("currentBasePosIndex = " + currentBasePosIndex);
        left.add("isDone = " + isDone);
    }

    @Override
    public Level getWorldBC() {
        return level;
    }

    @Override
    public MjBattery getBattery() {
        return battery;
    }

    @Override
    public BlockPos getBuilderPos() {
        return worldPosition;
    }

    @Override
    public boolean canExcavate() {
        return canExcavate;
    }

    @Override
    @Nullable
    public SnapshotBuilder<?> getBuilder() {
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            return templateBuilder;
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            return blueprintBuilder;
        }
        return null;
    }

    @Nullable
    private Snapshot.BuildingInfo getBuildingInfo() {
        if (snapshotType == EnumSnapshotType.TEMPLATE) {
            return templateBuildingInfo;
        }
        if (snapshotType == EnumSnapshotType.BLUEPRINT) {
            return blueprintBuildingInfo;
        }
        return null;
    }

    @Override
    public Template.BuildingInfo getTemplateBuildingInfo() {
        return templateBuildingInfo;
    }

    @Override
    public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
        return blueprintBuildingInfo;
    }

    @Override
    public IItemTransactor getInvResources() {
        return invResources;
    }

    @Override
    public TankManager getTankManager() {
        return tankManager;
    }

    /** For GameTests. */
    public boolean isDone() {
        return isDone;
    }
}
