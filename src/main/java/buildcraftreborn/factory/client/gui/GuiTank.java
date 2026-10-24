/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.factory.container.ContainerTank;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.elem.GuiElementFluidTank;
import buildcraftreborn.lib.gui.pos.GuiRectangle;

public class GuiTank extends GuiBC8<ContainerTank> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/tank.png");
    private static final int SIZE_X = 176, SIZE_Y = 181;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 64);

    public GuiTank(ContainerTank container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new GuiElementFluidTank(mainGui,
            new GuiRectangle(80, 18, 16, 64).offset(mainGui.rootElement), container.widgetTank, ICON_TANK_OVERLAY));
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = leftPos + (imageWidth - font.width(title)) / 2;
        graphics.drawString(font, title, titleX, topPos + 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, leftPos + 8, topPos + SIZE_Y - 96, 0x404040, false);
    }
}
