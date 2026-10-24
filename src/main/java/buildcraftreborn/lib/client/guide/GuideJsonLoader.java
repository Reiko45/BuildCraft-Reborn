/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import buildcraftreborn.BuildCraftReborn;

/** Loads the guide's books and entries from every resource pack.
 * <p>
 * 1.12 defined them in {@code compat/buildcraft/guide.txt} and {@code book.txt} files, written in BuildCraft's own
 * instruction language ({@code buildcraft/json/insn}, with imports and aliases). That language isn't ported: each
 * namespace now has plain JSON files, {@code compat/buildcraft/guide_books.json} and
 * {@code compat/buildcraft/guide_entries.json}, mapping each name to its definition. A resource pack can add entries or
 * replace them (higher packs win), and remove one by setting it to {@code null}. */
final class GuideJsonLoader {
    private GuideJsonLoader() {}

    static <T> Map<ResourceLocation, T> load(ResourceManager manager, String file,
        BiFunction<ResourceLocation, JsonObject, T> deserializer) {
        Map<ResourceLocation, T> map = new LinkedHashMap<>();
        for (String namespace : manager.getNamespaces()) {
            ResourceLocation location = ResourceLocation.fromNamespaceAndPath(namespace, "compat/buildcraft/" + file);
            // lowest priority pack first, so that higher ones override
            for (Resource resource : manager.getResourceStack(location)) {
                try (Reader reader = resource.openAsReader()) {
                    JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        ResourceLocation name = ResourceLocation.fromNamespaceAndPath(namespace, entry.getKey());
                        if (entry.getValue().isJsonNull()) {
                            map.remove(name);
                            continue;
                        }
                        try {
                            T value = deserializer.apply(name, entry.getValue().getAsJsonObject());
                            if (value == null) {
                                map.remove(name);
                            } else {
                                map.put(name, value);
                            }
                        } catch (RuntimeException e) {
                            BuildCraftReborn.LOGGER.warn("[lib.guide.loader] Couldn't load {} from {}: {}", name,
                                resource.sourcePackId(), e.getMessage());
                        }
                    }
                } catch (IOException | RuntimeException e) {
                    BuildCraftReborn.LOGGER.warn("[lib.guide.loader] Couldn't read {} from {}", location,
                        resource.sourcePackId(), e);
                }
            }
        }
        return map;
    }
}
