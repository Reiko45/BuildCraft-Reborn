/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.net;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.util.Mth;

/** Special {@link RegistryFriendlyByteBuf} class that provides methods specific to "offset" reading and writing - like
 * writing a single bit to the stream, and auto-compacting it with similar bits into a single byte.
 * <p>
 * 1.21: extends {@link RegistryFriendlyByteBuf} (rather than 1.12's {@code PacketBuffer}) so that item stacks, fluid
 * stacks and data components can be written with their vanilla stream codecs. */
public class PacketBufferBC extends RegistryFriendlyByteBuf {

    // Byte-based flag access
    private int readPartialOffset = 8;// so it resets down to 0 and reads a byte on read
    private int readPartialCache;

    /** The byte position that is currently being written to. -1 means that no bytes have been written to yet. */
    private int writePartialIndex = -1;
    /** The current bit based offset, used to add successive flags into the cached value held in
     * {@link #writePartialCache} */
    private int writePartialOffset;
    /** Holds the current set of flags that will be written out. This only saves having a read */
    private int writePartialCache;

    public PacketBufferBC(ByteBuf wrapped, RegistryAccess registries) {
        super(wrapped, registries);
    }

    /** Returns the given {@link ByteBuf} as {@link PacketBufferBC}. if the given instance is already a
     * {@link PacketBufferBC} then the given buffer is returned (note that this may result in unexpected consequences if
     * multiple read/write Boolean methods are called on the given buffer before you called this). */
    public static PacketBufferBC asPacketBufferBc(ByteBuf buf, RegistryAccess registries) {
        if (buf instanceof PacketBufferBC bc) {
            return bc;
        } else {
            return new PacketBufferBC(buf, registries);
        }
    }

