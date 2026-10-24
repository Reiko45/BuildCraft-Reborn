/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.block;

import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import net.neoforged.neoforge.capabilities.Capabilities;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.properties.BuildCraftProperties;
import buildcraftreborn.builders.tile.TileQuarry;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.block.IBlockWithFacing;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** The quarry. Its {@code connected_*} properties show an item hatch on each side that has an inventory next to it.
 * As in 1.12 the horizontal ones are relative to the quarry's facing (north is the front), so the model can simply
 * be rotated. */
public class BlockQuarry extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final Map<Direction, BooleanProperty> CONNECTED_MAP = BuildCraftProperties.CONNECTED_MAP;
    private static final ResourceLocation ADVANCEMENT = BuildCraftReborn.id("shaping_the_world");

    public BlockQuarry(Properties properties) {
        super(properties);
        BlockState state = defaultBlockState();
        for (BooleanProperty prop : CONNECTED_MAP.values()) {
            state = state.setValue(prop, false);
        }
        registerDefaultState(state);
    }

    @Override
    protected void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
        super.addProperties(builder);
        CONNECTED_MAP.values().forEach(builder::add);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileQuarry(pos, state);
    }

    /** @param side The side, relative to the quarry's facing (north is the front). */
    private boolean isConnected(Level level, BlockPos pos, BlockState state, Direction side) {
        Direction facing = side;
        if (side.getAxis().isHorizontal()) {
            facing = Direction.from2DDataValue(side.get2DDataValue() + 2 + state.getValue(getFacingProperty())
                .get2DDataValue());
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, pos.relative(facing), facing.getOpposite()) != null;
    }

    public BlockState withConnections(BlockState state, Level level, BlockPos pos) {
        for (Direction side : Direction.values()) {
            state = state.setValue(CONNECTED_MAP.get(side), isConnected(level, pos, state, side));
        }
        return state;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : withConnections(state, context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor world,
        BlockPos pos, BlockPos neighbourPos) {
        if (world instanceof Level level) {
            state = withConnections(state, level, pos);
        }
        return super.updateShape(state, direction, neighbour, world, pos, neighbourPos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
        boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        // A block entity appearing or disappearing next to the quarry doesn't always change the neighbour's shape.
        BlockState updated = withConnections(state, level, pos);
        if (updated != state) {
            level.setBlock(pos, updated, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean canBeRotated(Level world, BlockPos pos, BlockState state) {
        return false;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
        ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (placer instanceof Player player) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT);
        }
    }
}
