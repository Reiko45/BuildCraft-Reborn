/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.transport.BCTransportSprites;
import buildcraftreborn.transport.container.ContainerFilteredBuffer_BC8;

/** The filtered buffer: each storage slot shows a faded copy of its filter. */
public class GuiFilteredBuffer extends GuiBC8<ContainerFilteredBuffer_BC8> {
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/filtered_buffer.png");
    private static final int SIZE_X = 176, SIZE_Y = 169;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);

    public GuiFilteredBuffer(ContainerFilteredBuffer_BC8 container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
        double x0 = mainGui.rootElement.getX();
        double y0 = mainGui.rootElement.getY();
        for (int i = 0; i < 9; i++) {
            ItemStack filter = container.tile.invFilter.getStackInSlot(i);
            int x = (int) x0 + 8 + i * 18;
            if (filter.isEmpty()) {
                GuiIcon.draw(graphics, BCTransportSprites.NOTHING_FILTERED_BUFFER_SLOT, x, y0 + 61, x + 16,
                    y0 + 61 + 16);
            } else {
                graphics.renderItem(filter, x, (int) y0 + 61);
            }
        }
        // Fade the filter copies by drawing the background over them again
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 200);
        RenderSystem.enableBlend();
        graphics.setColor(1, 1, 1, 0.7f);
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
        graphics.setColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        // The empty filter slots show a funnel (in front of the fade)
        graphics.pose().translate(0, 0, 10);
        for (int i = 0; i < 9; i++) {
            if (container.tile.invFilter.getStackInSlot(i).isEmpty()) {
                int x = (int) x0 + 8 + i * 18;
                GuiIcon.draw(graphics, BCTransportSprites.EMPTY_FILTERED_BUFFER_SLOT, x, y0 + 27, x + 16, y0 + 27 + 16);
            }
        }
        graphics.pose().popPose();
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        int xPos = leftPos + (imageWidth - font.width(title)) / 2;
        // In front of the faded copy of the background
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);
        graphics.drawString(font, title, xPos, topPos + 10, 0x404040, false);
        graphics.pose().popPose();
    }
}
