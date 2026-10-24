/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.schematics;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.world.level.block.Block;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.api.core.BuildCraftAPI;

public class SchematicBlockFactoryRegistry {
    private static final Set<SchematicBlockFactory<?>> FACTORIES = new TreeSet<>();

    public static <S extends ISchematicBlock> void registerFactory(String name,
                                                                   int priority,
                                                                   Predicate<SchematicBlockContext> predicate,
                                                                   Supplier<S> supplier) {
        FACTORIES.add(new SchematicBlockFactory<>(
            BuildCraftAPI.nameToResourceLocation(name),
            priority,
            predicate,
            supplier
        ));
    }

    public static <S extends ISchematicBlock> void registerFactory(String name,
                                                                   int priority,
                                                                   List<Block> blocks,
                                                                   Supplier<S> supplier) {
        registerFactory(
            name,
            priority,
            context -> blocks.contains(context.block),
            supplier
        );
    }

    public static List<SchematicBlockFactory<?>> getFactories() {
        return ImmutableList.copyOf(FACTORIES);
    }

    
    public static <S extends ISchematicBlock> SchematicBlockFactory<S> getFactoryByInstance(S instance) {
        // noinspection unchecked
        return (SchematicBlockFactory<S>) FACTORIES.stream()
            .filter(schematicBlockFactory -> schematicBlockFactory.clazz == instance.getClass())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Didn't find a factory for " + instance.getClass()));
    }

    @Nullable
    public static SchematicBlockFactory<?> getFactoryByName(ResourceLocation name) {
        return FACTORIES.stream()
            .filter(schematicBlockFactory -> schematicBlockFactory.name.equals(name))
            .findFirst()
            .orElse(null);
    }
}
