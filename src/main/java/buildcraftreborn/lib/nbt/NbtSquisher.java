/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.nbt;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;

import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.data.NbtSquishConstants;

/** Writes and reads BuildCraft's saved NBT files (snapshots).
 * <p>
 * 1.21: only the vanilla formats (plain and gzip, with or without BuildCraft's 2-byte header) are written and read.
 * 1.12 also had its own dictionary format ({@code BUILDCRAFT_V1}), but never wrote it; reading it is refused. */
public class NbtSquisher {
    private static final int TYPE_MC_GZIP = NbtSquishConstants.VANILLA_COMPRESSED;
    private static final int TYPE_MC = NbtSquishConstants.VANILLA;

    public static void squishVanilla(CompoundTag nbt, OutputStream to) throws IOException {
        to.write(NbtSquishConstants.BUILDCRAFT_MAGIC_1);
        to.write(NbtSquishConstants.BUILDCRAFT_MAGIC_2);
        to.write(TYPE_MC_GZIP);
        NbtIo.writeCompressed(nbt, to);
    }

    public static void squishVanillaUncompressed(CompoundTag nbt, DataOutput to) throws IOException {
        to.writeShort(NbtSquishConstants.BUILDCRAFT_MAGIC);
        to.write(TYPE_MC);
        NbtIo.write(nbt, to);
    }

    public static CompoundTag expand(InputStream stream) throws IOException {
        return expand(stream, NbtAccounter.unlimitedHeap());
    }

    /** As {@link #expand(InputStream)}, but refuses data that would take more memory than the accounter allows (for
     * NBT sent by a client). */
    public static CompoundTag expand(InputStream stream, NbtAccounter accounter) throws IOException {
        if (!stream.markSupported()) {
            stream = new BufferedInputStream(stream);
        }
        stream.mark(5);
        int byte1 = stream.read();
        int byte2 = stream.read();

        if (byte1 == NbtSquishConstants.BUILDCRAFT_MAGIC_1 && byte2 == NbtSquishConstants.BUILDCRAFT_MAGIC_2) {
            return readType(stream, stream.read(), accounter);
        } else if (byte1 == NbtSquishConstants.GZIP_MAGIC_1 && byte2 == NbtSquishConstants.GZIP_MAGIC_2) {
            stream.reset();
            return NbtIo.readCompressed(stream, accounter);
        }
        // An older file, with only the type byte (or a plain vanilla compound)
        stream.reset();
        stream.mark(5);
        int type = stream.read();
        if (type == Tag.TAG_COMPOUND) {
            stream.reset();
            return NbtIo.read(new DataInputStream(stream), accounter);
        }
        return readType(stream, type, accounter);
    }

    private static CompoundTag readType(InputStream stream, int type, NbtAccounter accounter) throws IOException {
        if (type == TYPE_MC) {
            return NbtIo.read(new DataInputStream(stream), accounter);
        } else if (type == TYPE_MC_GZIP) {
            return NbtIo.readCompressed(stream, accounter);
        }
        throw new InvalidInputDataException("Cannot handle BuildCraft saved NBT type " + type);
    }
}
