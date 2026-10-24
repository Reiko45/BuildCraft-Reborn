/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.lib.net;

import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import buildcraftreborn.builders.snapshot.SnapshotRequestPayload;
import buildcraftreborn.builders.snapshot.SnapshotResponsePayload;
import buildcraftreborn.core.marker.volume.VolumeBoxesPayload;
import buildcraftreborn.lib.debug.DebugEntityRequestPayload;
import buildcraftreborn.lib.debug.DebugRequestPayload;
import buildcraftreborn.lib.debug.DebugResponsePayload;
import buildcraftreborn.robotics.zone.ZoneMapRequestPayload;
import buildcraftreborn.robotics.zone.ZoneMapResponsePayload;
import buildcraftreborn.transport.net.MultiPipeItemPayload;
import buildcraftreborn.transport.wire.WireSystemsPayload;
import buildcraftreborn.transport.wire.WireSystemsPoweredPayload;

/**
 * Network channel for BuildCraft Reborn. Replaces 1.12's {@code SimpleNetworkWrapper} and {@code MessageManager}.
 * Each 1.12 {@code IMessage} becomes a {@code CustomPacketPayload} record with a {@code StreamCodec}, registered
 * here.
 */
public final class BCNetwork {
    /** Bump whenever a payload's wire format changes, so mismatched clients are refused cleanly. */
    public static final String PROTOCOL_VERSION = "1";

    private BCNetwork() {}

    public static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playBidirectional(TileUpdatePayload.TYPE, TileUpdatePayload.STREAM_CODEC, TileUpdatePayload::handle);
        registrar.playBidirectional(ContainerPayload.TYPE, ContainerPayload.STREAM_CODEC, ContainerPayload::handle);
        registrar.playToClient(MarkerPayload.TYPE, MarkerPayload.STREAM_CODEC, MarkerPayload::handle);
        registrar.playToServer(SnapshotRequestPayload.TYPE, SnapshotRequestPayload.STREAM_CODEC,
            SnapshotRequestPayload::handle);
        registrar.playToClient(SnapshotResponsePayload.TYPE, SnapshotResponsePayload.STREAM_CODEC,
            SnapshotResponsePayload::handle);
        registrar.playToClient(VolumeBoxesPayload.TYPE, VolumeBoxesPayload.STREAM_CODEC, VolumeBoxesPayload::handle);
        registrar.playToClient(WireSystemsPayload.TYPE, WireSystemsPayload.STREAM_CODEC, WireSystemsPayload::handle);
        registrar.playToClient(WireSystemsPoweredPayload.TYPE, WireSystemsPoweredPayload.STREAM_CODEC,
            WireSystemsPoweredPayload::handle);
        registrar.playToClient(MultiPipeItemPayload.TYPE, MultiPipeItemPayload.STREAM_CODEC,
            MultiPipeItemPayload::handle);
        registrar.playToServer(DebugRequestPayload.TYPE, DebugRequestPayload.STREAM_CODEC, DebugRequestPayload::handle);
        registrar.playToServer(DebugEntityRequestPayload.TYPE, DebugEntityRequestPayload.STREAM_CODEC,
            DebugEntityRequestPayload::handle);
        registrar.playToClient(DebugResponsePayload.TYPE, DebugResponsePayload.STREAM_CODEC,
            DebugResponsePayload::handle);
        registrar.playToServer(ZoneMapRequestPayload.TYPE, ZoneMapRequestPayload.STREAM_CODEC,
            ZoneMapRequestPayload::handle);
        registrar.playToClient(ZoneMapResponsePayload.TYPE, ZoneMapResponsePayload.STREAM_CODEC,
            ZoneMapResponsePayload::handle);
    }
}
