/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.elem;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiElementSimple;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiPosition;

/** A line of text. Client only. */
public class GuiElementText extends GuiElementSimple {
    public boolean dropShadow = false;
    public boolean foreground = false;
    public boolean centered = false;

    private final Supplier<String> text;
    private final IntSupplier colour;

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, Supplier<String> text, IntSupplier colour) {
        super(gui, GuiRectangle.ZERO.offset(parent));
        this.text = text;
        this.colour = colour;
    }

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, Supplier<String> text, int colour) {
        this(gui, parent, text, () -> colour);
    }

    public GuiElementText(BuildCraftGui gui, IGuiPosition parent, String text, int colour) {
        this(gui, parent, () -> text, () -> colour);
    }

    public GuiElementText setDropShadow(boolean value) {
        dropShadow = value;
        return this;
    }

    public GuiElementText setForeground(boolean value) {
        foreground = value;
        return this;
    }

    public GuiElementText setCentered(boolean centered) {
        this.centered = centered;
        return this;
    }

    private static Font font() {
        return Minecraft.getInstance().font;
    }

    @Override
    public double getWidth() {
        return font().width(text.get());
    }

    @Override
    public double getHeight() {
        return font().lineHeight;
    }

    @Override
    public void drawBackground(GuiGraphics graphics, float partialTicks) {
        if (!foreground) {
            draw(graphics);
        }
    }

    @Override
    public void drawForeground(GuiGraphics graphics, float partialTicks) {
        if (foreground) {
            draw(graphics);
        }
    }

    private void draw(GuiGraphics graphics) {
        String str = text.get();
        int x = (int) getX();
        if (centered) {
            x -= font().width(str) / 2;
        }
        int argb = colour.getAsInt();
        if ((argb & 0xFF_00_00_00) == 0) {
            argb |= 0xFF_00_00_00;
        }
        graphics.drawString(font(), str, x, (int) getY(), argb, dropShadow);
    }

    @Override
    public String getDebugInfo(List<String> info) {
        info.add("text = " + text.get());
        return super.getDebugInfo(info);
    }
}
