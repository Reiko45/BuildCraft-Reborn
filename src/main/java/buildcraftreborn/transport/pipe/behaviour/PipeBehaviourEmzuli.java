/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.behaviour;

import buildcraftreborn.lib.net.PacketBufferBC;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.LogicalSide;
import net.minecraft.core.HolderLookup;
import java.io.IOException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;


import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;


import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.transport.pipe.IFlowItems;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraftreborn.api.transport.pipe.PipeEventActionActivate;
import buildcraftreborn.api.transport.pipe.PipeEventStatement;

import buildcraftreborn.lib.misc.EntityUtil;
import buildcraftreborn.lib.misc.MessageUtil;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

import buildcraftreborn.transport.BCTransportGuis;
import buildcraftreborn.transport.BCTransportStatements;
import buildcraftreborn.transport.statements.ActionExtractionPreset;

public class PipeBehaviourEmzuli extends PipeBehaviourWood {

    public enum SlotIndex {
        SQUARE(DyeColor.RED),
        CIRCLE(DyeColor.GREEN),
        TRIANGLE(DyeColor.BLUE),
        CROSS(DyeColor.YELLOW);

        public static final SlotIndex[] VALUES = values();

        public final DyeColor colour;

        SlotIndex(DyeColor colour) {
            this.colour = colour;
        }

        public SlotIndex next() {
            // @formatter:off
            switch (this) {
                case SQUARE: return CIRCLE;
                case CIRCLE: return TRIANGLE;
                case TRIANGLE: return CROSS;
                case CROSS: return SQUARE;
                default: throw new IllegalStateException("Unknown SlotIndex - " + this + " - " + this.ordinal());
            }
            // @formatter:on
        }
    }

    public final EnumMap<SlotIndex, DyeColor> slotColours = new EnumMap<>(SlotIndex.class);
    public final ItemHandlerSimple invFilters = new ItemHandlerSimple(4, null);
    private final EnumSet<SlotIndex> activeSlots;
    private final byte[] activatedTtl = new byte[SlotIndex.VALUES.length];
    private SlotIndex currentSlot = null;

    private final IStackFilter filter = this::filterMatches;

    public PipeBehaviourEmzuli(IPipe pipe) {
        super(pipe);
        activeSlots = EnumSet.noneOf(SlotIndex.class);
    }

    public PipeBehaviourEmzuli(IPipe pipe, CompoundTag nbt, HolderLookup.Provider registries) {
        super(pipe, nbt, registries);
        invFilters.deserializeNBT(registries, nbt.getCompound("Filters"));
        activeSlots = NBTUtilBC.readEnumSet(nbt.get("activeSlots"), SlotIndex.class);
        currentSlot = NBTUtilBC.readEnum(nbt.get("currentSlot"), SlotIndex.class);
        for (SlotIndex index : SlotIndex.VALUES) {
            byte c = nbt.getByte("slotColors[" + index.ordinal() + "]");
            if (c > 0 && c <= 16) {
                slotColours.put(index, DyeColor.byId(c - 1));
            }
        }
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = super.writeToNbt(registries);
        nbt.put("Filters", invFilters.serializeNBT(registries));
        nbt.put("activeSlots", NBTUtilBC.writeEnumSet(activeSlots, SlotIndex.class));
        nbt.put("currentSlot", NBTUtilBC.writeEnum(currentSlot));
        for (SlotIndex index : SlotIndex.VALUES) {
            DyeColor c = slotColours.get(index);
            nbt.putByte("slotColors[" + index.ordinal() + "]", (byte) (c == null ? 0 : c.getId() + 1));
        }
        return nbt;
    }

    @Override
    public void readPayload(PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            for (SlotIndex index : SlotIndex.VALUES) {
                DyeColor colour = MessageUtil.readEnumOrNull(buffer, DyeColor.class);
                if (colour == null) {
                    slotColours.remove(index);
                } else {
                    slotColours.put(index, colour);
                }
            }
            activeSlots.clear();
            activeSlots.addAll(MessageUtil.readEnumSet(buffer, SlotIndex.class));
            currentSlot = MessageUtil.readEnumOrNull(buffer, SlotIndex.class);
        }
    }

    @Override
    public void writePayload(PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(buffer, side);
        if (side == LogicalSide.SERVER) {
            for (SlotIndex index : SlotIndex.VALUES) {
                MessageUtil.writeEnumOrNull(buffer, slotColours.get(index));
            }
            MessageUtil.writeEnumSet(buffer, activeSlots, SlotIndex.class);
            MessageUtil.writeEnumOrNull(buffer, currentSlot);
        }
    }

    @Override
    protected int extractItems(IFlowItems flow, Direction dir, int count, boolean simulate) {
        if (currentSlot == null && activeSlots.size() > 0) {
            currentSlot = getNextSlot();
        }
        if (currentSlot == null) return 0;
        int extracted = flow.tryExtractItems(count, dir, slotColours.get(currentSlot), filter, simulate);
        if (extracted > 0 && !simulate) {
            currentSlot = getNextSlot();
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
        return extracted;
    }

    private boolean filterMatches(ItemStack stack) {
        if (currentSlot == null) return false;
        ItemStack current = invFilters.getStackInSlot(currentSlot.ordinal());
        if (StackUtil.isMatchingItemOrList(current, stack)) {
            return true;
        }
        return false;
    }

    @Override
    public void onTick() {
        super.onTick();
        if (pipe.getHolder().getPipeWorld().isClientSide) {
            return;
        }
        for (SlotIndex index : SlotIndex.VALUES) {
            byte val = activatedTtl[index.ordinal()];
            if (val > 0) {
                val--;
                activatedTtl[index.ordinal()] = val;
            }
            if (val == 0) {
                activeSlots.remove(index);
                if (currentSlot == index) {
                    currentSlot = getNextSlot();
                    pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
                }
            }
        }
    }

    private SlotIndex getNextSlot() {
        SlotIndex current = currentSlot == null ? SlotIndex.CROSS : currentSlot;
        int i = SlotIndex.VALUES.length;
        while (i-- > 0) {
            current = current.next();
            if (activeSlots.contains(current) && !invFilters.getStackInSlot(current.ordinal()).isEmpty()) {
                return current;
            }
        }
        return null;
    }

    public SlotIndex getCurrentSlot() {
        return this.currentSlot;
    }

    public EnumSet<SlotIndex> getActiveSlots() {
        return this.activeSlots;
    }

    @Override
    public boolean onPipeActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
        }
        if (!player.level().isClientSide) {
            BCTransportGuis.PIPE_EMZULI.openGui(player, pipe.getHolder().getPipePos());
        }
        return true;
    }

    @Override
    public void addActions(PipeEventStatement.AddActionInternal event) {
        super.addActions(event);
        Collections.addAll(event.actions, BCTransportStatements.ACTION_EXTRACTION_PRESET);
    }

    @Override
    public void onActionActivate(PipeEventActionActivate event) {
        super.onActionActivate(event);
        if (event.action instanceof ActionExtractionPreset) {
            ActionExtractionPreset preset = (ActionExtractionPreset) event.action;
            activeSlots.add(preset.index);
            activatedTtl[preset.index.ordinal()] = 2;
        }
    }
}
