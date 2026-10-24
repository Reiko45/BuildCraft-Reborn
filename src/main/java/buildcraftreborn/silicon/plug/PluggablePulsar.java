/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.plug;

import org.jetbrains.annotations.Nullable;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.fml.LogicalSide;
import net.minecraft.core.HolderLookup;
import java.io.IOException;
import java.util.Arrays;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import buildcraftreborn.api.core.render.EnumRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;


import buildcraftreborn.api.mj.IMjRedstoneReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.transport.pipe.IFlowFluid;
import buildcraftreborn.api.transport.pipe.IFlowItems;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeEventStatement;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;

import buildcraftreborn.lib.expression.DefaultContexts;
import buildcraftreborn.lib.expression.FunctionContext;
import buildcraftreborn.lib.expression.info.ContextInfo;
import buildcraftreborn.lib.expression.info.VariableInfo.CacheType;
import buildcraftreborn.lib.expression.info.VariableInfo.VariableInfoDouble;
import buildcraftreborn.lib.expression.info.VariableInfo.VariableInfoObject;
import buildcraftreborn.lib.expression.node.value.NodeVariableBoolean;
import buildcraftreborn.lib.expression.node.value.NodeVariableDouble;
import buildcraftreborn.lib.expression.node.value.NodeVariableObject;
import buildcraftreborn.lib.misc.MathUtil;
import buildcraftreborn.lib.misc.SoundUtil;
import buildcraftreborn.lib.misc.data.ModelVariableData;
import buildcraftreborn.lib.net.PacketBufferBC;

import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.BCSiliconStatements;
import buildcraftreborn.silicon.client.model.key.KeyPlugPulsar;
import buildcraftreborn.transport.BCTransportConfig;

public class PluggablePulsar extends PipePluggable {

    public static final FunctionContext MODEL_FUNC_CTX;
    private static final NodeVariableObject<Direction> MODEL_SIDE;
    private static final NodeVariableDouble MODEL_STAGE;
    private static final NodeVariableBoolean MODEL_ON;
    private static final NodeVariableBoolean MODEL_AUTO;
    private static final NodeVariableBoolean MODEL_MANUAL;
    public static final ContextInfo MODEL_VAR_INFO;

    private static final int PULSE_STAGE = 20;

    private static final AABB[] BOXES = new AABB[6];

    public final ModelVariableData clientModelData = new ModelVariableData();

    private boolean manuallyEnabled = false;
    /** Increments from 0 to {@link #PULSE_STAGE} to decide when it should pulse some power into the pipe behaviour */
    private int pulseStage = 0;
    private int gateEnabledTicks;
    private int gateSinglePulses;
    private boolean lastPulsing = false;

    /** Used on the client to determine if this should render pulsing */
    private boolean isPulsing = false;
    /** Used on the client to determine if this is being activated by a gate */
    private boolean autoEnabled = false;

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 5 / 16.0;
        double max = 11 / 16.0;

        BOXES[Direction.DOWN.ordinal()] = new AABB(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = new AABB(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = new AABB(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = new AABB(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = new AABB(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = new AABB(ul, min, min, uu, max, max);

        MODEL_FUNC_CTX = DefaultContexts.createWithAll();
        MODEL_SIDE = MODEL_FUNC_CTX.putVariableObject("side", Direction.class);
        MODEL_STAGE = MODEL_FUNC_CTX.putVariableDouble("stage");
        MODEL_ON = MODEL_FUNC_CTX.putVariableBoolean("on");
        MODEL_AUTO = MODEL_FUNC_CTX.putVariableBoolean("auto");
        MODEL_MANUAL = MODEL_FUNC_CTX.putVariableBoolean("manual");

        MODEL_VAR_INFO = new ContextInfo(MODEL_FUNC_CTX);
        VariableInfoObject<Direction> infoSide = MODEL_VAR_INFO.createInfoObject(MODEL_SIDE);
        infoSide.cacheType = CacheType.ALWAYS;
        infoSide.setIsComplete = true;
        infoSide.possibleValues.addAll(Arrays.asList(Direction.values()));

        VariableInfoDouble infoStage = MODEL_VAR_INFO.createInfoDouble(MODEL_STAGE);
        infoStage.cacheType = CacheType.IN_SET;
        infoStage.setIsComplete = false;
        infoStage.possibleValues.add(0.0);
    }

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    // Saving + Loading

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side, CompoundTag nbt,
        HolderLookup.Provider registries) {
        super(definition, holder, side);
        this.manuallyEnabled = nbt.getBoolean("manuallyEnabled");
        gateEnabledTicks = nbt.getInt("gateEnabledTicks");
        gateSinglePulses = nbt.getInt("gateSinglePulses");
        pulseStage = MathUtil.clamp(nbt.getInt("pulseStage"), 0, PULSE_STAGE);
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = super.writeToNbt(registries);
        nbt.putBoolean("manuallyEnabled", manuallyEnabled);
        nbt.putInt("gateEnabledTicks", gateEnabledTicks);
        nbt.putInt("gateSinglePulses", gateSinglePulses);
        nbt.putInt("pulseStage", pulseStage);
        return nbt;
    }

    // Networking

    public PluggablePulsar(PluggableDefinition definition, IPipeHolder holder, Direction side, PacketBufferBC buffer) {
        super(definition, holder, side);
        readData(buffer);
    }

    @Override
    public void writeCreationPayload(PacketBufferBC buffer) {
        super.writeCreationPayload(buffer);
        writeData(buffer);
    }

    @Override
    public void readPayload(PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            readData(buffer);
        }
    }

    @Override
    public void writePayload(PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(buffer, side);
        if (side == LogicalSide.SERVER) {
            writeData(buffer);
        }
    }

    private void writeData(PacketBufferBC buffer) {
        buffer.writeBoolean(isPulsing());
        buffer.writeBoolean(gateEnabledTicks > 0 || gateSinglePulses > 0);
        buffer.writeBoolean(manuallyEnabled);
        buffer.writeByte(pulseStage);
    }

    private void readData(PacketBufferBC buffer) {
        isPulsing = buffer.readBoolean();
        autoEnabled = buffer.readBoolean();
        manuallyEnabled = buffer.readBoolean();
        pulseStage = buffer.readByte();
    }

    // PipePluggable

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCSiliconItems.PLUG_PULSAR.get());
    }

