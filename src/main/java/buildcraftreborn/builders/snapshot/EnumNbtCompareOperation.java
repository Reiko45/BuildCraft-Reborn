/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.Arrays;
import java.util.Objects;

import com.google.gson.JsonDeserializer;

import net.minecraft.nbt.Tag;

/** How a rule compares a value in the NBT of a block entity or entity with the rule's value. */
public enum EnumNbtCompareOperation {
    EQ("=") {
        @Override
        public boolean compare(Tag a, Tag b) {
            // 1.12 compared the JSON forms, so 1 (byte) equals 1 (int): so does this
            return Objects.equals(RuleNbt.toJson(a), RuleNbt.toJson(b));
        }
    },
    NQE("!=") {
        @Override
        public boolean compare(Tag a, Tag b) {
            return !EQ.compare(a, b);
        }
    };

    public final String name;

    EnumNbtCompareOperation(String name) {
        this.name = name;
    }

    public static EnumNbtCompareOperation byName(String name) {
        return Arrays.stream(values())
            .filter(type -> type.name.equals(name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Compare operation not found"));
    }

    public abstract boolean compare(Tag a, Tag b);

    public static final JsonDeserializer<EnumNbtCompareOperation> DESERIALIZER = (json, typeOfT, context) ->
        byName(json.getAsJsonPrimitive().getAsString());
}
