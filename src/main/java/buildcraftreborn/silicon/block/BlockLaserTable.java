/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.block;

import java.util.function.BiFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraftreborn.api.enums.EnumLaserTableType;
import buildcraftreborn.api.mj.ILaserTargetBlock;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.silicon.tile.TileAdvancedCraftingTable;
import buildcraftreborn.silicon.tile.TileAssemblyTable;
import buildcraftreborn.silicon.tile.TileIntegrationTable;

/** A laser table: 9 pixels tall, powered by lasers. Each table opens its own GUI (see the tiles). The charging and
 * programming tables come from BuildCraft 7.1's robotics, so the robotics module passes in their tiles (silicon
 * doesn't depend on robotics). */
public class BlockLaserTable extends BlockBCTile_Neptune implements ILaserTargetBlock {
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 9, 16);

    public final EnumLaserTableType type;
    @Nullable
    private final BiFunction<BlockPos, BlockState, ? extends TileBC_Neptune> tileFactory;

    public BlockLaserTable(EnumLaserTableType type, Properties properties) {
        this(type, properties, null);
    }

    /** @param tileFactory Makes the table's tile, for the tables that silicon doesn't know about. */
    public BlockLaserTable(EnumLaserTableType type, Properties properties,
        @Nullable BiFunction<BlockPos, BlockState, ? extends TileBC_Neptune> tileFactory) {
        super(properties);
        this.type = type;
        this.tileFactory = tileFactory;
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        if (tileFactory != null) {
            return tileFactory.apply(pos, state);
        }
        return switch (type) {
            case ASSEMBLY_TABLE -> new TileAssemblyTable(pos, state);
            case ADVANCED_CRAFTING_TABLE -> new TileAdvancedCraftingTable(pos, state);
            case INTEGRATION_TABLE -> new TileIntegrationTable(pos, state);
            default -> throw new IllegalStateException("The " + type + " needs a tile factory");
        };
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
