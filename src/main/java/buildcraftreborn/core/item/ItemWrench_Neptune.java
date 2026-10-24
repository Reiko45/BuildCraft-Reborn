/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.blocks.CustomRotationHelper;
import buildcraftreborn.api.tools.IToolWrench;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.SoundUtil;

/** The wrench: rotates BuildCraft blocks and the rotatable vanilla blocks. Machines with a GUI open it on a normal
 * click, so sneak to rotate those (as in 1.12). */
public class ItemWrench_Neptune extends Item implements IToolWrench {
    private static final ResourceLocation ADVANCEMENT = BuildCraftReborn.id("wrenched");

    public ItemWrench_Neptune(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean canWrench(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace) {
        return true;
    }

    @Override
    public void wrenchUsed(Player player, InteractionHand hand, ItemStack wrench, HitResult rayTrace) {
        AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        player.swing(hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        // Like 1.12 this runs on both sides, so the arm swings and the client predicts the rotation.
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        InteractionResult result = CustomRotationHelper.INSTANCE.attemptRotateBlock(world, pos, state,
            context.getClickedFace());
        Player player = context.getPlayer();
        if (result.consumesAction() && player != null) {
            BlockHitResult hit = new BlockHitResult(context.getClickLocation(), context.getClickedFace(), pos,
                context.isInside());
            wrenchUsed(player, context.getHand(), context.getItemInHand(), hit);
        }
        if (!world.isClientSide) {
            SoundUtil.playSlideSound(world, pos, state, result);
        }
        return result;
    }
}
