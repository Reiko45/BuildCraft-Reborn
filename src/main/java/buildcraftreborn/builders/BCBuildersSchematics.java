/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.entity.BannerPatternLayers;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.schematics.SchematicBlockFactoryRegistry;
import buildcraftreborn.api.schematics.SchematicEntityFactoryRegistry;
import buildcraftreborn.builders.snapshot.SchematicBlockAir;
import buildcraftreborn.builders.snapshot.SchematicBlockDefault;
import buildcraftreborn.builders.snapshot.SchematicBlockFluid;
import buildcraftreborn.builders.snapshot.SchematicEntityDefault;
import buildcraftreborn.lib.misc.RegistryAccessUtil;

/** The block and entity schematics that builders know about (air, fluids, banners, vines and the rule based
 * default). */
public final class BCBuildersSchematics {
    private BCBuildersSchematics() {}

    public static void init() {
        SchematicBlockFactoryRegistry.registerFactory(name("air"), 0, SchematicBlockAir::predicate,
            SchematicBlockAir::new);
        SchematicBlockFactoryRegistry.registerFactory(name("default"), 100, SchematicBlockDefault::predicate,
            SchematicBlockDefault::new);
        SchematicBlockFactoryRegistry.registerFactory(name("fluid"), 200, SchematicBlockFluid::predicate,
            SchematicBlockFluid::new);

        SchematicBlockFactoryRegistry.registerFactory(name("banner"), 300,
            c -> c.block instanceof AbstractBannerBlock, BCBuildersSchematics::getBanner);
        SchematicBlockFactoryRegistry.registerFactory(name("vine"), 300, c -> c.block instanceof VineBlock,
            BCBuildersSchematics::getVine);
        // 1.21: a potted plant is its own block (1.12 kept the plant in the pot's NBT, and used a rule).
        SchematicBlockFactoryRegistry.registerFactory(name("flower_pot"), 300,
            c -> c.block instanceof FlowerPotBlock pot && pot.getPotted() != Blocks.AIR
                && SchematicBlockDefault.predicate(c),
            BCBuildersSchematics::getFlowerPot);

        SchematicEntityFactoryRegistry.registerFactory(name("default"), 100, SchematicEntityDefault::predicate,
            SchematicEntityDefault::new);
    }

    private static String name(String path) {
        return BuildCraftReborn.id(path).toString();
    }

    /** A banner needs a banner item of its colour with its patterns (1.21: the colour is the block's, and the
     * patterns are a component of the item). */
    private static SchematicBlockDefault getBanner() {
        return new SchematicBlockDefault() {
            @Override
            public List<ItemStack> computeRequiredItems() {
                ItemStack stack = new ItemStack(blockState.getBlock().asItem());
                if (tileNbt != null && tileNbt.contains("patterns")) {
                    RegistryOps<net.minecraft.nbt.Tag> ops = RegistryOps.create(NbtOps.INSTANCE,
                        RegistryAccessUtil.current());
                    BannerPatternLayers.CODEC.parse(ops, tileNbt.get("patterns")).result()
                        .ifPresent(layers -> stack.set(DataComponents.BANNER_PATTERNS, layers));
                }
                return Collections.singletonList(stack);
            }
        };
    }

    /** A potted plant needs the pot and the plant. */
    private static SchematicBlockDefault getFlowerPot() {
        return new SchematicBlockDefault() {
            @Override
            public List<ItemStack> computeRequiredItems() {
                if (blockState.getBlock() instanceof FlowerPotBlock pot) {
                    return List.of(new ItemStack(pot.getEmptyPot().asItem()), new ItemStack(pot.getPotted().asItem()));
                }
                return super.computeRequiredItems();
            }
        };
    }

    /** A vine is only placed once there is something for it to hang on. */
    private static SchematicBlockDefault getVine() {
        return new SchematicBlockDefault() {
            @Override
            public boolean isReadyToBuild(Level level, BlockPos blockPos) {
                if (!super.isReadyToBuild(level, blockPos)) {
                    return false;
                }
                if (level.getBlockState(blockPos.above()).getBlock() instanceof VineBlock) {
                    return true;
                }
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    BlockPos next = blockPos.relative(side);
                    if (level.getBlockState(next).isCollisionShapeFullBlock(level, next)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
