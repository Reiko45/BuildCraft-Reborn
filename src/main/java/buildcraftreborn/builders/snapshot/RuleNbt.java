/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.Map;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.mojang.serialization.JsonOps;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;

import buildcraftreborn.lib.misc.NBTUtilBC;

/** Converts between the JSON in rule files and NBT. 1.21: replaces 1.12's {@code JsonUtil} NBT (de)serializers. */
public final class RuleNbt {
    private RuleNbt() {}

    /** JSON to NBT. A JSON {@code null} (in {@code replaceNbt}, meaning "remove this key") becomes
     * {@link NBTUtilBC#NBT_NULL}. */
    public static Tag fromJson(JsonElement json) {
        if (json == null || json.isJsonNull()) {
            return NBTUtilBC.NBT_NULL;
        }
        if (json.isJsonObject()) {
            CompoundTag compound = new CompoundTag();
            for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
                compound.put(entry.getKey(), fromJson(entry.getValue()));
            }
            return compound;
        }
        return JsonOps.INSTANCE.convertTo(NbtOps.INSTANCE, json);
    }

    public static JsonElement toJson(Tag tag) {
        if (tag == null || tag == NBTUtilBC.NBT_NULL) {
            return JsonNull.INSTANCE;
        }
        return NbtOps.INSTANCE.convertTo(JsonOps.INSTANCE, tag);
    }

    public static final JsonDeserializer<Tag> DESERIALIZER = (json, typeOfT, context) -> fromJson(json);
}
