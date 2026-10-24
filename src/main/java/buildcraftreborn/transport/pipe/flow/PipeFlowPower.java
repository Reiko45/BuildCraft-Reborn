/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.flow;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.ToLongFunction;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.mj.IMjConnector;
import buildcraftreborn.api.mj.IMjPassiveProvider;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjToRfAutoConvertor;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.api.transport.pipe.IFlowPower;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipe.ConnectedType;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraftreborn.api.transport.pipe.PipeEventPower;
import buildcraftreborn.api.transport.pipe.PipeFlow;

import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.MathUtil;
import buildcraftreborn.lib.misc.VecUtil;
import buildcraftreborn.lib.misc.data.AverageInt;
import buildcraftreborn.lib.net.PacketBufferBC;

/** The flow of kinesis (MJ) pipes: each side asks for power, and power in a side is shared among the others. */
public class PipeFlowPower extends PipeFlow implements IFlowPower, IDebuggable {
    private static final long DEFAULT_MAX_POWER = MjAPI.MJ * 10;
    public static final int NET_POWER_AMOUNTS = 2;

    public Vec3 clientDisplayFlowCentre = VecUtil.VEC_HALF;
    public Vec3 clientDisplayFlowCentreLast = VecUtil.VEC_HALF;
    public long clientLastDisplayTime = 0;

    private long maxPower = -1;
    private long powerLoss = -1;
    private long powerResistance = -1;
    private boolean disabled = false;

    private long currentWorldTime;

    private boolean isReceiver = false;
    private final EnumMap<Direction, Section> sections;

