/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.gui;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.builders.container.ContainerElectronicLibrary;
import buildcraftreborn.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.pos.GuiRectangle;

/** The electronic library's GUI: the player's own snapshot library on the left (click one to select it, for
 * copying to the server), and the slots to copy blueprints and templates down and up. */
public class GuiElectronicLibrary extends GuiBC8<ContainerElectronicLibrary> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/electronic_library.png");
    private static final int SIZE_X = 244, SIZE_Y = 220;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS_DOWN = new GuiIcon(TEXTURE_BASE, 234, 240, 22, 16);
    private static final GuiRectangle RECT_PROGRESS_DOWN = new GuiRectangle(194, 58, 22, 16);
    private static final GuiIcon ICON_PROGRESS_UP = new GuiIcon(TEXTURE_BASE, 234, 224, 22, 16);
    private static final GuiRectangle RECT_PROGRESS_UP = new GuiRectangle(194, 79, 22, 16);
    private static final int LIST_X = 8, LIST_Y = 22, LIST_WIDTH = 154, ROW_HEIGHT = 8, MAX_ROWS = 13;

    private Button delButton;

    public GuiElectronicLibrary(ContainerElectronicLibrary container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Override
    protected void init() {
        super.init();
        delButton = addRenderableWidget(Button.builder(Component.translatable("gui.del"), b -> onDelete())
            .bounds(leftPos + 174, topPos + 109, 64, 16).build());
    }

    private void onDelete() {
        Snapshot.Key selected = container.tile.selected;
        if (selected != null) {
            Snapshot snapshot = getSnapshots().getSnapshot(selected);
            if (snapshot != null) {
                container.sendSelectedToServer(null);
                getSnapshots().removeSnapshot(snapshot.key);
            }
        }
    }

    private GlobalSavedDataSnapshots getSnapshots() {
        return GlobalSavedDataSnapshots.get(container.tile.getLevel());
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (delButton != null) {
            Snapshot.Key selected = container.tile.selected;
            delButton.active = selected != null && getSnapshots().getSnapshot(selected) != null;
        }
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, leftPos, topPos);
        drawProgress(graphics, RECT_PROGRESS_DOWN, ICON_PROGRESS_DOWN,
            -container.tile.deltaProgressDown.getDynamic(partialTicks), 1);
        drawProgress(graphics, RECT_PROGRESS_UP, ICON_PROGRESS_UP,
            container.tile.deltaProgressUp.getDynamic(partialTicks), 1);
        List<Snapshot.Key> list = getSnapshots().getList();
        for (int i = 0; i < list.size() && i < MAX_ROWS; i++) {
            Snapshot.Key key = list.get(i);
            int x = leftPos + LIST_X;
            int y = topPos + LIST_Y + i * ROW_HEIGHT;
            boolean isSelected = key.equals(container.tile.selected);
            if (isSelected) {
                graphics.fill(x, y, x + LIST_WIDTH, y + ROW_HEIGHT, 0xFF_55_55_55);
            }
            String text = key.header == null ? key.toString() : key.header.name;
            graphics.drawString(font, font.plainSubstrByWidth(text, LIST_WIDTH), x, y, isSelected ? 0xffffa0 : 0xe0e0e0,
                false);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        List<Snapshot.Key> list = getSnapshots().getList();
        for (int i = 0; i < list.size() && i < MAX_ROWS; i++) {
            int x = leftPos + LIST_X;
            int y = topPos + LIST_Y + i * ROW_HEIGHT;
            if (mouseX >= x && mouseX < x + LIST_WIDTH && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                container.sendSelectedToServer(list.get(i));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, leftPos + 8, topPos + 8, 0x404040, false);
    }
}
