/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.container;

import java.io.IOException;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.filler.IFillerPattern;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.filler.FillerType;
import buildcraftreborn.builders.tile.TileFiller;
import buildcraftreborn.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraftreborn.lib.gui.ContainerBCTile;
import buildcraftreborn.lib.gui.slot.SlotBase;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.statement.FullStatement;

public class ContainerFiller extends ContainerBCTile<TileFiller> implements IContainerFilling {
    private final FullStatement<IFillerPattern> patternStatementClient =
        new FullStatement<>(FillerType.INSTANCE, 4, (statement, paramIndex) -> onStatementChange());

    public ContainerFiller(int windowId, Player player, TileFiller tile) {
        super(BCBuildersBlocks.FILLER_MENU.get(), windowId, player, tile);
        addFullPlayerInventory(153);
        // 1.12 created these at y 40 and its JSON GUI moved them on the client to the "Filling Resources" grid
        // (y 85); slot positions are final now, so they are created there directly.
        for (int sy = 0; sy < 3; sy++) {
            for (int sx = 0; sx < 9; sx++) {
                addSlot(new SlotBase(tile.invResources, sx + sy * 9, sx * 18 + 8, sy * 18 + 85));
            }
        }
        init();
    }

    public static ContainerFiller fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        return new ContainerFiller(windowId, inventory.player, readTile(inventory, buf, TileFiller.class));
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public FullStatement<IFillerPattern> getPatternStatementClient() {
        return patternStatementClient;
    }

    @Override
    public FullStatement<IFillerPattern> getPatternStatement() {
        return tile.addon != null ? tile.addon.patternStatement : tile.patternStatement;
    }

    @Override
    public boolean isInverted() {
        return tile.addon != null ? tile.addon.inverted : tile.inverted;
    }

    @Override
    public void setInverted(boolean value) {
        if (tile.addon != null) {
            tile.addon.inverted = value;
        } else {
            tile.inverted = value;
        }
    }

    @Override
    public boolean isLocked() {
        return tile.isLocked();
    }

    @Override
    public void valuesChanged() {
        if (tile.addon != null) {
            tile.addon.updateBuildingInfo();
            if (!player.level().isClientSide) {
                WorldSavedDataVolumeBoxes.get(player.level()).setDirty();
            }
        }
        if (!player.level().isClientSide) {
            tile.onStatementChange();
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, LogicalSide side, IPayloadContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        IContainerFilling.super.readMessage(id, buffer, side);
    }
}
