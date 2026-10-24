/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.factory.container.ContainerAutoCraftItems;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.ledger.LedgerHelp;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** The auto workbench screen. Each material slot shows the ingredient it is reserved for, framed green when the
 * slot holds that item (or is empty) and red when it holds something else.
 * <p>
 * 1.12 also had a recipe book that filled in the blueprint; the 1.21 recipe book only works with menus built on
 * vanilla's {@code RecipeBookMenu}, so it isn't ported. The pattern is set by clicking items into the blueprint. */
public class GuiAutoCraftItems extends GuiBC8<ContainerAutoCraftItems> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/autobench_item.png");
    private static final ResourceLocation TEXTURE_MISC = BuildCraftReborn.id("textures/gui/misc_slots.png");
    private static final int SIZE_X = 176, SIZE_Y = 197;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);
    private static final GuiIcon ICON_FILTER_OVERLAY_SAME = new GuiIcon(TEXTURE_MISC, 54, 0, 18, 18);
    private static final GuiIcon ICON_FILTER_OVERLAY_DIFFERENT = new GuiIcon(TEXTURE_MISC, 72, 0, 18, 18);
    private static final GuiIcon ICON_FILTER_OVERLAY_SIMILAR = new GuiIcon(TEXTURE_MISC, 90, 0, 18, 18);
    private static final GuiIcon ICON_PROGRESS = new GuiIcon(TEXTURE_BASE, SIZE_X, 0, 23, 10);
    private static final GuiRectangle RECT_PROGRESS = new GuiRectangle(90, 47, 23, 10);

    public GuiAutoCraftItems(ContainerAutoCraftItems container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new LedgerHelp(mainGui, true));
    }

    @Override
    protected boolean shouldAddHelpLedger() {
        // Don't add it on the left side because it clashed with the recipe book in 1.12
        return false;
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);

        double progress = container.tile.getProgress(partialTicks);

        drawProgress(graphics, RECT_PROGRESS, ICON_PROGRESS, progress, 1);

        if (hasFilters()) {
            forEachFilter((slot, filterStack) -> {
                int x = slot.x + (int) mainGui.rootElement.getX();
                int y = slot.y + (int) mainGui.rootElement.getY();
                graphics.renderItem(filterStack, x, y);
                graphics.renderItemDecorations(font, filterStack, x, y);
            });

            RenderSystem.disableDepthTest();
            forEachFilter((slot, filterStack) -> {
                ItemStack real = slot.getItem();
                final GuiIcon icon;
                if (real.isEmpty() || StackUtil.canMerge(real, filterStack)) {
                    icon = ICON_FILTER_OVERLAY_SAME;
                } else {
                    icon = ICON_FILTER_OVERLAY_DIFFERENT;
                }
                int x = slot.x + (int) mainGui.rootElement.getX();
                int y = slot.y + (int) mainGui.rootElement.getY();
                icon.drawAt(graphics, x - 1, y - 1);
            });
            RenderSystem.enableDepthTest();
        }
    }

    private boolean hasFilters() {
        ItemHandlerSimple filters = container.tile.invMaterialFilter;
        for (int s = 0; s < filters.getSlots(); s++) {
            ItemStack filter = filters.getStackInSlot(s);
            if (!filter.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void forEachFilter(IFilterSlotIterator iter) {
        ItemHandlerSimple filters = container.tile.invMaterialFilter;
        for (int s = 0; s < filters.getSlots(); s++) {
            ItemStack filter = filters.getStackInSlot(s);
            if (!filter.isEmpty()) {
                iter.iterate(container.materialSlots[s], filter);
            }
        }
    }

    @FunctionalInterface
    private interface IFilterSlotIterator {
        void iterate(SlotBase drawSlot, ItemStack filterStack);
    }
}
