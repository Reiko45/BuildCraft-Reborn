/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.ledger;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.client.BCLibSprites;
import buildcraftreborn.lib.client.sprite.SpriteNineSliced;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.IGuiElement;
import buildcraftreborn.lib.gui.config.GuiConfigManager;
import buildcraftreborn.lib.gui.elem.GuiElementContainerHelp;
import buildcraftreborn.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.misc.GuiUtil;

/** The help ledger: while open, outlines every element that has help, and shows the help for the one under the
 * mouse. Client only. */
public class LedgerHelp extends Ledger_Neptune {
    private static final SpriteNineSliced[][] SPRITE_HELP_SPLIT = new SpriteNineSliced[2][2];

    static {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                SPRITE_HELP_SPLIT[i][j] = new SpriteNineSliced(subRelative(BCLibSprites.HELP_SPLIT, i * 8, j * 8),
                    2, 2, 6, 6, 8);
            }
        }
    }

    /** One 8x8 quarter of a 16x16 sprite. */
    private static ISprite subRelative(ISprite sprite, int x, int y) {
        return new ISprite() {
            @Override
            public net.minecraft.resources.ResourceLocation getTexture() {
                return sprite.getTexture();
            }

            @Override
            public double getInterpU(double u) {
                return sprite.getInterpU((x + u * 8) / 16);
            }

            @Override
            public double getInterpV(double v) {
                return sprite.getInterpV((y + v * 8) / 16);
            }
        };
    }

    private IGuiElement selected = null;
    private boolean foundAny = false, init = false;

    public LedgerHelp(BuildCraftGui gui, boolean expandPositive) {
        super(gui, 0xFF_CC_99_FF, expandPositive);
        title = I18n.get("gui.ledger.help");
        calculateMaxSize();
        setOpenProperty(GuiConfigManager.getOrAddBoolean(BuildCraftReborn.id("base"), "ledger.help.is_open", false));
    }

    @Override
    public void tick() {
        super.tick();
        if (currentWidth == CLOSED_WIDTH && currentHeight == CLOSED_HEIGHT) {
            selected = null;
            if (openElements.size() == 2) {
                openElements.remove(1);
                title = I18n.get("gui.ledger.help");
                calculateMaxSize();
            }
        }
    }

    @Override
    protected void drawIcon(GuiGraphics graphics, double x, double y) {
        if (!init) {
            init = true;
            List<HelpPosition> elements = new ArrayList<>();
            for (IGuiElement element : gui.shownElements) {
                element.addHelpElements(elements);
            }
            foundAny = !elements.isEmpty();
        }
        ISprite sprite = foundAny ? BCLibSprites.HELP : BCLibSprites.WARNING_MINOR;
        GuiIcon.draw(graphics, sprite, x, y, x + 16, y + 16);
    }

    @Override
    public void drawForeground(GuiGraphics graphics, float partialTicks) {
        super.drawForeground(graphics, partialTicks);
        if (!shouldDrawOpen()) {
            return;
        }
        boolean set = false;
        List<HelpPosition> elements = new ArrayList<>();
        for (IGuiElement element : gui.shownElements) {
            element.addHelpElements(elements);
            foundAny |= !elements.isEmpty();
            for (HelpPosition info : elements) {
                IGuiArea rect = info.target();
                boolean isHovered = rect.contains(gui.mouse);
                if (isHovered) {
                    if (selected != element && !set) {
                        selected = element;
                        GuiElementContainerHelp container = new GuiElementContainerHelp(gui, positionLedgerInnerStart);
                        GuiElementContainerHelp.addHelpInfo(container, info.info());
                        if (openElements.size() == 2) {
                            openElements.remove(1);
                        }
                        openElements.add(container);
                        title = I18n.get("gui.ledger.help") + ": " + I18n.get(info.info().title);
                        calculateMaxSize();
                        set = true;
                    }
                }
                boolean isSelected = selected == element;
                SpriteNineSliced split = SPRITE_HELP_SPLIT[isHovered ? 1 : 0][isSelected ? 1 : 0];
                GuiUtil.setColour(graphics, info.info().colour);
                split.draw(graphics, rect);
            }
            elements.clear();
        }
        GuiUtil.resetColour(graphics);
    }
}
