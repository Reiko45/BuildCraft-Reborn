/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.crops;

import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BambooSaplingBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.TallGrassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.common.SpecialPlantable;

import buildcraftreborn.api.crops.ICropHandler;

/** The default crop handler: seeds, saplings, flowers, mushrooms, sugar cane and the like.
 * <p>
 * 1.21: Forge's {@code IPlantable} is gone, so a seed is a block item for a plant (a {@link BushBlock}, sugar cane,
 * cactus or bamboo) or a NeoForge {@link SpecialPlantable}, and whether it can be planted is the plant's own
 * survival check. */
public class CropHandlerPlantable implements ICropHandler {

    private static final Set<Block> FORBIDDEN_BLOCKS = new HashSet<>();

    public static void forbidBlock(Block b) {
        FORBIDDEN_BLOCKS.add(b);
    }

    @Nullable
    private static Block getPlant(ItemStack stack) {
        if (stack.getItem() instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (isPlant(block) && !FORBIDDEN_BLOCKS.contains(block)) {
                return block;
            }
        }
        return null;
    }

    private static boolean isPlant(Block block) {
        return block instanceof BushBlock || block instanceof SugarCaneBlock || block instanceof CactusBlock
            || block instanceof BambooSaplingBlock;
    }

    @Override
    public boolean isSeed(ItemStack stack) {
        return stack.getItem() instanceof SpecialPlantable || getPlant(stack) != null;
    }

    @Override
    public boolean canSustainPlant(Level world, ItemStack seed, BlockPos pos) {
        BlockPos above = pos.above();
        if (!world.isEmptyBlock(above)) {
            return false;
        }
        if (seed.getItem() instanceof SpecialPlantable plantable) {
            return plantable.canPlacePlantAtPosition(seed, world, above, Direction.DOWN);
        }
        Block plant = getPlant(seed);
        if (plant == null || world.getBlockState(pos).is(plant)) {
            return false;
        }
        return plant.defaultBlockState().canSurvive(world, above);
    }

    @Override
    public boolean plantCrop(Level world, Player player, ItemStack seed, BlockPos pos) {
        if (seed.getItem() instanceof SpecialPlantable plantable) {
            plantable.spawnPlantAtPosition(seed, world, pos.above(), Direction.DOWN);
            seed.shrink(1);
            return true;
        }
        player.setItemInHand(InteractionHand.MAIN_HAND, seed);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos).add(0, 0.5, 0), Direction.UP, pos, false);
        InteractionResult result = seed.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        return result.consumesAction();
    }

    @Override
    public boolean isMature(BlockGetter blockAccess, BlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (FORBIDDEN_BLOCKS.contains(block)) {
            return false;
        } else if (block instanceof PitcherCropBlock) {
            return state.getValue(PitcherCropBlock.AGE) == PitcherCropBlock.MAX_AGE;
        } else if (block instanceof TallGrassBlock || block == Blocks.MELON || block instanceof MushroomBlock
            || block instanceof DoublePlantBlock || block == Blocks.PUMPKIN) {
            return true;
        } else if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        } else if (block instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) == NetherWartBlock.MAX_AGE;
        } else if (block instanceof SugarCaneBlock || block instanceof CactusBlock
            || block instanceof BambooStalkBlock) {
            // The top of a column (the bottom block stays to grow back)
            return blockAccess.getBlockState(pos.below()).is(block);
        }
        return false;
    }

    @Override
    public boolean harvestCrop(Level world, BlockPos pos, NonNullList<ItemStack> drops) {
        if (world instanceof ServerLevel server) {
            BlockState state = world.getBlockState(pos);
            drops.addAll(Block.getDrops(state, server, pos, world.getBlockEntity(pos)));
            world.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, pos, Block.getId(state));
            world.removeBlock(pos, false);
            return true;
        }
        return false;
    }
}
