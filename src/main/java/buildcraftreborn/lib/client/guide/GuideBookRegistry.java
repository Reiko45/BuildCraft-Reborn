/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

/** Every guide book, from {@code compat/buildcraft/guide_books.json}. 1.21: a resource (client) file, as only the
 * client uses books (1.12 read them from the mods' data). */
public final class GuideBookRegistry {
    public static final GuideBookRegistry INSTANCE = new GuideBookRegistry();

    private Map<ResourceLocation, GuideBook> books = ImmutableMap.of();

    private GuideBookRegistry() {}

    void reload(ResourceManager manager) {
        books = ImmutableMap.copyOf(GuideJsonLoader.load(manager, "guide_books.json", GuideBook::deserialize));
    }

    @Nullable
    public GuideBook getBook(String bookName) {
        ResourceLocation loc = ResourceLocation.tryParse(bookName);
        return loc == null ? null : books.get(loc);
    }

    @Nullable
    public GuideBook getBook(ResourceLocation bookName) {
        return books.get(bookName);
    }

    public Collection<GuideBook> getAllEntries() {
        return books.values();
    }
}
