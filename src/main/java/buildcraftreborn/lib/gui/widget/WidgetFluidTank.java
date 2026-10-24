/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.widget;

import java.io.IOException;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.gui.ContainerBC_Neptune;
import buildcraftreborn.lib.gui.Widget_Neptune;
import buildcraftreborn.lib.net.PacketBufferBC;

/** The server half of a tank shown in a GUI: clicking it with a bucket (or other fluid container) on the cursor fills
 * or empties the tank. The client half is {@code GuiElementFluidTank}.
 * <p>
 * Port note: 1.12 created the client element from a method on this class. That is split into its own client class
 * so dedicated servers never load GUI code. */
public class WidgetFluidTank extends Widget_Neptune<ContainerBC_Neptune> {
    private static final byte NET_CLICK = 0;

    public final Tank tank;

    public WidgetFluidTank(ContainerBC_Neptune container, Tank tank) {
        super(container);
        this.tank = tank;
    }

    /** Client side: tell the server the tank was clicked. */
    public void sendClick() {
        sendWidgetData(buffer -> buffer.writeByte(NET_CLICK));
    }

    @Override
    public void handleWidgetDataServer(IPayloadContext ctx, PacketBufferBC buffer) throws IOException {
        byte id = buffer.readByte();
        if (id == NET_CLICK) {
            container.setCarried(tank.transferStackToTank(container.player, container.getCarried()));
            container.broadcastChanges();
        }
    }
}
