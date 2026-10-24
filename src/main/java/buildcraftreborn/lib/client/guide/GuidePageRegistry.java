/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.client.guide.entry.PageEntry;
import buildcraftreborn.lib.client.guide.entry.PageEntryExternal;
import buildcraftreborn.lib.client.guide.entry.PageEntryItemStack;
import buildcraftreborn.lib.client.guide.entry.PageEntryStatement;
import buildcraftreborn.lib.client.guide.entry.PageValueType;
import buildcraftreborn.lib.misc.data.OptionallyDisabled;

/** Every guide entry, from {@code compat/buildcraft/guide_entries.json}: what each page is about. The page itself is
 * {@code compat/buildcraft/guide/<language>/<entry name>.md} in the same namespace. */
public final class GuidePageRegistry {
    public static final GuidePageRegistry INSTANCE = new GuidePageRegistry();

    public final Map<String, PageValueType<?>> types = new LinkedHashMap<>();
    private Map<ResourceLocation, PageEntry<?>> entries = Collections.emptyMap();

    private GuidePageRegistry() {
        addType("item_stack", PageEntryItemStack.INSTANCE);
        addType("external", PageEntryExternal.INSTANCE);
        addType("statement", PageEntryStatement.INSTANCE);
    }

    public <T> void addType(String name, PageValueType<T> type) {
        types.put(name, type);
    }

    void reload(ResourceManager manager) {
        entries = GuideJsonLoader.load(manager, "guide_entries.json", this::deserialize);
    }

    private PageEntry<?> deserialize(ResourceLocation name, JsonObject json) {
        String typeName = GsonHelper.getAsString(json, "type");
        PageValueType<?> type = types.get(typeName);
        if (type == null) {
            throw new JsonSyntaxException("Unknown type '" + typeName + "' (must be one of " + types.keySet() + ")");
        }
        OptionallyDisabled<? extends PageEntry<?>> entry = type.deserialize(name, json);
        if (!entry.isPresent()) {
            if (GuideManager.DEBUG) {
                BuildCraftReborn.LOGGER.info("[lib.guide.loader] Disabled {}: {}", name, entry.getDisabledReason());
            }
            return null;
        }
        return entry.get();
    }

    public Map<ResourceLocation, PageEntry<?>> getReloadableEntryMap() {
        return entries;
    }

    public Iterable<PageEntry<?>> getAllEntries() {
        return entries.values();
    }

    /** @return The namespaces that have entries. */
    public Set<String> getSourceDomains() {
        Set<String> domains = new TreeSet<>();
        for (ResourceLocation key : entries.keySet()) {
            domains.add(key.getNamespace());
        }
        return domains;
    }

    /** For tests: replaces the entries. */
    public void setEntries(Map<ResourceLocation, PageEntry<?>> newEntries) {
        entries = new HashMap<>(newEntries);
    }
}
