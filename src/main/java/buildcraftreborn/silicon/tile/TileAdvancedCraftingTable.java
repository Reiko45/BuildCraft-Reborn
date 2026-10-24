/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.tile;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.craft.IAutoCraft;
import buildcraftreborn.lib.tile.craft.WorkbenchCrafting;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.container.ContainerAdvancedCraftingTable;

/** Crafts the recipe in its (phantom) blueprint grid out of its materials, using 500 MJ of laser power per craft. */
public class TileAdvancedCraftingTable extends TileLaserTableBase implements IAutoCraft, MenuProvider {
    private static final long POWER_REQ = 500 * MjAPI.MJ;

    public final ItemHandlerSimple invBlueprint;
    public final ItemHandlerSimple invMaterials;
    public final ItemHandlerSimple invResults;
    private final WorkbenchCrafting crafting;

    public ItemStack resultClient = ItemStack.EMPTY;

    public TileAdvancedCraftingTable(BlockPos pos, BlockState state) {
        super(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE_TILE.get(), pos, state);
        invBlueprint = itemManager.addInvHandler("blueprint", 3 * 3, EnumAccess.PHANTOM);
        invMaterials = itemManager.addInvHandler("materials", 5 * 3, EnumAccess.INSERT, EnumPipePart.VALUES);
        invResults = itemManager.addInvHandler("result", 3 * 3, EnumAccess.EXTRACT, EnumPipePart.VALUES);
        crafting = new WorkbenchCrafting(3, 3, this, invBlueprint, invMaterials, invResults);
    }

    @Override
    protected void onSlotChange(IItemHandlerModifiable handler, int slot, ItemStack before, ItemStack after) {
        super.onSlotChange(handler, slot, before, after);
        if (!ItemStack.matches(before, after)) {
            crafting.onInventoryChange(handler);
        }
    }

    @Override
    public long getTarget() {
        if (level == null) {
            return 0;
        }
        return level.isClientSide ? POWER_REQ : crafting.canCraft() ? POWER_REQ : 0;
    }

    @Override
    public void update() {
        super.update();
        if (level.isClientSide) {
            return;
        }
        boolean didChange = crafting.tick();
        if (crafting.canCraft()) {
            if (power >= POWER_REQ) {
                if (crafting.craft()) {
                    // This is used for #hasWork(), to ensure that it doesn't return
                    // false for the one tick in between crafts.
                    power -= POWER_REQ;
                }
            }
        }
        if (didChange) {
            sendNetworkGuiUpdate(NET_GUI_DATA);
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_GUI_DATA) {
                resultClient = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_GUI_DATA) {
                ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, crafting.getAssumedResult());
            }
        }
    }

    public WorkbenchCrafting getWorkbenchCrafting() {
        return crafting;
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
        return new ContainerAdvancedCraftingTable(windowId, player, this);
    }

    // IAutoCraft

    @Override
    public ItemStack getCurrentRecipeOutput() {
        return crafting.getAssumedResult();
    }

    @Override
    public ItemHandlerSimple getInvBlueprint() {
        return invBlueprint;
    }
}
