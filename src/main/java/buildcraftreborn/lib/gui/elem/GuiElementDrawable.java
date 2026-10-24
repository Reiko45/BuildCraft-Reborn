/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.elem;

import java.util.function.BooleanSupplier;

import net.minecraft.client.gui.GuiGraphics;

import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiElementSimple;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.gui.pos.IGuiArea;

/** Draws an {@link ISimpleDrawable} in an area. Client only. */
public class GuiElementDrawable extends GuiElementSimple {
    private final ISimpleDrawable drawable;
    private final BooleanSupplier visible;
    private final boolean foreground;

    public GuiElementDrawable(BuildCraftGui gui, IGuiArea element, ISimpleDrawable drawable, boolean foreground) {
        this(gui, element, drawable, foreground, () -> true);
    }

    public GuiElementDrawable(BuildCraftGui gui, IGuiArea element, ISimpleDrawable drawable, boolean foreground,
        BooleanSupplier visible) {
        super(gui, element);
        this.drawable = drawable;
        this.visible = visible;
        this.foreground = foreground;
    }

    @Override
    public void drawBackground(GuiGraphics graphics, float partialTicks) {
        if (!foreground) {
            draw(graphics);
        }
    }

    @Override
    public void drawForeground(GuiGraphics graphics, float partialTicks) {
        if (foreground) {
            draw(graphics);
        }
    }

    private void draw(GuiGraphics graphics) {
        if (visible.getAsBoolean()) {
            drawable.drawAt(graphics, this);
        }
    }
}
