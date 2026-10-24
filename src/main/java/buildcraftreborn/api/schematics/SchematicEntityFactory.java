/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.schematics;

import java.util.function.Predicate;
import java.util.function.Supplier;


import net.minecraft.resources.ResourceLocation;

public class SchematicEntityFactory<S extends ISchematicEntity> implements Comparable<SchematicEntityFactory<?>> {
    
    public final ResourceLocation name;
    public final int priority;
    
    public final Predicate<SchematicEntityContext> predicate;
    
    public final Supplier<S> supplier;
    
    public final Class<S> clazz;

    @SuppressWarnings("unchecked")
    public SchematicEntityFactory(ResourceLocation name,
                                  int priority,
                                  Predicate<SchematicEntityContext> predicate,
                                  Supplier<S> supplier) {
        this.name = name;
        this.priority = priority;
        this.predicate = predicate;
        this.supplier = supplier;
        clazz = (Class<S>) supplier.get().getClass();
    }

    @Override
    public int compareTo(SchematicEntityFactory o) {
        return priority != o.priority
                ? Integer.compare(priority, o.priority)
                : name.toString().compareTo(o.name.toString());
    }
}
