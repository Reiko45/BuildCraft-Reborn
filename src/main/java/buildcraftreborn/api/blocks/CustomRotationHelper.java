/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.blocks;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Lets wrenches rotate blocks. Blocks can implement {@link ICustomRotationHandler} directly, or a handler can be
 * registered for them (BuildCraft registers handlers for the rotatable vanilla blocks). Register handlers during or
 * after common setup, once the block registry is complete. */
public enum CustomRotationHelper {
    INSTANCE;

    private final Map<Block, List<ICustomRotationHandler>> handlers = new IdentityHashMap<>();

    /** Registers the handler for every registered block that is an instance of the given class. */
    public void registerHandlerForAll(Class<? extends Block> blockClass, ICustomRotationHandler handler) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (blockClass.isInstance(block)) {
                registerHandler(block, handler);
            }
        }
    }

    public void registerHandler(Block block, ICustomRotationHandler handler) {
        handlers.computeIfAbsent(block, b -> new ArrayList<>()).add(handler);
    }

    public InteractionResult attemptRotateBlock(Level world, BlockPos pos, BlockState state, Direction sideWrenched) {
        Block block = state.getBlock();
        if (block instanceof ICustomRotationHandler handler) {
            return handler.attemptRotation(world, pos, state, sideWrenched);
        }
        List<ICustomRotationHandler> forBlock = handlers.get(block);
        if (forBlock == null) {
            return InteractionResult.PASS;
        }
        for (ICustomRotationHandler handler : forBlock) {
            InteractionResult result = handler.attemptRotation(world, pos, state, sideWrenched);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        return InteractionResult.PASS;
    }
}
