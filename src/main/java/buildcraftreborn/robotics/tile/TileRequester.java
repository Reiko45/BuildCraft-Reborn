/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.tile;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.robots.IRequestProvider;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.container.ContainerRequester;

/** The requester: 20 request templates, and 20 slots that fill up with what was asked for. Delivery robots read the
 * requests through a docking station next to it (with the "request items from machine" action), and pipes can put
 * matching items in or take them out. */
public class TileRequester extends TileBC_Neptune implements IRequestProvider, MenuProvider, IDebuggable {
    public static final int NB_ITEMS = 20;

    /** The templates (whole stacks: the count is how many are wanted). */
    public final ItemHandlerSimple requests = itemManager.addInvHandler("requests", NB_ITEMS, EnumAccess.PHANTOM);
    public final ItemHandlerSimple inv = itemManager.addInvHandler("items", NB_ITEMS, this::canStore,
        EnumAccess.BOTH, EnumPipePart.VALUES);

    public TileRequester(BlockPos pos, BlockState state) {
        super(BCRoboticsBlocks.REQUESTER_TILE.get(), pos, state);
    }

    private boolean canStore(int slot, ItemStack stack) {
        ItemStack request = requests.getStackInSlot(slot);
        return !request.isEmpty() && StackUtil.isMatchingItemOrList(request, stack);
    }

    public ItemStack getRequestTemplate(int index) {
        return requests.getStackInSlot(index);
    }

    public boolean isFulfilled(int i) {
        ItemStack request = requests.getStackInSlot(i);
        if (request.isEmpty()) {
            return true;
        }
        ItemStack stored = inv.getStackInSlot(i);
        return !stored.isEmpty() && StackUtil.isMatchingItemOrList(request, stored)
            && stored.getCount() >= request.getCount();
    }

    @Override
    public int getRequestsCount() {
        return NB_ITEMS;
    }

    @Override
    public ItemStack getRequest(int i) {
        ItemStack request = requests.getStackInSlot(i);
        if (request.isEmpty() || isFulfilled(i)) {
            return ItemStack.EMPTY;
        }
        ItemStack stored = inv.getStackInSlot(i);
        if (stored.isEmpty()) {
            return request.copy();
        }
        if (!StackUtil.isMatchingItemOrList(request, stored)) {
            return ItemStack.EMPTY;
        }
        int wanted = request.getCount() - stored.getCount();
        return wanted <= 0 ? ItemStack.EMPTY : request.copyWithCount(wanted);
    }

    @Override
    public ItemStack offerItem(int i, ItemStack stack) {
        ItemStack request = requests.getStackInSlot(i);
        if (request.isEmpty() || stack.isEmpty() || !StackUtil.isMatchingItemOrList(request, stack)) {
            return stack;
        }
        ItemStack stored = inv.getStackInSlot(i);
        int max = Math.min(request.getCount(), stack.getMaxStackSize());
        if (stored.isEmpty()) {
            int moved = Math.min(max, stack.getCount());
            inv.setStackInSlot(i, stack.copyWithCount(moved));
            return stack.copyWithCount(stack.getCount() - moved);
        }
        if (!ItemStack.isSameItemSameComponents(stored, stack)) {
            return stack;
        }
        int moved = Math.max(0, Math.min(max - stored.getCount(), stack.getCount()));
        if (moved > 0) {
            inv.setStackInSlot(i, stored.copyWithCount(stored.getCount() + moved));
        }
        return stack.copyWithCount(stack.getCount() - moved);
    }

    /** As in 7.1: only the slots with a request count, each by how full it is. */
    public int getComparatorLevel() {
        int count = 0;
        int nonEmpty = 0;
        float fullness = 0;
        for (int i = 0; i < NB_ITEMS; i++) {
            if (requests.getStackInSlot(i).isEmpty()) {
                continue;
            }
            count++;
            ItemStack stored = inv.getStackInSlot(i);
            if (!stored.isEmpty()) {
                nonEmpty++;
                fullness += (float) stored.getCount() / Math.min(inv.getSlotLimit(i), stored.getMaxStackSize());
            }
        }
        if (count == 0) {
            return 0;
        }
        return Mth.floor(fullness / count * 14.0F) + (nonEmpty > 0 ? 1 : 0);
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
        return new ContainerRequester(windowId, player, this);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        int requested = 0;
        int fulfilled = 0;
        for (int i = 0; i < NB_ITEMS; i++) {
            if (!requests.getStackInSlot(i).isEmpty()) {
                requested++;
                if (isFulfilled(i)) {
                    fulfilled++;
                }
            }
        }
        left.add("requests - " + fulfilled + " / " + requested + " fulfilled");
    }
}
