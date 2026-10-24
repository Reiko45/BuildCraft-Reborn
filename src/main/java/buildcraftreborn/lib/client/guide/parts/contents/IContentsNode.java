/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.contents;

import java.util.Set;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.parts.GuidePart;

/** A heading or link of the contents page. */
public interface IContentsNode {
    String getSearchName();

    boolean isVisible();

    void calcVisibility();

    void resetVisibility();

    /** Should set the visibility to true if this node is contained in the given set. */
    void setVisible(Set<PageLink> matches);

    void sort();

    IContentsNode[] getVisibleChildren();

    void addChild(IContentsNode node);

    GuidePart createGuidePart(GuiGuide gui);
}
