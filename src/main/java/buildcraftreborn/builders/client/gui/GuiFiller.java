/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.gui;

import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.tiles.IControllable.Mode;
import buildcraftreborn.builders.container.ContainerFiller;
import buildcraftreborn.builders.tile.TileFiller;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.lib.client.BCLibSprites;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.config.GuiConfigManager;
import buildcraftreborn.lib.gui.help.ElementHelpInfo;
import buildcraftreborn.lib.gui.ledger.Ledger_Neptune;

/** The filler's GUI (1.12's {@code filler.json}). */
public class GuiFiller extends GuiFillerBase<ContainerFiller> {
    public GuiFiller(ContainerFiller container, Inventory inventory, Component title) {
        super(container, inventory, title, BuildCraftReborn.id("textures/gui/filler.png"), container.tile, 176, 241);
        TileFiller tile = container.tile;
        mainGui.shownElements.add(new LedgerProgress(mainGui, tile));
        mainGui.shownElements.add(new ToggleButton(mainGui, area(130, 40, 16, 16), 192, tile::canExcavate,
            tile::sendCanExcavate, on -> "tip.filler.excavate." + (on ? "on" : "off"),
            new ElementHelpInfo("buildcraft.help.filler.excavate.title", 0xFF_CC_AA_88,
                "buildcraft.help.filler.excavate.desc")));
        mainGui.shownElements.add(new StatusIcon(mainGui, area(12, 16, 16, 16), tile::isLocked,
            () -> BCLibSprites.LOCK, () -> Component.translatable("gui.filler.locked"),
            new ElementHelpInfo("gui.filler.locked", 0xFF_FF_BB_33, "buildcraft.help.filler.locked")));
        mainGui.shownElements.add(new StatusIcon(mainGui, area(32, 16, 16, 16),
            () -> tile.getControlMode() != Mode.ON,
            () -> BCCoreSprites.ACTION_MACHINE_CONTROL.get(tile.getControlMode()),
            () -> Component.literal(tile.getControlMode().name().toLowerCase(Locale.ROOT)),
            new ElementHelpInfo("gui.filler.control_mode", 0xFF_33_BB_FF, "buildcraft.help.filler.control_mode")));
    }

    @Override
    protected void drawForegroundLayer(GuiGraphics graphics, int mouseX, int mouseY) {
        drawCentredTitle(graphics, title);
        graphics.drawString(font, Component.translatable("gui.filling.resources"), leftPos + 7, topPos + 74,
            0x404040, false);
        graphics.drawString(font, playerInventoryTitle, leftPos + 7, topPos + 141, 0x404040, false);
    }

    /** How many blocks are left to break and to place. */
    private static class LedgerProgress extends Ledger_Neptune {
        private static final int TEXT_COLOUR = 0xFF_33_33_33;

        LedgerProgress(BuildCraftGui gui, TileFiller tile) {
            super(gui, 0xFF_6C_D4_1F, true);
            title = "gui.progress";
            appendText(() -> I18n.get("gui.leftToBreak") + ": " + tile.getCountToBreak(), TEXT_COLOUR);
            appendText(() -> I18n.get("gui.leftToPlace") + ": " + tile.getCountToPlace(), TEXT_COLOUR);
            calculateMaxSize();
            setOpenProperty(GuiConfigManager.getOrAddBoolean(BuildCraftReborn.id("filler"), "ledger.progress.is_open",
                false));
        }

        @Override
        protected void drawIcon(GuiGraphics graphics, double x, double y) {
            graphics.renderItem(new ItemStack(Items.IRON_PICKAXE), (int) x, (int) y);
        }
    }
}
