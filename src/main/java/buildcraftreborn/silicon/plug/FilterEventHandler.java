/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.plug;

import net.minecraft.world.item.DyeColor;
import net.minecraft.core.Direction;

import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeEventItem;
import buildcraftreborn.api.transport.pluggable.PipePluggable;

public class FilterEventHandler {
    @PipeEventHandler
    public static void sideCheck(PipeEventItem.SideCheck event) {
        for (Direction side : Direction.values()) {
            if (!event.isAllowed(side)) {
                continue;
            }
            IPipe neighbour = event.holder.getNeighbourPipe(side);
            if (neighbour == null) {
                continue;
            }
            PipePluggable neighbourPlug = neighbour.getHolder().getPluggable(side.getOpposite());
            PipePluggable atPlug = event.holder.getPluggable(side);
            if (neighbourPlug instanceof PluggableLens) {
                DyeColor colourAt = event.colour;
                if (atPlug instanceof PluggableLens) {
                    PluggableLens lens = (PluggableLens) atPlug;
                    if (!lens.isFilter) {
                        colourAt = lens.colour;
                    }
                }

                PluggableLens lens = (PluggableLens) neighbourPlug;
                if (lens.isFilter) {
                    if (colourAt == lens.colour) {
                        event.increasePriority(side);
                    } else if (colourAt == null) {
                        event.decreasePriority(side);
                    }
                }
            }
        }
    }
}
