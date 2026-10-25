/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.silicon.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.silicon.item.ItemPackage.PackageTooltip;

/** Draws a package's crafting grid in its tooltip. 7.1 drew it at half size with a special font renderer; this draws
 * it at full size, like a crafting table's grid. */
public class ClientPackageTooltip implements ClientTooltipComponent {
    private static final int CELL = 18;
    private static final int BORDER = 0xFF_8B_8B_8B, CELL_BACK = 0xFF_37_37_37;

    private final NonNullList<ItemStack> contents;

    public ClientPackageTooltip(PackageTooltip tooltip) {
        this.contents = tooltip.contents();
    }

    @Override
    public int getHeight() {
        return CELL * 3 + 4;
    }

    @Override
    public int getWidth(Font font) {
        return CELL * 3;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        for (int i = 0; i < contents.size(); i++) {
            int cx = x + (i % 3) * CELL;
            int cy = y + (i / 3) * CELL;
            graphics.fill(cx, cy, cx + CELL, cy + CELL, BORDER);
            graphics.fill(cx + 1, cy + 1, cx + CELL - 1, cy + CELL - 1, CELL_BACK);
            ItemStack stack = contents.get(i);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, cx + 1, cy + 1);
                graphics.renderItemDecorations(font, stack, cx + 1, cy + 1);
            }
        }
    }
}
