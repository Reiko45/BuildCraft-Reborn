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

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.api.core.BuildCraftAPI;

public class SchematicEntityFactoryRegistry {
    private static final Set<SchematicEntityFactory<?>> FACTORIES = new TreeSet<>();

    public static <S extends ISchematicEntity> void registerFactory(String name,
                                                                    int priority,
                                                                    Predicate<SchematicEntityContext> predicate,
                                                                    Supplier<S> supplier) {
        FACTORIES.add(new SchematicEntityFactory<>(
            BuildCraftAPI.nameToResourceLocation(name),
            priority,
            predicate,
            supplier
        ));
    }

    public static <S extends ISchematicEntity> void registerFactory(String name,
                                                                    int priority,
                                                                    List<ResourceLocation> entities,
                                                                    Supplier<S> supplier) {
        registerFactory(
            name,
            priority,
            context -> entities.contains(BuiltInRegistries.ENTITY_TYPE.getKey(context.entity.getType())),
            supplier
        );
    }

    public static List<SchematicEntityFactory<?>> getFactories() {
        return ImmutableList.copyOf(FACTORIES);
    }

    
    public static <S extends ISchematicEntity> SchematicEntityFactory<S> getFactoryByInstance(S instance) {
        // noinspection unchecked
        return (SchematicEntityFactory<S>) FACTORIES.stream()
            .filter(schematicEntityFactory -> schematicEntityFactory.clazz == instance.getClass())
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("Didn't find a factory for " + instance.getClass()));
    }

    @Nullable
    public static SchematicEntityFactory<?> getFactoryByName(ResourceLocation name) {
        return FACTORIES.stream()
            .filter(schematicEntityFactory -> schematicEntityFactory.name.equals(name))
            .findFirst()
            .orElse(null);
    }
}
