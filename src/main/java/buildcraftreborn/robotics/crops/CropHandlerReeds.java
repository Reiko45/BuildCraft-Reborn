/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.crops;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.crops.CropManager;
import buildcraftreborn.api.crops.ICropHandler;

/** Sugar cane (1.12: reeds), which can't be planted on top of itself. */
public class CropHandlerReeds implements ICropHandler {

    @Override
    public boolean isSeed(ItemStack stack) {
        return stack.is(Items.SUGAR_CANE);
    }

    @Override
    public boolean canSustainPlant(Level world, ItemStack seed, BlockPos pos) {
        return !world.getBlockState(pos).is(Blocks.SUGAR_CANE) && world.isEmptyBlock(pos.above())
            && Blocks.SUGAR_CANE.defaultBlockState().canSurvive(world, pos.above());
    }

    @Override
    public boolean plantCrop(Level world, Player player, ItemStack seed, BlockPos pos) {
        return CropManager.getDefaultHandler().plantCrop(world, player, seed, pos);
    }

    @Override
    public boolean isMature(BlockGetter blockAccess, BlockState state, BlockPos pos) {
        return false;
    }

    @Override
    public boolean harvestCrop(Level world, BlockPos pos, NonNullList<ItemStack> drops) {
        return false;
    }
}
