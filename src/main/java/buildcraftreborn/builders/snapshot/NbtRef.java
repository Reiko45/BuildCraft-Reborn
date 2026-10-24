/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.nbt.Tag;

import buildcraftreborn.lib.misc.NBTUtilBC;

/** A value in a rule: either written in the rule, or read from NBT by a path.
 * <p>
 * In JSON a path is an array of keys (or {@code {"ref": [...]}} where the value itself would be an array); anything
 * else is the value. 1.21: values are kept as plain tags and converted where they are used, so a number written in
 * a rule works whatever NBT number type the game uses. */
public class NbtRef {
    private final NbtPath path;
    private final Tag value;

    private NbtRef(NbtPath path, Tag value) {
        this.path = path;
        this.value = value;
    }

    public Optional<Tag> get(Tag nbt) {
        if (path != null) {
            return NBTUtilBC.toOptional(path.get(nbt));
        }
        return NBTUtilBC.toOptional(value);
    }

    public Optional<String> getString(Tag nbt) {
        return get(nbt).map(Tag::getAsString);
    }

    public Optional<Integer> getInt(Tag nbt) {
        return get(nbt).map(tag -> tag instanceof net.minecraft.nbt.NumericTag number ? number.getAsInt()
            : Integer.parseInt(tag.getAsString()));
    }

    @Override
    public String toString() {
        return path != null ? "NbtRef{path=" + path + "}" : "NbtRef{value=" + value + "}";
    }

    public static final TypeAdapterFactory TYPE_ADAPTER_FACTORY = new TypeAdapterFactory() {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() != NbtRef.class) {
                return null;
            }
            return (TypeAdapter<T>) new TypeAdapter<NbtRef>() {
                @Override
                public void write(JsonWriter out, NbtRef value) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public NbtRef read(JsonReader in) throws IOException {
                    JsonElement json = gson.fromJson(in, JsonElement.class);
                    if (json.isJsonArray()) {
                        return new NbtRef(gson.fromJson(json, NbtPath.class), null);
                    }
                    if (json.isJsonObject() && json.getAsJsonObject().has("ref")) {
                        Map<String, NbtPath> map = gson.fromJson(json, new TypeToken<Map<String, NbtPath>>() {}
                            .getType());
                        return new NbtRef(map.get("ref"), null);
                    }
                    return new NbtRef(null, RuleNbt.fromJson(json));
                }
            };
        }
    };
}
