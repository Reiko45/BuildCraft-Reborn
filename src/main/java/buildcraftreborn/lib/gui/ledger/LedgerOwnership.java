/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.ledger;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.PlayerSkin;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.BuildCraftGui;
import buildcraftreborn.lib.gui.config.GuiConfigManager;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** Shows who owns (placed) the machine, with their face as the icon. Client only. */
public class LedgerOwnership extends Ledger_Neptune {
    private final TileBC_Neptune tile;

    public LedgerOwnership(BuildCraftGui gui, TileBC_Neptune tile, boolean expandPositive) {
        super(gui, 0xFF_E0_F0_FF, expandPositive);
        this.title = "gui.ledger.ownership";
        this.tile = tile;

        appendText(this::getOwnerName, 0);
        calculateMaxSize();
        setOpenProperty(GuiConfigManager.getOrAddBoolean(BuildCraftReborn.id("base"), "ledger.owner.is_open", false));
    }

    @Override
    protected void drawIcon(GuiGraphics graphics, double x, double y) {
        GameProfile owner = tile.getOwner();
        PlayerSkin skin = Minecraft.getInstance().getSkinManager().getInsecureSkin(owner);
        PlayerFaceRenderer.draw(graphics, skin, (int) x, (int) y, 16);
    }

    private String getOwnerName() {
        GameProfile owner = tile.getOwner();
        if (owner == null) {
            return "no-one";
        }
        return owner.getName();
    }
}
