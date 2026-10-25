/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.tile;

import java.io.IOException;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.enums.EnumPowerStage;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.container.ContainerDynamoMJ;
import buildcraftreborn.lib.block.VanillaRotationHandlers;
import buildcraftreborn.lib.engine.IEngineLikeForLedger;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.collect.OrderedEnumMap;
import buildcraftreborn.lib.mj.MjBatteryReceiver;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.lib.tile.item.StackInsertionFunction;

/** The MJ dynamo: takes MJ on its back and sides and pushes Forge Energy (1.12's RF) out of its front, into whatever
 * is there (through a chain of up to 3 dynamos facing the same way). It looks and moves like an engine. Only craftable
 * when {@code enableMjDynamo} is on in the energy config. */
public class TileDynamoMJ extends TileBC_Neptune implements ITickable, IEngineLikeForLedger, IDebuggable,
    MenuProvider {
    public static final int MAX_RF = 10_000;
    public static final long MAX_MJ = 1000 * MjAPI.MJ;

    public static final double HEAT_RATE = 0.06;
    public static final double COOLDOWN_RATE = 0.01;

    public static final double MIN_HEAT = 20;
    public static final double IDEAL_HEAT = 100;
    public static final double MAX_HEAT = 250;

    private final MjBattery mjBattery;
    private final MjCapabilityHelper mjCaps;
    private final Rf rf = new Rf();
    int currentRF;
    public final ItemHandlerSimple invUpgrades;

    protected double heat = MIN_HEAT;
    /** Increments from 0 to 1. Above 0.5 all of the held power is emitted. */
    private float progress, lastProgress;
    private int progressPart = 0;

    protected EnumPowerStage powerStage = EnumPowerStage.BLUE;
    protected Direction currentDirection = Direction.UP;

    /** The FE made last tick (RF per tick, as 1.12). */
    public long currentOutput;
    public boolean isRedstonePowered = false;
    protected boolean isPumping = false;

    public TileDynamoMJ(BlockPos pos, BlockState state) {
        super(BCEnergyBlocks.MJ_DYNAMO_TILE.get(), pos, state);
        mjBattery = new MjBattery(MAX_MJ);
        mjCaps = new MjCapabilityHelper(new MjBatteryReceiver(mjBattery));
        invUpgrades = itemManager.addInvHandler("upgrades", 4, (slot, stack) -> TileEngineRF.getUpgradeMj(stack) > 0,
            StackInsertionFunction.getInsertionFunction(1), EnumAccess.NONE);
    }

    // Saving and syncing

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putString("currentDirection", getCurrentDirection().getSerializedName());
        nbt.putBoolean("isRedstonePowered", isRedstonePowered);
        nbt.putDouble("heat", heat);
        nbt.putFloat("progress", progress);
        nbt.putInt("progressPart", progressPart);
        nbt.putInt("currentRF", currentRF);
        nbt.put("mj", mjBattery.serializeNBT());
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        Direction dir = Direction.byName(nbt.getString("currentDirection"));
        currentDirection = dir == null ? Direction.UP : dir;
        isRedstonePowered = nbt.getBoolean("isRedstonePowered");
        heat = nbt.getDouble("heat");
        progress = nbt.getFloat("progress");
        progressPart = nbt.getInt("progressPart");
        currentRF = nbt.getInt("currentRF");
        mjBattery.deserializeNBT(nbt.getCompound("mj"));
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
                currentRF = buffer.readInt();
                mjBattery.readFromBuffer(buffer);
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                buffer.writeBoolean(isPumping);
                buffer.writeEnum(getCurrentDirection());
                buffer.writeEnum(powerStage);
                buffer.writeFloat(progress);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                buffer.writeFloat((float) heat);
                buffer.writeLong(currentOutput);
                buffer.writeInt(currentRF);
                mjBattery.writeToBuffer(buffer);
            }
        }
    }

    // Facing

    public InteractionResult attemptRotation() {
        OrderedEnumMap<Direction> possible = VanillaRotationHandlers.ROTATE_FACING;
        Direction current = currentDirection;
        for (int i = 0; i < 6; i++) {
            current = possible.next(current);
            if (getReceiverToPower(current) != null) {
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

    /** @return The number of additional dynamos that this one can send power through. */
    protected int getMaxChainLength() {
        return 3;
    }

    public void rotateIfInvalid() {
        if (currentDirection != null && getReceiverToPower(currentDirection) != null) {
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
            currentDirection = null; // Force rotateIfInvalid to always attempt to rotate
            rotateIfInvalid();
            isRedstonePowered = level.hasNeighborSignal(worldPosition);
        }
    }

    public Direction getCurrentDirection() {
        return currentDirection == null ? Direction.UP : currentDirection;
    }

    // Heat and power stage

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

    public double getHeatLevel() {
        return (heat - MIN_HEAT) / (MAX_HEAT - MIN_HEAT);
    }

    @Override
    public double getHeat() {
        return heat;
    }

    public double getPistonSpeed() {
        return switch (getPowerStage()) {
            case BLUE -> 0.04;
            case GREEN -> 0.05;
            case YELLOW -> 0.06;
            case RED -> 0.07;
            default -> 0;
        };
    }

    public void updateHeatLevel() {
        if (heat > MIN_HEAT) {
            heat -= COOLDOWN_RATE;
        }
        if (heat <= MIN_HEAT) {
            heat = MIN_HEAT;
        }
        getPowerStage();
    }

    @Override
    public void onNeighbourBlockChanged(Block block, BlockPos neighbour) {
        super.onNeighbourBlockChanged(block, neighbour);
        if (level != null) {
            isRedstonePowered = level.hasNeighborSignal(worldPosition);
        }
    }

    // Ticking

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
            return;
        }

        if (!isRedstonePowered) {
            if (currentRF > 0) {
                currentRF--;
            }
            if (currentRF < 0) {
                currentRF = 0;
            }
        }

        updateHeatLevel();
        getPowerStage();

        IEnergyStorage receiver = getReceiverToPower(currentDirection);
        if (progressPart != 0) {
            progress += (float) getPistonSpeed();
            if (progress > 0.5 && progressPart == 1) {
                progressPart = 2;
            } else if (progress >= 1) {
                progress = 0;
                progressPart = 0;
            }
        } else if (isRedstonePowered) {
            if (getPowerToExtract(receiver, false) > 0) {
                progressPart = 1;
                setPumping(true);
            } else {
                setPumping(false);
            }
        } else {
            setPumping(false);
        }

        if (isRedstonePowered) {
            sendPower(receiver);
        } else {
            currentOutput = 0;
        }

        if (!overheat) {
            burn();
        }

        markChunkDirty();
    }

    private int getPowerToExtract(@Nullable IEnergyStorage receiver, boolean doExtract) {
        if (receiver == null) {
            return 0;
        }
        return extractPower(0, receiver.getMaxEnergyStored() - receiver.getEnergyStored(), doExtract);
    }

    private void sendPower(@Nullable IEnergyStorage receiver) {
        if (receiver != null) {
            int extracted = getPowerToExtract(receiver, false);
            if (extracted > 0) {
                int received = receiver.receiveEnergy(extracted, false);
                extractPower(received, received, true);
            }
        }
    }

    protected final void setPumping(boolean isActive) {
        if (this.isPumping == isActive) {
            return;
        }
        this.isPumping = isActive;
        sendNetworkUpdate(NET_RENDER_DATA);
    }

    public void addPower(int rfAmount) {
        currentRF += rfAmount;
        if (currentRF > MAX_RF) {
            currentRF = MAX_RF;
        }
    }

    public int extractPower(int min, int max, boolean doExtract) {
        if (currentRF < min) {
            return 0;
        }
        int actualMax = Math.min(max, maxPowerExtracted());
        if (actualMax < min) {
            return 0;
        }
        int extracted;
        if (currentRF >= actualMax) {
            extracted = actualMax;
            if (doExtract) {
                currentRF -= actualMax;
            }
        } else {
            extracted = currentRF;
            if (doExtract) {
                currentRF = 0;
            }
        }
        return extracted;
    }

    /** Follows a chain of dynamos (all facing the same way) to the Forge Energy receiver at the end. */
    @Nullable
    public IEnergyStorage getReceiverToPower(@Nullable Direction side) {
        if (side == null || level == null) {
            return null;
        }
        TileDynamoMJ dynamo = this;
        BlockEntity next = null;
        BlockPos nextPos = worldPosition;

        for (int len = 0; len <= getMaxChainLength(); len++) {
            nextPos = dynamo.getBlockPos().relative(side);
            next = level.isLoaded(nextPos) ? level.getBlockEntity(nextPos) : null;
            if (next instanceof TileDynamoMJ nextDynamo) {
                if (side != nextDynamo.currentDirection) {
                    return null;
                }
                dynamo = nextDynamo;
            } else {
                break;
            }
        }

        if (next instanceof TileDynamoMJ) {
            return null;
        }

        // 1.12 required a tile entity here; 1.21 capabilities can come from any block.
        IEnergyStorage recv = level.getCapability(Capabilities.EnergyStorage.BLOCK, nextPos, side.getOpposite());
        if (recv != null && recv.canReceive()) {
            return recv;
        }
        return null;
    }

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side) {
        if (side != null && side == currentDirection) {
            if (capability == Capabilities.EnergyStorage.BLOCK) {
                return castRf(capability);
            }
            return super.getCapability(capability, side);
        }
        if (capability == Capabilities.EnergyStorage.BLOCK) {
            // (it takes MJ only: FE in on its other sides would loop back out of its front)
            return super.getCapability(capability, side);
        }
        T value = mjCaps.getCapability(capability);
        return value != null ? value : super.getCapability(capability, side);
    }

    @SuppressWarnings("unchecked")
    private <T> T castRf(BlockCapability<T, @Nullable Direction> capability) {
        return (T) rf;
    }

    public int maxPowerExtracted() {
        return MAX_RF / 10;
    }

    // Upgrades and conversion

    public long getMjPerTick() {
        return TileEngineRF.getMjPerTick(invUpgrades);
    }

    public int getRfGenerationRate() {
        return (int) (getMjPerTick() / MjAPI.getRfConversion().mjPerRf);
    }

    protected void burn() {
        long mjStored = mjBattery.getStored();
        if (mjStored <= 0) {
            return;
        }

        if (isRedstonePowered) {
            long mjPerRf = MjAPI.getRfConversion().mjPerRf;
            int genRf = getRfGenerationRate();

            int maxRf = (int) Math.min(genRf, mjStored / mjPerRf);
            if (maxRf <= 0) {
                return;
            }
            if (currentRF + maxRf >= MAX_RF) {
                return;
            }
            if (mjBattery.extractPower(maxRf * mjPerRf)) {
                currentOutput = maxRf;
                addPower(maxRf);
                heat += HEAT_RATE;
                if (heat >= 200) {
                    heat = 200;
                }
            }
        }
    }

    /** @return The FE per tick this makes while it has MJ to convert. */
    public int getCurrentOutput() {
        return currentRF > 0 ? getRfGenerationRate() : 0;
    }

    public int getCurrentRF() {
        return currentRF;
    }

    // IEngineLikeForLedger

    @Override
    public boolean isEngineOn() {
        return isPumping;
    }

    @Override
    public long getCurrentMjOutput() {
        return currentRF > 0 ? getMjPerTick() : 0;
    }

    @Override
    public long getMjStored() {
        return mjBattery.getStored();
    }

    // Interaction and GUI

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        openGui(player);
        return true;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerDynamoMJ(windowId, player, this);
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

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("facing = " + currentDirection);
        left.add("heat = " + LocaleUtil.localizeHeat(heat).getString());
        left.add("mj = " + LocaleUtil.localizeMj(mjBattery.getStored()).getString());
        left.add("rf = " + currentRF + " / " + MAX_RF);
        left.add("stage = " + powerStage);
        left.add("progress = " + progress);
    }

    /** The Forge Energy output on the front: other blocks may pull from it, nothing can push into it. */
    private final class Rf implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            maxExtract = Math.min(maxExtract, currentRF);
            if (maxExtract <= 0) {
                return 0;
            }
            if (!simulate) {
                currentRF -= maxExtract;
                setChanged();
            }
            return maxExtract;
        }

        @Override
        public int getEnergyStored() {
            return currentRF;
        }

        @Override
        public int getMaxEnergyStored() {
            return MAX_RF;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