    @Override
    public void onTick() {
        if (holder.getPipeWorld().isClientSide) {
            if (isPulsing) {
                pulseStage++;
                if (pulseStage == PULSE_STAGE) {
                    pulseStage = 0;
                }
            } else {
                // pulseStage--;
                // if (pulseStage < 0) {
                pulseStage = 0;
                // }
            }
            setModelVariables(1);
            clientModelData.tick();
            return;
        }
        boolean isOn = isPulsing();

        if (isOn) {
            pulseStage++;
        } else {
            // pulseStage--;
            // if (pulseStage < 0) {
            pulseStage = 0;
            // }
        }
        if (gateEnabledTicks > 0) {
            gateEnabledTicks--;
        }
        if (pulseStage == PULSE_STAGE) {
            pulseStage = 0;
            IMjRedstoneReceiver rsRec = (IMjRedstoneReceiver) holder.getPipe().getBehaviour();
            if (gateSinglePulses > 0) {
                long power = MjAPI.MJ;
                if (holder.getPipe().getFlow() instanceof IFlowFluid) {
                    power = BCTransportConfig.mjPerMillibucket * 1000;
                } else if (holder.getPipe().getFlow() instanceof IFlowItems) {
                    power = BCTransportConfig.mjPerItem;
                }
                long excess = rsRec.receivePower(power, true);
                if (excess == 0) {
                    rsRec.receivePower(power, false);
                } else {
                    // Nothing was extracted, so lets extract in the future
                    gateSinglePulses++;
                    // ParticleUtil.spawnFailureParticles
                }
            } else {
                rsRec.receivePower(MjAPI.MJ, false);
            }
            if (gateSinglePulses > 0) {
                gateSinglePulses--;
            }
        }
        if (isOn != lastPulsing) {
            lastPulsing = isOn;
            scheduleNetworkUpdate();
        }
    }

    @PipeEventHandler
    public void onAddActions(PipeEventStatement.AddActionInternalSided event) {
        if (event.side == this.side) {
            event.actions.add(BCSiliconStatements.ACTION_PULSAR_CONSTANT);
            event.actions.add(BCSiliconStatements.ACTION_PULSAR_SINGLE);
        }
    }

    @Override
    public boolean onPluggableActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ) {
        if (!holder.getPipeWorld().isClientSide) {
            manuallyEnabled = !manuallyEnabled;
            SoundUtil.playLeverSwitch(holder.getPipeWorld(), holder.getPipePos(), manuallyEnabled);
            scheduleNetworkUpdate();
        }
        return true;
    }

    @Override
    public PluggableModelKey getModelRenderKey(EnumRenderLayer layer) {
        if (layer == EnumRenderLayer.CUTOUT) return new KeyPlugPulsar(side);
        return null;
    }

    public void enablePulsar() {
        gateEnabledTicks = 10;
    }

    public void addSinglePulse() {
        gateSinglePulses++;
    }

    private boolean isPulsing() {
        return manuallyEnabled || gateEnabledTicks > 0 || gateSinglePulses > 0;
    }

    // Model

    public static void setModelVariablesForItem() {
        MODEL_STAGE.value = 0;
        MODEL_AUTO.value = false;
        MODEL_MANUAL.value = false;
        MODEL_ON.value = false;
        MODEL_SIDE.value = Direction.WEST;
    }

    public void setModelVariables(float partialTicks) {
        if (isPulsing) {
            MODEL_STAGE.value = (pulseStage + partialTicks) / 20 % 1;
        } else {
            MODEL_STAGE.value = 0;
        }
        MODEL_ON.value = isPulsing;
        MODEL_MANUAL.value = manuallyEnabled;
        MODEL_AUTO.value = autoEnabled;
        MODEL_SIDE.value = side;
    }
}
