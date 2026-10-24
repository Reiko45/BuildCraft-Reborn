/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.chunkload;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import it.unimi.dsi.fastutil.longs.LongSet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.common.world.chunk.TicketHelper;
import net.neoforged.neoforge.common.world.chunk.TicketSet;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.BCLibConfig;

/** Keeps the chunks of BuildCraft machines loaded.
 * <p>
 * 1.21: NeoForge's {@link TicketController} replaces Forge's tickets. Tickets are owned by the machine's position and
 * saved by NeoForge; when a level loads they are checked against the machines that are still there. */
public class ChunkLoaderManager {
    public static final TicketController CONTROLLER =
        new TicketController(BuildCraftReborn.id("chunk_loader"), ChunkLoaderManager::validateTickets);

    /** The chunks each machine has forced, per level. */
    private static final Map<Level, Map<BlockPos, Set<ChunkPos>>> FORCED = new WeakHashMap<>();

    public static void onRegisterControllers(RegisterTicketControllersEvent event) {
        event.register(CONTROLLER);
    }

    /** This should be called when a tile entity that might be able to load chunks is loaded or changes the chunks it
     * needs. A check is performed to see if the config allows it. */
    public static <T extends BlockEntity & IChunkLoadingTile> void loadChunksForTile(T tile) {
        if (!(tile.getLevel() instanceof ServerLevel)) {
            return;
        }
        if (!canLoadFor(tile)) {
            releaseChunksFor(tile);
            return;
        }
        updateChunksFor(tile);
    }

    public static <T extends BlockEntity & IChunkLoadingTile> void releaseChunksFor(T tile) {
        if (!(tile.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Set<ChunkPos> forced = forcedFor(level).remove(tile.getBlockPos());
        if (forced != null) {
            for (ChunkPos pos : forced) {
                CONTROLLER.forceChunk(level, tile.getBlockPos(), pos.x, pos.z, false, true);
            }
        }
    }

    private static <T extends BlockEntity & IChunkLoadingTile> void updateChunksFor(T tile) {
        ServerLevel level = (ServerLevel) tile.getLevel();
        BlockPos owner = tile.getBlockPos();
        Set<ChunkPos> forced = forcedFor(level).computeIfAbsent(owner, p -> new HashSet<>());
        Set<ChunkPos> chunks = getChunksToLoad(tile);
        for (ChunkPos pos : Set.copyOf(forced)) {
            if (!chunks.contains(pos)) {
                CONTROLLER.forceChunk(level, owner, pos.x, pos.z, false, true);
                forced.remove(pos);
            }
        }
        for (ChunkPos pos : chunks) {
            if (forced.add(pos)) {
                CONTROLLER.forceChunk(level, owner, pos.x, pos.z, true, true);
            }
        }
    }

    public static <T extends BlockEntity & IChunkLoadingTile> Set<ChunkPos> getChunksToLoad(T tile) {
        Set<ChunkPos> chunksToLoad = tile.getChunksToLoad();
        Set<ChunkPos> chunkPoses = new HashSet<>(chunksToLoad != null ? chunksToLoad : Collections.emptyList());
        chunkPoses.add(new ChunkPos(tile.getBlockPos()));
        return chunkPoses;
    }

    private static Map<BlockPos, Set<ChunkPos>> forcedFor(Level level) {
        return FORCED.computeIfAbsent(level, l -> new HashMap<>());
    }

    /** 1.12's {@code rebindTickets}: drops the tickets of machines that are gone or may no longer load chunks, and
     * remembers the rest so later updates can unforce chunks the machine stops needing. */
    private static void validateTickets(ServerLevel level, TicketHelper helper) {
        Map<BlockPos, Set<ChunkPos>> forced = forcedFor(level);
        forced.clear();
        for (Map.Entry<BlockPos, TicketSet> entry : Map.copyOf(helper.getBlockTickets()).entrySet()) {
            BlockPos pos = entry.getKey();
            boolean keep = BCLibConfig.chunkLoadLevel() != BCLibConfig.ChunkLoaderLevel.NONE;
            if (keep) {
                BlockEntity tile = level.getBlockEntity(pos);
                keep = tile instanceof IChunkLoadingTile loader && canLoadFor(loader);
            }
            if (!keep) {
                helper.removeAllTickets(pos);
                continue;
            }
            Set<ChunkPos> chunks = new HashSet<>();
            addAll(chunks, entry.getValue().ticking());
            addAll(chunks, entry.getValue().nonTicking());
            forced.put(pos, chunks);
        }
    }

    private static void addAll(Set<ChunkPos> to, LongSet chunks) {
        chunks.forEach((long chunk) -> to.add(new ChunkPos(chunk)));
    }

    private static boolean canLoadFor(IChunkLoadingTile tile) {
        IChunkLoadingTile.LoadType type = tile.getLoadType();
        return type != null && BCLibConfig.chunkLoadLevel().canLoad(type);
    }
}
