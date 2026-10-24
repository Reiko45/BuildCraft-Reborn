/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.robotics.container.ContainerProgrammingTable;
import buildcraftreborn.robotics.tile.TileProgrammingTable;
import buildcraftreborn.silicon.client.gui.LedgerTablePower;

/** The programming table: click an option to select it, and again to deselect it. */
public class GuiProgrammingTable extends GuiBC8<ContainerProgrammingTable> {
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/programming_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 207;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_SELECTED = new GuiIcon(TEXTURE, 196, 1, 16, 16);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE, SIZE_X, 18, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164, 36, 4, 70);

    public GuiProgrammingTable(ContainerProgrammingTable container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerTablePower(mainGui, container.tile, true));
    }

    private IGuiArea getOptionArea(int index) {
        int x = index % TileProgrammingTable.WIDTH;
        int y = index / TileProgrammingTable.WIDTH;
        return new GuiRectangle(ContainerProgrammingTable.OPTIONS_X + x * 18, ContainerProgrammingTable.OPTIONS_Y
            + y * 18, 16, 16).offset(mainGui.rootElement);
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
        TileProgrammingTable tile = container.tile;
        if (tile.optionId >= 0 && tile.optionId < tile.options.size()) {
            ICON_SELECTED.drawAt(graphics, getOptionArea(tile.optionId));
        }
        long target = tile.getTarget();
        if (target > 0) {
            double v = Math.min(1, (double) tile.power / target);
            int h = (int) Math.ceil(RECT_PROGRESS.height * v);
            ICON_PROGRESS.drawCutInside(graphics, new GuiRectangle(RECT_PROGRESS.x, RECT_PROGRESS.y
                + RECT_PROGRESS.height - h, RECT_PROGRESS.width, h).offset(mainGui.rootElement));
        }
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, leftPos + (imageWidth - font.width(title)) / 2, topPos + 15, 0x404040,
            false);
        graphics.drawString(font, playerInventoryTitle, leftPos + 8, topPos + imageHeight - 97, 0x404040, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            TileProgrammingTable tile = container.tile;
            for (int i = 0; i < tile.options.size(); i++) {
                if (getOptionArea(i).contains(mouseX, mouseY)) {
                    tile.sendSelectOption(tile.optionId == i ? -1 : i);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
