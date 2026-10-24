/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.recipe;

import java.util.Objects;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.parts.GuidePartFactory;
import buildcraftreborn.lib.recipe.ChangingItemStack;

/** Makes a {@link GuideSmelting} for a smelting recipe. */
public class GuideSmeltingFactory implements GuidePartFactory {
    private final ChangingItemStack input, output;
    private final int hash;

    public GuideSmeltingFactory(Ingredient input, ItemStack output) {
        this(new ChangingItemStack(input), new ChangingItemStack(output));
    }

    public GuideSmeltingFactory(ChangingItemStack input, ChangingItemStack output) {
        this.input = input;
        this.output = output;
        this.hash = Objects.hash(input, output);
    }

    @Override
    public GuideSmelting createNew(GuiGuide gui) {
        return new GuideSmelting(gui, input, output);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        GuideSmeltingFactory other = (GuideSmeltingFactory) obj;
        return hash == other.hash && input.equals(other.input) && output.equals(other.output);
    }
}
