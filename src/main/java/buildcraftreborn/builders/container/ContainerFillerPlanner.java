/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.container;

import java.io.IOException;
import java.util.UUID;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.filler.IFillerPattern;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.addon.AddonFillerPlanner;
import buildcraftreborn.builders.filler.FillerType;
import buildcraftreborn.core.marker.volume.Addon;
import buildcraftreborn.core.marker.volume.ClientVolumeBoxes;
import buildcraftreborn.core.marker.volume.EnumAddonSlot;
import buildcraftreborn.core.marker.volume.VolumeBox;
import buildcraftreborn.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraftreborn.lib.gui.ContainerBC_Neptune;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.statement.FullStatement;

/** The filler planner addon's pattern GUI. It has no slots. */
public class ContainerFillerPlanner extends ContainerBC_Neptune implements IContainerFilling {
    public final AddonFillerPlanner addon;
    private final FullStatement<IFillerPattern> patternStatementClient =
        new FullStatement<>(FillerType.INSTANCE, 4, (statement, paramIndex) -> onStatementChange());

    public ContainerFillerPlanner(int windowId, Player player, AddonFillerPlanner addon) {
        super(BCBuildersBlocks.FILLER_PLANNER_MENU.get(), windowId, player);
        this.addon = addon;
        init();
    }

    /** Written when the server opens the menu, so the client knows which addon it's for. */
    public static void writeAddon(FriendlyByteBuf buf, AddonFillerPlanner addon) {
        buf.writeUUID(addon.volumeBox.id);
        buf.writeEnum(addon.getSlot());
    }

    public static ContainerFillerPlanner fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        UUID id = buf.readUUID();
        EnumAddonSlot slot = buf.readEnum(EnumAddonSlot.class);
        for (VolumeBox volumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
            if (volumeBox.id.equals(id) && volumeBox.addons.get(slot) instanceof AddonFillerPlanner addon) {
                return new ContainerFillerPlanner(windowId, inventory.player, addon);
            }
        }
        BuildCraftReborn.LOGGER.warn("[builders.filler_planner] Couldn't find the addon {} / {} to open!", id, slot);
        // A detached addon: stillValid is false for it, so the menu closes straight away.
        AddonFillerPlanner detached = new AddonFillerPlanner();
        detached.volumeBox = new VolumeBox(inventory.player.level(), inventory.player.blockPosition());
        return new ContainerFillerPlanner(windowId, inventory.player, detached);
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
        return addon.patternStatement;
    }

    @Override
    public boolean isInverted() {
        return addon.inverted;
    }

    @Override
    public void setInverted(boolean value) {
        addon.inverted = value;
    }

    @Override
    public void valuesChanged() {
        addon.updateBuildingInfo();
        if (!player.level().isClientSide) {
            WorldSavedDataVolumeBoxes.get(player.level()).setDirty();
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, LogicalSide side, IPayloadContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        IContainerFilling.super.readMessage(id, buffer, side);
    }

    @Override
    public boolean stillValid(Player player) {
        Addon current = addon.volumeBox.addons.get(slotOf(addon));
        return current == addon;
    }

    private static EnumAddonSlot slotOf(AddonFillerPlanner addon) {
        for (var entry : addon.volumeBox.addons.entrySet()) {
            if (entry.getValue() == addon) {
                return entry.getKey();
            }
        }
        return EnumAddonSlot.VALUES[0];
    }
}
