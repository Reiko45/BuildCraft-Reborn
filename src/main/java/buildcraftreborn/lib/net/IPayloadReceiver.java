/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.net;

import java.io.IOException;

import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Something that reads BuildCraft's raw tile payloads. Implemented by {@code TileBC_Neptune}. */
public interface IPayloadReceiver {
    void receivePayload(IPayloadContext ctx, PacketBufferBC buffer) throws IOException;
}
