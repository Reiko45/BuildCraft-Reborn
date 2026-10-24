/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.gui;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.builders.container.ContainerArchitectTable;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.pos.GuiRectangle;

/** The architect table's GUI: the snapshot slots, the scan progress and the name to give the scan. */
public class GuiArchitectTable extends GuiBC8<ContainerArchitectTable> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/architect.png");
    private static final int SIZE_X = 256, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, 0, 166, 24, 17);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(159, 34, 24, 17);

    private EditBox nameField;

    public GuiArchitectTable(ContainerArchitectTable container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Override
    protected void init() {
        super.init();
        nameField = new EditBox(font, leftPos + 90, topPos + 62, 156, 12, Component.translatable("gui.architect.name"));
        nameField.setMaxLength(64);
        nameField.setValue(container.tile.name);
        nameField.setResponder(text -> container.sendNameToServer(text.trim()));
        addRenderableWidget(nameField);
        setInitialFocus(nameField);
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, leftPos, topPos);
        drawProgress(graphics, RECT_PROGRESS, ICON_PROGRESS, container.tile.deltaProgress.getDynamic(partialTicks), 1);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, leftPos + 90, topPos + 6, 0x404040, false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        // Typing a name mustn't close the GUI (on the inventory key) or drop items
        if (nameField.isFocused()) {
            return nameField.keyPressed(keyCode, scanCode, modifiers) || nameField.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
