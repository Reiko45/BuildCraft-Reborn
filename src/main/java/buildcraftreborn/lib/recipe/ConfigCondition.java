/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.lib.recipe;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BooleanSupplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.neoforged.neoforge.common.conditions.ICondition;

/** A data condition ({@code "type": "buildcraftreborn:config"}) that is true while a BuildCraft config switch is on:
 * recipes for things a config can turn off (the RF engine, the MJ dynamo) use it. 1.12 did this by not registering
 * the item at all, which also dropped its recipe. */
public record ConfigCondition(String flag) implements ICondition {
    private static final Map<String, BooleanSupplier> FLAGS = new ConcurrentHashMap<>();

    public static final MapCodec<ConfigCondition> CODEC = RecordCodecBuilder.mapCodec(
        instance -> instance.group(Codec.STRING.fieldOf("flag").forGetter(ConfigCondition::flag))
            .apply(instance, ConfigCondition::new));

    /** Makes a config switch usable by name in {@code buildcraftreborn:config} conditions. */
    public static void registerFlag(String name, BooleanSupplier value) {
        FLAGS.put(name, value);
    }

    /** @return The current value of the named switch (false for unknown names). */
    public static boolean isEnabled(String name) {
        BooleanSupplier value = FLAGS.get(name);
        return value != null && value.getAsBoolean();
    }

    @Override
    public boolean test(IContext context) {
        return isEnabled(flag);
    }

    @Override
    public MapCodec<? extends ICondition> codec() {
        return CODEC;
    }
}
