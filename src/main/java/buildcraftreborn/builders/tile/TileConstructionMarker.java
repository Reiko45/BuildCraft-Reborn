/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.builders.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.enums.EnumSnapshotType;
import buildcraftreborn.api.inventory.IItemTransactor;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.api.tools.IToolWrench;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.item.ItemSnapshot;
import buildcraftreborn.builders.snapshot.Blueprint;
import buildcraftreborn.builders.snapshot.BlueprintBuilder;
import buildcraftreborn.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraftreborn.builders.snapshot.ITileForBlueprintBuilder;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.builders.snapshot.SnapshotBuilder;
import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.fluid.TankManager;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.lib.misc.data.Box;
import buildcraftreborn.lib.mj.MjBatteryReceiver;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** A construction marker holds a blueprint, and builder robots build it: they bring the items it needs and the power
 * to place them.
 * <p>
 * 1.21: rebuilt on 8.0's builder (7.1's construction marker used 7.1's builder, which 8.0 replaced). The blueprint is
 * placed as a builder standing on the marker, facing the player who placed it, would place it. The marker keeps what
 * robots bring in its own inventory and tanks, which it drops when broken. Engines can power it too. */
public class TileConstructionMarker extends TileBC_Neptune implements ITickable, IDebuggable, ITileForBlueprintBuilder {
    private static final Set<TileConstructionMarker> MARKERS = Collections.newSetFromMap(new WeakHashMap<>());

    public final ItemHandlerSimple invSnapshot = itemManager.addInvHandler("snapshot", 1,
        (slot, stack) -> isBlueprint(stack), EnumAccess.NONE);
    public final ItemHandlerSimple invResources = itemManager.addInvHandler("resources", 27, EnumAccess.NONE);
    private final MjBattery battery = new MjBattery(4000 * MjAPI.MJ);
    public final BlueprintBuilder builder = new BlueprintBuilder(this);

    /** Where the blueprint's front faces: towards the player that placed the marker. */
    private Direction direction = Direction.NORTH;
    @Nullable
    private Snapshot snapshot;
    @Nullable
    private Blueprint.BuildingInfo buildingInfo;
    public final Box box = new Box();
    /** Client: there is a blueprint being built. */
    private boolean hasBlueprint;
    private boolean done;
    @Nullable
    private CompoundTag pendingBuilderNbt;

    public TileConstructionMarker(BlockPos pos, BlockState state) {
        super(BCBuildersBlocks.CONSTRUCTION_MARKER_TILE.get(), pos, state);
        for (int i = 1; i <= 4; i++) {
            tankManager.add(new Tank("tank" + i, 1000 * 8, this) {
                @Override
                protected void onContentsChanged() {
                    super.onContentsChanged();
                    builder.resourcesChanged();
                }
            });
        }
        caps.addMj(new MjCapabilityHelper(new MjBatteryReceiver(battery)));
    }

    private static boolean isBlueprint(ItemStack stack) {
        return ItemSnapshot.isUsed(stack) && stack.getItem() instanceof ItemSnapshot snapshotItem
            && snapshotItem.snapshotType == EnumSnapshotType.BLUEPRINT;
    }

    /** @return The loaded markers of every server level. */
    public static Collection<TileConstructionMarker> getMarkers() {
        return List.copyOf(MARKERS);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        builder.validate();
        if (!level.isClientSide) {
            MARKERS.add(this);
            readSnapshot();
            if (pendingBuilderNbt != null && buildingInfo != null) {
                builder.deserializeNBT(pendingBuilderNbt, level.registryAccess());
            }
            pendingBuilderNbt = null;
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        MARKERS.remove(this);
        if (level != null) {
            builder.invalidate();
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        MARKERS.remove(this);
    }

    @Override
    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer != null) {
            direction = placer.getDirection().getOpposite();
        }
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        if (level != null && !level.isClientSide) {
            if (handler == invSnapshot) {
                readSnapshot();
                sendNetworkUpdate(NET_RENDER_DATA);
            } else if (handler == invResources) {
                builder.resourcesChanged();
            }
        }
        super.onSlotChange(handler, slot, before, after);
    }

    private void readSnapshot() {
        builder.cancel();
        snapshot = null;
        buildingInfo = null;
        done = false;
        Snapshot.Header header = ItemSnapshot.getHeader(invSnapshot.getStackInSlot(0));
        if (header != null) {
            snapshot = GlobalSavedDataSnapshots.get(level).getSnapshot(header.key);
        }
        if (snapshot instanceof Blueprint blueprint) {
            Rotation rotation = Arrays.stream(Rotation.values()).filter(r -> r.rotate(snapshot.facing) == direction)
                .findFirst().orElse(Rotation.NONE);
            buildingInfo = blueprint.new BuildingInfo(worldPosition.relative(direction.getOpposite()), rotation);
            builder.updateSnapshot();
        } else {
            snapshot = null;
        }
        box.reset();
        if (buildingInfo != null) {
            box.initialize(buildingInfo.box);
        }
        setChanged();
    }

    @Override
    public void update() {
        battery.tick(level, worldPosition);
        if (getBuilder() != null) {
            boolean finished = builder.tick();
            if (!level.isClientSide && finished != done) {
                done = finished;
                setChanged();
            }
        }
        if (!level.isClientSide) {
            sendNetworkUpdate(NET_RENDER_DATA);
        }
    }

    // Using the marker

