/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.properties;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.common.Tags;

/** An ore that a pickaxe of the given harvest level can mine.
 * <p>
 * 1.21: an ore is in the {@code c:ores} block tag (1.12: an "ore..." ore dictionary name), and harvest levels 0 to 4
 * are wood, stone, iron, diamond and netherite pickaxes (1.12 stopped at diamond). */
public class WorldPropertyIsOre extends WorldProperty {
    public static final int MAX_HARVEST_LEVEL = 4;

    private static final Tiers[] TIERS = { Tiers.WOOD, Tiers.STONE, Tiers.IRON, Tiers.DIAMOND, Tiers.NETHERITE };

    @Nullable
    private final TagKey<Block> incorrect;

    /** @param harvestLevel The pickaxe's harvest level, or -1 for any ore a pickaxe mines. */
    public WorldPropertyIsOre(int harvestLevel) {
        this.incorrect = harvestLevel < 0 ? null
            : TIERS[Math.min(MAX_HARVEST_LEVEL, harvestLevel)].getIncorrectBlocksForDrops();
    }

    @Override
    protected boolean get(BlockGetter blockAccess, BlockState state, BlockPos pos) {
        return state.is(Tags.Blocks.ORES) && state.is(BlockTags.MINEABLE_WITH_PICKAXE)
            && (incorrect == null || !state.is(incorrect));
    }
}
