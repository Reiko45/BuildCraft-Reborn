/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.gui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.DyeColor;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.lib.client.BCLibSprites;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.transport.BCTransportSprites;
import buildcraftreborn.transport.container.ContainerEmzuliPipe_BC8;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

/** The emzuli pipe: four filters, each with a paint button. Left click picks the next colour, right click the
 * previous one, and middle click (or shift click) removes the paint. */
public class GuiEmzuliPipe_BC8 extends GuiBC8<ContainerEmzuliPipe_BC8> {
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/pipe_emzuli.png");
    private static final int SIZE_X = 176, SIZE_Y = 166;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_NO_PAINT = new GuiIcon(TEXTURE, SIZE_X, 40, 16, 16);
    private static final GuiIcon ICON_BUTTON = new GuiIcon(TEXTURE, 176, 0, 20, 20);
    private static final GuiIcon ICON_BUTTON_ACTIVE = ICON_BUTTON.offset(0, 20);
    /** Button positions, by slot index. */
    private static final int[][] BUTTONS = { { 49, 19 }, { 49, 47 }, { 106, 19 }, { 106, 47 } };

    public GuiEmzuliPipe_BC8(ContainerEmzuliPipe_BC8 container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Nullable
    private SlotIndex getHoveredButton(double mouseX, double mouseY) {
        for (SlotIndex index : SlotIndex.VALUES) {
            int x = leftPos + BUTTONS[index.ordinal()][0];
            int y = topPos + BUTTONS[index.ordinal()][1];
            if (mouseX >= x && mouseX < x + 20 && mouseY >= y && mouseY < y + 20) {
                return index;
            }
        }
        return null;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        SlotIndex index = getHoveredButton(mouseX, mouseY);
        if (index != null) {
            DyeColor old = container.behaviour.slotColours.get(index);
            DyeColor colour;
            if (button == 2 || Screen.hasShiftDown()) {
                colour = null;
            } else if (button == 0) {
                colour = ColourUtil.getNextOrNull(old);
            } else if (button == 1) {
                colour = ColourUtil.getPrevOrNull(old);
            } else {
                return true;
            }
            container.paintWidgets.get(index).setColour(colour);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);

        SlotIndex currentSlot = container.behaviour.getCurrentSlot();
        for (SlotIndex index : container.behaviour.getActiveSlots()) {
            boolean current = index == currentSlot;
            int x = leftPos + (index.ordinal() < 2 ? 4 : 155);
            int y = topPos + (index.ordinal() % 2 == 0 ? 21 : 49);
            ISprite sprite = current ? BCCoreSprites.TRIGGER_TRUE : BCLibSprites.LOCK;
            GuiIcon.draw(graphics, sprite, x, y, x + 16, y + 16);
        }

        for (SlotIndex index : SlotIndex.VALUES) {
            int x = leftPos + BUTTONS[index.ordinal()][0];
            int y = topPos + BUTTONS[index.ordinal()][1];
            DyeColor colour = container.behaviour.slotColours.get(index);
            (colour == null ? ICON_BUTTON : ICON_BUTTON_ACTIVE).drawAt(graphics, x, y);
            if (colour == null) {
                ICON_NO_PAINT.drawAt(graphics, x + 2, y + 2);
            } else {
                ISprite sprite = BCTransportSprites.ACTION_PIPE_COLOUR[colour.ordinal()];
                GuiIcon.draw(graphics, sprite, x + 2, y + 2, x + 18, y + 18);
            }
        }
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = leftPos + (imageWidth - font.width(title)) / 2;
        graphics.drawString(font, title, titleX, topPos + 6, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.inventory"), leftPos + 8, topPos + imageHeight - 93,
            0x404040, false);
    }

    @Override
    protected void drawForegroundLayerAboveElements(GuiGraphics graphics, int mouseX, int mouseY) {
        SlotIndex index = getHoveredButton(mouseX, mouseY);
        if (index != null) {
            DyeColor colour = container.behaviour.slotColours.get(index);
            Component line = colour == null ? Component.translatable("gui.pipes.emzuli.nopaint")
                : Component.translatable("gui.pipes.emzuli.paint", ColourUtil.getTextFullTooltip(colour));
            graphics.renderTooltip(font, line, mouseX, mouseY);
        }
    }
}
