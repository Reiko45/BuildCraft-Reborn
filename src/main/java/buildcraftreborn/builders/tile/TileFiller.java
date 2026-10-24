/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.tile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.IAreaProvider;
import buildcraftreborn.api.core.IBox;
import buildcraftreborn.api.filler.IFillerPattern;
import buildcraftreborn.api.inventory.IItemTransactor;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.containers.IFillerStatementContainer;
import buildcraftreborn.api.tiles.IControllable;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.api.tiles.TilesAPI;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.addon.AddonFillerPlanner;
import buildcraftreborn.builders.container.ContainerFiller;
import buildcraftreborn.builders.filler.FillerType;
import buildcraftreborn.builders.filler.FillerUtil;
import buildcraftreborn.builders.snapshot.ITileForTemplateBuilder;
import buildcraftreborn.builders.snapshot.SnapshotBuilder;
import buildcraftreborn.builders.snapshot.Template;
import buildcraftreborn.builders.snapshot.TemplateBuilder;
import buildcraftreborn.core.marker.volume.Addon;
import buildcraftreborn.core.marker.volume.ClientVolumeBoxes;
import buildcraftreborn.core.marker.volume.EnumAddonSlot;
import buildcraftreborn.core.marker.volume.Lock;
import buildcraftreborn.core.marker.volume.VolumeBox;
import buildcraftreborn.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.misc.data.Box;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.mj.MjBatteryReceiver;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.statement.FullStatement;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** The filler: builds a filler pattern (box, pyramid, clear...) in the area of the markers or volume box behind it,
 * using the blocks in its inventory. With a filler planner addon on that volume box, it builds the addon's pattern.
 * <p>
 * 1.21: any {@link BlockItem} can go in its inventory (1.12 needed a list for doors, beds and so on, which are block
 * items now). The volume box addon and the builder's progress are read back once the tile has a level. */
