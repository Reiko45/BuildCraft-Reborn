/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.tile;

import java.io.IOException;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.items.IMapLocation.MapLocationType;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.item.ItemMapLocation;
import buildcraftreborn.core.item.ItemPaintbrush_BC8;
import buildcraftreborn.lib.delta.DeltaInt;
import buildcraftreborn.lib.delta.DeltaManager.EnumNetworkVisibility;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.lib.tile.item.StackInsertionFunction;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.container.ContainerZonePlanner;
import buildcraftreborn.robotics.zone.ZonePlan;

/** The zone planner: sixteen zones (one per paintbrush colour), painted on a map in its GUI, and copied to and from
 * map locations. The zones are kept relative to the planner.
 * <ul>
 * <li>input (bottom left): a paintbrush and a zone map location give the brush colour's zone the map's zone, and
 * leave a clean map location.</li>
 * <li>output (top right): a paintbrush and any map location give the map location the brush colour's zone.</li>
 * </ul>
 * Each takes {@link #PROCESS_TICKS} ticks. 1.21: both can work at once (1.12's input stopped the output while it
 * worked), and only players using the GUI can change a zone. */
public class TileZonePlanner extends TileBC_Neptune implements ITickable, IDebuggable, MenuProvider {
    protected static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("zone_planner");
    public static final int NET_PLAN_CHANGE = IDS.allocId("PLAN_CHANGE");

    public static final int PROCESS_TICKS = 200;
    /** The most chunks a zone sent by a client may have (a client message can't be much bigger). */
    public static final int MAX_SENT_CHUNKS = 512;

    public final ItemHandlerSimple invPaintbrushes = itemManager.addInvHandler("paintbrushes", 16,
        (slot, stack) -> stack.getItem() instanceof ItemPaintbrush_BC8, EnumAccess.NONE);
    public final ItemHandlerSimple invInputPaintbrush = itemManager.addInvHandler("inputPaintbrush", 1,
        (slot, stack) -> stack.getItem() instanceof ItemPaintbrush_BC8, EnumAccess.NONE);
    // 1.21: the map slots take one map from a stack (1.12 refused stacks of maps)
    public final ItemHandlerSimple invInputMapLocation = itemManager.addInvHandler("inputMapLocation", 1,
        (slot, stack) -> isZoneMap(stack), StackInsertionFunction.getInsertionFunction(1), EnumAccess.NONE);
    public final ItemHandlerSimple invInputResult = itemManager.addInvHandler("inputResult", 1, EnumAccess.NONE);
    public final ItemHandlerSimple invOutputPaintbrush = itemManager.addInvHandler("outputPaintbrush", 1,
        (slot, stack) -> stack.getItem() instanceof ItemPaintbrush_BC8, EnumAccess.NONE);
    public final ItemHandlerSimple invOutputMapLocation = itemManager.addInvHandler("outputMapLocation", 1,
        (slot, stack) -> stack.getItem() instanceof ItemMapLocation, StackInsertionFunction.getInsertionFunction(1),
        EnumAccess.NONE);
    public final ItemHandlerSimple invOutputResult = itemManager.addInvHandler("outputResult", 1, EnumAccess.NONE);

    private int progressInput = -1;
    public final DeltaInt deltaProgressInput = deltaManager.addDelta("progressInput", EnumNetworkVisibility.GUI_ONLY);
    private int progressOutput = -1;
    public final DeltaInt deltaProgressOutput =
        deltaManager.addDelta("progressOutput", EnumNetworkVisibility.GUI_ONLY);
    public final ZonePlan[] layers = new ZonePlan[16];

    public TileZonePlanner(BlockPos pos, BlockState state) {
        super(BCRoboticsBlocks.ZONE_PLANNER_TILE.get(), pos, state);
        for (int i = 0; i < layers.length; i++) {
            layers[i] = new ZonePlan();
        }
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private static boolean isZoneMap(ItemStack stack) {
        return stack.getItem() instanceof ItemMapLocation
            && ItemMapLocation.getData(stack).type() == MapLocationType.ZONE;
    }

    /** @return The colour of a paintbrush, or null if it isn't a paintbrush or is clean. */
    @Nullable
    public static DyeColor getBrushColour(ItemStack stack) {
        if (!(stack.getItem() instanceof ItemPaintbrush_BC8)) {
            return null;
        }
        return stack.get(BCItemComponents.COLOUR.get());
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                for (ZonePlan layer : layers) {
                    layer.writeToByteBuf(buffer);
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
                for (ZonePlan layer : layers) {
                    layer.readFromByteBuf(buffer);
                }
            }
        } else if (side == LogicalSide.SERVER) {
            if (id == NET_PLAN_CHANGE && ctx != null) {
                int index = buffer.readUnsignedShort();
                ZonePlan plan = new ZonePlan().readFromByteBuf(buffer);
                receivePlanChange(ctx.player(), index, plan);
            }
        }
    }

