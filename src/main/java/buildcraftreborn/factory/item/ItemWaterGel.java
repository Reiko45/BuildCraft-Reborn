/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.item;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.block.BlockWaterGel;

/** Water gel. Right-click on water (up to 7 blocks away) to start turning it into gel. */
public class ItemWaterGel extends Item {

    public ItemWaterGel(Properties properties) {
        super(properties.stacksTo(16));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(7));
        BlockHitResult ray = world.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE,
            ClipContext.Fluid.ANY, player));

        if (ray.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.fail(stack);
        }

        if (world.getBlockState(ray.getBlockPos()).getBlock() != Blocks.WATER) {
            return InteractionResultHolder.fail(stack);
        }

        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        // Same as the snowball
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
            SoundSource.NEUTRAL, 0.5F, 0.4F / (world.getRandom().nextFloat() * 0.4F + 0.8F));

        if (!world.isClientSide) {
            world.setBlockAndUpdate(ray.getBlockPos(), BCFactoryBlocks.WATER_GEL.get().defaultBlockState()
                .setValue(BlockWaterGel.PROP_STAGE, BlockWaterGel.GelStage.SPREAD_0));
            world.scheduleTick(ray.getBlockPos(), BCFactoryBlocks.WATER_GEL.get(), 200);
        }

        return InteractionResultHolder.sidedSuccess(stack, world.isClientSide);
    }
}
