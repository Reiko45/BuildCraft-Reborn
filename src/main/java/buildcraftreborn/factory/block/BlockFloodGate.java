/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.block;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

import buildcraftreborn.api.properties.BuildCraftProperties;
import buildcraftreborn.api.tools.IToolWrench;
import buildcraftreborn.factory.tile.TileFloodGate;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** The flood gate. Right-click a side (other than the top) with a wrench to close or open it. */
public class BlockFloodGate extends BlockBCTile_Neptune {
    public static final Map<Direction, BooleanProperty> CONNECTED_MAP;

    static {
        Map<Direction, BooleanProperty> map = new EnumMap<>(BuildCraftProperties.CONNECTED_MAP);
        map.remove(Direction.UP);
        CONNECTED_MAP = map;
    }

    public BlockFloodGate(Properties properties) {
        super(properties);
        BlockState state = defaultBlockState();
        for (BooleanProperty prop : CONNECTED_MAP.values()) {
            state = state.setValue(prop, true);
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
        return new TileFloodGate(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos,
        Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof IToolWrench) {
            Direction side = hit.getDirection();
            BooleanProperty prop = CONNECTED_MAP.get(side);
            if (prop == null) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            if (!world.isClientSide) {
                world.setBlockAndUpdate(pos, state.cycle(prop));
                if (world.getBlockEntity(pos) instanceof TileFloodGate floodGate) {
                    floodGate.onSidesChanged();
                }
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }
}
