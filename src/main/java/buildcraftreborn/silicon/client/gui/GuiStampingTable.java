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
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.silicon.container.ContainerStampingTable;
import buildcraftreborn.silicon.tile.TileStampingTable;

/** The stamping table. The press between the input and the output fills up with power, flashing as it goes (7.1). */
public class GuiStampingTable extends GuiBC8<ContainerStampingTable> {
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/stamping_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 151;
    private static final int FLASH_DELAY = 3;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS_A = new GuiIcon(TEXTURE, 0, 197, 98, 24);
    private static final GuiIcon ICON_PROGRESS_B = new GuiIcon(TEXTURE, 0, 221, 98, 24);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(36, 14, 98, 24);

    private boolean flash;
    private int flashDelay;

    public GuiStampingTable(ContainerStampingTable container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerTablePower(mainGui, container.tile, true));
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (flashDelay <= 0) {
            flashDelay = FLASH_DELAY;
            flash = !flash;
        } else {
            flashDelay--;
        }
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
        TileStampingTable tile = container.tile;
        long target = tile.getTarget();
        if (target > 0 && tile.power > 0) {
            double done = Math.min(1, (double) tile.power / target);
            int width = (int) Math.ceil(RECT_PROGRESS.width * done);
            (flash ? ICON_PROGRESS_A : ICON_PROGRESS_B).drawCutInside(graphics, new GuiRectangle(RECT_PROGRESS.x,
                RECT_PROGRESS.y, width, RECT_PROGRESS.height).offset(mainGui.rootElement));
        }
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, leftPos + (imageWidth - font.width(title)) / 2, topPos + 5, 0x404040, false);
    }
}
