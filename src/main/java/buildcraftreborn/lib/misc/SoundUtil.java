/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public class SoundUtil {
    public static void playBlockPlace(Level world, BlockPos pos) {
        playBlockPlace(world, pos, world.getBlockState(pos));
    }

    public static void playBlockPlace(Level world, BlockPos pos, BlockState state) {
        SoundType soundType = state.getSoundType(world, pos, null);
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundType.getPlaceSound(), SoundSource.BLOCKS, volume, pitch);
    }

    public static void playBlockBreak(Level world, BlockPos pos) {
        playBlockBreak(world, pos, world.getBlockState(pos));
    }

    public static void playBlockBreak(Level world, BlockPos pos, BlockState state) {
        SoundType soundType = state.getSoundType(world, pos, null);
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundType.getBreakSound(), SoundSource.BLOCKS, volume, pitch);
    }

    public static void playLeverSwitch(Level world, BlockPos pos, boolean isNowOn) {
        float pitch = isNowOn ? 0.6f : 0.5f;
        world.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.2f, pitch);
    }

    public static void playChangeColour(Level world, BlockPos pos, @Nullable DyeColor colour) {
        SoundType soundType = SoundType.SLIME_BLOCK;
        final SoundEvent soundEvent = colour == null ? SoundEvents.BUCKET_EMPTY : SoundEvents.SLIME_SQUISH;
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, soundEvent, SoundSource.BLOCKS, volume, pitch);
    }

    public static void playSlideSound(Level world, BlockPos pos) {
        playSlideSound(world, pos, world.getBlockState(pos));
    }

    public static void playSlideSound(Level world, BlockPos pos, InteractionResult result) {
        playSlideSound(world, pos, world.getBlockState(pos), result);
    }

    public static void playSlideSound(Level world, BlockPos pos, BlockState state) {
        playSlideSound(world, pos, state, InteractionResult.SUCCESS);
    }

    /** Piston contract for a successful rotation, piston extend for a failed one, nothing for a pass. */
    public static void playSlideSound(Level world, BlockPos pos, BlockState state, InteractionResult result) {
        if (result == InteractionResult.PASS) return;
        SoundType soundType = state.getSoundType(world, pos, null);
        SoundEvent event = result.consumesAction() ? SoundEvents.PISTON_CONTRACT : SoundEvents.PISTON_EXTEND;
        float volume = (soundType.getVolume() + 1.0F) / 2.0F;
        float pitch = soundType.getPitch() * 0.8F;
        world.playSound(null, pos, event, SoundSource.BLOCKS, volume, pitch);
    }
}
