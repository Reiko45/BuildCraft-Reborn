/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.util.BitSet;
import java.util.EnumSet;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.BuildCraftReborn;

/** NBT helpers for the formats 1.12 BuildCraft saved in (enums as names, enum sets as bit sets, positions as int
 * arrays). Item NBT ({@code getItemData}) is gone: items use data components. */
public final class NBTUtilBC {
    private NBTUtilBC() {}

    public static IntArrayTag writeBlockPos(BlockPos pos) {
        return new IntArrayTag(new int[] { pos.getX(), pos.getY(), pos.getZ() });
    }

    @Nullable
    public static BlockPos readBlockPos(@Nullable Tag base) {
        if (base instanceof IntArrayTag array) {
            int[] a = array.getAsIntArray();
            return a.length == 3 ? new BlockPos(a[0], a[1], a[2]) : null;
        }
        if (base instanceof CompoundTag nbt) {
            if (nbt.contains("i")) {
                return new BlockPos(nbt.getInt("i"), nbt.getInt("j"), nbt.getInt("k"));
            } else if (nbt.contains("x")) {
                return new BlockPos(nbt.getInt("x"), nbt.getInt("y"), nbt.getInt("z"));
            } else if (nbt.contains("pos")) {
                return readBlockPos(nbt.get("pos"));
            }
        }
        if (base != null) {
            BuildCraftReborn.LOGGER.warn("Attempted to read a block position from an invalid tag! ({})", base);
        }
        return null;
    }

    public static ListTag writeVec3d(Vec3 vec) {
        ListTag list = new ListTag();
        list.add(DoubleTag.valueOf(vec.x));
        list.add(DoubleTag.valueOf(vec.y));
        list.add(DoubleTag.valueOf(vec.z));
        return list;
    }

    @Nullable
    public static Vec3 readVec3d(@Nullable Tag nbt) {
        if (nbt instanceof ListTag list && list.size() == 3) {
            return new Vec3(list.getDouble(0), list.getDouble(1), list.getDouble(2));
        }
        return null;
    }

    private static final String NULL_ENUM_STRING = "_NULL";

    public static Tag writeEnum(@Nullable Enum<?> value) {
        return StringTag.valueOf(value == null ? NULL_ENUM_STRING : value.name());
    }

    @Nullable
    public static <E extends Enum<E>> E readEnum(@Nullable Tag nbt, Class<E> clazz) {
        if (nbt instanceof StringTag str) {
            String value = str.getAsString();
            if (NULL_ENUM_STRING.equals(value)) {
                return null;
            }
            try {
                return Enum.valueOf(clazz, value);
            } catch (IllegalArgumentException e) {
                BuildCraftReborn.LOGGER.warn("Tried and failed to read the value({}) from {}", value,
                    clazz.getSimpleName());
                return null;
            }
        } else if (nbt instanceof ByteTag b) {
            byte value = b.getAsByte();
            E[] constants = clazz.getEnumConstants();
            return value < 0 || value >= constants.length ? null : constants[value];
        }
        return null;
    }

    /** Writes an {@link EnumSet} as a bit set, either a {@link ByteTag} or a {@link ByteArrayTag}. */
    public static <E extends Enum<E>> Tag writeEnumSet(EnumSet<E> set, Class<E> clazz) {
        BitSet bitset = new BitSet();
        for (E e : set) {
            bitset.set(e.ordinal());
        }
        byte[] bytes = bitset.toByteArray();
        if (bytes.length == 1) {
            return ByteTag.valueOf(bytes[0]);
        }
        return new ByteArrayTag(bytes);
    }

    public static <E extends Enum<E>> EnumSet<E> readEnumSet(@Nullable Tag tag, Class<E> clazz) {
        byte[] bytes;
        if (tag instanceof ByteTag b) {
            bytes = new byte[] { b.getAsByte() };
        } else if (tag instanceof ByteArrayTag array) {
            bytes = array.getAsByteArray();
        } else {
            bytes = new byte[0];
        }
        BitSet bitset = BitSet.valueOf(bytes);
        EnumSet<E> set = EnumSet.noneOf(clazz);
        for (E e : clazz.getEnumConstants()) {
            if (bitset.get(e.ordinal())) {
                set.add(e);
            }
        }
        return set;
    }

    /** Stands for "no value" in the builders' NBT paths and merges. */
    public static final CompoundTag NBT_NULL = new CompoundTag();

    public static <N extends Tag> java.util.Optional<N> toOptional(@Nullable N value) {
        return value == null || value == NBT_NULL ? java.util.Optional.empty() : java.util.Optional.of(value);
    }

    /** Merges source into destination: compounds are merged key by key (a key set to {@link #NBT_NULL} is removed),
     * anything else is replaced by the source. */
    @Nullable
    public static Tag merge(@Nullable Tag destination, @Nullable Tag source) {
        if (source == null) {
            return null;
        }
        if (destination == null) {
            return source;
        }
        if (destination instanceof CompoundTag dst && source instanceof CompoundTag src) {
            CompoundTag result = new CompoundTag();
            java.util.Set<String> keys = new java.util.HashSet<>(dst.getAllKeys());
            keys.addAll(src.getAllKeys());
            for (String key : keys) {
                if (!src.contains(key)) {
                    result.put(key, dst.get(key));
                } else if (src.get(key) != NBT_NULL) {
                    if (!dst.contains(key)) {
                        result.put(key, src.get(key));
                    } else {
                        result.put(key, merge(dst.get(key), src.get(key)));
                    }
                }
            }
            return result;
        }
        return source;
    }
}
