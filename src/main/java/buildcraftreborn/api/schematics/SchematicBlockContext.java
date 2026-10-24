/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.schematics;


import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SchematicBlockContext {
    
    public final Level level;
    
    public final BlockPos basePos;
    
    public final BlockPos pos;
    
    public final BlockState blockState;
    
    public final Block block;

    public SchematicBlockContext(Level level,
                                 BlockPos basePos,
                                 BlockPos pos,
                                 BlockState blockState,
                                 Block block) {
        this.level = level;
        this.basePos = basePos;
        this.pos = pos;
        this.blockState = blockState;
        this.block = block;
    }
}
