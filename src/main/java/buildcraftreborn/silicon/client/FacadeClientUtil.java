/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.client.model.data.ModelData;

/** The client-only parts of facades, kept out of the common facade classes. */
public final class FacadeClientUtil {
    private FacadeClientUtil() {}

    /** @return True if the block draws in the translucent layer (stained glass, ice...). */
    public static boolean isTranslucent(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state)
            .getRenderTypes(state, RandomSource.create(42), ModelData.EMPTY).contains(RenderType.translucent());
    }

    public static int getBlockColour(BlockState state, BlockAndTintGetter level, BlockPos pos, int tintIndex) {
        try {
            return Minecraft.getInstance().getBlockColors().getColor(state, level, pos, tintIndex);
        } catch (RuntimeException e) {
            // (some blocks don't like being asked about a position that isn't theirs)
            return -1;
        }
    }
}