public class TileFiller extends TileBC_Neptune implements ITickable, IDebuggable, ITileForTemplateBuilder,
    IFillerStatementContainer, IControllable, MenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("filler");
    public static final int NET_CAN_EXCAVATE = IDS.allocId("CAN_EXCAVATE");
    public static final int NET_INVERT = IDS.allocId("INVERT");
    public static final int NET_PATTERN = IDS.allocId("PATTERN");
    public static final int NET_BOX = IDS.allocId("BOX");

    public final ItemHandlerSimple invResources = itemManager.addInvHandler("resources", 27,
        (slot, stack) -> stack.getItem() instanceof BlockItem, EnumAccess.INSERT, EnumPipePart.VALUES);
    private final MjBattery battery = new MjBattery(16000 * MjAPI.MJ);
    private boolean canExcavate = true;
    public boolean inverted = false;
    private boolean finished = false;
    private byte lockedTicks = 0;
    private Mode mode = Mode.ON;

    public final Box box = new Box();
    @Nullable
    public AddonFillerPlanner addon;
    public boolean markerBox = true;

    public final FullStatement<IFillerPattern> patternStatement =
        new FullStatement<>(FillerType.INSTANCE, 4, (statement, paramIndex) -> onStatementChange());
    private Template.BuildingInfo buildingInfo;
    public final TemplateBuilder builder = new TemplateBuilder(this);

    /** Read from NBT before the tile had a level: applied in {@link #onLoad()}. */
    @Nullable
    private UUID pendingAddonBox;
    @Nullable
    private EnumAddonSlot pendingAddonSlot;
    @Nullable
    private CompoundTag pendingBuilderNbt;
    private HolderLookup.Provider pendingRegistries;

    public TileFiller(BlockPos pos, BlockState state) {
        super(BCBuildersBlocks.FILLER_TILE.get(), pos, state);
        caps.addMj(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
        caps.addCapabilityInstance(TilesAPI.CAP_CONTROLLABLE, this, EnumPipePart.VALUES);
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
        BlockState blockState = level.getBlockState(worldPosition);
        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
        BlockPos offsetPos = worldPosition.relative(blockState.getValue(BlockBCBase_Neptune.PROP_FACING).getOpposite());
        VolumeBox volumeBox = volumeBoxes.getVolumeBoxAt(offsetPos);
        BlockEntity tile = level.getBlockEntity(offsetPos);
        if (volumeBox != null) {
            addon = (AddonFillerPlanner) volumeBox.addons.values().stream()
                .filter(AddonFillerPlanner.class::isInstance).findFirst().orElse(null);
            if (addon != null) {
                volumeBox.locks.add(new Lock(new Lock.Cause.CauseBlock(worldPosition, blockState.getBlock()),
                    new Lock.Target.TargetAddon(addon.getSlot()), new Lock.Target.TargetRemove(),
                    new Lock.Target.TargetResize(),
                    new Lock.Target.TargetUsedByMachine(Lock.Target.TargetUsedByMachine.EnumType.STRIPES_WRITE)));
                volumeBoxes.setDirty();
                addon.updateBuildingInfo();
                markerBox = false;
            } else {
                box.reset();
                box.setMin(volumeBox.box.min());
                box.setMax(volumeBox.box.max());
                volumeBox.locks.add(new Lock(new Lock.Cause.CauseBlock(worldPosition, blockState.getBlock()),
                    new Lock.Target.TargetRemove(), new Lock.Target.TargetResize(),
                    new Lock.Target.TargetUsedByMachine(Lock.Target.TargetUsedByMachine.EnumType.STRIPES_WRITE)));
                volumeBoxes.setDirty();
                markerBox = false;
            }
        } else if (tile instanceof IAreaProvider provider) {
            box.reset();
            box.setMin(provider.min());
            box.setMax(provider.max());
            provider.removeFromWorld();
        }
        updateBuildingInfo();
        sendNetworkUpdate(NET_RENDER_DATA);
        setChanged();
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        if (level != null && !level.isClientSide && handler == invResources) {
            Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::resourcesChanged);
        }
        super.onSlotChange(handler, slot, before, after);
    }

    @Override
    public void update() {
        if (level.isClientSide) {
            if (isValid()) {
                builder.tick();
            }
            patternStatement.canInteract = !isLocked();
            return;
        }
        sendNetworkUpdate(NET_RENDER_DATA);
        lockedTicks--;
        if (lockedTicks < 0) {
            lockedTicks = 0;
        }
        if (mode == Mode.OFF) {
            return;
        }
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::tick);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        builder.validate();
        if (!level.isClientSide) {
            if (pendingAddonBox != null && pendingAddonSlot != null) {
                VolumeBox volumeBox = WorldSavedDataVolumeBoxes.get(level).getVolumeBoxFromId(pendingAddonBox);
                Addon found = volumeBox == null ? null : volumeBox.addons.get(pendingAddonSlot);
                addon = found instanceof AddonFillerPlanner planner ? planner : null;
            }
            pendingAddonBox = null;
            pendingAddonSlot = null;
            updateBuildingInfo();
            if (pendingBuilderNbt != null) {
                CompoundTag builderNbt = pendingBuilderNbt;
                pendingBuilderNbt = null;
                Optional.ofNullable(getBuilder())
                    .ifPresent(b -> b.deserializeNBT(builderNbt, level.registryAccess()));
            }
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null) {
            builder.invalidate();
        }
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                builder.writeToByteBuf(buffer);
                writePayload(NET_BOX, buffer, side);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                writePayload(NET_CAN_EXCAVATE, buffer, side);
                writePayload(NET_INVERT, buffer, side);
                writePayload(NET_PATTERN, buffer, side);
                builder.writeToByteBuf(buffer);
                buffer.writeBoolean(finished);
                buffer.writeBoolean(lockedTicks > 0);
                buffer.writeEnum(mode);
            } else if (id == NET_BOX) {
                box.writeData(buffer);
                buffer.writeBoolean(markerBox);
                buffer.writeBoolean(addon != null);
                if (addon != null) {
                    buffer.writeUUID(addon.volumeBox.id);
                    buffer.writeEnum(addon.getSlot());
                }
            } else if (id == NET_CAN_EXCAVATE) {
                buffer.writeBoolean(canExcavate);
            } else if (id == NET_INVERT) {
                buffer.writeBoolean(inverted);
            } else if (id == NET_PATTERN) {
                patternStatement.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                builder.readFromByteBuf(buffer);
                readPayload(NET_BOX, buffer, side, ctx);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                readPayload(NET_CAN_EXCAVATE, buffer, side, ctx);
                readPayload(NET_INVERT, buffer, side, ctx);
                readPayload(NET_PATTERN, buffer, side, ctx);
                builder.readFromByteBuf(buffer);
                finished = buffer.readBoolean();
                lockedTicks = buffer.readBoolean() ? (byte) 1 : (byte) 0;
                mode = buffer.readEnum(Mode.class);
            } else if (id == NET_BOX) {
                box.readData(buffer);
                markerBox = buffer.readBoolean();
                addon = null;
                if (buffer.readBoolean()) {
                    UUID volumeBoxId = buffer.readUUID();
                    EnumAddonSlot slot = buffer.readEnum(EnumAddonSlot.class);
                    for (VolumeBox volumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
                        if (volumeBox.id.equals(volumeBoxId)
                            && volumeBox.addons.get(slot) instanceof AddonFillerPlanner planner) {
                            addon = planner;
                            break;
                        }
                    }
                }
            } else if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
            } else if (id == NET_INVERT) {
                inverted = buffer.readBoolean();
            } else if (id == NET_PATTERN) {
                patternStatement.readFromBuffer(buffer);
            }
        }
        if (side == LogicalSide.SERVER) {
            if (id == NET_CAN_EXCAVATE) {
                canExcavate = buffer.readBoolean();
                setChanged();
                sendNetworkGuiUpdate(NET_CAN_EXCAVATE);
            }
        }
    }

    private void updateBuildingInfo() {
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::cancel);
        buildingInfo = (hasBox() && addon == null) ? FillerUtil.createBuildingInfo(this, patternStatement,
            IntStream.range(0, patternStatement.maxParams).mapToObj(patternStatement::get)
                .toArray(IStatementParameter[]::new),
            inverted) : null;
        Optional.ofNullable(getBuilder()).ifPresent(SnapshotBuilder::updateSnapshot);
    }

    /** Client side: asks the server to change the "can excavate" setting. */
    public void sendCanExcavate(boolean newValue) {
        PacketDistributor.sendToServer(createMessage(NET_CAN_EXCAVATE, buffer -> buffer.writeBoolean(newValue)));
    }

    public void onStatementChange() {
        if (level != null && !level.isClientSide) {
            createAndSendMessage(NET_PATTERN, patternStatement::writeToBuffer);
            setChanged();
        }
        finished = false;
        if (level != null) {
            updateBuildingInfo();
        }
    }

    // Saving

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("battery", battery.serializeNBT());
        nbt.putBoolean("canExcavate", canExcavate);
        nbt.putBoolean("inverted", inverted);
        nbt.putBoolean("finished", finished);
        nbt.putByte("lockedTicks", lockedTicks);
        nbt.put("mode", NBTUtilBC.writeEnum(mode));
        nbt.put("box", box.writeToNBT());
        if (addon != null) {
            nbt.putUUID("addonVolumeBoxId", addon.volumeBox.id);
            nbt.put("addonSlot", NBTUtilBC.writeEnum(addon.getSlot()));
        }
        nbt.putBoolean("markerBox", markerBox);
        nbt.put("patternStatement", patternStatement.writeToNbt(registries));
        if (pendingBuilderNbt != null) {
            nbt.put("builder", pendingBuilderNbt);
        } else {
            Optional.ofNullable(getBuilder()).ifPresent(b -> nbt.put("builder", b.serializeNBT(registries)));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        battery.deserializeNBT(nbt.getCompound("battery"));
        canExcavate = nbt.getBoolean("canExcavate");
        inverted = nbt.getBoolean("inverted");
        finished = nbt.getBoolean("finished");
        lockedTicks = nbt.getByte("lockedTicks");
        mode = Optional.ofNullable(NBTUtilBC.readEnum(nbt.get("mode"), Mode.class)).orElse(Mode.ON);
        box.initialize(nbt.getCompound("box"));
        if (nbt.contains("addonSlot") && nbt.hasUUID("addonVolumeBoxId")) {
            pendingAddonBox = nbt.getUUID("addonVolumeBoxId");
            pendingAddonSlot = NBTUtilBC.readEnum(nbt.get("addonSlot"), EnumAddonSlot.class);
        }
        markerBox = nbt.getBoolean("markerBox");
        patternStatement.readFromNbt(nbt.getCompound("patternStatement"), registries);
        pendingBuilderNbt = nbt.contains("builder") ? nbt.getCompound("builder") : null;
    }

    // GUI

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (!hasBox()) {
            return false;
        }
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
        return new ContainerFiller(windowId, player, this);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("box = " + box);
        left.add("pattern = " + patternStatement.get());
        left.add("mode = " + mode);
        left.add("is_finished = " + finished);
        left.add("lockedTicks = " + lockedTicks);
        left.add("addon = " + addon);
        left.add("markerBox = " + markerBox);
    }

    // ITileForTemplateBuilder

    @Override
    public Level getWorldBC() {
        return level;
    }

    public int getCountToPlace() {
        return builder.leftToPlace;
    }

    public int getCountToBreak() {
        return builder.leftToBreak;
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

    public boolean isFinished() {
        return mode != Mode.LOOP && this.finished;
    }

    public boolean isLocked() {
        return lockedTicks > 0;
    }

    @Override
    @Nullable
    public TemplateBuilder getBuilder() {
        return isValid() ? builder : null;
    }

    @Override
    @Nullable
    public Template.BuildingInfo getTemplateBuildingInfo() {
        return isValid() ? addon != null ? addon.buildingInfo : buildingInfo : null;
    }

    @Override
    public IItemTransactor getInvResources() {
        return invResources;
    }

    // IFillerStatementContainer

    @Override
    public BlockEntity getTile() {
        return this;
    }

    @Override
    public Level getFillerWorld() {
        return level;
    }

    @Override
    public boolean hasBox() {
        return addon != null || box.isInitialized();
    }

    public boolean isValid() {
        return hasBox() && level != null
            && (level.isClientSide || (addon != null ? addon.buildingInfo : buildingInfo) != null);
    }

    @Override
    public IBox getBox() {
        if (!hasBox()) {
            throw new IllegalStateException("Called getBox() when hasBox() returned false!");
        }
        return addon != null ? addon.volumeBox.box : box;
    }

    @Override
    /** Called every tick by a gate's filler pattern action. Unlike 1.12 the pattern is rebuilt when the gate changes
     * it (1.12 only rebuilt it for GUI changes), but only when it really changed. */
    public void setPattern(IFillerPattern pattern, IStatementParameter[] params) {
        IFillerPattern before = patternStatement.get();
        IStatementParameter[] beforeParams = patternStatement.getParameters().clone();
        patternStatement.set(pattern, params);
        lockedTicks = 3;
        if (before != pattern || !Arrays.equals(beforeParams, patternStatement.getParameters())) {
            finished = false;
            onStatementChange();
        }
    }

    // IControllable

    @Override
    public Mode getControlMode() {
        return mode;
    }

    @Override
    public void setControlMode(Mode mode) {
        if (this.mode == Mode.OFF && mode != Mode.OFF) {
            finished = false;
        }
        this.mode = mode;
        setChanged();
    }
}
