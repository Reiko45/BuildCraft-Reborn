/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.zone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.core.IZone;

/** A zone drawn in the zone planner: a set of block columns, stored per chunk.
 * <p>
 * 1.21 fixes: {@link #distanceToSquared} measures to each chunk's centre (1.12 shifted by 12 bits instead of adding
 * 8), and {@link #getAll()} no longer lists some columns twice. */
public class ZonePlan implements IZone {
    /** A block column (x and z). */
    public record Point(int x, int z) {}

    /** A zone plan this big (in chunks) is refused when read from a client. */
    public static final int MAX_CHUNKS = 64 * 64;

    private final HashMap<ChunkPos, ZoneChunk> chunkMapping = new HashMap<>();

    public ZonePlan() {}

    public ZonePlan(ZonePlan old) {
        for (Map.Entry<ChunkPos, ZoneChunk> entry : old.chunkMapping.entrySet()) {
            chunkMapping.put(entry.getKey(), new ZoneChunk(entry.getValue()));
        }
    }

    public boolean get(int x, int z) {
        ZoneChunk property = chunkMapping.get(new ChunkPos(x >> 4, z >> 4));
        return property != null && property.get(x & 0xF, z & 0xF);
    }

    public void set(int x, int z, boolean val) {
        ChunkPos chunkId = new ChunkPos(x >> 4, z >> 4);
        ZoneChunk property = chunkMapping.get(chunkId);
        if (property == null) {
            if (val) {
                property = new ZoneChunk();
                chunkMapping.put(chunkId, property);
            } else {
                return;
            }
        }
        property.set(x & 0xF, z & 0xF, val);
        if (property.isEmpty()) {
            chunkMapping.remove(chunkId);
        }
    }

    /** @return Every column in the zone. */
    public List<Point> getAll() {
        List<Point> all = new ArrayList<>();
        chunkMapping.forEach((chunkPos, zoneChunk) -> {
            for (Point p : zoneChunk.getAll()) {
                all.add(new Point(p.x() + chunkPos.getMinBlockX(), p.z() + chunkPos.getMinBlockZ()));
            }
        });
        return all;
    }

    public ZonePlan getWithOffset(int offsetX, int offsetZ) {
        ZonePlan zonePlan = new ZonePlan();
        getAll().forEach(p -> zonePlan.set(p.x() + offsetX, p.z() + offsetZ, true));
        return zonePlan;
    }

    public boolean hasChunk(ChunkPos chunkPos) {
        return chunkMapping.containsKey(chunkPos);
    }

    public Set<ChunkPos> getChunkPoses() {
        return chunkMapping.keySet();
    }

    public Map<ChunkPos, ZoneChunk> getChunkMapping() {
        return chunkMapping;
    }

    public boolean isEmpty() {
        return chunkMapping.isEmpty();
    }

    public void writeToNBT(CompoundTag nbt) {
        ListTag list = new ListTag();
        for (Map.Entry<ChunkPos, ZoneChunk> entry : chunkMapping.entrySet()) {
            CompoundTag zoneChunkTag = new CompoundTag();
            entry.getValue().writeToNBT(zoneChunkTag);
            zoneChunkTag.putInt("chunkX", entry.getKey().x);
            zoneChunkTag.putInt("chunkZ", entry.getKey().z);
            list.add(zoneChunkTag);
        }
        nbt.put("chunkMapping", list);
    }

    public ZonePlan readFromNBT(CompoundTag nbt) {
        chunkMapping.clear();
        ListTag list = nbt.getList("chunkMapping", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            CompoundTag zoneChunkTag = list.getCompound(i);
            ZoneChunk chunk = new ZoneChunk();
            chunk.readFromNBT(zoneChunkTag);
            if (!chunk.isEmpty()) {
                chunkMapping.put(new ChunkPos(zoneChunkTag.getInt("chunkX"), zoneChunkTag.getInt("chunkZ")), chunk);
            }
        }
        return this;
    }

    public CompoundTag toNBT() {
        CompoundTag nbt = new CompoundTag();
        writeToNBT(nbt);
        return nbt;
    }

    @Override
    public double distanceTo(BlockPos index) {
        return Math.sqrt(distanceToSquared(index));
    }

    @Override
    public double distanceToSquared(BlockPos index) {
        double maxSqrDistance = Double.MAX_VALUE;
        for (ChunkPos chunk : chunkMapping.keySet()) {
            double dx = (chunk.x << 4) + 8 - index.getX();
            double dz = (chunk.z << 4) + 8 - index.getZ();
            double sqrDistance = dx * dx + dz * dz;
            if (sqrDistance < maxSqrDistance) {
                maxSqrDistance = sqrDistance;
            }
        }
        return maxSqrDistance;
    }

    @Override
    public boolean contains(Vec3 point) {
        return get((int) Math.floor(point.x), (int) Math.floor(point.z));
    }

    @Override
    @Nullable
    public BlockPos getRandomBlockPos(RandomSource rand) {
        if (chunkMapping.isEmpty()) {
            return null;
        }
        int chunkId = rand.nextInt(chunkMapping.size());
        for (Map.Entry<ChunkPos, ZoneChunk> e : chunkMapping.entrySet()) {
            if (chunkId == 0) {
                BlockPos i = e.getValue().getRandomBlockPos(rand);
                return new BlockPos((e.getKey().x << 4) + i.getX(), i.getY(), (e.getKey().z << 4) + i.getZ());
            }
            chunkId--;
        }
        return null;
    }

    public ZonePlan readFromByteBuf(FriendlyByteBuf buf) {
        chunkMapping.clear();
        int size = buf.readInt();
        if (size < 0 || size > MAX_CHUNKS) {
            throw new IllegalArgumentException("Too many chunks in a zone plan: " + size);
        }
        for (int i = 0; i < size; i++) {
            ChunkPos key = new ChunkPos(buf.readInt(), buf.readInt());
            ZoneChunk value = new ZoneChunk();
            value.readFromByteBuf(buf);
            if (!value.isEmpty()) {
                chunkMapping.put(key, value);
            }
        }
        return this;
    }

    public void writeToByteBuf(FriendlyByteBuf buf) {
        buf.writeInt(chunkMapping.size());
        for (Map.Entry<ChunkPos, ZoneChunk> e : chunkMapping.entrySet()) {
            buf.writeInt(e.getKey().x);
            buf.writeInt(e.getKey().z);
            e.getValue().writeToByteBuf(buf);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ZonePlan other)) return false;
        return getAll().stream().collect(java.util.stream.Collectors.toSet())
            .equals(other.getAll().stream().collect(java.util.stream.Collectors.toSet()));
    }

    @Override
    public int hashCode() {
        return chunkMapping.keySet().hashCode();
    }
}
