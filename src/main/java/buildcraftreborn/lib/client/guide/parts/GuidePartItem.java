/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.misc.ItemStackKey;

/** A part that shows item stacks (recipes): hovering one shows its tooltip, and clicking one opens its page. */
public abstract class GuidePartItem extends GuidePart {
    public static final GuiRectangle STACK_RECT = new GuiRectangle(0, 0, 16, 16);

    public GuidePartItem(GuiGuide gui) {
        super(gui);
    }

    protected void drawItemStack(ItemStackKey stack, int x, int y) {
        drawItemStack(stack.baseStack, x, y);
    }

    protected void drawItemStack(ItemStack stack, int x, int y) {
        if (stack != null && !stack.isEmpty()) {
            graphics().renderItem(stack, x, y);
            graphics().renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
            if (STACK_RECT.offset(x, y).contains(gui.mouse)) {
                gui.tooltipStack = stack;
            }
        }
    }

    protected void testClickItemStack(ItemStackKey stack, int x, int y) {
        testClickItemStack(stack.baseStack, x, y);
    }

    protected void testClickItemStack(ItemStack stack, int x, int y) {
        if (stack != null && !stack.isEmpty() && STACK_RECT.offset(x, y).contains(gui.mouse)) {
            gui.openPage(GuideManager.INSTANCE.getPageFor(stack).createNew(gui));
        }
    }
}
