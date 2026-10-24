/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.enums.EnumSpring;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;

/** A bedrock-like block that keeps making a fluid block above itself. 1.12 had one block with a {@code type}
 * property; here each {@link EnumSpring} is its own block. The oil spring (with its tile) is
 * {@code buildcraftreborn.energy.block.BlockSpringOil}. */
public class BlockSpring extends BlockBCBase_Neptune {
    public final EnumSpring type;

    public BlockSpring(EnumSpring type, BlockBehaviour.Properties properties) {
        super(properties);
        this.type = type;
    }

    /** Bedrock-like: unbreakable, blast proof, no drops, stone sound and random ticks. */
    public static BlockBehaviour.Properties springProperties() {
        return BlockBehaviour.Properties.ofFullCopy(Blocks.BEDROCK).strength(-1.0F, 6000000.0F).randomTicks();
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        generateSpringBlock(level, pos, random);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // 1.12's random ticks called updateTick too.
        generateSpringBlock(level, pos, random);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        level.scheduleTick(pos, this, type.tickRate);
    }

    private void generateSpringBlock(Level level, BlockPos pos, RandomSource random) {
        level.scheduleTick(pos, this, type.tickRate);
        if (!type.canGen || type.liquidBlock == null) {
            return;
        }
        if (!level.isEmptyBlock(pos.above())) {
            return;
        }
        if (type.chance != -1 && random.nextInt(type.chance) != 0) {
            return;
        }
        level.setBlockAndUpdate(pos.above(), type.liquidBlock);
    }
}
