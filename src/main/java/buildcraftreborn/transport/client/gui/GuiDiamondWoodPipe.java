/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.pipe.IFlowItems;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.transport.container.ContainerDiamondWoodPipe;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourWoodDiamond;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourWoodDiamond.FilterMode;

/** The emerald pipe: 9 filters, and a whitelist / blacklist / round robin choice (round robin only for items). */
public class GuiDiamondWoodPipe extends GuiBC8<ContainerDiamondWoodPipe> {
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/pipe_emerald.png");
    private static final ResourceLocation TEXTURE_BUTTON = BuildCraftReborn.id("textures/gui/pipe_emerald_button.png");
    private static final int SIZE_X = 175, SIZE_Y = 161;
    private static final int BUTTON_SIZE = 18;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_ROUND_ROBIN_INDEX = new GuiIcon(TEXTURE, 176, 0, 20, 20);
    private static final GuiIcon ICON_ROUND_ROBIN_NONE = new GuiIcon(TEXTURE, 176, 20, 20, 20);
    /** The button icons, by filter mode. */
    private static final GuiIcon[] ICON_MODES = { //
        new GuiIcon(TEXTURE_BUTTON, 19, 19, 16, 16), //
        new GuiIcon(TEXTURE_BUTTON, 37, 19, 16, 16), //
        new GuiIcon(TEXTURE_BUTTON, 55, 19, 16, 16) //
    };
    private static final String[] TOOLTIPS = { //
        "tip.PipeItemsEmerald.whitelist", "tip.PipeItemsEmerald.blacklist", "tip.PipeItemsEmerald.roundrobin" };

    private final PipeBehaviourWoodDiamond pipe;

    public GuiDiamondWoodPipe(ContainerDiamondWoodPipe container, Inventory inventory, Component title) {
        super(container, inventory, title);
        this.pipe = container.behaviour;
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    /** Round robin isn't implemented for fluids. */
    private int buttonCount() {
        return pipe.pipe.getFlow() instanceof IFlowItems ? 3 : 2;
    }

    private int buttonX(int index) {
        return leftPos + 7 + index * BUTTON_SIZE;
    }

    private int buttonY() {
        return topPos + 41;
    }

    private int getHoveredButton(double mouseX, double mouseY) {
        for (int i = 0; i < buttonCount(); i++) {
            int x = buttonX(i);
            if (mouseX >= x && mouseX < x + BUTTON_SIZE && mouseY >= buttonY() && mouseY < buttonY() + BUTTON_SIZE) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int hovered = getHoveredButton(mouseX, mouseY);
        if (hovered >= 0 && button == 0) {
            FilterMode mode = FilterMode.get(hovered);
            if (pipe.filterMode != mode) {
                pipe.filterMode = mode;
                container.sendNewFilterMode(mode);
            }
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        int titleX = leftPos + (imageWidth - font.width(title)) / 2;
        graphics.drawString(font, title, titleX, topPos + 6, 0x404040, false);
        graphics.drawString(font, Component.translatable("gui.inventory"), leftPos + 8, topPos + imageHeight - 93,
            0x404040, false);

        int hovered = getHoveredButton(mouseX, mouseY);
        for (int i = 0; i < buttonCount(); i++) {
            boolean active = pipe.filterMode.ordinal() == i;
            // Button states (1.12's GuiImageButton): 1 = up, 2 = up and hovered, 3 = down, 4 = down and hovered
            int state = active ? (hovered == i ? 4 : 3) : (hovered == i ? 2 : 1);
            new GuiIcon(TEXTURE_BUTTON, state * BUTTON_SIZE, 0, BUTTON_SIZE, BUTTON_SIZE)
                .drawAt(graphics, buttonX(i), buttonY());
            ICON_MODES[i].drawAt(graphics, buttonX(i) + 1, buttonY() + 1);
        }

        if (pipe.filterMode == FilterMode.ROUND_ROBIN) {
            GuiIcon icon = pipe.filterValid ? ICON_ROUND_ROBIN_INDEX : ICON_ROUND_ROBIN_NONE;
            int x = pipe.filterValid ? 18 * pipe.currentFilter : 0;
            icon.drawAt(graphics, leftPos + 6 + x, topPos + 16);
        }
    }

    @Override
    protected void drawForegroundLayerAboveElements(GuiGraphics graphics, int mouseX, int mouseY) {
        int hovered = getHoveredButton(mouseX, mouseY);
        if (hovered >= 0) {
            graphics.renderTooltip(font, Component.translatable(TOOLTIPS[hovered]), mouseX, mouseY);
        }
    }
}
