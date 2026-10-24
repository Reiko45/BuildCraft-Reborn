/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Lets robots plant and harvest a kind of crop. */
public interface ICropHandler {

    /** Check if an item is a seed.
     *
     * @return true if the item can be planted. */
    boolean isSeed(ItemStack stack);

    /** Check if the item can be planted. You can assume canSustainPlant() will only be called if isSeed() returned
     * true.
     *
     * @return true if the item can be planted at pos. */
    boolean canSustainPlant(Level world, ItemStack seed, BlockPos pos);

    /** Plant the item in the block. You can assume plantCrop() will only be called if canSustainPlant() returned true.
     *
     * @return true if the item was planted at pos. */
    boolean plantCrop(Level world, Player player, ItemStack seed, BlockPos pos);

    /** Check if a crop is mature and can be harvested.
     *
     * @return true if the block at pos is mature and can be harvested. */
    boolean isMature(BlockGetter blockAccess, BlockState state, BlockPos pos);

    /** Harvest the crop. You can assume harvestCrop() will only be called if isMature() returned true.
     *
     * @param drops a list to return the harvest's drops.
     * @return true if the block was successfully harvested. */
    boolean harvestCrop(Level world, BlockPos pos, NonNullList<ItemStack> drops);
}
