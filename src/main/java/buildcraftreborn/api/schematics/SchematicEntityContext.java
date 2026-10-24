/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.schematics;


import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class SchematicEntityContext {
    
    public final Level level;
    
    public final BlockPos basePos;
    
    public final Entity entity;

    public SchematicEntityContext(Level level,
                                  BlockPos basePos,
                                  Entity entity) {
        this.level = level;
        this.basePos = basePos;
        this.entity = entity;
    }
}
