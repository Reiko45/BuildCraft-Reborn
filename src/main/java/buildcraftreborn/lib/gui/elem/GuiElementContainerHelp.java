/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.elem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.IGuiElement;
import buildcraftreborn.lib.gui.help.ElementHelpInfo;
import buildcraftreborn.lib.gui.pos.IGuiPosition;

/** Holds the text shown by the help ledger for one element. Client only. */
public class GuiElementContainerHelp implements IGuiElement {
    /** Help text wraps at this width, so the help ledger doesn't cover the whole screen. */
    public static final int MAX_LINE_WIDTH = 160;

    public final BuildCraftGui gui;
    private final IGuiPosition position;
    private final List<IGuiElement> internalElements = new ArrayList<>();
    private double width, height;
    private boolean calc = false;

    public GuiElementContainerHelp(BuildCraftGui gui, IGuiPosition position) {
        this.gui = gui;
        this.position = position;
    }

    /** Adds the text of a help entry (1.12's {@code ElementHelpInfo.addGuiElements}). */
    public static void addHelpInfo(GuiElementContainerHelp container, ElementHelpInfo info) {
        Font font = Minecraft.getInstance().font;
        int y = 20;
        for (String key : info.localeKeys) {
            if (key == null) {
                y += font.lineHeight + 5;
                continue;
            }
            Component localized = info.isPreTranslated ? Component.literal(key) : Component.translatable(key);
            for (String paragraph : localized.getString().split("\\\\n|\n")) {
                for (FormattedCharSequence line : font.split(Component.literal(paragraph), MAX_LINE_WIDTH)) {
                    StringBuilder sb = new StringBuilder();
                    line.accept((index, style, codePoint) -> {
                        sb.appendCodePoint(codePoint);
                        return true;
                    });
                    GuiElementText elemText = new GuiElementText(gui(container), container.offset(0, y),
                        sb.toString(), 0);
                    container.add(elemText);
                    y += (int) elemText.getHeight() + 5;
                }
            }
        }
    }

    private static BuildCraftGui gui(GuiElementContainerHelp container) {
        return container.gui;
    }

    private void recalcSize() {
        calc = true;
        width = 0;
        height = 0;
        double w = 0;
        double h = 0;
        for (IGuiElement element : internalElements) {
            w = Math.max(w, element.getEndX());
            h = Math.max(h, element.getEndY());
        }
        width = w;
        height = h;
        calc = false;
    }

    /** Adds the given element to be drawn. Note that the element must be based around (0, 0), NOT this element. */
    public void add(IGuiElement element) {
        internalElements.add(element);
        recalcSize();
    }

    public void addAll(IGuiElement... elements) {
        Collections.addAll(internalElements, elements);
        recalcSize();
    }

    public void addAll(Collection<IGuiElement> elements) {
        internalElements.addAll(elements);
        recalcSize();
    }

    @Override
    public double getX() {
        return calc ? 0 : position.getX();
    }

    @Override
    public double getY() {
        return calc ? 0 : position.getY();
    }

    @Override
    public double getWidth() {
        return this.width;
    }

    @Override
    public double getHeight() {
        return this.height;
    }

    @Override
    public void drawBackground(GuiGraphics graphics, float partialTicks) {
        for (IGuiElement element : internalElements) {
            element.drawBackground(graphics, partialTicks);
        }
    }

    @Override
    public void drawForeground(GuiGraphics graphics, float partialTicks) {
        for (IGuiElement element : internalElements) {
            element.drawForeground(graphics, partialTicks);
        }
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        for (IGuiElement element : internalElements) {
            element.addToolTips(tooltips);
        }
    }
}
