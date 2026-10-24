/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.recipe;

import java.util.Arrays;
import java.util.Collections;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.parts.GuidePartItem;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.misc.LegacyText;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.recipe.ChangingItemStack;
import buildcraftreborn.lib.recipe.ChangingObject;

/** An assembly table recipe: up to six inputs, the power it needs, and the output. */
public class GuideAssembly extends GuidePartItem {
    public static final GuiIcon INPUT_LIST = new GuiIcon(GuiGuide.ICONS_2, 119, 108, 98, 54);
    public static final GuiRectangle[] ITEM_POSITION = new GuiRectangle[6];
    public static final GuiRectangle OUT_POSITION = new GuiRectangle(77, 19, 16, 16);
    public static final GuiRectangle MJ_POSITION = new GuiRectangle(50, 4, 6, 46);
    public static final GuiRectangle OFFSET = new GuiRectangle((GuiGuide.PAGE_LEFT_TEXT.width - INPUT_LIST.width) / 2,
        0, INPUT_LIST.width, INPUT_LIST.height);
    public static final int PIXEL_HEIGHT = 60;

    static {
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 3; y++) {
                ITEM_POSITION[x + y * 2] = new GuiRectangle(1 + x * 18, 1 + y * 18, 16, 16);
            }
        }
    }

    private final ChangingItemStack[] input;
    private final ChangingItemStack output;
    private final ChangingObject<Long> mjCost;
    private final int hash;

    GuideAssembly(GuiGuide gui, ChangingItemStack[] input, ChangingItemStack output, ChangingObject<Long> mjCost) {
        super(gui);
        this.input = input;
        this.output = output;
        this.mjCost = mjCost;
        this.hash = Arrays.deepHashCode(new Object[] { input, output, mjCost });
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        GuideAssembly other = (GuideAssembly) obj;
        return Arrays.equals(input, other.input) && output.equals(other.output) && mjCost.equals(other.mjCost);
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (current.pixel + PIXEL_HEIGHT > height) {
            current = current.newPage();
        }
        x += (int) OFFSET.x;
        y += (int) OFFSET.y + current.pixel;
        if (current.page == index) {
            INPUT_LIST.drawAt(graphics(), x, y);
            for (int i = 0; i < input.length && i < ITEM_POSITION.length; i++) {
                GuiRectangle rect = ITEM_POSITION[i];
                drawItemStack(input[i].get(), x + (int) rect.x, y + (int) rect.y);
            }
            drawItemStack(output.get(), x + (int) OUT_POSITION.x, y + (int) OUT_POSITION.y);
            if (MJ_POSITION.offset(x, y).contains(gui.mouse)) {
                gui.tooltips.add(Collections.singletonList(LegacyText.toLegacy(LocaleUtil.localizeMj(mjCost.get()))));
            }
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
            for (int i = 0; i < input.length && i < ITEM_POSITION.length; i++) {
                GuiRectangle rect = ITEM_POSITION[i];
                testClickItemStack(input[i].get(), x + (int) rect.x, y + (int) rect.y);
            }
            testClickItemStack(output.get(), x + (int) OUT_POSITION.x, y + (int) OUT_POSITION.y);
        }
        return current.nextLine(PIXEL_HEIGHT, height);
    }
}
