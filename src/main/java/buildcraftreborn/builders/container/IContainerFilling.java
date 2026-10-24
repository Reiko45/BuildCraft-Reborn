/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.container;

import java.io.IOException;
import java.util.stream.IntStream;

import net.minecraft.world.entity.player.Player;

import net.neoforged.fml.LogicalSide;

import buildcraftreborn.api.filler.IFillerPattern;
import buildcraftreborn.builders.filler.FillerType;
import buildcraftreborn.lib.gui.ContainerBC_Neptune;
import buildcraftreborn.lib.misc.MessageUtil;
import buildcraftreborn.lib.net.IPayloadWriter;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.statement.FullStatement;

/** The shared part of the filler and filler planner menus: a filler pattern (with its parameters) and the "inverted"
 * toggle, edited on the client and sent to the server. */
public interface IContainerFilling {
    Player getPlayer();

    void sendMessage(int id, IPayloadWriter writer);

    /** The copy the GUI edits. */
    FullStatement<IFillerPattern> getPatternStatementClient();

    /** The real one (in the filler or addon). */
    FullStatement<IFillerPattern> getPatternStatement();

    boolean isInverted();

    void setInverted(boolean value);

    default boolean isLocked() {
        return false;
    }

    void valuesChanged();

    default void init() {
        if (!getPlayer().level().isClientSide) {
            // Sent once the client has opened the menu.
            MessageUtil.doDelayedServer(this::sendData);
        }
    }

    default void sendData() {
        sendMessage(ContainerBC_Neptune.NET_DATA, buffer -> {
            (getPlayer().level().isClientSide ? getPatternStatementClient() : getPatternStatement())
                .writeToBuffer(buffer);
            buffer.writeBoolean(isInverted());
        });
    }

    default void onStatementChange() {
        sendData();
    }

    default void sendInverted(boolean value) {
        setInverted(value);
        sendData();
    }

    default void readMessage(int id, PacketBufferBC buffer, LogicalSide side) throws IOException {
        if (side == LogicalSide.SERVER) {
            if (id == ContainerBC_Neptune.NET_DATA) {
                if (isLocked()) {
                    // A gate is setting the pattern: ignore what the player picked.
                    new FullStatement<>(FillerType.INSTANCE, 4, (a, b) -> {}).readFromBuffer(buffer);
                } else {
                    getPatternStatement().readFromBuffer(buffer);
                }
                setInverted(buffer.readBoolean());
                valuesChanged();
                sendData();
            }
        } else if (side == LogicalSide.CLIENT) {
            if (id == ContainerBC_Neptune.NET_DATA) {
                getPatternStatement().readFromBuffer(buffer);
                setInverted(buffer.readBoolean());
                getPatternStatementClient().set(getPatternStatement().get());
                IntStream.range(0, 4).forEach(i -> getPatternStatementClient().getParamRef(i)
                    .set(getPatternStatement().getParamRef(i).get()));
                valuesChanged();
            }
        }
    }
}
