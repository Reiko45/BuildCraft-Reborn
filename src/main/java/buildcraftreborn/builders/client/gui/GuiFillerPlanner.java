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
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.builders.container.ContainerFillerPlanner;

/** The filler planner addon's GUI (1.12's {@code filler_planner.json}). */
public class GuiFillerPlanner extends GuiFillerBase<ContainerFillerPlanner> {
    public GuiFillerPlanner(ContainerFillerPlanner container, Inventory inventory, Component title) {
        super(container, inventory, title, BuildCraftReborn.id("textures/gui/filler_planner.png"), container.addon,
            176, 81);
    }

    @Override
    protected boolean shouldAddHelpLedger() {
        return true;
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        drawCentredTitle(graphics, title);
    }
}
