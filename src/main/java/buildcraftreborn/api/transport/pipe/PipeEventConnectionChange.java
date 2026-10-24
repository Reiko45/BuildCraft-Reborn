/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import net.minecraft.core.Direction;

/** Fired whenever a connection change is picked up by an {@link IPipe}. This even doesn't include the new value
 * (boolean isConnected) as it can be accessed via {@link IPipe#isConnected(Direction)}. */
public class PipeEventConnectionChange extends PipeEvent {

    public final Direction direction;

    public PipeEventConnectionChange(IPipeHolder holder, Direction direction) {
        super(holder);
        this.direction = direction;
    }
}
