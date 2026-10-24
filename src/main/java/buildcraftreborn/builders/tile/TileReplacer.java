/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.tile;

import java.util.Date;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.enums.EnumSnapshotType;
import buildcraftreborn.api.schematics.ISchematicBlock;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.container.ContainerReplacer;
import buildcraftreborn.builders.item.ItemSchematicSingle;
import buildcraftreborn.builders.item.ItemSnapshot;
import buildcraftreborn.builders.snapshot.Blueprint;
import buildcraftreborn.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** The replacer: with a used blueprint and two single schematics, swaps every block like the first one in the
 * blueprint for the second one.
 * <p>
 * 1.21: the blueprint that comes out points at the changed copy (1.12 wrote the old one's key into it, so nothing
 * seemed to change). */
public class TileReplacer extends TileBC_Neptune implements ITickable, MenuProvider {
    public final ItemHandlerSimple invSnapshot = itemManager.addInvHandler("snapshot", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSnapshot item && item.snapshotType == EnumSnapshotType.BLUEPRINT
            && ItemSnapshot.isUsed(stack),
        EnumAccess.NONE);
    public final ItemHandlerSimple invSchematicFrom = itemManager.addInvHandler("schematicFrom", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSchematicSingle && ItemSchematicSingle.isUsed(stack),
        EnumAccess.NONE);
    public final ItemHandlerSimple invSchematicTo = itemManager.addInvHandler("schematicTo", 1,
        (slot, stack) -> stack.getItem() instanceof ItemSchematicSingle && ItemSchematicSingle.isUsed(stack),
        EnumAccess.NONE);

    public TileReplacer(BlockPos pos, BlockState state) {
        super(BCBuildersBlocks.REPLACER_TILE.get(), pos, state);
    }

    @Override
    public void update() {
        if (level.isClientSide) {
            return;
        }
        if (invSnapshot.getStackInSlot(0).isEmpty() || invSchematicFrom.getStackInSlot(0).isEmpty()
            || invSchematicTo.getStackInSlot(0).isEmpty()) {
            return;
        }
        Snapshot.Header header = ItemSnapshot.getHeader(invSnapshot.getStackInSlot(0));
        if (header == null) {
            return;
        }
        if (GlobalSavedDataSnapshots.get(level).getSnapshot(header.key) instanceof Blueprint blueprint) {
            try {
                ISchematicBlock from = ItemSchematicSingle.getSchematic(invSchematicFrom.getStackInSlot(0));
                ISchematicBlock to = ItemSchematicSingle.getSchematic(invSchematicTo.getStackInSlot(0));
                if (from == null || to == null) {
                    return;
                }
                Blueprint newBlueprint = blueprint.copy();
                newBlueprint.replace(from, to);
                newBlueprint.computeKey();
                GlobalSavedDataSnapshots.get(level).addSnapshot(newBlueprint);
                invSnapshot.setStackInSlot(0, ItemSnapshot.getUsed(EnumSnapshotType.BLUEPRINT,
                    new Snapshot.Header(newBlueprint.key, getOwner().getId(), new Date(), header.name)));
                invSchematicFrom.setStackInSlot(0, ItemStack.EMPTY);
                invSchematicTo.setStackInSlot(0, ItemStack.EMPTY);
            } catch (InvalidInputDataException e) {
                BuildCraftReborn.LOGGER.warn("[builders.replacer] Invalid schematic", e);
            }
        }
    }

    // GUI

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (!level.isClientSide) {
            openGui(player);
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerReplacer(windowId, player, this);
    }
}
