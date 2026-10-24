/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.marker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.network.PacketDistributor;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.net.MarkerPayload;
import buildcraftreborn.lib.tile.TileMarker;

/** Every marker and connection of one kind in one level, on one side. The server's copy is the real one; it tells
 * every player in the level about each change, and the client keeps a copy so it can draw the lasers. Markers in
 * unloaded chunks stay in the cache (with no tile), so connections reach into unloaded areas. */
public abstract class MarkerSubCache<C extends MarkerConnection<C>> {
    public static final boolean DEBUG_FULL = Boolean.getBoolean("buildcraftreborn.debug.markers.full");

    public final int cacheId;
    public final boolean isServer;
    protected final Level level;

    private final Map<BlockPos, C> posToConnection = new ConcurrentHashMap<>();
    private final Map<C, Set<BlockPos>> connectionToPos = new ConcurrentHashMap<>();
    private final Map<BlockPos, Optional<TileMarker<C>>> tileCache = new ConcurrentHashMap<>();

    public MarkerSubCache(Level world, int cacheId) {
        this.isServer = !world.isClientSide;
        this.level = world;
        this.cacheId = cacheId;
    }

    private void send(MarkerPayload message) {
        if (isServer && level instanceof ServerLevel server) {
            PacketDistributor.sendToPlayersInDimension(server, message);
        }
    }

    public void onPlayerJoinWorld(ServerPlayer player) {
        if (isServer) {// Sanity Check
            // Send ALL loaded markers
            if (!tileCache.isEmpty()) {
                PacketDistributor.sendToPlayer(player,
                    new MarkerPayload(true, false, cacheId, new ArrayList<>(tileCache.keySet())));
            }
            // Send ALL connections.
            for (C connection : connectionToPos.keySet()) {
                PacketDistributor.sendToPlayer(player,
                    new MarkerPayload(true, true, cacheId, new ArrayList<>(connection.getMarkerPositions())));
            }
        }
    }

    public boolean hasLoadedOrUnloadedMarker(BlockPos pos) {
        return tileCache.containsKey(pos);
    }

    @Nullable
    public TileMarker<C> getMarker(BlockPos pos) {
        Optional<TileMarker<C>> op = tileCache.get(pos);
        if (op == null) {
            return null;
        } else {
            return op.orElse(null);
        }
    }

    public void loadMarker(BlockPos pos, @Nullable TileMarker<C> marker) {
        boolean did = tileCache.containsKey(pos);
        tileCache.put(pos, Optional.ofNullable(marker));
        if (DEBUG_FULL) {
            BuildCraftReborn.LOGGER.info("[lib.marker.full] Set a marker at {} as {}", pos, marker);
        }
        if (isServer && !did) {
            send(new MarkerPayload(true, false, cacheId, List.of(pos)));
        }
    }

    public void unloadMarker(BlockPos pos) {
        loadMarker(pos, null);
    }

    public void removeMarker(BlockPos pos) {
        if (DEBUG_FULL) {
            BuildCraftReborn.LOGGER.info("[lib.marker.full] Removed a marker at {}", pos);
        }
        tileCache.remove(pos);
        C connection = getConnection(pos);
        if (connection != null) {
            connection.removeMarker(pos);
            refreshConnection(connection);
        }
        send(new MarkerPayload(false, false, cacheId, List.of(pos)));
    }

    public ImmutableList<BlockPos> getAllMarkers() {
        return ImmutableList.copyOf(tileCache.keySet());
    }

    @Nullable
    public C getConnection(BlockPos pos) {
        return posToConnection.get(pos);
    }

    public void destroyConnection(@Nullable C connection) {
        if (connection == null) {
            return;
        }
        Set<BlockPos> set = connectionToPos.remove(connection);
        if (set != null) {
            deinitConnection(set);
        }
        if (DEBUG_FULL) {
            validateAllConnections();
        }
    }

    public void addConnection(C connection) {
        Set<BlockPos> lastSeen = new HashSet<>(connection.getMarkerPositions());
        initConnection(connection, lastSeen);
        if (DEBUG_FULL) {
            validateAllConnections();
        }
    }

