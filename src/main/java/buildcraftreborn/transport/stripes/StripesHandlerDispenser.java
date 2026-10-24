/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.stripes;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.core.dispenser.DispenseItemBehavior;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.transport.IStripesActivator;
import buildcraftreborn.api.transport.IStripesHandlerItem;

/** Uses an item's dispenser behaviour (buckets, minecarts, fire charges, spawn eggs...) as if a dispenser facing out
 * of the pipe had dispensed it. */
public enum StripesHandlerDispenser implements IStripesHandlerItem {
    INSTANCE;

    /** Unused by BuildCraft itself (as in 1.12), kept for addons. */
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final List<Class<? extends Item>> ITEM_CLASSES = new ArrayList<>();

    @Override
    public boolean handle(Level level, BlockPos pos, Direction direction, ItemStack stack, Player player,
        IStripesActivator activator) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return false;
        }
        if (!DispenserBlock.DISPENSER_REGISTRY.containsKey(stack.getItem())) {
            return false;
        }
        DispenseItemBehavior behaviour = DispenserBlock.DISPENSER_REGISTRY.get(stack.getItem());
        BlockState state = Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, direction);
        // 1.21: some behaviours put extra items (an emptied bucket, say) into the dispenser's inventory. A detached
        // dispenser takes those, and they're sent back down the pipe.
        DispenserBlockEntity fakeDispenser = new DispenserBlockEntity(pos, state);
        BlockSource source = new BlockSource(serverLevel, pos, state, fakeDispenser);
        ItemStack output = behaviour.dispense(source, stack.copy());
        player.getInventory().setItem(player.getInventory().selected, output);
        for (int i = 0; i < fakeDispenser.getContainerSize(); i++) {
            ItemStack extra = fakeDispenser.removeItemNoUpdate(i);
            if (!extra.isEmpty()) {
                activator.sendItem(extra, direction);
            }
        }
        return true;
    }
}
