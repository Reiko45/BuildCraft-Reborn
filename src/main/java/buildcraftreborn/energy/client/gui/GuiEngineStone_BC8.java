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
import buildcraftreborn.energy.container.ContainerEngineStone_BC8;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.help.DummyHelpElement;
import buildcraftreborn.lib.gui.help.ElementHelpInfo;
import buildcraftreborn.lib.gui.ledger.LedgerEngine;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;

public class GuiEngineStone_BC8 extends GuiBC8<ContainerEngineStone_BC8> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/steam_engine_gui.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    private final IGuiArea flameRect = new GuiRectangle(81, 25, 14, 14).offset(mainGui.rootElement);
    private final IGuiArea fuelSlotRect = new GuiRectangle(78, 39, 20, 20).offset(mainGui.rootElement);

    public GuiEngineStone_BC8(ContainerEngineStone_BC8 container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerEngine(mainGui, container.tile, true));
        ElementHelpInfo helpFlame = new ElementHelpInfo("buildcraft.help.stone_engine.flame.title", 0xFF_FF_FF_1F,
            "buildcraft.help.stone_engine.flame");
        ElementHelpInfo helpFuel = new ElementHelpInfo("buildcraft.help.stone_engine.fuel.title", 0xFF_AA_33_33,
            "buildcraft.help.stone_engine.fuel");
        mainGui.shownElements.add(new DummyHelpElement(flameRect.expand(2), helpFlame));
        mainGui.shownElements.add(new DummyHelpElement(fuelSlotRect, helpFuel));
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);

        double amount = container.tile.deltaFuelLeft.getDynamic(partialTicks) / 100;

        if (amount > 0) {
            int flameHeight = (int) Math.ceil(amount * flameRect.getHeight());
            graphics.blit(TEXTURE_BASE, (int) flameRect.getX(),
                (int) (flameRect.getY() + flameRect.getHeight() - flameHeight), 176, 14 - flameHeight, 14,
                flameHeight + 2);
        }
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
