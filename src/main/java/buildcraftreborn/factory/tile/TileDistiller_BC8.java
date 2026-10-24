/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.factory.tile;

import java.io.IOException;
import java.util.List;

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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.SafeTimeTracker;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.recipes.BuildcraftRecipeRegistry;
import buildcraftreborn.api.recipes.IRefineryRecipeManager;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.api.tiles.IHasWork;
import buildcraftreborn.api.tiles.TilesAPI;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.container.ContainerDistiller;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.fluid.FluidSmoother;
import buildcraftreborn.lib.fluid.FluidSmoother.IFluidDataSender;
import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.gui.help.ElementHelpInfo;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.data.AverageLong;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.mj.MjBatteryReceiver;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** Splits a fluid into a gas (out of the top) and a liquid (out of the bottom), using MJ. Recipes come from
 * {@link IRefineryRecipeManager#getDistillationRegistry()}.
 * <p>
 * 1.12 drew the two moving "power" blocks with a JSON variable model ({@code models/tiles/distiller.json}); the same
 * animation state is kept here ({@link #modelState}) and drawn by {@code RenderDistiller}. */
public class TileDistiller_BC8 extends TileBC_Neptune implements ITickable, IDebuggable, MenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("Distiller");
    public static final int NET_TANK_IN = IDS.allocId("TANK_IN");
    public static final int NET_TANK_GAS_OUT = IDS.allocId("TANK_GAS_OUT");
    public static final int NET_TANK_LIQUID_OUT = IDS.allocId("TANK_LIQUID_OUT");

    public static final long MAX_MJ_PER_TICK = 6 * MjAPI.MJ;

    public final Tank tankIn = new Tank("in", 4 * 1000, this, this::isDistillableFluid);
    public final Tank tankGasOut = new Tank("gasOut", 4 * 1000, this);
    public final Tank tankLiquidOut = new Tank("liquidOut", 4 * 1000, this);

    private final MjBattery mjBattery = new MjBattery(1024 * MjAPI.MJ);

    public final FluidSmoother smoothedTankIn;
    public final FluidSmoother smoothedTankGasOut;
    public final FluidSmoother smoothedTankLiquidOut;

    @Nullable
    private IDistillationRecipe currentRecipe;
    private long distillPower = 0;
    private boolean hasWork, isActive = false;
    private final AverageLong powerAvg = new AverageLong(100);
    private final SafeTimeTracker updateTracker = new SafeTimeTracker(BCLibConfig.networkUpdateRate(), 2);
    private boolean changedSinceNetUpdate = true;

    private long powerAvgClient;

    /** Client side: the animation state of the power blocks (1.12's {@code state} model variable). Starts at 0 when
     * off, goes up to 1.5 then resets back down to 0.5 while active. */
    public double modelState, modelStateLast;

    public TileDistiller_BC8(BlockPos pos, BlockState state) {
        super(BCFactoryBlocks.DISTILLER_TILE.get(), pos, state);
        tankIn.setCanDrain(false);
        tankGasOut.setCanFill(false);
        tankLiquidOut.setCanFill(false);

        tankIn.helpInfo = new ElementHelpInfo("buildcraft.help.distiller.tank_in.title", 0xFF_e2_63_63,
            Tank.DEFAULT_HELP_KEY, "buildcraft.help.distiller.tank_in.desc");
        tankGasOut.helpInfo = new ElementHelpInfo("buildcraft.help.distiller.tank_gas_out.title", 0xFF_e4_e4_00,
            Tank.DEFAULT_HELP_KEY, "buildcraft.help.distiller.tank_gas_out.desc");
        tankLiquidOut.helpInfo = new ElementHelpInfo("buildcraft.help.distiller.tank_liquid_out.title",
            0xFF_b5_00_ff, Tank.DEFAULT_HELP_KEY, "buildcraft.help.distiller.tank_liquid_out.desc");

        tankManager.add(tankIn);
        tankManager.add(tankGasOut);
        tankManager.add(tankLiquidOut);

        smoothedTankIn = new FluidSmoother(createSender(NET_TANK_IN), tankIn);
        smoothedTankGasOut = new FluidSmoother(createSender(NET_TANK_GAS_OUT), tankGasOut);
        smoothedTankLiquidOut = new FluidSmoother(createSender(NET_TANK_LIQUID_OUT), tankLiquidOut);

        caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, tankIn, EnumPipePart.HORIZONTALS);
        caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, tankGasOut, EnumPipePart.UP);
        caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, tankLiquidOut, EnumPipePart.DOWN);
        caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, (IHasWork) () -> hasWork, EnumPipePart.VALUES);
        caps.addMj(new MjCapabilityHelper(new MjBatteryReceiver(mjBattery)));
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    private IFluidDataSender createSender(int netId) {
        return writer -> createAndSendMessage(netId, writer);
    }

    private boolean isDistillableFluid(FluidStack fluid) {
        IRefineryRecipeManager manager = BuildcraftRecipeRegistry.refineryRecipes;
        IDistillationRecipe recipe = manager.getDistillationRegistry().getRecipeForInput(fluid);
        return recipe != null;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("battery", mjBattery.serializeNBT());
        nbt.putLong("distillPower", distillPower);
        powerAvg.writeToNbt(nbt, "powerAvg");
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        mjBattery.deserializeNBT(nbt.getCompound("battery"));
        distillPower = nbt.getLong("distillPower");
        powerAvg.readFromNbt(nbt, "powerAvg");
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_TANK_IN, buffer, side);
                writePayload(NET_TANK_GAS_OUT, buffer, side);
                writePayload(NET_TANK_LIQUID_OUT, buffer, side);
                buffer.writeBoolean(isActive);
                powerAvgClient = powerAvg.getAverageLong();
                final long div = MjAPI.MJ / 2;
                powerAvgClient = Math.round(powerAvgClient / (double) div) * div;
                buffer.writeLong(powerAvgClient);
            } else if (id == NET_TANK_IN) {
                smoothedTankIn.writeInit(buffer);
            } else if (id == NET_TANK_GAS_OUT) {
                smoothedTankGasOut.writeInit(buffer);
            } else if (id == NET_TANK_LIQUID_OUT) {
                smoothedTankLiquidOut.writeInit(buffer);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.writeData(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_TANK_IN, buffer, side, ctx);
                readPayload(NET_TANK_GAS_OUT, buffer, side, ctx);
                readPayload(NET_TANK_LIQUID_OUT, buffer, side, ctx);

                smoothedTankIn.resetSmoothing(level);
                smoothedTankGasOut.resetSmoothing(level);
                smoothedTankLiquidOut.resetSmoothing(level);

                isActive = buffer.readBoolean();
                powerAvgClient = buffer.readLong();
            } else if (id == NET_TANK_IN) {
                smoothedTankIn.handleMessage(level, buffer);
            } else if (id == NET_TANK_GAS_OUT) {
                smoothedTankGasOut.handleMessage(level, buffer);
            } else if (id == NET_TANK_LIQUID_OUT) {
                smoothedTankLiquidOut.handleMessage(level, buffer);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.readData(buffer);
            }
        }
    }

    // Client model variables (1.12's power_average and power_max, in whole MJ)

    public long getModelPowerAverage() {
        return powerAvgClient / MjAPI.MJ;
    }

    public static long getModelPowerMax() {
        return MAX_MJ_PER_TICK / MjAPI.MJ;
    }

    /** One client tick of 1.12's {@code change_speed} and {@code state} variables. */
    private void tickModel() {
        double changeSpeed = isActive ? getModelPowerAverage() * 0.06 / getModelPowerMax() : 0.01;
        double current = modelState;
        double next;
        if (isActive) {
            next = current + changeSpeed;
        } else {
            next = current > changeSpeed ? current - changeSpeed : 0;
        }
        double last = current;
        if (last >= 1.5 && next >= 1.5) {
            last -= 1;
            next -= 1;
        }
        modelStateLast = last;
        modelState = next;
    }

    /** @return The animation state for this frame. */
    public double getModelState(float partialTicks) {
        return modelStateLast * (1 - partialTicks) + modelState * partialTicks;
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        if (super.onActivated(player, hand, held)) {
            return true;
        }
        if (!level.isClientSide) {
            openGui(player);
        }
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerDistiller(windowId, player, this);
    }

    public boolean isActive() {
        return isActive;
    }

    @Override
    public void update() {
        smoothedTankIn.tick(level);
        smoothedTankGasOut.tick(level);
        smoothedTankLiquidOut.tick(level);
        if (level.isClientSide) {
            tickModel();
            return;
        }
        powerAvg.tick();
        changedSinceNetUpdate |= powerAvgClient != powerAvg.getAverageLong();

        currentRecipe =
            BuildcraftRecipeRegistry.refineryRecipes.getDistillationRegistry().getRecipeForInput(tankIn.getFluid());
        if (currentRecipe == null) {
            mjBattery.addPowerChecking(distillPower, false);
            distillPower = 0;
            isActive = false;
            hasWork = false;
        } else {
            FluidStack reqIn = currentRecipe.in();
            FluidStack outLiquid = currentRecipe.outLiquid();
            FluidStack outGas = currentRecipe.outGas();

            FluidStack potentialIn = tankIn.drainInternal(reqIn.getAmount(), FluidAction.SIMULATE);
            boolean canExtract = FluidStack.matches(reqIn, potentialIn);

            boolean canFillLiquid = tankLiquidOut.fillInternal(outLiquid, FluidAction.SIMULATE) == outLiquid.getAmount();
            boolean canFillGas = tankGasOut.fillInternal(outGas, FluidAction.SIMULATE) == outGas.getAmount();

            if (canExtract && canFillLiquid && canFillGas) {
                hasWork = true;
                long max = MAX_MJ_PER_TICK;
                max *= mjBattery.getStored() + max;
                max /= mjBattery.getCapacity() / 2;
                max = Math.min(max, MAX_MJ_PER_TICK);
                long powerReq = currentRecipe.powerRequired();
                long power = mjBattery.extractPower(0, max);
                powerAvg.push(max);
                distillPower += power;
                isActive = power > 0;
                if (distillPower >= powerReq) {
                    isActive = true;
                    distillPower -= powerReq;
                    tankIn.drainInternal(reqIn.getAmount(), FluidAction.EXECUTE);
                    tankGasOut.fillInternal(outGas, FluidAction.EXECUTE);
                    tankLiquidOut.fillInternal(outLiquid, FluidAction.EXECUTE);
                }
            } else {
                hasWork = false;
                mjBattery.addPowerChecking(distillPower, false);
                distillPower = 0;
                isActive = false;
            }
        }

        if (changedSinceNetUpdate && updateTracker.markTimeIfDelay(level)) {
            powerAvgClient = powerAvg.getAverageLong();
            sendNetworkUpdate(NET_RENDER_DATA);
            changedSinceNetUpdate = false;
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("In = " + tankIn.getDebugString());
        left.add("GasOut = " + tankGasOut.getDebugString());
        left.add("LiquidOut = " + tankLiquidOut.getDebugString());
        left.add("Battery = " + mjBattery.getDebugString());
        left.add("Progress = " + MjAPI.formatMj(distillPower));
        left.add("Rate = " + LocaleUtil.localizeMjFlow(powerAvgClient).getString());
        left.add("CurrRecipe = " + currentRecipe);
    }

    @Override
    public void getClientDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("Model Variables:");
        left.add("  active = " + isActive);
        left.add("  power_average = " + getModelPowerAverage());
        left.add("  power_max = " + getModelPowerMax());
        left.add("  state = " + modelState);
    }
}
