/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import java.util.List;

import buildcraftreborn.lib.client.guide.GuiGuide;

@FunctionalInterface
public interface GuidePartFactory {
    GuidePart createNew(GuiGuide gui);

    /** Adds the parts for this to a page. Most factories make one part; some (like a list of recipes, which is only
     * known once the page is opened) make several, so that their chapters are part of the page. */
    default void createAll(GuiGuide gui, List<GuidePart> out) {
        out.add(createNew(gui));
    }
}
