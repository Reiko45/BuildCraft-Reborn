/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.recipe;

import java.util.Arrays;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.parts.GuidePartFactory;
import buildcraftreborn.lib.recipe.ChangingItemStack;
import buildcraftreborn.lib.recipe.ChangingObject;

/** Makes a {@link GuideAssembly}. */
public class GuideAssemblyFactory implements GuidePartFactory {
    private final ChangingItemStack[] input;
    private final ChangingItemStack output;
    private final ChangingObject<Long> mjCost;
    private final int hash;

    public GuideAssemblyFactory(ChangingItemStack[] input, ChangingItemStack output, ChangingObject<Long> mjCost) {
        this.input = input;
        this.output = output;
        this.mjCost = mjCost;
        this.hash = Arrays.deepHashCode(new Object[] { input, output, mjCost });
    }

    @Override
    public GuideAssembly createNew(GuiGuide gui) {
        return new GuideAssembly(gui, input, output, mjCost);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        GuideAssemblyFactory other = (GuideAssemblyFactory) obj;
        return hash == other.hash && Arrays.equals(input, other.input) && output.equals(other.output);
    }
}