    /** A blueprint goes in (7.1); sneaking with an empty hand or a wrench takes it back out. */
    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (isBlueprint(held)) {
            if (invSnapshot.getStackInSlot(0).isEmpty()) {
                if (!level.isClientSide) {
                    invSnapshot.setStackInSlot(0, held.copyWithCount(1));
                    if (!player.getAbilities().instabuild) {
                        held.shrink(1);
                    }
                }
                return true;
            }
            return false;
        }
        if (player.isShiftKeyDown() && (held.isEmpty() || held.getItem() instanceof IToolWrench)) {
            ItemStack stored = invSnapshot.getStackInSlot(0);
            if (stored.isEmpty()) {
                return false;
            }
            if (!level.isClientSide) {
                invSnapshot.setStackInSlot(0, ItemStack.EMPTY);
                if (!player.addItem(stored)) {
                    player.drop(stored, false);
                }
            }
            return true;
        }
        return false;
    }

    // For builder robots

    /** @return True if there is a blueprint still to build. */
    public boolean needsToBuild() {
        return !isRemoved() && buildingInfo != null && !done;
    }

    /** @return What the rest of the blueprint needs, less what the marker already has. */
    public List<ItemStack> getNeededItems() {
        List<ItemStack> needed = new ArrayList<>();
        for (ItemStack required : builder.remainingDisplayRequired) {
            int count = required.getCount();
            FluidStack fluid = FluidUtil.getFluidContained(required).orElse(FluidStack.EMPTY);
            if (!fluid.isEmpty()) {
                count -= tankManager.drain(fluid.copyWithAmount(Integer.MAX_VALUE), FluidAction.SIMULATE)
                    .getAmount() / Math.max(1, fluid.getAmount());
            } else {
                for (ItemStack has : invResources.stacks) {
                    if (StackUtil.canMerge(required, has)) {
                        count -= has.getCount();
                    }
                }
            }
            if (count > 0) {
                needed.add(required.copyWithCount(count));
            }
        }
        return needed;
    }

    /** Takes items a robot brought. Fluid containers are emptied into the tanks.
     *
     * @return What didn't fit (or the empty container). */
    public ItemStack addResources(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack single = stack.copyWithCount(1);
        IFluidHandlerItem handler = single.getCapability(Capabilities.FluidHandler.ITEM);
        if (handler != null && stack.getCount() == 1) {
            FluidStack contained = handler.drain(Integer.MAX_VALUE, FluidAction.SIMULATE);
            if (!contained.isEmpty() && tankManager.fill(contained, FluidAction.SIMULATE) == contained.getAmount()) {
                tankManager.fill(handler.drain(Integer.MAX_VALUE, FluidAction.EXECUTE), FluidAction.EXECUTE);
                return handler.getContainer();
            }
        }
        ItemStack left = stack;
        for (int i = 0; i < invResources.getSlots() && !left.isEmpty(); i++) {
            left = invResources.insertItem(i, left, false);
        }
        return left;
    }

    /** @return The power the marker could take. */
    public long getPowerWanted() {
        return battery.getCapacity() - battery.getStored();
    }

    public void addPower(long microJoules) {
        battery.addPower(Math.min(microJoules, getPowerWanted()), false);
    }

    // ITileForBlueprintBuilder

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
        return true;
    }

    @Override
    @Nullable
    public SnapshotBuilder<?> getBuilder() {
        boolean active = level != null && level.isClientSide ? hasBlueprint : buildingInfo != null;
        return active ? builder : null;
    }

    @Override
    public Blueprint.BuildingInfo getBlueprintBuildingInfo() {
        return buildingInfo;
    }

    @Override
    public IItemTransactor getInvResources() {
        return invResources;
    }

    @Override
    public TankManager getTankManager() {
        return tankManager;
    }

    public Direction getDirection() {
        return direction;
    }

    // Networking and saving

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER && id == NET_RENDER_DATA) {
            buffer.writeEnum(direction);
            buffer.writeBoolean(buildingInfo != null);
            if (buildingInfo != null) {
                builder.writeToByteBuf(buffer);
            }
            box.writeData(buffer);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT && id == NET_RENDER_DATA) {
            direction = buffer.readEnum(Direction.class);
            hasBlueprint = buffer.readBoolean();
            if (hasBlueprint) {
                builder.readFromByteBuf(buffer);
            }
            box.readData(buffer);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putByte("direction", (byte) direction.get3DDataValue());
        nbt.put("battery", battery.serializeNBT());
        nbt.putBoolean("done", done);
        if (pendingBuilderNbt != null) {
            nbt.put("builder", pendingBuilderNbt);
        } else if (buildingInfo != null) {
            nbt.put("builder", builder.serializeNBT(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        direction = Direction.from3DDataValue(nbt.getByte("direction"));
        if (direction.getAxis() == Direction.Axis.Y) {
            direction = Direction.NORTH;
        }
        battery.deserializeNBT(nbt.getCompound("battery"));
        pendingBuilderNbt = nbt.contains("builder") ? nbt.getCompound("builder") : null;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("direction = " + direction);
        left.add("battery = " + battery.getDebugString());
        left.add("blueprint = " + (snapshot == null ? (hasBlueprint ? "(client)" : "none") : snapshot.key));
        left.add("done = " + done);
        left.add("left to place = " + builder.leftToPlace + ", to break = " + builder.leftToBreak);
        if (!level.isClientSide) {
            left.add("needs = " + getNeededItems());
        }
    }
}
