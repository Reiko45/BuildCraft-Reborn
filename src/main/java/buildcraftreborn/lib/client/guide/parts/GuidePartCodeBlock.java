/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.font.IFontRenderer;

/** A block of code, with line numbers. */
public class GuidePartCodeBlock extends GuidePart {
    public final List<String> lines;

    public GuidePartCodeBlock(GuiGuide gui, List<String> lines) {
        super(gui);
        this.lines = lines;
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        IFontRenderer font = gui.getCurrentFont();
        List<String> wrappedLines = new ArrayList<>();
        IntList lineNumbers = new IntArrayList();
        int lineNumberWidth = font.getStringWidth(Integer.toString(lines.size() - 1));
        int widthForDecoration = 8 + lineNumberWidth;
        int innerMaxWidth = 0;
        for (int i = 0; i < lines.size(); i++) {
            List<String> wrapped = font.wrapString(lines.get(i), width - widthForDecoration, false, 1);
            wrappedLines.addAll(wrapped);
            for (int j = 0; j < wrapped.size(); j++) {
                lineNumbers.add(j == 0 ? (i + 1) : -1);
                innerMaxWidth = Math.max(innerMaxWidth, font.getStringWidth(wrapped.get(j)));
            }
        }
        int innerHeight = wrappedLines.size() * (font.getMaxFontHeight() + 2);
        int outerHeight = innerHeight + 6;
        current = current.guaranteeSpace(outerHeight, height);
        if (index == current.page) {
            int outerWidth = innerMaxWidth + 8;
            int _y = y + current.pixel;
            GuiGuide.BOX_CODE_SLICED.draw(graphics(), x + lineNumberWidth + 5, _y, outerWidth, outerHeight);
            _y += 4;
            boolean darken = true;
            for (int i = 0; i < wrappedLines.size(); i++) {
                String line = wrappedLines.get(i);
                int number = lineNumbers.getInt(i);
                if (number != -1) {
                    darken = !darken;
                    if (wrappedLines.size() > 1) {
                        String ns = Integer.toString(number);
                        int addX = lineNumberWidth - font.getStringWidth(ns);
                        font.drawString(graphics(), ns, x + 4 + addX, _y, 0);
                    }
                }
                int _x = x + 8 + lineNumberWidth;
                if (darken) {
                    graphics().fill(_x - 2, _y - 1, _x + innerMaxWidth + 4, _y + font.getMaxFontHeight() + 1,
                        0xFF_F0_F0_F0);
                }
                font.drawString(graphics(), line, _x, _y, 0);
                _y += font.getMaxFontHeight() + 2;
            }
        }
        return current.nextLine(outerHeight, height);
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        return renderIntoArea(x, y, width, height, current, -1);
    }
}
