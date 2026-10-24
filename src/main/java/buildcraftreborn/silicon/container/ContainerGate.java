/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.container;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.StatementManager;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.lib.gui.ContainerBC_Neptune;
import buildcraftreborn.lib.gui.ContainerPipe;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.statement.ActionWrapper;
import buildcraftreborn.lib.statement.StatementWrapper;
import buildcraftreborn.lib.statement.TriggerWrapper;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.gate.GateContext;
import buildcraftreborn.silicon.gate.GateContext.GateGroup;
import buildcraftreborn.silicon.gate.GateLogic;
import buildcraftreborn.silicon.plug.PluggableGate;

/** The gate GUI's menu. The client asks for the triggers and actions it can pick from (the server knows what's next
 * to the pipe), and sends connection changes; statement changes go through the gate itself. */
public class ContainerGate extends ContainerPipe {
    protected static final IdAllocator IDS = ContainerBC_Neptune.IDS.makeChild("gate");

    public static final int ID_CONNECTION = IDS.allocId("CONNECTION");
    public static final int ID_VALID_STATEMENTS = IDS.allocId("VALID_STATEMENTS");

    public final GateLogic gate;

    public final int slotHeight;

    public final SortedSet<TriggerWrapper> possibleTriggers;
    public final SortedSet<ActionWrapper> possibleActions;

    public final GateContext<TriggerWrapper> possibleTriggersContext;
    public final GateContext<ActionWrapper> possibleActionsContext;

    public ContainerGate(int windowId, Player player, GateLogic logic) {
        super(BCSiliconBlocks.GATE_MENU.get(), windowId, player, logic.getPipeHolder());
        this.gate = logic;

        boolean split = gate.isSplitInTwo();
        int s = gate.variant.numSlots;
        if (split) {
            s = (int) Math.ceil(s / 2.0);
        }
        slotHeight = s;

        if (gate.getPipeHolder().getPipeWorld().isClientSide) {
            possibleTriggers = new TreeSet<>();
            possibleActions = new TreeSet<>();
        } else {
            possibleTriggers = gate.getAllValidTriggers();
            possibleActions = gate.getAllValidActions();
        }

        possibleTriggersContext = new GateContext<>(new ArrayList<>());
        possibleActionsContext = new GateContext<>(new ArrayList<>());

        refreshPossibleGroups();

        addFullPlayerInventory(33 + slotHeight * 18);
    }

    /** Reads the pipe position and gate side written by {@link buildcraftreborn.silicon.BCSiliconGuis#openGate}. */
    public static ContainerGate fromNetwork(int windowId, Inventory inventory, RegistryFriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        Direction side = buf.readEnum(Direction.class);
        BlockEntity tile = inventory.player.level().getBlockEntity(pos);
        IPipeHolder holder = CapUtil.getCapability(tile, PipeApi.CAP_PIPE_HOLDER, null);
        if (holder != null && holder.getPluggable(side) instanceof PluggableGate gate) {
            return new ContainerGate(windowId, inventory.player, gate.logic);
        }
        throw new IllegalStateException("Expected a gate on the " + side + " of " + pos + ", found " + tile);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private void refreshPossibleGroups() {
        refresh(possibleActions, possibleActionsContext);
        refresh(possibleTriggers, possibleTriggersContext);
    }

    private static <T extends StatementWrapper> void refresh(SortedSet<T> from, GateContext<T> to) {
        to.groups.clear();
        Map<EnumPipePart, List<T>> parts = new EnumMap<>(EnumPipePart.class);
        for (T val : from) {
            parts.computeIfAbsent(val.sourcePart, p -> new ArrayList<>()).add(val);
        }
        List<T> list = parts.get(EnumPipePart.CENTER);
        if (list == null) {
            list = new ArrayList<>(1);
            list.add(null);
        } else {
            list.add(0, null);
        }
        to.groups.add(new GateGroup<>(EnumPipePart.CENTER, list));
        for (EnumPipePart part : EnumPipePart.FACES) {
            list = parts.get(part);
            if (list != null) {
                to.groups.add(new GateGroup<>(part, list));
            }
        }
    }

    @Override
    public void readMessage(int id, PacketBufferBC buffer, LogicalSide side, IPayloadContext ctx) throws IOException {
        super.readMessage(id, buffer, side, ctx);
        if (side == LogicalSide.SERVER) {
            if (id == ID_CONNECTION) {
                int index = buffer.readUnsignedByte();
                boolean to = buffer.readBoolean();
                if (index < gate.connections.length) {
                    gate.connections[index] = to;
                    gate.sendResolveData();
                }
            } else if (id == ID_VALID_STATEMENTS) {
                sendMessage(ID_VALID_STATEMENTS);
            }
        } else if (side == LogicalSide.CLIENT) {
            if (id == ID_VALID_STATEMENTS) {
                possibleTriggers.clear();
                possibleActions.clear();
                int numTriggers = buffer.readInt();
                int numActions = buffer.readInt();
                for (int i = 0; i < numTriggers; i++) {
                    String tag = buffer.readUtf(256);
                    EnumPipePart part = buffer.readEnum(EnumPipePart.class);
                    IStatement statement = StatementManager.statements.get(tag);
                    if (statement == null) {
                        continue;
                    }
                    TriggerWrapper wrapper = TriggerWrapper.wrap(statement, part.face);
                    if (gate.isValidTrigger(wrapper)) {
                        possibleTriggers.add(wrapper);
                    }
                }
                for (int i = 0; i < numActions; i++) {
                    String tag = buffer.readUtf(256);
                    EnumPipePart part = buffer.readEnum(EnumPipePart.class);
                    IStatement statement = StatementManager.statements.get(tag);
                    if (statement == null) {
                        continue;
                    }
                    ActionWrapper wrapper = ActionWrapper.wrap(statement, part.face);
                    if (gate.isValidAction(wrapper)) {
                        possibleActions.add(wrapper);
                    }
                }
                refreshPossibleGroups();
            }
        }
    }

    @Override
    public void writeMessage(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writeMessage(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == ID_VALID_STATEMENTS) {
                buffer.writeInt(possibleTriggers.size());
                buffer.writeInt(possibleActions.size());
                for (TriggerWrapper wrapper : possibleTriggers) {
                    buffer.writeUtf(wrapper.getUniqueTag());
                    buffer.writeEnum(wrapper.sourcePart);
                }

                for (ActionWrapper wrapper : possibleActions) {
                    buffer.writeUtf(wrapper.getUniqueTag());
                    buffer.writeEnum(wrapper.sourcePart);
                }
            }
        }
    }

    public void setConnected(int index, boolean to) {
        sendMessage(ID_CONNECTION, (buffer) -> {
            buffer.writeByte(index);
            buffer.writeBoolean(to);
        });
    }
}
