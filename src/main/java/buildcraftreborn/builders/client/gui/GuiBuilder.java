/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.builders.container.ContainerBuilder;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.elem.GuiElementFluidTank;
import buildcraftreborn.lib.gui.pos.GuiRectangle;

/** The builder's GUI: its snapshot slot and resources, with what a blueprint still needs and its four tanks on the
 * right. */
public class GuiBuilder extends GuiBC8<ContainerBuilder> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/builder.png");
    private static final ResourceLocation TEXTURE_BLUEPRINT = BuildCraftReborn.id("textures/gui/builder_blueprint.png");
    private static final int SIZE_X = 176, SIZE_BLUEPRINT_X = 256, SIZE_Y = 222, BLUEPRINT_WIDTH = 87;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_BLUEPRINT_GUI =
        new GuiIcon(TEXTURE_BLUEPRINT, SIZE_BLUEPRINT_X - BLUEPRINT_WIDTH, 0, BLUEPRINT_WIDTH, SIZE_Y);
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(TEXTURE_BLUEPRINT, 0, 54, 16, 47);

    public GuiBuilder(ContainerBuilder container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_BLUEPRINT_X;
        imageHeight = SIZE_Y;
        for (int i = 0; i < container.widgetTanks.size(); i++) {
            mainGui.shownElements.add(new GuiElementFluidTank(mainGui,
                new GuiRectangle(179 + i * 18, 145, 16, 47).offset(mainGui.rootElement), container.widgetTanks.get(i),
                ICON_TANK_OVERLAY));
        }
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, leftPos, topPos);
        ICON_BLUEPRINT_GUI.drawAt(graphics, leftPos + SIZE_BLUEPRINT_X - BLUEPRINT_WIDTH, topPos);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, leftPos + (SIZE_X - font.width(title)) / 2, topPos + 6, 0x404040, false);
    }
}
