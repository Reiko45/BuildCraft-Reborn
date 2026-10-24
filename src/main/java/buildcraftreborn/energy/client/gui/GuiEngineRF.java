/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.client.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.energy.container.ContainerEngineRF;
import buildcraftreborn.lib.gui.ledger.LedgerEngine;
import buildcraftreborn.lib.misc.LocaleUtil;

public class GuiEngineRF extends GuiRfConverter<ContainerEngineRF> {
    public GuiEngineRF(ContainerEngineRF container, Inventory inventory, Component title) {
        super(container, inventory, title, BuildCraftReborn.id("textures/gui/rf_engine_gui.png"), 60, 31);
        mainGui.shownElements.add(new LedgerEngine(mainGui, container.tile, true));
    }

    @Override
    protected long getMjPerTick() {
        return container.tile.getMjPerTick();
    }

    @Override
    protected int getCurrentRF() {
        return container.tile.getCurrentRF();
    }

    @Override
    protected String getBatteryHelpKey() {
        return "buildcraft.help.rf_engine.rf_battery";
    }

    @Override
    protected String conversionText(long mjFlow, int rfFlow) {
        return LocaleUtil.localize("buildcraft.help.rf_engine.rf_battery.desc",
            LocaleUtil.localizeRf(rfFlow).getString(), LocaleUtil.localizeMjFlow(mjFlow).getString());
    }

    @Override
    protected String upgradeValue(long mj) {
        return LocaleUtil.localizeMjFlow(mj).getString();
    }
}
