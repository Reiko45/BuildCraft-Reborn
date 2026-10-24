/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.engine;

import java.io.IOException;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.enums.EnumPowerStage;
import buildcraftreborn.api.mj.IMjConnector;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.IMjRedstoneReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.mj.MjToRfAutoConvertor;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.lib.block.VanillaRotationHandlers;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.collect.OrderedEnumMap;
import buildcraftreborn.lib.misc.data.ModelVariableData;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** Base for every engine: heat, power stages, the piston animation and sending power to the block in front.
 * <p>
 * 1.21 notes: an engine only exposes its MJ capabilities on its output side, so turning it invalidates the cached
 * capabilities at its position ({@code Level#invalidateCapabilities}). Neighbours are found through the level's
 * capability lookup instead of {@code TileEntity#getCapability}. */
public abstract class TileEngineBase_BC8 extends TileBC_Neptune implements ITickable, IDebuggable, IEngineLikeForLedger {

    /** Heat per {@link MjAPI#MJ}. */
    public static final double HEAT_PER_MJ = 0.0023;

    public static final double MIN_HEAT = 20;
    public static final double IDEAL_HEAT = 100;
    public static final double MAX_HEAT = 250;

    public final IMjConnector mjConnector = createConnector();
    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(mjConnector);

    protected double heat = MIN_HEAT;
    protected long power = 0;
    private long lastPower = 0;
    /** Increments from 0 to 1. Above 0.5 all of the held power is emitted. */
    private float progress, lastProgress;
    private int progressPart = 0;

    protected EnumPowerStage powerStage = EnumPowerStage.BLUE;
    @Nullable
    protected Direction currentDirection = Direction.UP;

    public long currentOutput;
    public boolean isRedstonePowered = false;
    protected boolean isPumping = false;

    /** The model variables, used to keep track of the various state-based variables. */
    public final ModelVariableData clientModelData = new ModelVariableData();

    public TileEngineBase_BC8(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        currentDirection = nbt.contains("currentDirection") ? Direction.byName(nbt.getString("currentDirection"))
            : null;
        if (currentDirection == null) {
            currentDirection = Direction.UP;
        }
        isRedstonePowered = nbt.getBoolean("isRedstonePowered");
        heat = nbt.getDouble("heat");
        power = nbt.getLong("power");
        progress = nbt.getFloat("progress");
        progressPart = nbt.getInt("progressPart");
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putString("currentDirection", getCurrentFacing().getSerializedName());
        nbt.putBoolean("isRedstonePowered", isRedstonePowered);
        nbt.putDouble("heat", heat);
        nbt.putLong("power", power);
        nbt.putFloat("progress", progress);
        nbt.putInt("progressPart", progressPart);
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                isPumping = buffer.readBoolean();
                Direction before = currentDirection;
                currentDirection = buffer.readEnum(Direction.class);
                powerStage = buffer.readEnum(EnumPowerStage.class);
                progress = buffer.readFloat();
                if (before != currentDirection && level != null) {
                    level.invalidateCapabilities(worldPosition);
                }
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                heat = buffer.readFloat();
                currentOutput = buffer.readLong();
                power = buffer.readLong();
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(isPumping);
                buffer.writeEnum(getCurrentFacing());
                buffer.writeEnum(powerStage);
                buffer.writeFloat(progress);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                buffer.writeFloat((float) heat);
                buffer.writeLong(currentOutput);
                buffer.writeLong(power);
            }
        }
    }

    public InteractionResult attemptRotation() {
        OrderedEnumMap<Direction> possible = VanillaRotationHandlers.ROTATE_FACING;
        Direction current = currentDirection;
        for (int i = 0; i < 6; i++) {
            current = possible.next(current);
            if (isFacingReceiver(current)) {
                if (currentDirection != current) {
                    currentDirection = current;
                    onFacingChanged();
                    return InteractionResult.SUCCESS;
                }
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.FAIL;
    }

    private void onFacingChanged() {
        if (level == null) {
            return;
        }
        level.invalidateCapabilities(worldPosition);
        if (!level.isClientSide) {
            sendNetworkUpdate(NET_RENDER_DATA);
            redrawBlock();
            level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
            setChanged();
        }
    }

    private boolean isFacingReceiver(Direction dir) {
        return getReceiverToPower(dir) != null;
    }

    protected final boolean canChain() {
        return getMaxChainLength() > 0;
    }

    /** @return The number of additional engines that this engine can send power through. */
    protected int getMaxChainLength() {
        return 2;
    }

    public void rotateIfInvalid() {
        if (currentDirection != null && isFacingReceiver(currentDirection)) {
            return;
        }
        attemptRotation();
        if (currentDirection == null) {
            currentDirection = Direction.UP;
            onFacingChanged();
        }
    }

    @Override
    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (level != null && !level.isClientSide) {
            currentDirection = null;// Force rotateIfInvalid to always attempt to rotate
            rotateIfInvalid();
            isRedstonePowered = level.hasNeighborSignal(worldPosition);
        }
    }

    /** @return The heat of the current biome, in celsius. */
    protected float getBiomeHeat() {
        if (level == null) {
            return 0;
        }
        float temp = level.getBiome(worldPosition).value().getBaseTemperature();
        return Math.max(0, Math.min(30, temp * 15f));
    }

    public double getPowerLevel() {
        return power / (double) getMaxPower();
    }

    protected EnumPowerStage computePowerStage() {
        double heatLevel = getHeatLevel();
        if (heatLevel < 0.25f) return EnumPowerStage.BLUE;
        else if (heatLevel < 0.5f) return EnumPowerStage.GREEN;
        else if (heatLevel < 0.75f) return EnumPowerStage.YELLOW;
        else if (heatLevel < 0.85f) return EnumPowerStage.RED;
        else return EnumPowerStage.OVERHEAT;
    }

    @Override
    public final EnumPowerStage getPowerStage() {
        if (level != null && !level.isClientSide) {
            EnumPowerStage newStage = computePowerStage();

            if (powerStage != newStage) {
                powerStage = newStage;
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        }

        return powerStage;
    }

    public void updateHeatLevel() {
        heat = ((MAX_HEAT - MIN_HEAT) * getPowerLevel()) + MIN_HEAT;
    }

    public double getHeatLevel() {
        return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
    }

    public double getIdealHeatLevel() {
        return heat / IDEAL_HEAT;
    }

    @Override
    public double getHeat() {
        return heat;
    }

    public double getPistonSpeed() {
        switch (getPowerStage()) {
            case BLUE:
                return 0.02;
            case GREEN:
                return 0.04;
            case YELLOW:
                return 0.08;
            case RED:
                return 0.12;
            default:
                return 0;
        }
    }

    protected abstract IMjConnector createConnector();

    @Override
    public void onNeighbourBlockChanged(Block block, BlockPos neighbour) {
        super.onNeighbourBlockChanged(block, neighbour);
        if (level != null) {
            isRedstonePowered = level.hasNeighborSignal(worldPosition);
        }
    }

    @Override
    public void update() {
        deltaManager.tick();
        if (cannotUpdate()) return;

        boolean overheat = getPowerStage() == EnumPowerStage.OVERHEAT;

        if (level.isClientSide) {
            lastProgress = progress;

            if (isPumping) {
                progress += (float) getPistonSpeed();

                if (progress >= 1) {
                    progress = 0;
                }
            } else if (progress > 0) {
                progress -= 0.01f;
            }
            clientModelData.tick();
            return;
        }

        lastPower = 0;

        if (!isRedstonePowered) {
            if (power > MjAPI.MJ) {
                power -= MjAPI.MJ;
            } else if (power > 0) {
                power = 0;
            }
        }

        updateHeatLevel();
        getPowerStage();
        engineUpdate();

        IMjReceiver receiver = getReceiverToPower(currentDirection);
        boolean pulsedPower = receiver instanceof IMjRedstoneReceiver;

        if (progressPart != 0) {
            progress += (float) getPistonSpeed();

            if (progress > 0.5 && progressPart == 1) {
                progressPart = 2;
                if (pulsedPower) {
                    sendPower(receiver);
                }
            } else if (progress >= 1) {
                progress = 0;
                progressPart = 0;
            }
        } else if (isRedstonePowered && isActive()) {
            if (getPowerToExtract(false) > 0) {
                progressPart = 1;
                setPumping(true);
            } else {
                setPumping(false);
            }
        } else {
            setPumping(false);
        }

        // Comment for constant power
        if (!pulsedPower) {
            if (isRedstonePowered && isActive()) {
                sendPower(receiver);
            } else {
                currentOutput = 0;
            }
        }

        if (!overheat) {
            burn();
        }

        markChunkDirty();
    }

    private long getPowerToExtract(boolean doExtract) {
        IMjReceiver receiver = getReceiverToPower(currentDirection);
        if (receiver == null) {
            return 0;
        }

        // Pulsed power
        return extractPower(0, receiver.getPowerRequested(), doExtract);
    }

    private void sendPower(@Nullable IMjReceiver receiver) {
        if (receiver != null) {
            long extracted = getPowerToExtract(false);
            if (extracted > 0) {
                long excess = receiver.receivePower(extracted, false);
                extractPower(extracted - excess, extracted - excess, true); // Comment out for constant power
            }
        }
    }

    protected void burn() {}

    protected void engineUpdate() {
        if (!isRedstonePowered) {
            if (power >= 1) {
                power -= 1;
            } else if (power < 1) {
                power = 0;
            }
        }
    }

    public boolean isActive() {
        return true;
    }

    protected final void setPumping(boolean isActive) {
        if (this.isPumping == isActive) {
            return;
        }

        this.isPumping = isActive;
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    /* STATE INFORMATION */
    public abstract boolean isBurning();

    public void addPower(long microJoules) {
        power += microJoules;
        lastPower += microJoules;

        if (power > getMaxPower()) {
            power = getMaxPower();
        }
    }

    public long extractPower(long min, long max, boolean doExtract) {
        if (power < min) {
            return 0;
        }

        long actualMax = Math.min(max, maxPowerExtracted());

        if (actualMax < min) {
            return 0;
        }

        long extracted;

        if (power >= actualMax) {
            extracted = actualMax;

            if (doExtract) {
                power -= actualMax;
            }
        } else {
            extracted = power;

            if (doExtract) {
                power = 0;
            }
        }

        return extracted;
    }

    public final boolean isPoweredTile(@Nullable BlockEntity tile, Direction side) {
        if (tile == null) return false;
        if (tile.getClass() == getClass()) {
            TileEngineBase_BC8 other = (TileEngineBase_BC8) tile;
            return other.currentDirection == currentDirection;
        }
        return getReceiverToPower(tile.getBlockPos(), side) != null;
    }

    /** Finds an MJ receiver (or, with RF auto-conversion, a Forge Energy storage) at the given position, accessed
     * from the side facing this engine. */
    @Nullable
    protected IMjReceiver getReceiverToPower(BlockPos pos, Direction side) {
        if (level == null) return null;
        IMjReceiver rec = level.getCapability(MjAPI.CAP_RECEIVER, pos, side.getOpposite());
        if (rec != null && rec.canConnect(mjConnector) && mjConnector.canConnect(rec)) {
            return rec;
        } else if (couldPowerRf()) {
            IEnergyStorage rf = level.getCapability(Capabilities.EnergyStorage.BLOCK, pos, side.getOpposite());
            return MjToRfAutoConvertor.createReceiver(rf);
        } else {
            return null;
        }
    }

    /** @return True if this engine is allowed to autoconvert output MJ to RF. By default this checks
     *         {@link MjAPI#isRfAutoConversionEnabled()} */
    protected boolean couldPowerRf() {
        return MjAPI.isRfAutoConversionEnabled();
    }

    /** Follows a chain of engines of the same type (all facing the same way) to the receiver at the end. */
    @Nullable
    public IMjReceiver getReceiverToPower(@Nullable Direction side) {
        if (side == null || level == null) {
            return null;
        }
        TileEngineBase_BC8 engine = this;
        BlockEntity next = null;
        BlockPos nextPos = worldPosition;

        for (int len = 0; len <= getMaxChainLength(); len++) {
            nextPos = engine.getBlockPos().relative(side);
            next = level.isLoaded(nextPos) ? level.getBlockEntity(nextPos) : null;

            if (next == null) {
                break;
            }

            if (next.getClass() == getClass()) {
                if (side != ((TileEngineBase_BC8) next).currentDirection) {
                    return null;
                }
            }

            if (next instanceof TileEngineBase_BC8 nextEngine) {
                if (next.getClass() != getClass()) {
                    return null;
                }
                engine = nextEngine;
            } else {
                break;
            }
        }

        if (next instanceof TileEngineBase_BC8) {
            return null;
        }

        // 1.12 required a tile entity here. 1.21 capabilities can come from blocks without one, so look regardless.
        return getReceiverToPower(nextPos, side);
    }

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        if (side != null && side == currentDirection) {
            T value = mjCaps.getCapability(capability);
            if (value != null) {
                return value;
            }
        }
        return super.getCapability(capability, side);
    }

    public abstract long getMaxPower();

    public long minPowerReceived() {
        return 2 * MjAPI.MJ;
    }

    public abstract long maxPowerReceived();

    public abstract long maxPowerExtracted();

    public abstract float explosionRange();

    public long getEnergyStored() {
        return power;
    }

    public abstract long getCurrentOutput();

    @Override
    public boolean isEngineOn() {
        return isPumping;
    }

    // IEngineLikeForLedger

    @Override
    public final long getCurrentMjOutput() {
        return getLedgerOutput();
    }

    /** What the power ledger shows as the current output. In 1.12 the ledger called {@link #getCurrentOutput()} on the
     * client, which for the fuel engines depends on state only the server has (the fuel, the stirling engine's
     * controller), so their ledgers showed 0 or a made-up number. Those engines return the synced
     * {@link #currentOutput} instead. */
    protected long getLedgerOutput() {
        return getCurrentOutput();
    }

    @Override
    public final long getMjStored() {
        return getEnergyStored();
    }

    @OnlyIn(Dist.CLIENT)
    public float getProgressClient(float partialTicks) {
        float last = lastProgress;
        float now = progress;
        if (last > 0.5 && now < 0.5) {
            // we just returned
            now += 1;
        }
        float interp = last * (1 - partialTicks) + now * partialTicks;
        return interp % 1;
    }

    public Direction getCurrentFacing() {
        return currentDirection == null ? Direction.UP : currentDirection;
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("facing = " + currentDirection);
        left.add("heat = " + LocaleUtil.localizeHeat(heat).getString() + " -- "
            + String.format("%.2f %%", getHeatLevel()));
        left.add("power = " + LocaleUtil.localizeMj(power).getString());
        left.add("stage = " + powerStage);
        left.add("progress = " + progress);
        left.add("last = " + LocaleUtil.localizeMjFlow(lastPower).getString());
    }

    @Override
    public void getClientDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("Current Model Variables:");
        clientModelData.addDebugInfo(left);
    }
}
