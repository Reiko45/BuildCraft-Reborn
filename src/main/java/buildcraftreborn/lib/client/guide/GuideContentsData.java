/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.neoforged.fml.ModList;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.misc.LocaleUtil;

/** The mods and resource packs that a book has pages from, for its contents page. */
public class GuideContentsData {
    @Nullable
    public final GuideBook book;
    public final List<String> loadedMods = new ArrayList<>();
    public final List<String> loadedOther = new ArrayList<>();

    public GuideContentsData(@Nullable GuideBook book) {
        this.book = book;
    }

    /** 1.21: BuildCraft is one mod, so 1.12's list of loaded BuildCraft modules is just "BuildCraft Reborn". */
    public void generate(Set<String> domains) {
        loadedMods.clear();
        loadedOther.clear();
        boolean hasBuildCraft = false;
        for (String domain : domains) {
            if (domain.equals(BuildCraftReborn.MODID)) {
                hasBuildCraft = true;
                continue;
            }
            String name = ModList.get().getModContainerById(domain)
                .map(container -> container.getModInfo().getDisplayName()).orElse(null);
            if (name != null) {
                loadedMods.add(name);
            } else {
                loadedOther.add(LocaleUtil.localize(domain + ".compat.buildcraft.guide.domain_name"));
            }
        }
        Collections.sort(loadedMods);
        Collections.sort(loadedOther);
        if (hasBuildCraft) {
            loadedMods.add(0, "BuildCraft Reborn");
        }
    }
}
