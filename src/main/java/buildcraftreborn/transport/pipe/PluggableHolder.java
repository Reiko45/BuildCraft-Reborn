/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.transport.tile.TilePipeHolder;

/** Holds the pluggable on one side of a pipe, and saves and syncs it. */
public final class PluggableHolder {
    private static final IdAllocator ID_ALLOC = new IdAllocator("PlugHolder");
    public static final int ID_REMOVE_PLUG = ID_ALLOC.allocId("REMOVE_PLUG");
    public static final int ID_UPDATE_PLUG = ID_ALLOC.allocId("UPDATE_PLUG");
    public static final int ID_CREATE_PLUG = ID_ALLOC.allocId("CREATE_PLUG");

    public final TilePipeHolder holder;
    public final Direction side;
    @Nullable
    public PipePluggable pluggable;

    public PluggableHolder(TilePipeHolder holder, Direction side) {
        this.holder = holder;
        this.side = side;
    }

    // Saving + Loading

    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        if (pluggable != null) {
            nbt.putString("id", pluggable.definition.identifier.toString());
            nbt.put("data", pluggable.writeToNbt(registries));
        }
        return nbt;
    }

    public void readFromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        if (pluggable != null) {
            holder.eventBus.unregisterHandler(pluggable);
        }
        if (nbt.isEmpty()) {
            pluggable = null;
            return;
        }
        String id = nbt.getString("id");
        CompoundTag data = nbt.getCompound("data");
        ResourceLocation identifier = ResourceLocation.tryParse(id);
        PluggableDefinition def = identifier == null ? null : PipeApi.pluggableRegistry.getDefinition(identifier);
        if (def == null) {
            BuildCraftReborn.LOGGER.warn("Unknown pluggable id '{}' at {}, removing it", id, holder.getBlockPos());
            pluggable = null;
        } else {
            pluggable = def.readFromNbt(holder, side, data, registries);
            holder.eventBus.registerHandler(pluggable);
        }
    }

    // Network

    /** Called by {@link TilePipeHolder#replacePluggable(Direction, PipePluggable)} to inform clients about the new
     * pluggable. */
    public void sendNewPluggableData() {
        holder.sendMessage(PipeMessageReceiver.PLUGGABLES[side.ordinal()], this::writeCreationPayload);
    }

    public void writeCreationPayload(PacketBufferBC buffer) {
        if (pluggable == null) {
            buffer.writeByte(ID_REMOVE_PLUG);
        } else {
            buffer.writeByte(ID_CREATE_PLUG);
            buffer.writeUtf(pluggable.definition.identifier.toString());
            pluggable.writeCreationPayload(buffer);
        }
    }

    public void readCreationPayload(PacketBufferBC buffer) throws IOException {
        int id = buffer.readUnsignedByte();
        if (id == ID_CREATE_PLUG) {
            readCreateInternal(buffer);
        } else if (id == ID_REMOVE_PLUG) {
            holder.eventBus.unregisterHandler(pluggable);
            pluggable = null;
        } else {
            throw new InvalidInputDataException("Invalid ID for creation! " + ID_ALLOC.getNameFor(id));
        }
    }

    private void readCreateInternal(PacketBufferBC buffer) throws IOException {
        ResourceLocation identifier = ResourceLocation.parse(buffer.readUtf(256));
        PluggableDefinition def = PipeApi.pluggableRegistry.getDefinition(identifier);
        if (def == null) {
            throw new InvalidInputDataException("Unknown remote pluggable \"" + identifier + "\"");
        }
        if (pluggable != null) {
            holder.eventBus.unregisterHandler(pluggable);
        }
        pluggable = def.loadFromBuffer(holder, side, buffer);
        holder.eventBus.registerHandler(pluggable);
    }

    public void writePayload(PacketBufferBC buffer, LogicalSide netSide) {
        if (netSide == LogicalSide.CLIENT) {
            buffer.writeByte(ID_UPDATE_PLUG);
            if (pluggable != null) {
                pluggable.writePayload(buffer, netSide);
            }
        } else {
            if (pluggable == null) {
                buffer.writeByte(ID_REMOVE_PLUG);
            } else {
                buffer.writeByte(ID_UPDATE_PLUG);
                pluggable.writePayload(buffer, netSide);
            }
        }
    }

    public void readPayload(PacketBufferBC buffer, LogicalSide netSide, @Nullable IPayloadContext ctx)
        throws IOException {
        int id = buffer.readUnsignedByte();
        if (netSide == LogicalSide.SERVER) {
            if (id == ID_UPDATE_PLUG) {
                if (pluggable != null) {
                    pluggable.readPayload(buffer, netSide, ctx);
                }
            } else {
                throw new InvalidInputDataException("Unknown ID " + ID_ALLOC.getNameFor(id));
            }
        } else {
            if (id == ID_REMOVE_PLUG) {
                holder.eventBus.unregisterHandler(pluggable);
                pluggable = null;
            } else if (id == ID_UPDATE_PLUG) {
                if (pluggable != null) {
                    pluggable.readPayload(buffer, netSide, ctx);
                }
            } else if (id == ID_CREATE_PLUG) {
                readCreateInternal(buffer);
            } else {
                throw new InvalidInputDataException("Unknown ID " + ID_ALLOC.getNameFor(id));
            }
        }
    }

    public void onTick() {
        if (pluggable != null) {
            pluggable.onTick();
        }
    }
}
