/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.tile;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.enums.EnumPowerStage;
import buildcraftreborn.api.mj.IMjConnector;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.lib.engine.EngineConnector;
import buildcraftreborn.lib.engine.TileEngineBase_BC8;
import buildcraftreborn.lib.misc.EntityUtil;
import buildcraftreborn.lib.misc.MathUtil;
import buildcraftreborn.lib.net.PacketBufferBC;

/** The creative engine: unlimited power, with the output chosen by right-clicking it with a wrench. */
public class TileEngineCreative extends TileEngineBase_BC8 {
    public static final long[] outputs = { 1, 2, 4, 8, 16, 32, 64, 128, 256 };
    public int currentOutputIndex = 0;

    public TileEngineCreative(BlockPos pos, BlockState state) {
        super(BCCoreBlocks.ENGINE_CREATIVE_TILE.get(), pos, state);
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeByte(currentOutputIndex);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                currentOutputIndex = buffer.readUnsignedByte() % outputs.length;
            }
        }
    }

    @Override
    protected void engineUpdate() {
        if (isBurning()) {
            power += getCurrentOutput();
            long max = getMaxPower();
            if (power > max) {
                power = getMaxPower();
            }
        } else {
            power = 0;
        }
    }

    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        return isRedstonePowered;
    }

    @Override
    public double getPistonSpeed() {
        final double max = 0.08;
        final double min = 0.01;
        double interp = currentOutputIndex / (double) (outputs.length - 1);
        return MathUtil.interp(interp, min, max);
    }

    @Override
    protected EnumPowerStage computePowerStage() {
        return EnumPowerStage.BLACK;
    }

    @Override
    public long getMaxPower() {
        return getCurrentOutput() * 10_000;
    }

    @Override
    public long maxPowerReceived() {
        return 2_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 20 * getCurrentOutput();
    }

    @Override
    public float explosionRange() {
        return 0;
    }

    @Override
    public long getCurrentOutput() {
        return outputs[MathUtil.clamp(currentOutputIndex, 0, outputs.length - 1)] * MjAPI.MJ;
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (EntityUtil.isWrench(held)) {
            if (!level.isClientSide) {
                currentOutputIndex++;
                currentOutputIndex %= outputs.length;
                player.displayClientMessage(
                    Component.translatable("chat.pipe.power.iron.mode", outputs[currentOutputIndex]), true);
                sendNetworkUpdate(NET_RENDER_DATA);
                setChanged();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putInt("currentOutputIndex", currentOutputIndex);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        currentOutputIndex = MathUtil.clamp(nbt.getInt("currentOutputIndex"), 0, outputs.length - 1);
    }
}
