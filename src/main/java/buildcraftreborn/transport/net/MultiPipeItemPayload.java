/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.net;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.transport.pipe.flow.PipeFlowItems;

/** Server to client: items that started moving through pipes this tick, for every pipe the player can see (1.12's
 * {@code MessageMultiPipeItem}).
 * <p>
 * 1.21: each item carries its stack (1.12 sent an id into a per-client stack cache). */
public final class MultiPipeItemPayload implements CustomPacketPayload {
    public static final Type<MultiPipeItemPayload> TYPE = new Type<>(BuildCraftReborn.id("pipe_items"));

    private static final int MAX_ITEMS_PER_PIPE = 10;
    private static final int MAX_POSITIONS = 4000;

    public static final StreamCodec<RegistryFriendlyByteBuf, MultiPipeItemPayload> STREAM_CODEC =
        StreamCodec.of((buf, payload) -> payload.write(buf), MultiPipeItemPayload::new);

    public final Map<BlockPos, List<TravellingItemData>> items = new LinkedHashMap<>();

    public MultiPipeItemPayload() {}

    private MultiPipeItemPayload(RegistryFriendlyByteBuf buf) {
        int blockCount = buf.readShort();
        for (int b = 0; b < blockCount; b++) {
            BlockPos pos = buf.readBlockPos();
            List<TravellingItemData> posItems = new ArrayList<>();
            items.put(pos, posItems);
            int itemCount = buf.readUnsignedByte();
            for (int i = 0; i < itemCount; i++) {
                posItems.add(new TravellingItemData(buf));
            }
        }
    }

    private void write(RegistryFriendlyByteBuf buf) {
        int blockCount = Math.min(items.size(), MAX_POSITIONS);
        buf.writeShort(blockCount);
        int blockIndex = 0;
        for (Entry<BlockPos, List<TravellingItemData>> entry : items.entrySet()) {
            if (blockIndex++ >= blockCount) {
                break;
            }
            buf.writeBlockPos(entry.getKey());
            List<TravellingItemData> list = entry.getValue();
            int itemCount = Math.min(list.size(), MAX_ITEMS_PER_PIPE);
            buf.writeByte(itemCount);
            for (int i = 0; i < itemCount; i++) {
                list.get(i).toBuffer(buf);
            }
        }
    }

    public void append(BlockPos pos, TravellingItemData data) {
        List<TravellingItemData> list = items.get(pos);
        if (list == null) {
            if (items.size() >= MAX_POSITIONS) {
                return;
            }
            list = new ArrayList<>();
            items.put(pos, list);
        }
        if (list.size() < MAX_ITEMS_PER_PIPE) {
            list.add(data);
        }
    }

    @Override
    public Type<MultiPipeItemPayload> type() {
        return TYPE;
    }

    /** One item entering a pipe section (towards the centre, or from the centre towards a side). */
    public record TravellingItemData(ItemStack stack, boolean toCenter, Direction side, @Nullable DyeColor colour,
        int timeToDest) {

        TravellingItemData(RegistryFriendlyByteBuf buf) {
            this(ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), buf.readBoolean(), buf.readEnum(Direction.class),
                buf.readBoolean() ? DyeColor.byId(buf.readByte()) : null, buf.readUnsignedByte());
        }

        void toBuffer(RegistryFriendlyByteBuf buf) {
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, stack);
            buf.writeBoolean(toCenter);
            buf.writeEnum(side);
            buf.writeBoolean(colour != null);
            if (colour != null) {
                buf.writeByte(colour.getId());
            }
            buf.writeByte(Math.min(timeToDest, 255));
        }
    }

    public static void handle(MultiPipeItemPayload message, IPayloadContext ctx) {
        Level level = ctx.player().level();
        for (Entry<BlockPos, List<TravellingItemData>> entry : message.items.entrySet()) {
            if (level.getBlockEntity(entry.getKey()) instanceof IPipeHolder holder) {
                IPipe pipe = holder.getPipe();
                if (pipe != null && pipe.getFlow() instanceof PipeFlowItems flow) {
                    flow.handleClientReceviedItems(entry.getValue());
                }
            }
        }
    }
}
