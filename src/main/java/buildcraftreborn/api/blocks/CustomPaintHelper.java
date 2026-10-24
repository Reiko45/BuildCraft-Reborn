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

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Provides a simple way to paint a single block, iterating through all {@link ICustomPaintHandler}'s that are
 * registered for the block.
 * <p>
 * 1.21: blocks have no {@code recolorBlock} any more, so the fallback swaps the colour in the block's id (red_wool to
 * blue_wool, for example) when a block of the new colour exists, keeping the block state's properties. */
public enum CustomPaintHelper {
    INSTANCE;

    private final Map<Block, List<ICustomPaintHandler>> handlers = new IdentityHashMap<>();
    private final List<ICustomPaintHandler> allHandlers = new ArrayList<>();

    /** Registers a handler that will be called LAST for ALL blocks, if all other paint handlers have returned PASS or
     * none are registered for that block. */
    public void registerHandlerForAll(ICustomPaintHandler handler) {
        allHandlers.add(handler);
    }

    /** Registers a paint handler for every block of the given class. */
    public void registerHandlerForAll(Class<? extends Block> blockClass, ICustomPaintHandler handler) {
        for (Block block : BuiltInRegistries.BLOCK) {
            if (blockClass.isAssignableFrom(block.getClass())) {
                registerHandler(block, handler);
            }
        }
    }

    public void registerHandler(Block block, ICustomPaintHandler handler) {
        handlers.computeIfAbsent(block, b -> new ArrayList<>()).add(handler);
    }

    /** Attempts to paint a block at the given position. Basically iterates through all registered paint handlers. */
    public InteractionResult attemptPaintBlock(Level level, BlockPos pos, BlockState state, Vec3 hitPos,
        @Nullable Direction hitSide, @Nullable DyeColor paint) {
        Block block = state.getBlock();
        if (block instanceof ICustomPaintHandler custom) {
            return custom.attemptPaint(level, pos, state, hitPos, hitSide, paint);
        }
        List<ICustomPaintHandler> custom = handlers.get(block);
        if (custom != null) {
            for (ICustomPaintHandler handler : custom) {
                InteractionResult result = handler.attemptPaint(level, pos, state, hitPos, hitSide, paint);
                if (result != InteractionResult.PASS) {
                    return result;
                }
            }
        }
        return defaultAttemptPaint(level, pos, state, hitPos, hitSide, paint);
    }

    private InteractionResult defaultAttemptPaint(Level level, BlockPos pos, BlockState state, Vec3 hitPos,
        @Nullable Direction hitSide, @Nullable DyeColor paint) {
        for (ICustomPaintHandler handler : allHandlers) {
            InteractionResult result = handler.attemptPaint(level, pos, state, hitPos, hitSide, paint);
            if (result != InteractionResult.PASS) {
                return result;
            }
        }
        if (paint == null) {
            return InteractionResult.FAIL;
        }
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        String path = id.getPath();
        for (DyeColor colour : DyeColor.values()) {
            String prefix = colour.getName() + "_";
            if (path.startsWith(prefix)) {
                if (colour == paint) {
                    return InteractionResult.FAIL;
                }
                ResourceLocation newId = id.withPath(paint.getName() + "_" + path.substring(prefix.length()));
                Block newBlock = BuiltInRegistries.BLOCK.getOptional(newId).orElse(null);
                if (newBlock == null) {
                    continue;
                }
                if (!level.isClientSide) {
                    level.setBlockAndUpdate(pos, newBlock.withPropertiesOf(state));
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }
}
