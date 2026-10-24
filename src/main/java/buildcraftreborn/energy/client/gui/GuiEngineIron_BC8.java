/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.energy.container.ContainerEngineIron_BC8;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.elem.GuiElementFluidTank;
import buildcraftreborn.lib.gui.ledger.LedgerEngine;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.gui.widget.WidgetFluidTank;

public class GuiEngineIron_BC8 extends GuiBC8<ContainerEngineIron_BC8> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/combustion_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 177;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 60);

    public GuiEngineIron_BC8(ContainerEngineIron_BC8 container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerEngine(mainGui, container.tile, true));
        // The tanks' help text is set on the tanks (see TileEngineIron_BC8)
        addTank(container.widgetTankFuel, 26);
        addTank(container.widgetTankCoolant, 80);
        addTank(container.widgetTankResidue, 134);
    }

    private void addTank(WidgetFluidTank widget, int x) {
        IGuiArea area = new GuiRectangle(x, 18, 16, 60).offset(mainGui.rootElement);
        mainGui.shownElements.add(new GuiElementFluidTank(mainGui, area, widget, ICON_TANK_OVERLAY));
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        Component str = container.tile.getDisplayName();
        int strWidth = font.width(str);
        double titleX = mainGui.rootElement.getCenterX() - strWidth / 2.0;
        double titleY = mainGui.rootElement.getY() + 6;
        graphics.drawString(font, str, (int) titleX, (int) titleY, 0x404040, false);

        double invX = mainGui.rootElement.getX() + 8;
        double invY = mainGui.rootElement.getY() + SIZE_Y - 96;
        graphics.drawString(font, Component.translatable("container.inventory"), (int) invX, (int) invY, 0x404040,
            false);
    }
}