    public PipeFlowPower(IPipe pipe) {
        super(pipe);
        sections = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            sections.put(face, new Section(face));
        }
    }

    public PipeFlowPower(IPipe pipe, CompoundTag nbt, HolderLookup.Provider registries) {
        super(pipe, nbt, registries);
        isReceiver = nbt.getBoolean("isReceiver");
        sections = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            sections.put(face, new Section(face));
        }
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = super.writeToNbt(registries);
        nbt.putBoolean("isReceiver", isReceiver);
        return nbt;
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
                for (Direction face : Direction.values()) {
                    Section s = sections.get(face);
                    buffer.writeInt(s.displayPower);
                    buffer.writeEnum(s.displayFlow);
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side) throws IOException {
        super.readPayload(id, buffer, side);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
                for (Direction face : Direction.values()) {
                    Section s = sections.get(face);
                    s.displayPower = buffer.readInt();
                    s.displayFlow = buffer.readEnum(EnumFlow.class);
                }
            }
        }
    }

    @Override
    public boolean canConnect(Direction face, PipeFlow other) {
        return other instanceof PipeFlowPower;
    }

    @Override
    public boolean canConnect(Direction face, BlockEntity oTile) {
        if (isReceiver) {
            IMjPassiveProvider provider = CapUtil.getCapability(oTile, MjAPI.CAP_PASSIVE_PROVIDER, face.getOpposite());
            if (provider != null) {
                return true;
            }
        }
        IMjConnector receiver = CapUtil.getCapability(oTile, MjAPI.CAP_CONNECTOR, face.getOpposite());
        return receiver != null && receiver.canConnect(sections.get(face));
    }

    @Override
    public void reconfigure() {
        PipeEventPower.Configure configure = new PipeEventPower.Configure(pipe.getHolder(), this);
        PowerTransferInfo pti = PipeApi.getPowerTransferInfo(pipe.getDefinition());
        configure.setReceiver(pti.isReceiver);
        configure.setMaxPower(pti.transferPerTick);
        configure.setPowerLoss(pti.lossPerTick);
        configure.setPowerResistance(pti.resistancePerTick);
        pipe.getHolder().fireEvent(configure);
        isReceiver = configure.isReceiver();
        maxPower = configure.getMaxPower();
        disabled = configure.isTransferDisabled();
        if (maxPower <= 0) {
            maxPower = DEFAULT_MAX_POWER;
        }
        powerLoss = MathUtil.clamp(configure.getPowerLoss(), -1, maxPower);
        powerResistance = MathUtil.clamp(configure.getPowerResistance(), -1, MjAPI.MJ);

        if (powerLoss < 0) {
            if (powerResistance < 0) {
                // 1% resistance
                powerResistance = MjAPI.MJ / 100;
            }
            powerLoss = maxPower * powerResistance / MjAPI.MJ;
        } else if (powerResistance < 0) {
            powerResistance = powerLoss * MjAPI.MJ / maxPower;
        }
    }

    @Override
    public long tryExtractPower(long maxExtracted, Direction from) {
        if (!isReceiver || disabled) {
            return 0;
        }
        BlockEntity tile = pipe.getConnectedTile(from);
        if (tile == null) {
            return 0;
        }
        IMjPassiveProvider receiver = CapUtil.getCapability(tile, MjAPI.CAP_PASSIVE_PROVIDER, from.getOpposite());
        if (receiver == null) {
            return 0;
        }

        // TODO!
        return 0;
    }

    @Override
    public boolean onFlowActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ,
        EnumPipePart part) {
        return super.onFlowActivate(player, trace, hitX, hitY, hitZ, part);
    }

    public Section getSection(Direction side) {
        return sections.get(side);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction facing) {
        if (facing == null) {
            return null;
        } else if (capability == MjAPI.CAP_RECEIVER) {
            return isReceiver ? (T) sections.get(facing) : null;
        } else if (capability == MjAPI.CAP_CONNECTOR) {
            return (T) sections.get(facing);
        } else {
            return null;
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        left.add("maxPower = " + LocaleUtil.localizeMj(maxPower));
        left.add("isReceiver = " + isReceiver);
        left.add(
            "internalPower = " + arrayToString(s -> s.internalPower) + " <- " + arrayToString(s -> s.internalNextPower)
        );
        left.add("- powerQuery: " + arrayToString(s -> s.powerQuery) + " <- " + arrayToString(s -> s.nextPowerQuery));
        left.add(
            "- power: IN " + arrayToString(s -> s.debugPowerInput) + ", OUT " + arrayToString(s -> s.debugPowerOutput)
        );
        left.add("- power: OFFERED " + arrayToString(s -> s.debugPowerOffered));
    }

    private String arrayToString(ToLongFunction<Section> getter) {
        long[] arr = new long[6];
        for (Direction face : Direction.values()) {
            arr[face.ordinal()] = getter.applyAsLong(sections.get(face)) / MjAPI.MJ;
        }
        return Arrays.toString(arr);
    }

    @Override
    public void onTick() {
        if (maxPower == -1) {
            reconfigure();
        }
        if (pipe.getHolder().getPipeWorld().isClientSide) {
            clientDisplayFlowCentreLast = clientDisplayFlowCentre;
            for (Direction face : Direction.values()) {
                Section s = sections.get(face);
                s.clientDisplayFlowLast = s.clientDisplayFlow;
                double diff = s.displayFlow.value * 2.4 * face.getAxisDirection().getStep();
                s.clientDisplayFlow += 16 + diff;
                s.clientDisplayFlow %= 16;

                double cVal = VecUtil.getValue(clientDisplayFlowCentre, face.getAxis());
                cVal += 16 + diff / 2;
                cVal %= 16;
                clientDisplayFlowCentre = VecUtil.replaceValue(clientDisplayFlowCentre, face.getAxis(), cVal);
            }
            return;
        }

        EnumFlow[] lastFlows = new EnumFlow[6];
        int[] lastDisplayPower = new int[6];

        for (Direction face : Direction.values()) {
            Section s = sections.get(face);
            int i = face.ordinal();
            lastFlows[i] = s.displayFlow;
            lastDisplayPower[i] = s.displayPower;
        }

        step();

        init();

        for (Direction face : Direction.values()) {
            Section s = sections.get(face);
            if (s.internalPower > 0) {
                long totalPowerQuery = 0;
                for (Direction face2 : Direction.values()) {
                    if (face != face2) {
                        totalPowerQuery += sections.get(face2).powerQuery;
                    }
                }

                boolean returnPower = false;
                if (totalPowerQuery <= 0 && s.powerQuery > 0) {
                    totalPowerQuery = s.powerQuery;
                    returnPower = true;
                }

                if (totalPowerQuery > 0) {
                    long unusedPowerQuery = totalPowerQuery;
                    for (Direction face2 : Direction.values()) {
                        if (face == face2 && !returnPower) {
                            continue;
                        }
                        Section s2 = sections.get(face2);
                        if (s2.powerQuery > 0) {
                            long watts = Math.min(
                                BigInteger.valueOf(s.internalPower).multiply(BigInteger.valueOf(s2.powerQuery)).divide(
                                    BigInteger.valueOf(unusedPowerQuery)
                                ).longValue(), s.internalPower
                            );
                            unusedPowerQuery -= s2.powerQuery;
                            IPipe neighbour = pipe.getConnectedPipe(face2);
                            long leftover = watts;
                            if (neighbour != null && neighbour.getFlow() instanceof PipeFlowPower oFlow
                                && neighbour.isConnected(face2.getOpposite())) {
                                leftover = oFlow.sections.get(face2.getOpposite()).receivePowerInternal(watts);
                            } else {
                                IMjReceiver receiver = getReceiver(face2);
                                if (receiver != null && receiver.canReceive()) {
                                    leftover = receiver.receivePower(watts, false);
                                }
                            }
                            long used = watts - leftover;
                            s.internalPower -= used;
                            s2.debugPowerOutput += used;

                            s.powerAverage.push((int) used);
                            s2.powerAverage.push((int) used);

                            s.displayFlow = EnumFlow.OUT;
                            s2.displayFlow = EnumFlow.IN;
                        }
                    }
                }
            }
        }
        // Render compute goes here
        for (Section s : sections.values()) {
            s.powerAverage.tick();
            double value = s.powerAverage.getAverage() / maxPower;
            value = Math.sqrt(value);
            s.displayPower = (int) (value * MjAPI.MJ);
        }

        // Compute the tiles requesting power that are not power pipes
        for (Direction face : Direction.values()) {
            IMjReceiver recv;
            if (pipe.getConnectedType(face) == ConnectedType.TILE) {
                recv = getReceiver(face);
            } else {
                // 1.21: pluggables that take power from the pipe (robot stations, as in 7.1)
                PipePluggable plug = pipe.getHolder().getPluggable(face);
                recv = plug == null ? null : plug.getInternalCapability(MjAPI.CAP_RECEIVER);
            }
            if (recv != null && recv.canReceive()) {
                long requested = recv.getPowerRequested();
                if (requested > 0) {
                    requestPower(face, requested);
                }
            }
        }

        // Sum the amount of power requested on each side
        long[] transferQuery = new long[6];
        for (Direction face : Direction.values()) {
            if (!pipe.isConnected(face)) {
                continue;
            }
            long query = 0;
            for (Direction face2 : Direction.values()) {
                if (face != face2) {
                    query += sections.get(face2).powerQuery;
                }
            }
            transferQuery[face.ordinal()] = query;
        }

        // Transfer requested power to neighbouring pipes
        for (Direction face : Direction.values()) {
            if (disabled) {
                continue;
            }
            if (transferQuery[face.ordinal()] <= 0 || !pipe.isConnected(face)) {
                continue;
            }
            IPipe oPipe = pipe.getHolder().getNeighbourPipe(face);
            if (oPipe == null || !(oPipe.getFlow() instanceof PipeFlowPower oFlow)) {
                continue;
            }
            oFlow.requestPower(face.getOpposite(), transferQuery[face.ordinal()]);
        }
        // Networking
        boolean didChange = false;
        for (Direction face : Direction.values()) {
            Section s = sections.get(face);
            int i = face.ordinal();
            if (lastFlows[i] != s.displayFlow || lastDisplayPower[i] != s.displayPower) {
                didChange = true;
                break;
            }
        }

        // if (tracker.markTimeIfDelay(pipe.getHolder().getPipeWorld())) {
        if (didChange) {
            sendPayload(NET_POWER_AMOUNTS);
        }

    }

    private IMjReceiver getReceiver(Direction side) {
        IMjReceiver receiver = pipe.getHolder().getCapabilityFromPipe(side, MjAPI.CAP_RECEIVER);
        if (receiver == null && MjAPI.isRfAutoConversionEnabled()) {
            receiver = MjToRfAutoConvertor
                .createReceiver(pipe.getHolder().getCapabilityFromPipe(side, Capabilities.EnergyStorage.BLOCK));
        }
        return receiver;
    }

    private void step() {
        long now = pipe.getHolder().getPipeWorld().getGameTime();
        if (currentWorldTime != now) {
            currentWorldTime = now;
            sections.values().forEach(Section::step);
        }
    }

    private void init() {
        // TODO: use this for initialising the tile cache
    }

    private void requestPower(Direction from, long amount) {
        step();

        Section s = sections.get(from);
        if (pipe.getBehaviour() instanceof IPipeTransportPowerHook hook) {
            s.nextPowerQuery += hook.requestPower(from, amount);
        } else {
            s.nextPowerQuery += amount;
        }
        s.nextPowerQuery = Math.min(s.nextPowerQuery, maxPower);
    }

    public long getPowerRequested(@Nullable Direction side) {
        long req = 0;
        for (Direction face : Direction.values()) {
            if (side == null || face != side) {
                req += sections.get(face).powerQuery;
            }
        }
        return req;
    }

    public double getMaxTransferForRender(float partialTicks) {
        if (true) return maxPower / (double) MjAPI.MJ;
        double max = 0;
        for (Section s : sections.values()) {
            double value = s.displayPower / (double) MjAPI.MJ;
            // value = MathUtil.interp(partialTicks, value, value);
            max = Math.max(max, value);
        }
        return max;
    }

    public class Section implements IMjReceiver {
        public final Direction side;

        public final AverageInt clientDisplayAverage = new AverageInt(10);
        public double clientDisplayFlow, clientDisplayFlowLast;

        /** Range: 0 to {@link MjAPI#MJ} */
        public int displayPower;
        public EnumFlow displayFlow = EnumFlow.STATIONARY;
        public long nextPowerQuery;
        public long internalNextPower;
        public final AverageInt powerAverage = new AverageInt(1);

        long powerQuery;
        long internalPower;

        /** Debugging fields */
        long debugPowerInput, debugPowerOutput, debugPowerOffered;

        public Section(Direction side) {
            this.side = side;
            clientDisplayFlow = (side.getAxisDirection() == AxisDirection.POSITIVE ? 7 : 1) / 8.0;
        }

        void step() {
            powerQuery = nextPowerQuery;
            nextPowerQuery = 0;

            internalPower += internalNextPower;
            internalNextPower = 0;
        }

        @Override
        public boolean canConnect(IMjConnector other) {
            return true;
        }

        @Override
        public long getPowerRequested() {
            return PipeFlowPower.this.getPowerRequested(side);
        }

        long receivePowerInternal(long sent) {
            if (sent > 0) {
                PipeFlowPower.this.step();
                debugPowerOffered += sent;
                internalNextPower += sent;
                return 0;
            }
            return sent;
        }

        @Override
        public long receivePower(long microJoules, boolean simulate) {
            if (isReceiver) {
                if (!simulate) {
                    return this.receivePowerInternal(microJoules);
                }
                return 0;
            }
            return microJoules;
        }

        @Override
        public boolean canReceive() {
            return isReceiver;
        }
    }

    public enum EnumFlow {
        IN(-1),
        OUT(1),
        STATIONARY(0);

        public final int value;

        private EnumFlow(int value) {
            this.value = value;
        }
    }
}