    public void refreshConnection(C connection) {
        Set<BlockPos> lastSeen = connectionToPos.get(connection);
        if (DEBUG_FULL) {
            BuildCraftReborn.LOGGER.info("[lib.marker.full] Refreshing a connection");
            BuildCraftReborn.LOGGER.info("[lib.marker.full]    - Old = {}", lastSeen);
            BuildCraftReborn.LOGGER.info("[lib.marker.full]    - New = {}", connection.getMarkerPositions());
        }
        if (lastSeen == null) {
            // Why did you call this?
            addConnection(connection);
        } else {
            Set<BlockPos> invalid = new HashSet<>(lastSeen);
            lastSeen = new HashSet<>(connection.getMarkerPositions());
            invalid.removeAll(lastSeen);
            deinitConnection(invalid);
            initConnection(connection, lastSeen);
            if (lastSeen.isEmpty()) {
                connectionToPos.remove(connection);
            }
        }
        if (DEBUG_FULL) {
            validateAllConnections();
        }
    }

    private void validateAllConnections() {
        final String logStart = "[lib.marker.full][" + cacheId + "]";
        Set<C> visited = new HashSet<>();
        Set<BlockPos> visitedPos = new HashSet<>();
        for (Entry<C, Set<BlockPos>> entry : connectionToPos.entrySet()) {
            C con = entry.getKey();
            Set<BlockPos> positions = entry.getValue();
            Set<BlockPos> actual = new HashSet<>(con.getMarkerPositions());
            if (!positions.equals(actual)) {
                BuildCraftReborn.LOGGER.warn("{} Positions differed! {} vs {}", logStart, positions, actual);
            }
            for (BlockPos p : positions) {
                if (visitedPos.contains(p)) {
                    BuildCraftReborn.LOGGER.warn("{} Duplicate block positions! {} - {}", logStart, p, con);
                }
                visitedPos.add(p);
            }
            visited.add(con);
        }
        for (Entry<BlockPos, C> entry : posToConnection.entrySet()) {
            C connection = entry.getValue();
            BlockPos p = entry.getKey();
            if (!visited.contains(connection)) {
                BuildCraftReborn.LOGGER.warn("{} Unknown connection {} ({})", logStart, connection, p);
            }
            if (!visitedPos.contains(p)) {
                BuildCraftReborn.LOGGER.warn("{} Unknown Position {} ({})", logStart, p, connection);
            }
        }
    }

    private void deinitConnection(Set<BlockPos> set) {
        if (DEBUG_FULL) {
            BuildCraftReborn.LOGGER.info("[lib.marker.full] Tearing down all connections in {}", set);
        }
        for (BlockPos p : set) {
            posToConnection.remove(p);
        }
        if (!set.isEmpty()) {
            send(new MarkerPayload(false, true, cacheId, new ArrayList<>(set)));
        }
    }

    private void initConnection(C connection, Set<BlockPos> lastSeen) {
        if (DEBUG_FULL) {
            BuildCraftReborn.LOGGER.info("[lib.marker.full] Setting up a connection with {}", lastSeen);
        }
        if (lastSeen.size() < 2) {
            connectionToPos.remove(connection);
            for (BlockPos p : lastSeen) {
                posToConnection.remove(p);
            }
            return;
        }
        connectionToPos.put(connection, lastSeen);
        for (BlockPos p : lastSeen) {
            posToConnection.put(p, connection);
        }
        send(new MarkerPayload(true, true, cacheId, new ArrayList<>(connection.getMarkerPositions())));
    }

    public ImmutableList<C> getConnections() {
        return ImmutableList.copyOf(connectionToPos.keySet());
    }

    public abstract boolean tryConnect(BlockPos from, BlockPos to);

    /** Checks if {@link #tryConnect(BlockPos, BlockPos)} would succeed at this time. */
    public abstract boolean canConnect(BlockPos a, BlockPos b);

    public abstract ImmutableList<BlockPos> getValidConnections(BlockPos from);

    /** Client side: applies a change the server sent. */
    public final void handleMessageMain(MarkerPayload message) {
        if (handleMessage(message)) {
            return;
        }
        if (!message.connection()) {
            Collection<BlockPos> positions = message.positions();
            if (message.add()) {
                for (BlockPos p : positions) {
                    if (!hasLoadedOrUnloadedMarker(p)) {
                        loadMarker(p, null);
                    }
                }
            } else {
                for (BlockPos p : positions) {
                    removeMarker(p);
                }
            }
        }
    }

    /** Client side: applies a connection change. @return true if the message was fully handled. */
    protected abstract boolean handleMessage(MarkerPayload message);
}
