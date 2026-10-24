/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.client.guide.entry.PageEntry;

/** A page loaded from a file, which can be loaded again (keeping its place) when the guide is reloaded. */
public class GuidePageEntry extends GuidePage {
    public final ResourceLocation name;

    public GuidePageEntry(GuiGuide gui, List<GuidePart> parts, PageEntry<?> entry, ResourceLocation name) {
        super(gui, parts, entry);
        this.name = name;
    }

    @Override
    @Nullable
    public GuidePageBase createReloaded() {
        GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(name);
        if (factory == null) {
            return null;
        }
        GuidePageBase page = factory.createNew(gui);
        page.goToPage(getPage());
        return page;
    }
}
