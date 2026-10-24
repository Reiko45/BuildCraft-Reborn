/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

import java.io.IOException;

import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.lib.net.IPayloadWriter;
import buildcraftreborn.lib.net.PacketBufferBC;

/** Defines some sort of separate element that exists on both the server and client. Doesn't draw directly: the
 * screen creates a client-only element that talks to it. */
public abstract class Widget_Neptune<C extends ContainerBC_Neptune> {
    public final C container;

    public Widget_Neptune(C container) {
        this.container = container;
    }

    public boolean isRemote() {
        return container.player.level().isClientSide;
    }

    // Net updating
    protected final void sendWidgetData(IPayloadWriter writer) {
        container.sendWidgetData(this, writer);
    }

    public void handleWidgetDataServer(IPayloadContext ctx, PacketBufferBC buffer) throws IOException {}

    public void handleWidgetDataClient(IPayloadContext ctx, PacketBufferBC buffer) throws IOException {}
}
