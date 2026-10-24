/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import buildcraftreborn.lib.client.guide.GuiGuide;

@FunctionalInterface
public interface GuidePageFactory extends GuidePartFactory {
    @Override
    GuidePageBase createNew(GuiGuide gui);
}
