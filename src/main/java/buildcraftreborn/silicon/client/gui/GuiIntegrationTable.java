/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.silicon.container.ContainerIntegrationTable;

/** The integration table: the target in the middle, the items to add around it, the preview and the result. */
public class GuiIntegrationTable extends GuiBC8<ContainerIntegrationTable> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/integration_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 191;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164, 22, 4, 70);

    public GuiIntegrationTable(ContainerIntegrationTable container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
        drawPower(graphics, ICON_PROGRESS, RECT_PROGRESS);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        drawTitle(graphics, 10);
    }

    /** Draws the power bar filling up from the bottom as the table charges. */
    private void drawPower(GuiGraphics graphics, GuiIcon icon, GuiRectangle rect) {
        long target = container.tile.getTarget();
        if (target != 0) {
            double v = (double) container.tile.power / target;
            icon.drawCutInside(graphics, new GuiRectangle(rect.x,
                (int) (rect.y + rect.height * Math.max(1 - v, 0)), rect.width,
                (int) Math.ceil(rect.height * Math.min(v, 1))).offset(mainGui.rootElement));
        }
    }

    private void drawTitle(GuiGraphics graphics, int y) {
        graphics.drawString(font, title, leftPos + (imageWidth - font.width(title)) / 2, topPos + y, 0x404040, false);
    }
}
