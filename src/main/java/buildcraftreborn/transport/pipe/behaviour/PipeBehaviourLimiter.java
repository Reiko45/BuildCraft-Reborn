/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.behaviour;

import buildcraftreborn.lib.net.PacketBufferBC;
import org.jetbrains.annotations.Nullable;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.LogicalSide;
import net.minecraft.core.HolderLookup;
import java.io.IOException;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;


import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.transport.pipe.IFlowPowerLike;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraftreborn.api.transport.pipe.PipeApi.RedstoneFluxTransferInfo;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.api.transport.pipe.PipeEventActionActivate;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeEventPower;
import buildcraftreborn.api.transport.pipe.PipeEventRedstoneFlux;

import buildcraftreborn.lib.misc.EntityUtil;
import buildcraftreborn.lib.misc.MathUtil;

import buildcraftreborn.transport.pipe.flow.PipeFlowRedstoneFlux;
import buildcraftreborn.transport.statements.ActionPowerLimit;

public class PipeBehaviourLimiter extends PipeBehaviour {

    public static final int MAX_SHIFT = 6;

    private int limitShift = 0;

    public PipeBehaviourLimiter(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourLimiter(IPipe pipe, CompoundTag nbt, HolderLookup.Provider registries) {
        super(pipe, nbt, registries);
        limitShift = MathUtil.clamp(nbt.getInt("limitShift"), 0, MAX_SHIFT);
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = super.writeToNbt(registries);
        nbt.putInt("limitShift", limitShift);
        return nbt;
    }

    @Override
    public void readPayload(PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        limitShift = buffer.readUnsignedByte();
    }

    @Override
    public void writePayload(PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(buffer, side);
        buffer.writeByte(limitShift);
    }

    @PipeEventHandler
    public void configurePower(PipeEventPower.Configure event) {
        if (limitShift == MAX_SHIFT) {
            event.disableTransfer();
        } else {
            event.setMaxPower(event.getMaxPower() >> limitShift);
        }
    }

    @PipeEventHandler
    public void configurePower(PipeEventRedstoneFlux.Configure event) {
        if (limitShift == MAX_SHIFT) {
            event.disableTransfer();
        } else {
            event.setMaxPower(event.getMaxPower() >> limitShift);
        }
    }

    @PipeEventHandler
    public void onActionActivate(PipeEventActionActivate event) {
        if (event.action instanceof ActionPowerLimit) {
            limitShift = ((ActionPowerLimit) event.action).limitShift;

            requestReconfigure();
        }
    }

    @Override
    public boolean onPipeActivate(
        Player player, BlockHitResult trace, float hitX, float hitY, float hitZ, EnumPipePart part
    ) {
        if (EntityUtil.getWrenchHand(player) == null) {
            return false;
        }

        if (!player.level().isClientSide) {
            EntityUtil.activateWrench(player, trace);
            limitShift++;
            if (limitShift > MAX_SHIFT) {
                limitShift = 0;
            }

            boolean isRf = pipe.getFlow() instanceof PipeFlowRedstoneFlux;
            final int limit;
            if (limitShift == MAX_SHIFT) {
                limit = 0;
            } else if (isRf) {
                RedstoneFluxTransferInfo transferInfo = PipeApi.getRfTransferInfo(pipe.getDefinition());
                limit = transferInfo.transferPerTick >> limitShift;
            } else {
                PowerTransferInfo transferInfo = PipeApi.getPowerTransferInfo(pipe.getDefinition());
                limit = (int) ((transferInfo.transferPerTick >> limitShift) / MjAPI.MJ);
            }
            String key = "chat.pipe." + (isRf ? "rf" : "power") + ".iron.mode";
            Component chat = Component.translatable(key, limit);
            player.displayClientMessage(chat, true);

            requestReconfigure();
        }
        return true;
    }

    private void requestReconfigure() {
        if (pipe.getFlow() instanceof IFlowPowerLike) {
            ((IFlowPowerLike) pipe.getFlow()).reconfigure();
            pipe.getHolder().scheduleNetworkUpdate(PipeMessageReceiver.BEHAVIOUR);
        }
    }

    @Override
    public int getTextureIndex(Direction face) {
        return MAX_SHIFT - limitShift;
    }
}
