/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.client.gui;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.lists.ListMatchHandler;
import buildcraftreborn.core.item.ItemList_BC8;
import buildcraftreborn.core.list.ContainerList;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiElementSimple;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.IInteractionElement;
import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.list.ListHandler;

/** The list GUI: a label, two lines of nine filter slots, and per line the "precise", "same type" and "same material"
 * buttons. In "same type" or "same material" mode the other slots of a line show examples of what it matches. */
public class GuiList extends GuiBC8<ContainerList> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/list_new.png");
    private static final int SIZE_X = 176, SIZE_Y = 191;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_HIGHLIGHT = new GuiIcon(TEXTURE_BASE, 176, 0, 16, 16);
    private static final GuiIcon ICON_ONE_STACK = new GuiIcon(TEXTURE_BASE, 0, 191, 20, 20);
    private static final int BUTTON_COUNT = 3;
    private static final String[] BUTTON_TOOLTIPS = { "gui.list.nbt", "gui.list.metadata", "gui.list.oredict" };

    private final Map<Integer, Map<ListMatchHandler.Type, NonNullList<ItemStack>>> exampleCache = new HashMap<>();
    private EditBox textField;

    public GuiList(ContainerList container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        for (int line = 0; line < ListHandler.HEIGHT; line++) {
            int bOffX = 8 + ListHandler.WIDTH * 18 - BUTTON_COUNT * 11;
            int bOffY = 32 + line * 34 + 18;
            for (int button = 0; button < BUTTON_COUNT; button++) {
                mainGui.shownElements.add(new OptionButton(mainGui, new GuiRectangle(bOffX + button * 11, bOffY, 11,
                    11).offset(mainGui.rootElement), line, button));
            }
        }
    }

    @Override
    protected void init() {
        super.init();
        textField = new EditBox(font, leftPos + 10, topPos + 10, 156, 12, Component.translatable("item.buildcraftreborn.list"));
        textField.setMaxLength(ContainerList.MAX_LABEL_LENGTH);
        textField.setValue(container.getLabel());
        textField.setResponder(container::setLabel);
        textField.setFocused(false);
        addRenderableWidget(textField);
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);

        for (int i = 0; i < ListHandler.HEIGHT; i++) {
            if (container.lines[i].isOneStackMode()) {
                ICON_ONE_STACK.drawAt(graphics, leftPos + 6, topPos + 30 + i * 34);
                NonNullList<ItemStack> examples = getExamplesList(i, container.lines[i].getSortingType());
                for (int slot = 1; slot < ListHandler.WIDTH; slot++) {
                    int x = leftPos + 8 + slot * 18;
                    int y = topPos + 32 + i * 34;
                    ICON_HIGHLIGHT.drawAt(graphics, x, y);
                    if (slot - 1 < examples.size()) {
                        graphics.renderItem(examples.get(slot - 1), x, y);
                    }
                }
            }
        }
    }

    @Override
    protected void drawForegroundLayerAboveElements(GuiGraphics graphics, int mouseX, int mouseY) {
        super.drawForegroundLayerAboveElements(graphics, mouseX, mouseY);
        // Tooltips for the examples (they aren't in any slot)
        for (int i = 0; i < ListHandler.HEIGHT; i++) {
            if (!container.lines[i].isOneStackMode()) {
                continue;
            }
            NonNullList<ItemStack> examples = getExamplesList(i, container.lines[i].getSortingType());
            for (int slot = 1; slot < ListHandler.WIDTH; slot++) {
                int x = leftPos + 8 + slot * 18;
                int y = topPos + 32 + i * 34;
                if (mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16 && slot - 1 < examples.size()) {
                    graphics.renderTooltip(font, examples.get(slot - 1), mouseX, mouseY);
                }
            }
        }
    }

    private boolean isCarryingNonEmptyList() {
        ItemStack stack = menu.getCarried();
        return stack.getItem() instanceof ItemList_BC8 && !ListHandler.isDefault(stack);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isCarryingNonEmptyList() || container.getListItemStack().isEmpty()) {
            textField.setFocused(false);
        }
        boolean result = super.mouseClicked(mouseX, mouseY, button);
        // A phantom slot click changes what the examples should show
        for (int line = 0; line < ListHandler.HEIGHT; line++) {
            clearExamplesCache(line);
        }
        return result;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            return super.keyPressed(keyCode, scanCode, modifiers);
        }
        // Typing a label mustn't close the GUI (on the inventory key) or drop items
        if (textField.isFocused()) {
            return textField.keyPressed(keyCode, scanCode, modifiers) || textField.canConsumeInput();
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void clearExamplesCache(int lineId) {
        Map<ListMatchHandler.Type, NonNullList<ItemStack>> exampleList = exampleCache.get(lineId);
        if (exampleList != null) {
            exampleList.clear();
        }
    }

    private NonNullList<ItemStack> getExamplesList(int lineId, ListMatchHandler.Type type) {
        Map<ListMatchHandler.Type, NonNullList<ItemStack>> exampleList =
            exampleCache.computeIfAbsent(lineId, k -> new EnumMap<>(ListMatchHandler.Type.class));

        if (!exampleList.containsKey(type)) {
            NonNullList<ItemStack> examples = container.lines[lineId].getExamples();
            ItemStack input = container.lines[lineId].stacks.get(0);
            if (!input.isEmpty()) {
                examples.removeIf(is -> ItemStack.isSameItem(input, is));
            }
            exampleList.put(type, examples);
        }
        return exampleList.get(type);
    }

    /** One of a line's three option buttons (1.12's toggle {@code GuiImageButton}s): the button face is drawn from
     * the GUI texture at 176+11*state,16 and its icon at 176/185/194,28. */
    private class OptionButton extends GuiElementSimple implements IInteractionElement {
        private final int line, option;

        OptionButton(BuildCraftGui gui, buildcraftreborn.lib.gui.pos.IGuiArea area, int line, int option) {
            super(gui, area);
            this.line = line;
            this.option = option;
        }

        @Override
        public void drawBackground(GuiGraphics graphics, float partialTicks) {
            boolean active = container.lines[line].getOption(option);
            boolean hovered = contains(gui.mouse);
            int state = hovered ? (active ? 4 : 2) : (active ? 3 : 1);
            graphics.blit(TEXTURE_BASE, (int) getX(), (int) getY(), 176 + state * 11, 16, 11, 11);
            graphics.blit(TEXTURE_BASE, (int) getX() + 1, (int) getY() + 1, 176 + option * 9, 28, 9, 9);
        }

        @Override
        public void onMouseClicked(int button) {
            if (button == 0 && contains(gui.mouse)) {
                container.switchButton(line, option);
                clearExamplesCache(line);
            }
        }

        @Override
        public void addToolTips(List<ToolTip> tooltips) {
            if (contains(gui.mouse)) {
                tooltips.add(ToolTip.createLocalized(BUTTON_TOOLTIPS[option]));
            }
        }
    }
}