    /** Server side: a player painted a zone in the GUI.
     * @return True if the zone was changed. */
    public boolean receivePlanChange(Player player, int index, ZonePlan plan) {
        if (index < 0 || index >= layers.length || !getUsingPlayers().contains(player)
            || plan.getChunkPoses().size() > MAX_SENT_CHUNKS) {
            return false;
        }
        layers[index] = plan;
        setChanged();
        sendNetworkUpdate(NET_RENDER_DATA);
        return true;
    }

    /** Client side: sends a zone (after the player painted it) to the server. */
    public void sendLayerToServer(int index) {
        if (layers[index].getChunkPoses().size() > MAX_SENT_CHUNKS) {
            BuildCraftReborn.LOGGER.warn("[robotics.zone_planner] Not sending a zone with more than {} chunks",
                MAX_SENT_CHUNKS);
            return;
        }
        PacketDistributor.sendToServer(createMessage(NET_PLAN_CHANGE, buffer -> {
            buffer.writeShort(index);
            layers[index].writeToByteBuf(buffer);
        }));
    }

    // Saving

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        for (int i = 0; i < layers.length; i++) {
            nbt.put("layer_" + i, layers[i].toNBT());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        for (int i = 0; i < layers.length; i++) {
            layers[i].readFromNBT(nbt.getCompound("layer_" + i));
        }
    }

    // Ticking

    @Override
    public void update() {
        deltaManager.tick();
        if (level.isClientSide) {
            return;
        }
        tickInput();
        tickOutput();
    }

    private void tickInput() {
        DyeColor colour = getBrushColour(invInputPaintbrush.getStackInSlot(0));
        ItemStack map = invInputMapLocation.getStackInSlot(0);
        if (colour == null || !isZoneMap(map) || !invInputResult.getStackInSlot(0).isEmpty()) {
            if (progressInput != -1) {
                progressInput = -1;
                deltaProgressInput.setValue(0);
            }
            return;
        }
        if (progressInput == -1) {
            progressInput = 0;
            deltaProgressInput.addDelta(0, PROCESS_TICKS, 1);
            deltaProgressInput.addDelta(PROCESS_TICKS, PROCESS_TICKS + 5, -1);
        }
        if (progressInput < PROCESS_TICKS) {
            progressInput++;
            return;
        }
        ZonePlan zone = new ZonePlan().readFromNBT(ItemMapLocation.getData(map).zone());
        layers[colour.getId()] = zone.getWithOffset(-worldPosition.getX(), -worldPosition.getZ());
        invInputMapLocation.setStackInSlot(0, ItemStack.EMPTY);
        invInputResult.setStackInSlot(0, new ItemStack(BCCoreItems.MAP_LOCATION.get()));
        setChanged();
        sendNetworkUpdate(NET_RENDER_DATA);
        progressInput = -1;
    }

    private void tickOutput() {
        DyeColor colour = getBrushColour(invOutputPaintbrush.getStackInSlot(0));
        ItemStack map = invOutputMapLocation.getStackInSlot(0);
        if (colour == null || !(map.getItem() instanceof ItemMapLocation)
            || !invOutputResult.getStackInSlot(0).isEmpty()) {
            if (progressOutput != -1) {
                progressOutput = -1;
                deltaProgressOutput.setValue(0);
            }
            return;
        }
        if (progressOutput == -1) {
            progressOutput = 0;
            deltaProgressOutput.addDelta(0, PROCESS_TICKS, 1);
            deltaProgressOutput.addDelta(PROCESS_TICKS, PROCESS_TICKS + 5, -1);
        }
        if (progressOutput < PROCESS_TICKS) {
            progressOutput++;
            return;
        }
        ItemStack result = map.copy();
        ItemMapLocation.setZone(result, layers[colour.getId()].getWithOffset(worldPosition.getX(),
            worldPosition.getZ()));
        invOutputResult.setStackInSlot(0, result);
        invOutputMapLocation.setStackInSlot(0, ItemStack.EMPTY);
        progressOutput = -1;
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
        return new ContainerZonePlanner(windowId, player, this);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("progress_input = " + progressInput);
        left.add("progress_output = " + progressOutput);
        int used = 0;
        for (ZonePlan layer : layers) {
            if (!layer.isEmpty()) {
                used++;
            }
        }
        left.add("zones = " + used);
    }
}
