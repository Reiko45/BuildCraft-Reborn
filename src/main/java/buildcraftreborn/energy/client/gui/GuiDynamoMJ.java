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
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.energy.container.ContainerDynamoMJ;
import buildcraftreborn.lib.misc.LocaleUtil;

public class GuiDynamoMJ extends GuiRfConverter<ContainerDynamoMJ> {
    public GuiDynamoMJ(ContainerDynamoMJ container, Inventory inventory, Component title) {
        super(container, inventory, title, BuildCraftReborn.id("textures/gui/mj_dynamo_gui.png"), 42, 139);
        mainGui.shownElements.add(new LedgerDynamoMJ(mainGui, container.tile, true));
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
        return "buildcraft.help.mj_dynamo.rf_battery";
    }

    @Override
    protected String conversionText(long mjFlow, int rfFlow) {
        return LocaleUtil.localize("buildcraft.help.mj_dynamo.rf_battery.desc",
            LocaleUtil.localizeMj(mjFlow).getString(), LocaleUtil.localizeRfFlow(rfFlow).getString());
    }

    @Override
    protected String upgradeValue(long mj) {
        return LocaleUtil.localizeRfFlow((int) (mj / MjAPI.getRfConversion().mjPerRf)).getString();
    }
}
