/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;

import buildcraftreborn.silicon.container.ContainerGate;
import buildcraftreborn.silicon.plug.PluggableGate;

/** Opens the gate GUI. (1.12's other silicon GUIs, the laser tables', are opened by their tiles.) */
public final class BCSiliconGuis {
    private BCSiliconGuis() {}

    /** Opens the GUI of the gate on the given side of the pipe at {@code pos}. */
    public static void openGate(Player player, BlockPos pos, Direction side, PluggableGate gate) {
        player.openMenu(new SimpleMenuProvider(
            (windowId, inventory, p) -> new ContainerGate(windowId, p, gate.logic),
            gate.logic.variant.getDisplayName()), buf -> {
                buf.writeBlockPos(pos);
                buf.writeEnum(side);
            });
    }
}
