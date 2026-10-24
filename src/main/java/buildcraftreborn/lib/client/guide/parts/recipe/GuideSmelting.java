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
import net.minecraft.world.level.block.Blocks;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.parts.GuidePartItem;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.recipe.ChangingItemStack;

/** A smelting recipe: the input, a furnace and the output. */
public class GuideSmelting extends GuidePartItem {
    public static final GuiIcon SMELTING_ICON = new GuiIcon(GuiGuide.ICONS_2, 119, 54, 80, 54);
    public static final GuiRectangle OFFSET = new GuiRectangle(
        (GuiGuide.PAGE_LEFT_TEXT.width - SMELTING_ICON.width) / 2, 0, SMELTING_ICON.width, SMELTING_ICON.height);
    public static final GuiRectangle IN_POS = new GuiRectangle(1, 1, 16, 16);
    public static final GuiRectangle OUT_POS = new GuiRectangle(59, 19, 16, 16);
    public static final GuiRectangle FURNACE_POS = new GuiRectangle(1, 37, 16, 16);
    public static final int PIXEL_HEIGHT = 60;

    private final ChangingItemStack input, output;
    private final ItemStack furnace;
    private final int hash;

    public GuideSmelting(GuiGuide gui, ChangingItemStack input, ChangingItemStack output) {
        super(gui);
        this.input = input;
        this.output = output;
        furnace = new ItemStack(Blocks.FURNACE);
        this.hash = Objects.hash(input, output);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        GuideSmelting other = (GuideSmelting) obj;
        return hash == other.hash && input.equals(other.input) && output.equals(other.output);
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += (int) OFFSET.x;
        y += (int) OFFSET.y + current.pixel;
        if (current.page == index) {
            SMELTING_ICON.drawAt(graphics(), x, y);
            drawItemStack(input.get(), x + (int) IN_POS.x, y + (int) IN_POS.y);
            drawItemStack(output.get(), x + (int) OUT_POS.x, y + (int) OUT_POS.y);
            drawItemStack(furnace, x + (int) FURNACE_POS.x, y + (int) FURNACE_POS.y);
        }
        return current.nextLine(PIXEL_HEIGHT, height);
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += (int) OFFSET.x;
        y += (int) OFFSET.y + current.pixel;
        if (current.page == index) {
            testClickItemStack(input.get(), x + (int) IN_POS.x, y + (int) IN_POS.y);
            testClickItemStack(output.get(), x + (int) OUT_POS.x, y + (int) OUT_POS.y);
            testClickItemStack(furnace, x + (int) FURNACE_POS.x, y + (int) FURNACE_POS.y);
        }
        return current.nextLine(PIXEL_HEIGHT, height);
    }
}
