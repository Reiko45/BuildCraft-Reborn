/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.builders.client.SnapshotPreview;
import buildcraftreborn.builders.container.ContainerReplacer;
import buildcraftreborn.builders.item.ItemSnapshot;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;

/** The replacer's GUI: a preview of the blueprint, and the slots for it and the two single schematics.
 * <p>
 * 1.21: 1.12's name field did nothing (it was never sent anywhere), so it is gone. */
public class GuiReplacer extends GuiBC8<ContainerReplacer> {
    private static final ResourceLocation TEXTURE_BASE = BuildCraftReborn.id("textures/gui/replacer.png");
    private static final int SIZE_X = 176, SIZE_Y = 241;
    private static final GuiIcon ICON_GUI = new GuiIcon(TEXTURE_BASE, 0, 0, SIZE_X, SIZE_Y);

    public GuiReplacer(ContainerReplacer container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, leftPos, topPos);
        SnapshotPreview.render(graphics, ItemSnapshot.getHeader(container.tile.invSnapshot.getStackInSlot(0)),
            leftPos + 8, topPos + 9, 160, 100);
    }
}
