/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.energy.tile.TileDynamoMJ;
import buildcraftreborn.lib.client.BCLibSprites;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.GuiIcon;
import buildcraftreborn.lib.gui.config.GuiConfigManager;
import buildcraftreborn.lib.gui.ledger.Ledger_Neptune;
import buildcraftreborn.lib.misc.LocaleUtil;

/** The dynamo's power ledger: like the engine one, but its output is Forge Energy. */
public class LedgerDynamoMJ extends Ledger_Neptune {
    private static final int OVERLAY_COLOUR = 0xFF_D4_6C_1F;
    private static final int HEADER_COLOUR = 0xFF_E1_C9_2F;
    private static final int SUB_HEADER_COLOUR = 0xFF_AA_AF_b8;
    private static final int TEXT_COLOUR = 0xFF_00_00_00;

    public final TileDynamoMJ engine;

    public LedgerDynamoMJ(BuildCraftGui gui, TileDynamoMJ engine, boolean expandPositive) {
        super(gui, OVERLAY_COLOUR, expandPositive);
        this.engine = engine;
        this.title = "gui.power";

        appendText(I18n.get("gui.currentOutput") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeRfFlow(engine.getCurrentOutput()).getString(), TEXT_COLOUR);
        appendText(I18n.get("gui.stored") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeMj(engine.getMjStored()).getString(), TEXT_COLOUR);
        appendText(I18n.get("gui.heat") + ":", SUB_HEADER_COLOUR).setDropShadow(true);
        appendText(() -> LocaleUtil.localizeHeat(engine.getHeat()).getString(), TEXT_COLOUR);
        calculateMaxSize();

        setOpenProperty(GuiConfigManager.getOrAddBoolean(BuildCraftReborn.id("engine"), "ledger.power.is_open",
            false));
    }

    @Override
    public int getTitleColour() {
        return HEADER_COLOUR;
    }

    @Override
    protected void drawIcon(GuiGraphics graphics, double x, double y) {
        ISprite sprite = switch (engine.getPowerStage()) {
            case OVERHEAT -> BCLibSprites.ENGINE_OVERHEAT;
            case RED, YELLOW -> BCLibSprites.ENGINE_WARM;
            default -> engine.isEngineOn() ? BCLibSprites.ENGINE_ACTIVE : BCLibSprites.ENGINE_INACTIVE;
        };
        GuiIcon.draw(graphics, sprite, x, y, x + 16, y + 16);
    }
}
