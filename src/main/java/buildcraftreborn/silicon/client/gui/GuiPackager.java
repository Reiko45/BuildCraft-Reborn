/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.silicon.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.silicon.container.ContainerPackager;

/** The recipe packager. Pattern slots that are in use are lit up. */
public class GuiPackager extends GuiBC8<ContainerPackager> {
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/packager.png");
    private static final int SIZE_X = 176, SIZE_Y = 197;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_SET = new GuiIcon(TEXTURE, SIZE_X, 0, 18, 18);

    public GuiPackager(ContainerPackager container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (container.tile.isPatternSlotSet(x + y * 3)) {
                    ICON_SET.drawAt(graphics, leftPos + ContainerPackager.PATTERN_X - 1 + x * 18,
                        topPos + ContainerPackager.PATTERN_Y - 1 + y * 18);
                }
            }
        }
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, playerInventoryTitle, leftPos + 8, topPos + imageHeight - 94, 0x404040, false);
    }
}
