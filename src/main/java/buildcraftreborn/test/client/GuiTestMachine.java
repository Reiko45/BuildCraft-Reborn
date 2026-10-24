/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.elem.GuiElementFluidTank;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.test.ContainerTestMachine;
import buildcraftreborn.test.TileTestMachine;

/** Screen for the test machine, drawn on BuildCraft's tank GUI texture. Client only. */
public class GuiTestMachine extends GuiBC8<ContainerTestMachine> {
    private static final int SIZE_X = 176, SIZE_Y = 181;
    private static final GuiIcon ICON_GUI = new GuiIcon(BuildCraftReborn.id("textures/gui/tank.png"), 0, 0, SIZE_X,
        SIZE_Y);
    private static final GuiIcon ICON_TANK_OVERLAY = new GuiIcon(BuildCraftReborn.id("textures/gui/tank.png"), 176,
        0, 16, 64);
    /** slot.png is 32x32 with the 18x18 slot frame at (7, 7). */
    private static final GuiIcon ICON_SLOT = new GuiIcon(BuildCraftReborn.id("textures/gui/slot.png"), 7, 7, 18, 18,
        32);

    public GuiTestMachine(ContainerTestMachine container, Inventory inventory, Component title) {
        super(container, inventory, title);
        imageWidth = SIZE_X;
        imageHeight = SIZE_Y;
        mainGui.shownElements.add(new GuiElementFluidTank(mainGui,
            new GuiRectangle(80, 18, 16, 64).offset(mainGui.rootElement), container.widgetTank, ICON_TANK_OVERLAY));
    }

    @Override
    protected void drawBackgroundLayer(GuiGraphics graphics, float partialTicks) {
        ICON_GUI.drawAt(graphics, mainGui.rootElement);
        ICON_SLOT.drawAt(graphics, mainGui.rootElement.getX() + ContainerTestMachine.SLOT_X - 1,
            mainGui.rootElement.getY() + ContainerTestMachine.SLOT_Y - 1);
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.drawString(font, title, x + (imageWidth - font.width(title)) / 2, y + 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, x + 8, y + imageHeight - 94, 0x404040, false);
        TileTestMachine tile = container.tile;
        graphics.drawString(font, LocaleUtil.localizeMj(tile.getClientStored()), x + 104, y + 30, 0x404040, false);
        graphics.drawString(font, Component.literal("/ ").append(LocaleUtil.localizeMj(TileTestMachine.BATTERY_CAPACITY)),
            x + 104, y + 42, 0x404040, false);
    }
}
