/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.tile;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.mj.IMjConnector;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.container.ContainerEngineRF;
import buildcraftreborn.lib.engine.EngineConnector;
import buildcraftreborn.lib.engine.TileEngineBase_BC8;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;
import buildcraftreborn.lib.tile.item.StackInsertionFunction;

/** The RF engine: takes Forge Energy (1.12's RF) from any side and turns it into MJ. Iron and gold gears in its
 * upgrade slots raise its rate. Only craftable when {@code enableRfEngine} is on in the energy config. */
public class TileEngineRF extends TileEngineBase_BC8 implements MenuProvider {
    public static final int MAX_RF = 10_000;
    public static final double HEAT_RATE = 0.06;
    public static final double COOLDOWN_RATE = 0.01;

    /** The upgrade items and the MJ per tick each one adds (1.12's {@code RF_UPGRADE}). */
    public static final Map<Supplier<? extends Item>, Long> RF_UPGRADE = new LinkedHashMap<>();

    static {
        RF_UPGRADE.put(BCCoreItems.GEAR_IRON, MjAPI.MJ * 2);
        RF_UPGRADE.put(BCCoreItems.GEAR_GOLD, MjAPI.MJ * 3);
    }

    int currentRF;
    public final ItemHandlerSimple invUpgrades;

    public TileEngineRF(BlockPos pos, BlockState state) {
        super(BCEnergyBlocks.ENGINE_RF_TILE.get(), pos, state);
        caps.addCapabilityInstance(Capabilities.EnergyStorage.BLOCK, new Rf(), EnumPipePart.VALUES);
        invUpgrades = itemManager.addInvHandler("upgrades", 4, (slot, stack) -> getUpgradeMj(stack) > 0,
            StackInsertionFunction.getInsertionFunction(1), EnumAccess.NONE);
    }

    /** @return The MJ per tick that the given upgrade adds, or 0 if it isn't an upgrade. */
    public static long getUpgradeMj(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        for (Map.Entry<Supplier<? extends Item>, Long> entry : RF_UPGRADE.entrySet()) {
            if (stack.is(entry.getKey().get())) {
                return entry.getValue();
            }
        }
        return 0;
    }

    /** @return The MJ per tick for the given upgrade slots (4 MJ, plus the upgrades). */
    public static long getMjPerTick(ItemHandlerSimple upgrades) {
        long value = MjAPI.MJ * 4;
        for (int slot = 0; slot < upgrades.getSlots(); slot++) {
            value += getUpgradeMj(upgrades.getStackInSlot(slot));
        }
        return value;
    }

    // TileEntity overrides

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putInt("currentRF", currentRF);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        currentRF = nbt.getInt("currentRF");
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                currentRF = buffer.readInt();
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                buffer.writeInt(currentRF);
            }
        }
    }

    // TileEngineBase overrides

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        openGui(player);
        return true;
    }

    @Override
    public double getPistonSpeed() {
        return switch (getPowerStage()) {
            case BLUE -> 0.04;
            case GREEN -> 0.05;
            case YELLOW -> 0.06;
            case RED -> 0.07;
            default -> 0;
        };
    }

    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        return currentRF > 0 && isRedstonePowered;
    }

    public long getMjPerTick() {
        return getMjPerTick(invUpgrades);
    }

    public int getRfConsumptionRate() {
        return (int) (getMjPerTick() / MjAPI.getRfConversion().mjPerRf);
    }

    @Override
    protected void burn() {
        if (currentRF <= 0) {
            return;
        }

        if (isRedstonePowered) {
            long mjPerRf = MjAPI.getRfConversion().mjPerRf;
            int maxRf = getRfConsumptionRate();

            int rfConsumed = Math.min(currentRF, maxRf);
            long mjGenerated = rfConsumed * mjPerRf;

            if (power + mjGenerated >= getMaxPower()) {
                return;
            }

            currentOutput = mjGenerated;
            addPower(mjGenerated);
            currentRF -= rfConsumed;
            heat += HEAT_RATE;
            if (heat >= 200) {
                heat = 200;
            }
        }
    }

    @Override
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
    public long getMaxPower() {
        return 1000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerReceived() {
        return 200 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 500 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 4;
    }

    @Override
    protected int getMaxChainLength() {
        return 4;
    }

    @Override
    public long getCurrentOutput() {
        return currentRF > 0 ? getMjPerTick() : 0;
    }

    public int getCurrentRF() {
        return currentRF;
    }

    // GUI

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerEngineRF(windowId, player, this);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        super.getDebugInfo(left, right, side);
        left.add("rf = " + currentRF + " / " + MAX_RF);
    }

    /** The Forge Energy input: accepts energy on every side, never gives any back. */
    private final class Rf implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int max = Math.min(MAX_RF - currentRF, maxReceive);
            if (max <= 0) {
                return 0;
            }
            if (!simulate) {
                currentRF += max;
                setChanged();
            }
            return max;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
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
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
