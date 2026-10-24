/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.ref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.lib.client.guide.entry.ItemStackValueFilter;
import buildcraftreborn.lib.client.guide.entry.PageEntryItemStack;
import buildcraftreborn.lib.client.guide.entry.PageEntryStatement;
import buildcraftreborn.lib.client.guide.entry.PageValue;
import buildcraftreborn.lib.client.guide.entry.PageValueType;

/** The guide's groups of related things, and how objects (items, blocks, stacks, statements) become page values.
 * <p>
 * 1.21: the built-in groups are made the first time they're needed (1.12 made them when the class loaded, from its
 * static item fields). */
public final class GuideGroupManager {
    private static final Map<Class<?>, PageValueType<?>> knownClasses = new HashMap<>();
    private static final Map<Class<?>, Function<Object, PageValue<?>>> transformers = new HashMap<>();
    private static final Map<ResourceLocation, GuideGroupSet> sets = new LinkedHashMap<>();
    private static boolean defaultsAdded = false;

    static {
        addValidClass(ItemStackValueFilter.class, PageEntryItemStack.INSTANCE);
        addValidClass(IStatement.class, PageEntryStatement.INSTANCE);
        addTransformer(ItemStack.class, ItemStackValueFilter.class, ItemStackValueFilter::new);
        addTransformer(Item.class, ItemStack.class, ItemStack::new);
        addTransformer(Block.class, ItemStack.class, ItemStack::new);
    }

    private GuideGroupManager() {}

    private static void addDefaults() {
        if (defaultsAdded) {
            return;
        }
        defaultsAdded = true;
        addEntries("buildcraft", "pipe_power_providers", items("plug_pulsar", "plug_power_adaptor", "engine_wood",
            "engine_stone", "engine_iron")).addKeyCollection(items("pipe_wood_item", "pipe_diamond_wood_item",
                "pipe_emzuli_item", "pipe_wood_fluid", "pipe_diamond_wood_fluid"));
        addEntries("buildcraft", "full_power_providers", items("engine_stone", "engine_iron")).addKeyCollection(
            items("builder", "filler", "quarry", "distiller", "mining_well", "pump", "laser"));
        addEntries("buildcraft", "laser_power_providers", items("laser")).addKeyCollection(items(
            "advanced_crafting_table", "assembly_table", "integration_table"));
        addEntries("buildcraft", "area_markers", items("marker_volume", "volume_box")).addKeyCollection(items(
            "quarry", "architect", "filler"));
    }

    private static List<Object> items(String... names) {
        List<Object> list = new ArrayList<>();
        for (String name : names) {
            Item item = BuiltInRegistries.ITEM.get(BuildCraftReborn.id(name));
            if (item != Items.AIR) {
                list.add(item);
            }
        }
        return list;
    }

    // Known types

    public static <F, T> void addTransformer(Class<F> fromClass, Class<T> toClass, Function<F, T> transform) {
        PageValueType<?> destType = getEntryType(toClass);
        Function<Object, PageValue<?>> realTransform;
        if (destType == null) {
            Function<Object, PageValue<?>> destTransform = getTransform(toClass);
            if (destTransform == null) {
                throw new IllegalArgumentException("You cannot register a transformer to an unregistered class!");
            }
            realTransform = o -> destTransform.apply(transform.apply(fromClass.cast(o)));
        } else {
            realTransform = o -> destType.wrap(transform.apply(fromClass.cast(o)));
        }
        transformers.put(fromClass, realTransform);
    }

    public static <T> void addValidClass(Class<T> clazz, PageValueType<T> type) {
        if (clazz.isArray()) {
            throw new IllegalArgumentException("Arrays are never valid!");
        }
        knownClasses.put(clazz, type);
    }

    @Nullable
    public static PageValue<?> toPageValue(@Nullable Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof PageValue<?> pageValue) {
            return pageValue;
        }
        PageValueType<?> entryType = getEntryType(value.getClass());
        if (entryType != null) {
            return entryType.wrap(value);
        }
        Function<Object, PageValue<?>> transform = getTransform(value.getClass());
        if (transform != null) {
            return transform.apply(value);
        }
        throw new IllegalArgumentException("Unknown " + value.getClass()
            + " - is this a programming mistake, or have you forgotten to register the class as valid?");
    }

    @Nullable
    private static PageValueType<?> getEntryType(Class<?> clazz) {
        if (knownClasses.containsKey(clazz)) {
            return knownClasses.get(clazz);
        }
        PageValueType<?> type = null;
        if (!clazz.isArray()) {
            Class<?> superClazz = clazz.getSuperclass();
            if (superClazz != null) {
                type = getEntryType(superClazz);
            }
            if (type == null) {
                for (Class<?> cls : clazz.getInterfaces()) {
                    type = getEntryType(cls);
                    if (type != null) {
                        break;
                    }
                }
            }
            knownClasses.put(clazz, type);
        }
        return type;
    }

    @Nullable
    private static Function<Object, PageValue<?>> getTransform(Class<?> clazz) {
        if (transformers.containsKey(clazz)) {
            return transformers.get(clazz);
        }
        Function<Object, PageValue<?>> func = null;
        if (!clazz.isArray()) {
            Class<?> superClazz = clazz.getSuperclass();
            if (superClazz != null) {
                func = getTransform(superClazz);
            }
            if (func == null) {
                for (Class<?> cls : clazz.getInterfaces()) {
                    func = getTransform(cls);
                    if (func != null) {
                        break;
                    }
                }
            }
            transformers.put(clazz, func);
        }
        return func;
    }

    // Internals

    public static Collection<GuideGroupSet> getSets() {
        addDefaults();
        return sets.values();
    }

    @Nullable
    public static GuideGroupSet get(ResourceLocation group) {
        addDefaults();
        return sets.get(group);
    }

    @Nullable
    public static GuideGroupSet get(String domain, String group) {
        return get(ResourceLocation.fromNamespaceAndPath(domain, group));
    }

    public static GuideGroupSet getOrCreate(String domain, String group) {
        return sets.computeIfAbsent(ResourceLocation.fromNamespaceAndPath(domain, group), GuideGroupSet::new);
    }

    // Basic adders

    public static GuideGroupSet addEntry(String domain, String group, Object value) {
        return getOrCreate(domain, group).addSingle(value);
    }

    public static GuideGroupSet addEntries(String domain, String group, Collection<?> values) {
        return getOrCreate(domain, group).addCollection(values);
    }

    public static GuideGroupSet addKey(String domain, String group, Object value) {
        return getOrCreate(domain, group).addKey(value);
    }

    public static GuideGroupSet addKeys(String domain, String group, Collection<?> values) {
        return getOrCreate(domain, group).addKeyCollection(values);
    }
}
