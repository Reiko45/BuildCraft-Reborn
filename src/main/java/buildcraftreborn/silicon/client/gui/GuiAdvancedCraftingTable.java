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
import buildcraftreborn.lib.gui.ledger.LedgerHelp;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.silicon.container.ContainerAdvancedCraftingTable;

/** The advanced crafting table: a (phantom) recipe grid, its result, the materials and the output.
 * <p>
 * 1.12 also had a recipe book to fill in the grid; the 1.21 recipe book only works with menus built on vanilla's
 * {@code RecipeBookMenu}, so (as with the auto workbench) it isn't ported. */
public class GuiAdvancedCraftingTable extends GuiBC8<ContainerAdvancedCraftingTable> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/advanced_crafting_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 241;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(164, 7, 4, 70);

    public GuiAdvancedCraftingTable(ContainerAdvancedCraftingTable container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerHelp(mainGui, true));
    }

    @Override
    protected boolean shouldAddHelpLedger() {
        // 1.12 put it on the right side, so it wouldn't clash with the recipe book
        return false;
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
        drawPower(graphics, ICON_PROGRESS, RECT_PROGRESS);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        drawTitle(graphics, 5);
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
