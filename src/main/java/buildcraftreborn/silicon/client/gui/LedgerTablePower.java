/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.client.BCLibSprites;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.config.GuiConfigManager;
import buildcraftreborn.lib.gui.ledger.Ledger_Neptune;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.silicon.tile.TileLaserTableBase;

/** The power ledger of the laser tables: the power the current job needs, the power stored and the laser power
 * coming in. */
public class LedgerTablePower extends Ledger_Neptune {
    private static final int OVERLAY_COLOUR = 0xFF_D4_6C_1F;
    private static final int SUB_HEADER_COLOUR = 0xFF_AA_AF_b8;
    private static final int TEXT_COLOUR = 0xFF_00_00_00;

    public final TileLaserTableBase tile;

    public LedgerTablePower(BuildCraftGui gui, TileLaserTableBase tile, boolean expandPositive) {
        super(gui, OVERLAY_COLOUR, expandPositive);
        this.tile = tile;
        title = "gui.power";

        appendText(I18n.get("gui.assemblyCurrentRequired") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(tile.getTarget()).getString(), TEXT_COLOUR);
        appendText(I18n.get("gui.stored") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(tile.power).getString(), TEXT_COLOUR);
        appendText(I18n.get("gui.assemblyRate") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMjFlow(tile.avgPowerClient).getString(), TEXT_COLOUR);
        calculateMaxSize();

        setOpenProperty(GuiConfigManager.getOrAddBoolean(BuildCraftReborn.id("all_tables"), "ledger.power.is_open",
            false));
    }

    @Override
    protected void drawIcon(GuiGraphics graphics, double x, double y) {
        ISprite sprite = tile.avgPowerClient > 0 ? BCLibSprites.ENGINE_ACTIVE : BCLibSprites.ENGINE_INACTIVE;
        GuiIcon.draw(graphics, sprite, x, y, x + 16, y + 16);
    }
}
