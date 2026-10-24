/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.elem;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiElementSimple;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraftreborn.lib.gui.IInteractionElement;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.gui.widget.WidgetFluidTank;
import buildcraftreborn.lib.misc.GuiUtil;

/** Draws a {@link WidgetFluidTank}'s fluid with an optional overlay (the gauge lines), shows its tooltip, and sends
 * clicks to the server. Client only. */
public class GuiElementFluidTank extends GuiElementSimple implements IInteractionElement {
    private final WidgetFluidTank widget;
    @Nullable
    private final GuiIcon overlay;

    public GuiElementFluidTank(BuildCraftGui gui, IGuiArea area, WidgetFluidTank widget, @Nullable GuiIcon overlay) {
        super(gui, area);
        this.widget = widget;
        this.overlay = overlay;
    }

    @Override
    public void drawBackground(GuiGraphics graphics, float partialTicks) {
        GuiUtil.drawFluid(graphics, this, widget.tank.getFluidForRender(), widget.tank.getCapacity());
        if (overlay != null) {
            overlay.drawCutInside(graphics, this);
        }
    }

    @Override
    public void onMouseClicked(int button) {
        if (contains(gui.mouse)) {
            widget.sendClick();
        }
    }

    @Override
    public void addHelpElements(List<HelpPosition> elements) {
        elements.add(widget.tank.helpInfo.target(this.expand(4)));
    }

    @Override
    public void addToolTips(List<ToolTip> tooltips) {
        if (contains(gui.mouse)) {
            List<Component> lines = new ArrayList<>();
            widget.tank.addTooltip(lines);
            tooltips.add(new ToolTip(lines));
        }
    }
}
