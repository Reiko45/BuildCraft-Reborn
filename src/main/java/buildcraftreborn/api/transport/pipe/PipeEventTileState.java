/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import net.minecraft.world.level.block.entity.BlockEntity;

/** Fired when the state of a pipe's tile entity changes. Listen for subclasses, not this one! */
public abstract class PipeEventTileState extends PipeEvent {
    PipeEventTileState(IPipeHolder holder) {
        super(holder);
    }

    /** Fired in {@link BlockEntity#invalidate()} */
    public static class Invalidate extends PipeEventTileState {
        public Invalidate(IPipeHolder holder) {
            super(holder);
        }
    }

    /** Fired in {@link BlockEntity#validate()} */
    public static class Validate extends PipeEventTileState {
        public Validate(IPipeHolder holder) {
            super(holder);
        }
    }

    /** Fired in {@link BlockEntity#onChunkUnload()} */
    public static class ChunkUnload extends PipeEventTileState {
        public ChunkUnload(IPipeHolder holder) {
            super(holder);
        }
    }
}
