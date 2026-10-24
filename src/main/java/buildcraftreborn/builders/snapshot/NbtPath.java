/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.List;

import com.google.gson.JsonDeserializer;
import com.google.gson.reflect.TypeToken;

import net.minecraft.nbt.ByteArrayTag;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongArrayTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.Tag;

import buildcraftreborn.lib.misc.NBTUtilBC;

/** A path into an NBT tag, by compound keys and list or array indexes. Missing values are {@link NBTUtilBC#NBT_NULL}. */
public class NbtPath {
    private final List<String> elements;

    private NbtPath(List<String> elements) {
        this.elements = elements;
    }

    public Tag get(Tag tag) {
        if (tag == null) {
            return NBTUtilBC.NBT_NULL;
        }
        if (elements.isEmpty()) {
            return tag;
        }
        if (tag instanceof CompoundTag compound) {
            String key = elements.get(0);
            return compound.contains(key) ? rest().get(compound.get(key)) : NBTUtilBC.NBT_NULL;
        }
        int index;
        try {
            index = Integer.parseInt(elements.get(0));
        } catch (NumberFormatException e) {
            return NBTUtilBC.NBT_NULL;
        }
        if (tag instanceof ListTag list) {
            return index >= 0 && index < list.size() ? rest().get(list.get(index)) : NBTUtilBC.NBT_NULL;
        }
        if (elements.size() != 1) {
            return NBTUtilBC.NBT_NULL;
        }
        if (tag instanceof ByteArrayTag array) {
            byte[] bytes = array.getAsByteArray();
            return index >= 0 && index < bytes.length ? ByteTag.valueOf(bytes[index]) : NBTUtilBC.NBT_NULL;
        }
        if (tag instanceof IntArrayTag array) {
            int[] ints = array.getAsIntArray();
            return index >= 0 && index < ints.length ? IntTag.valueOf(ints[index]) : NBTUtilBC.NBT_NULL;
        }
        if (tag instanceof LongArrayTag array) {
            long[] longs = array.getAsLongArray();
            return index >= 0 && index < longs.length ? LongTag.valueOf(longs[index]) : NBTUtilBC.NBT_NULL;
        }
        return NBTUtilBC.NBT_NULL;
    }

    private NbtPath rest() {
        return new NbtPath(elements.subList(1, elements.size()));
    }

    @Override
    public String toString() {
        return "NbtPath{" + elements + "}";
    }

    public static final JsonDeserializer<NbtPath> DESERIALIZER = (json, typeOfT, context) ->
        new NbtPath(context.deserialize(json, new TypeToken<List<String>>() {}.getType()));
}
