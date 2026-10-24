/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import java.util.Collection;


import net.minecraft.core.Direction;

import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IActionInternalSided;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.api.statements.ITriggerInternalSided;

public abstract class PipeEventStatement extends PipeEvent {
    public PipeEventStatement(IPipeHolder holder) {
        super(holder);
    }

    /** Fired when a pipe should add internal triggers to the list of all possible triggers */
    public static class AddTriggerInternal extends PipeEventStatement {
        public final Collection<ITriggerInternal> triggers;

        public AddTriggerInternal(IPipeHolder holder, Collection<ITriggerInternal> triggers) {
            super(holder);
            this.triggers = triggers;
        }
    }

    /** Fired when a pipe should add internal sided triggers to the list of all possible triggers */
    public static class AddTriggerInternalSided extends PipeEventStatement {
        public final Collection<ITriggerInternalSided> triggers;

        
        public final Direction side;

        public AddTriggerInternalSided(IPipeHolder holder, Collection<ITriggerInternalSided> triggers, Direction side) {
            super(holder);
            this.triggers = triggers;
            this.side = side;
        }
    }

    /** Fired when a pipe should add internal actions to the list of all possible actions */
    public static class AddActionInternal extends PipeEventStatement {
        public final Collection<IActionInternal> actions;

        public AddActionInternal(IPipeHolder holder, Collection<IActionInternal> actions) {
            super(holder);
            this.actions = actions;
        }
    }

    /** Fired when a pipe should add internal actions to the list of all possible actions */
    public static class AddActionInternalSided extends PipeEventStatement {
        public final Collection<IActionInternalSided> actions;

        
        public final Direction side;

        public AddActionInternalSided(IPipeHolder holder, Collection<IActionInternalSided> actions, Direction side) {
            super(holder);
            this.actions = actions;
            this.side = side;
        }
    }
}
