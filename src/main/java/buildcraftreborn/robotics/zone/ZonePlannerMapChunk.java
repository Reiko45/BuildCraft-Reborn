/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.zone;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.MapColor;

/** The top block (height and map colour) of each column of a chunk, as the zone planner's map shows it. */
public class ZonePlannerMapChunk {
    public static final StreamCodec<FriendlyByteBuf, ZonePlannerMapChunk> STREAM_CODEC =
        StreamCodec.of((buf, chunk) -> chunk.write(buf), ZonePlannerMapChunk::new);

    private final MapColourData[][] data = new MapColourData[16][16];

    /** Scans down each column, from the key's level (or the surface, if that is lower) to the first block that has
     * a map colour.
     * <p>
     * 1.21: starts from the surface heightmap rather than always from the top of the level, and goes down to the
     * bottom of the world (which can be below 0). The caller makes sure that the chunk is loaded. */
    public ZonePlannerMapChunk(Level level, ZonePlannerMapChunkKey key) {
        LevelChunk chunk = level.getChunk(key.chunkPos().x, key.chunkPos().z);
        int top = Math.min(key.level() * ZonePlannerMapChunkKey.LEVEL_HEIGHT, level.getMaxBuildHeight() - 1);
        int bottom = level.getMinBuildHeight();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int surface = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                for (int y = Math.min(top, surface); y >= bottom; y--) {
                    pos.set(key.chunkPos().getMinBlockX() + x, y, key.chunkPos().getMinBlockZ() + z);
                    int colour = chunk.getBlockState(pos).getMapColor(level, pos).col;
                    if (colour != MapColor.NONE.col) {
                        data[x][z] = new MapColourData(y, colour);
                        break;
                    }
                }
            }
        }
    }

    private ZonePlannerMapChunk(FriendlyByteBuf buffer) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // 1.21: a flag for empty columns (1.12 wrote a height of -1, which is now a real height)
                if (buffer.readBoolean()) {
                    data[x][z] = new MapColourData(buffer.readInt(), buffer.readInt());
                }
            }
        }
    }

    private void write(FriendlyByteBuf buffer) {
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                MapColourData colour = data[x][z];
                buffer.writeBoolean(colour != null);
                if (colour != null) {
                    buffer.writeInt(colour.posY);
                    buffer.writeInt(colour.colour);
                }
            }
        }
    }

    /** @return The RGB map colour of a column (block coordinates, any chunk), or -1 if it has no block. */
    public int getColour(int x, int z) {
        MapColourData col = getData(x, z);
        return col == null ? -1 : col.colour;
    }

    @Nullable
    public MapColourData getData(int x, int z) {
        return data[x & 15][z & 15];
    }

    /** @param posY The height of the column's top block.
     * @param colour Its RGB map colour. */
    public record MapColourData(int posY, int colour) {}
}
