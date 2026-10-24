/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.tile;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.core.IFluidHandlerAdv;
import buildcraftreborn.api.fuels.BuildcraftFuelRegistry;
import buildcraftreborn.api.fuels.IFuel;
import buildcraftreborn.api.fuels.IFuelManager.IDirtyFuel;
import buildcraftreborn.api.fuels.ISolidCoolant;
import buildcraftreborn.api.mj.IMjConnector;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.tools.IToolWrench;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.container.ContainerEngineIron_BC8;
import buildcraftreborn.lib.engine.EngineConnector;
import buildcraftreborn.lib.engine.TileEngineBase_BC8;
import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.gui.help.ElementHelpInfo;
import buildcraftreborn.lib.net.PacketBufferBC;

/** The combustion engine: burns liquid fuel, needs coolant to stay out of the red, and collects residue from dirty
 * fuels (oil, heavy oil, dense oil). */
public class TileEngineIron_BC8 extends TileEngineBase_BC8 implements MenuProvider {
    public static final int MAX_FLUID = 10_000;
    public static final double COOLDOWN_RATE = 0.05;
    public static final int MAX_COOLANT_PER_TICK = 40;

    public final Tank tankFuel = new Tank("fuel", MAX_FLUID, this, this::isValidFuel);
    public final Tank tankCoolant = new Tank("coolant", MAX_FLUID, this, this::isValidCoolant) {
        @Override
        @Nullable
        protected FluidGetResult map(ItemStack stack, int space) {
            ISolidCoolant coolant = BuildcraftFuelRegistry.coolant.getSolidCoolant(stack);
            if (coolant == null) {
                return super.map(stack, space);
            }
            FluidStack fluidCoolant = coolant.getFluidFromSolidCoolant(stack);
            if (fluidCoolant == null || fluidCoolant.isEmpty() || fluidCoolant.getAmount() > space) {
                return super.map(stack, space);
            }
            return new FluidGetResult(ItemStack.EMPTY, fluidCoolant);
        }
    };
    public final Tank tankResidue = new Tank("residue", MAX_FLUID, this, this::isResidue);
    private final IFluidHandlerAdv fluidHandler = new InternalFluidHandler();

    private int penaltyCooling = 0;
    private boolean lastPowered = false;
    private double burnTime;
    private double residueAmount = 0;
    @Nullable
    private IFuel currentFuel;

