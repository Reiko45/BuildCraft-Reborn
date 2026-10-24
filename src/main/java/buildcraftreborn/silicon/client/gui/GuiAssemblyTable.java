/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.silicon.EnumAssemblyRecipeState;
import buildcraftreborn.silicon.container.ContainerAssemblyTable;
import buildcraftreborn.silicon.tile.TileAssemblyTable.AssemblyInstruction;

/** The assembly table: its 12 slots on the left, and on the right everything they can make. Click a recipe to save
 * it (the table makes saved recipes in turn, while it has the ingredients) and again to forget it. */
public class GuiAssemblyTable extends GuiBC8<ContainerAssemblyTable> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/assembly_table.png");
    private static final int SIZE_X = 176, SIZE_Y = 220;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_SAVED = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 16, 16);
    private static final GuiIcon ICON_SAVED_ENOUGH = new GuiIcon(TEXTURE_BASE, SIZE_X, 16, 16, 16);
    private static final GuiIcon ICON_SAVED_ENOUGH_ACTIVE = new GuiIcon(TEXTURE_BASE, SIZE_X, 32, 16, 16);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 48, 4, 70);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(86, 36, 4, 70);

    public GuiAssemblyTable(ContainerAssemblyTable container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerTablePower(mainGui, container.tile, true));
    }

    private IGuiArea getArea(int index) {
        if (index >= 3 * 4) {
            return GuiRectangle.ZERO;
        }
        return new GuiRectangle(116 + (index % 3) * 18, 36 + (index / 3) * 18, 16, 16).offset(mainGui.rootElement);
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
        drawPower(graphics, ICON_PROGRESS, RECT_PROGRESS);
        List<EnumAssemblyRecipeState> states = new ArrayList<>(container.tile.recipesStates.values());
        for (int i = 0; i < states.size(); i++) {
            GuiIcon icon = switch (states.get(i)) {
                case SAVED -> ICON_SAVED;
                case SAVED_ENOUGH -> ICON_SAVED_ENOUGH;
                case SAVED_ENOUGH_ACTIVE -> ICON_SAVED_ENOUGH_ACTIVE;
                default -> null;
            };
            if (icon != null) {
                icon.drawAt(graphics, getArea(i));
            }
        }
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        drawTitle(graphics, 15);
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean handled = super.mouseClicked(mouseX, mouseY, button);
        if (button == 0) {
            int i = 0;
            for (Map.Entry<AssemblyInstruction, EnumAssemblyRecipeState> entry : container.tile.recipesStates
                .entrySet()) {
                if (getArea(i).contains(mouseX, mouseY)) {
                    container.tile.sendRecipeStateToServer(entry.getKey(),
                        entry.getValue() == EnumAssemblyRecipeState.POSSIBLE ? EnumAssemblyRecipeState.SAVED
                            : EnumAssemblyRecipeState.POSSIBLE);
                    return true;
                }
                i++;
            }
        }
        return handled;
    }
}