    public static PacketBufferBC write(RegistryAccess registries, IPayloadWriter writer) {
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer(), registries);
        writer.write(buffer);
        return buffer;
    }

    /** @return The readable bytes of this buffer, copied into a new array. */
    public byte[] toByteArray() {
        byte[] bytes = new byte[readableBytes()];
        getBytes(readerIndex(), bytes);
        return bytes;
    }

    @Override
    public PacketBufferBC clear() {
        super.clear();
        readPartialOffset = 8;
        readPartialCache = 0;
        writePartialIndex = -1;
        writePartialOffset = 0;
        writePartialCache = 0;
        return this;
    }

    void writePartialBitsBegin() {
        if (writePartialIndex == -1 || writePartialOffset == 8) {
            writePartialIndex = writerIndex();
            writePartialOffset = 0;
            writePartialCache = 0;
            writeByte(0);
        }
    }

    void readPartialBitsBegin() {
        if (readPartialOffset == 8) {
            readPartialOffset = 0;
            readPartialCache = readUnsignedByte();
        }
    }

    /** Writes a single boolean out to some position in this buffer. The boolean flag might be written to a new byte
     * (increasing the writerIndex) or it might be added to an existing byte that was written with a previous call to
     * this method. */
    @Override
    public PacketBufferBC writeBoolean(boolean flag) {
        writePartialBitsBegin();
        int toWrite = (flag ? 1 : 0) << writePartialOffset;
        writePartialCache |= toWrite;
        writePartialOffset++;
        setByte(writePartialIndex, writePartialCache);
        return this;
    }

    /** Reads a single boolean from some position in this buffer. The boolean flag might be read from a new byte
     * (increasing the readerIndex) or it might be read from a previous byte that was read with a previous call to this
     * method. */
    @Override
    public boolean readBoolean() {
        readPartialBitsBegin();
        int offset = 1 << readPartialOffset++;
        return (readPartialCache & offset) == offset;
    }

    /** Writes a fixed number of bits out to the stream.
     *
     * @param value the value to write out.
     * @param length The number of bits to write.
     * @return This buffer.
     * @throws IllegalArgumentException if the length argument was less than 1 or greater than 32. */
    public PacketBufferBC writeFixedBits(int value, int length) throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("Tried to write too few bits! (" + length + ")");
        }
        if (length > 32) {
            throw new IllegalArgumentException("Tried to write more bits than are in an integer! (" + length + ")");
        }

        writePartialBitsBegin();

        // first stage: take the toppermost bits and append them to the cache (if the cache contains bits)
        if (writePartialOffset > 0) {
            // top length = 8 - (num bits in cache) or length, whichever is SMALLER
            int availableBits = 8 - writePartialOffset;

            if (availableBits >= length) {
                int mask = (1 << length) - 1;
                int bitsToWrite = value & mask;

                writePartialCache |= bitsToWrite << writePartialOffset;
                setByte(writePartialIndex, writePartialCache);
                writePartialOffset += length;
                // we just wrote out the entire length, no need to do anything else.
                return this;
            } else { // topLength < length -- we will still need to be writing out more bits after this
                int mask = (1 << availableBits) - 1;
                int shift = length - availableBits;
                int bitsToWrite = (value >>> shift) & mask;

                writePartialCache |= bitsToWrite << writePartialOffset;
                setByte(writePartialIndex, writePartialCache);

                // we finished a byte, reset values so that the next write will reset and create a new byte
                writePartialCache = 0;
                writePartialOffset = 8;

                // now shift the value down ready for the next iteration
                length -= availableBits;
            }
        }

        while (length >= 8) {
            // write out full 8 bit chunks of the length until we reach 0
            writePartialBitsBegin();

            int byteToWrite = (value >>> (length - 8)) & 0xFF;

            setByte(writePartialIndex, byteToWrite);

            // we finished a byte, reset values so that the next write will reset and create a new byte
            writePartialCache = 0;
            writePartialOffset = 8;

            length -= 8;
        }

        if (length > 0) {
            // we have a few bits left over to append
            writePartialBitsBegin();

            int mask = (1 << length) - 1;
            writePartialCache = value & mask;
            setByte(writePartialIndex, writePartialCache);
            writePartialOffset = length;
        }

        return this;
    }

    /** @return The read bits, compacted into an int.
     * @throws IllegalArgumentException if the length argument was less than 1 or greater than 32. */
    public int readFixedBits(int length) throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("Tried to read too few bits! (" + length + ")");
        }
        if (length > 32) {
            throw new IllegalArgumentException("Tried to read more bits than are in an integer! (" + length + ")");
        }
        readPartialBitsBegin();

        int value = 0;

        if (readPartialOffset > 0) {
            // If we have bits left at the top of the buffer...
            int availableBits = 8 - readPartialOffset;
            if (availableBits >= length) {
                // If the wanted bits are completely contained within the cache
                int mask = (1 << length) - 1;
                value = (readPartialCache >>> readPartialOffset) & mask;
                readPartialOffset += length;
                return value;
            } else {
                // If we need to read more bits than are available in the cache
                value = readPartialCache >>> readPartialOffset;

                // We finished reading a byte, reset values so the next step will read them properly
                readPartialCache = 0;
                readPartialOffset = 8;

                length -= availableBits;
            }
        }

        while (length >= 8) {
            readPartialBitsBegin();
            length -= 8;
            value <<= 8;
            value |= readPartialCache;
            readPartialOffset = 8;
        }

        if (length > 0) {
            readPartialBitsBegin();

            int mask = (1 << length) - 1;

            value <<= length;
            value |= readPartialCache & mask;
            readPartialOffset = length;
        }

        return value;
    }

    /** Writes an enum using the minimum number of bits for its constant count. */
    @Override
    public PacketBufferBC writeEnum(Enum<?> value) {
        Enum<?>[] possible = value.getDeclaringClass().getEnumConstants();
        if (possible == null) throw new IllegalArgumentException("Not an enum " + value.getClass());
        if (possible.length == 0) throw new IllegalArgumentException("Tried to write an enum value without any values!");
        if (possible.length == 1) return this;
        writeFixedBits(value.ordinal(), Mth.ceillog2(possible.length));
        return this;
    }

    @Override
    public <E extends Enum<E>> E readEnum(Class<E> enumClass) {
        // No need to lookup the declaring class as you cannot refer to sub-classes of Enum.
        E[] enums = enumClass.getEnumConstants();
        if (enums == null) throw new IllegalArgumentException("Not an enum " + enumClass);
        if (enums.length == 0) throw new IllegalArgumentException("Tried to read an enum value without any values!");
        if (enums.length == 1) return enums[0];
        int length = Mth.ceillog2(enums.length);
        int index = readFixedBits(length);
        return enums[index];
    }

    /** Writes a string of any length (1.21's {@code writeUtf} limits length to 32767 characters). */
    public PacketBufferBC writeLongString(String string) {
        byte[] bytes = string.getBytes(StandardCharsets.UTF_8);
        writeVarInt(bytes.length);
        writeBytes(bytes);
        return this;
    }

    /** Reads a string of any possible length, written by {@link #writeLongString(String)}. */
    public String readLongString() {
        int length = readVarInt();
        byte[] array = new byte[length];
        readBytes(array);
        return new String(array, StandardCharsets.UTF_8);
    }
}