    public TileEngineIron_BC8(BlockPos pos, BlockState state) {
        super(BCEnergyBlocks.ENGINE_IRON_TILE.get(), pos, state);
        tankManager.addAll(tankFuel, tankCoolant, tankResidue);

        // 1.12's lang file names these titles "tankFuel" and so on (not after the tank's name)
        tankFuel.helpInfo = new ElementHelpInfo("buildcraft.help.tank.title.tankFuel", 0xFF_FF_33_33,
            Tank.DEFAULT_HELP_KEY, null, "buildcraft.help.tank.fuel");
        tankCoolant.helpInfo = new ElementHelpInfo("buildcraft.help.tank.title.tankCoolant", 0xFF_55_55_FF,
            Tank.DEFAULT_HELP_KEY, null, "buildcraft.help.tank.coolant");
        tankResidue.helpInfo = new ElementHelpInfo("buildcraft.help.tank.title.tankResidue", 0xFF_AA_33_AA,
            Tank.DEFAULT_HELP_KEY, null, "buildcraft.help.tank.residue");

        caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, fluidHandler, EnumPipePart.VALUES);
    }

    // TileEntity overrides

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putInt("penaltyCooling", penaltyCooling);
        nbt.putDouble("burnTime", burnTime);
        nbt.putDouble("residueAmount", residueAmount);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        penaltyCooling = nbt.getInt("penaltyCooling");
        burnTime = nbt.getDouble("burnTime");
        residueAmount = Math.max(0, nbt.getDouble("residueAmount"));
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.readData(buffer);
            }
        }
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.writeData(buffer);
            }
        }
    }

    // TileEngineBase overrides

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        ItemStack current = held.copy();
        if (super.onActivated(player, hand, held)) {
            return true;
        }
        if (!current.isEmpty()) {
            if (player.getMainHandItem().getItem() instanceof IToolWrench
                || player.getOffhandItem().getItem() instanceof IToolWrench) {
                // Let the wrench rotate the engine
                return false;
            }
        }
        openGui(player);
        return true;
    }

    @Override
    public double getPistonSpeed() {
        switch (getPowerStage()) {
            case BLUE:
                return 0.04;
            case GREEN:
                return 0.05;
            case YELLOW:
                return 0.06;
            case RED:
                return 0.07;
            default:
                return 0;
        }
    }

    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(false);
    }

    @Override
    public boolean isBurning() {
        FluidStack fuel = tankFuel.getFluid();
        return !fuel.isEmpty() && penaltyCooling == 0 && isRedstonePowered;
    }

    @Override
    protected void burn() {
        final FluidStack fuel = this.tankFuel.getFluid();
        if (currentFuel == null || !FluidStack.isSameFluidSameComponents(currentFuel.getFluid(), fuel)) {
            currentFuel = BuildcraftFuelRegistry.fuel.getFuel(fuel);
        }

        if (fuel.isEmpty() || currentFuel == null) {
            return;
        }

        if (penaltyCooling <= 0) {
            if (isRedstonePowered) {
                lastPowered = true;

                if (burnTime > 0 || fuel.getAmount() > 0) {
                    if (burnTime > 0) {
                        burnTime--;
                    }
                    if (burnTime <= 0) {
                        if (fuel.getAmount() > 0) {
                            tankFuel.drainInternal(1, net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                            burnTime += currentFuel.getTotalBurningTime() / 1000.0;

                            // If we also produce residue then put it out too
                            if (currentFuel instanceof IDirtyFuel dirtyFuel) {
                                FluidStack residueFluid = dirtyFuel.getResidue().copy();
                                residueAmount += residueFluid.getAmount() / 1000.0;
                                if (residueAmount >= 1) {
                                    residueFluid.setAmount(Mth.floor(residueAmount));
                                    residueAmount -= tankResidue.fillInternal(residueFluid,
                                        net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                                }
                            }
                        } else {
                            tankFuel.setFluid(FluidStack.EMPTY);
                            currentFuel = null;
                            currentOutput = 0;
                            return;
                        }
                    }
                    currentOutput = currentFuel.getPowerPerCycle(); // Comment out for constant power
                    addPower(currentFuel.getPowerPerCycle());
                    heat += currentFuel.getPowerPerCycle() * HEAT_PER_MJ / MjAPI.MJ;// * getBiomeTempScalar();
                }
            } else if (lastPowered) {
                lastPowered = false;
                penaltyCooling = 10;
                // 10 tick of penalty on top of the cooling
            }
        }

        if (burnTime <= 0 && tankFuel.getFluidAmount() <= 0) {
            tankFuel.setFluid(FluidStack.EMPTY);
        }
    }

    @Override
    public void updateHeatLevel() {
        double target;
        if (heat > MIN_HEAT && (penaltyCooling > 0 || !isRedstonePowered)) {
            heat -= COOLDOWN_RATE;
            target = MIN_HEAT;
        } else if (heat > IDEAL_HEAT) {
            target = IDEAL_HEAT;
        } else {
            target = heat;
        }

        if (target != heat) {
            // coolEngine(target)
            double coolingBuffer = 0;
            double extraHeat = heat - target;

            if (extraHeat > 0) {
                // fillCoolingBuffer();
                if (tankCoolant.getFluidAmount() > 0) {
                    float coolPerMb = BuildcraftFuelRegistry.coolant.getDegreesPerMb(tankCoolant.getFluid(),
                        (float) heat);
                    if (coolPerMb > 0) {
                        int coolantAmount = Math.min(MAX_COOLANT_PER_TICK, tankCoolant.getFluidAmount());
                        float cooling = coolPerMb;
                        // cooling /= getBiomeTempScalar();
                        coolingBuffer += coolantAmount * cooling;
                        tankCoolant.drainInternal(coolantAmount,
                            net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.EXECUTE);
                    }
                }
            }

            heat -= coolingBuffer;
            getPowerStage();
        }

        if (heat <= MIN_HEAT && penaltyCooling > 0) {
            penaltyCooling--;
        }

        if (heat <= MIN_HEAT) {
            heat = MIN_HEAT;
        }
    }

    @Override
    public boolean isActive() {
        return penaltyCooling <= 0;
    }

    @Override
    public long getMaxPower() {
        return 10_000 * MjAPI.MJ;
    }

    @Override
    public long maxPowerReceived() {
        return 2_000 * MjAPI.MJ;
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
    protected long getLedgerOutput() {
        return currentOutput;
    }

    @Override
    public long getCurrentOutput() {
        if (currentFuel == null) {
            return 0;
        } else {
            return currentFuel.getPowerPerCycle();
        }
    }

    // Fluid related

    private boolean isValidFuel(FluidStack fluid) {
        return BuildcraftFuelRegistry.fuel.getFuel(fluid) != null;
    }

    private boolean isValidCoolant(FluidStack fluid) {
        return BuildcraftFuelRegistry.coolant.getCoolant(fluid) != null;
    }

    private boolean isResidue(FluidStack fluid) {
        // If this is the client then we don't have a current fuel- just trust the server that its correct
        if (level != null && level.isClientSide) {
            return true;
        }
        if (currentFuel instanceof IDirtyFuel dirty) {
            return FluidStack.isSameFluidSameComponents(fluid, dirty.getResidue());
        }
        return false;
    }

    // GUI

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerEngineIron_BC8(windowId, player, this);
    }

    /** What pipes and other machines see: fuel and coolant can be filled, residue can be drained. */
    private class InternalFluidHandler implements IFluidHandlerAdv {
        private Tank tank(int index) {
            return index == 0 ? tankFuel : index == 1 ? tankCoolant : tankResidue;
        }

        @Override
        public int getTanks() {
            return 3;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank(tank).getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank(tank).getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank != 2 && tank(tank).isFluidValid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            int filled = tankFuel.fill(resource, action);
            if (filled == 0) {
                filled = tankCoolant.fill(resource, action);
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return tankResidue.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return tankResidue.drain(maxDrain, action);
        }

        @Override
        public FluidStack drain(IFluidFilter filter, int maxDrain, FluidAction action) {
            return tankResidue.drain(filter, maxDrain, action);
        }
    }
}
