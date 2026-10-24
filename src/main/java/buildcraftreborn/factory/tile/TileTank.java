/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.tile;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.core.IFluidHandlerAdv;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.container.ContainerTank;
import buildcraftreborn.lib.fluid.FluidSmoother;
import buildcraftreborn.lib.fluid.FluidSmoother.FluidStackInterp;
import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.misc.FluidUtilBC;
import buildcraftreborn.lib.misc.data.IdAllocator;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** A 16 bucket glass tank. Tanks stacked on top of each other act as one tank: liquids settle into the lowest tank and
 * gases into the highest.
 * <p>
 * 1.21: the whole column is exposed as a single NeoForge fluid tank (index 0) rather than 1.12's tank properties
 * array. */
public class TileTank extends TileBC_Neptune implements ITickable, IDebuggable, IFluidHandlerAdv, MenuProvider {
    public static final IdAllocator IDS = TileBC_Neptune.IDS.makeChild("tank");
    public static final int NET_FLUID_DELTA = IDS.allocId("FLUID_DELTA");

    private static final ResourceLocation ADVANCEMENT_STORE_FLUIDS = BuildCraftReborn.id("fluid_storage");

    private static boolean isPlayerInteracting = false;

    public final Tank tank;
    public final FluidSmoother smoothedTank;

    private int lastComparatorLevel;

    public TileTank(BlockPos pos, BlockState state) {
        this(BCFactoryBlocks.TANK_TILE.get(), pos, state, 16 * 1000);
    }

    protected TileTank(BlockEntityType<?> type, BlockPos pos, BlockState state, int capacity) {
        super(type, pos, state);
        this.tank = new Tank("tank", capacity, this);
        tankManager.add(tank);
        caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, this, EnumPipePart.VALUES);
        smoothedTank = new FluidSmoother(w -> createAndSendMessage(NET_FLUID_DELTA, w), tank);
    }

    @Override
    public IdAllocator getIdAllocator() {
        return IDS;
    }

    public int getComparatorLevel() {
        int amount = tank.getFluidAmount();
        int cap = tank.getCapacity();
        return amount * 14 / cap + (amount > 0 ? 1 : 0);
    }

    // ITickable

    @Override
    public void update() {
        smoothedTank.tick(level);

        if (!level.isClientSide) {
            int compLevel = getComparatorLevel();
            if (compLevel != lastComparatorLevel) {
                lastComparatorLevel = compLevel;
                setChanged();
            }
        }
    }

    // TileEntity

    @Override
    public void onPlacedBy(@Nullable LivingEntity placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (placer != null && !placer.level().isClientSide) {
            isPlayerInteracting = true;
            balanceTankFluids();
            isPlayerInteracting = false;
        }
    }

    /** Moves fluids around to their preferred positions. (For gaseous fluids this will move everything as high as
     * possible, for liquid fluids this will move everything as low as possible.) */
    public void balanceTankFluids() {
        List<TileTank> tanks = getConnectedTanks();
        FluidStack fluid = FluidStack.EMPTY;
        for (TileTank tile : tanks) {
            FluidStack held = tile.tank.getFluid();
            if (held.isEmpty()) {
                continue;
            }
            if (fluid.isEmpty()) {
                fluid = held;
            } else if (!FluidStack.isSameFluidSameComponents(fluid, held)) {
                return;
            }
        }
        if (fluid.isEmpty()) {
            return;
        }
        if (isGaseous(fluid)) {
            Collections.reverse(tanks);
        }
        TileTank prev = null;
        for (TileTank tile : tanks) {
            if (prev != null) {
                FluidUtilBC.move(tile.tank, prev.tank);
            }
            prev = tile;
        }
    }

    private static boolean isGaseous(FluidStack fluid) {
        return fluid.getFluid().getFluidType().isLighterThanAir();
    }

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        int amountBefore = tank.getFluidAmount();
        isPlayerInteracting = true;
        boolean didChange = FluidUtilBC.onTankActivated(player, worldPosition, hand, this);
        isPlayerInteracting = false;
        if (didChange && !player.level().isClientSide && amountBefore < tank.getFluidAmount()) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_STORE_FLUIDS);
        }
        if (!didChange) {
            if (!level.isClientSide) {
                openGui(player);
            }
        }
        return true;
    }

    // GUI

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerTank(windowId, player, this);
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_FLUID_DELTA, buffer, side);
            } else if (id == NET_FLUID_DELTA) {
                smoothedTank.writeInit(buffer);
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
                readPayload(NET_FLUID_DELTA, buffer, side, ctx);
                smoothedTank.resetSmoothing(level);
            } else if (id == NET_FLUID_DELTA) {
                smoothedTank.handleMessage(level, buffer);
            } else if (id == NET_GUI_DATA || id == NET_GUI_TICK) {
                tankManager.readData(buffer);
            }
        }
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("fluid = " + tank.getDebugString());
        smoothedTank.getDebugInfo(left, right, side);
    }

    // Rendering

    @Nullable
    public FluidStackInterp getFluidForRender(float partialTicks) {
        return smoothedTank.getFluidForRender(partialTicks);
    }

    // Tank helper methods

    /** Tests to see if this tank can connect to the other one, in the given direction. BuildCraft itself only calls
     * with {@link Direction#UP} or {@link Direction#DOWN}, however addons are free to call with any of the other 4
     * faces. (Although an addon calling from other faces must provide some way of transferring fluids around).
     *
     * @param other The other tank.
     * @param direction The direction that the other tank is, from this tank.
     * @return True if this can connect, false otherwise. */
    public boolean canConnectTo(TileTank other, Direction direction) {
        return true;
    }

    /** Helper for {@link #canConnectTo(TileTank, Direction)} that only returns true if both tanks can connect to each
     * other.
     *
     * @param direction The direction from the "from" tank, to the "to" tank.
     * @return True if both could connect, false otherwise. */
    public static boolean canTanksConnect(TileTank from, TileTank to, Direction direction) {
        return from.canConnectTo(to, direction) && to.canConnectTo(from, direction.getOpposite());
    }

    /** @return A list of all connected tanks around this block, ordered by position from bottom to top. */
    private List<TileTank> getConnectedTanks() {
        // double-ended queue rather than array list to avoid
        // the copy operation when we search downwards
        Deque<TileTank> tanks = new ArrayDeque<>();
        tanks.add(this);
        if (level == null) {
            return new ArrayList<>(tanks);
        }
        TileTank prevTank = this;
        while (true) {
            BlockEntity tileAbove = prevTank.getNeighbourTile(Direction.UP);
            if (!(tileAbove instanceof TileTank tankUp)) {
                break;
            }
            if (canTanksConnect(prevTank, tankUp, Direction.UP)) {
                tanks.addLast(tankUp);
            } else {
                break;
            }
            prevTank = tankUp;
        }
        prevTank = this;
        while (true) {
            BlockEntity tileBelow = prevTank.getNeighbourTile(Direction.DOWN);
            if (!(tileBelow instanceof TileTank tankBelow)) {
                break;
            }
            if (canTanksConnect(prevTank, tankBelow, Direction.DOWN)) {
                tanks.addFirst(tankBelow);
            } else {
                break;
            }
            prevTank = tankBelow;
        }
        return new ArrayList<>(tanks);
    }

    // IFluidHandler

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int index) {
        List<TileTank> tanks = getConnectedTanks();
        TileTank bottom = tanks.get(0);
        TileTank top = tanks.get(tanks.size() - 1);
        FluidStack total = bottom.tank.getFluid();
        if (total.isEmpty()) {
            total = top.tank.getFluid();
        }
        if (total.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int amount = 0;
        for (TileTank t : tanks) {
            amount += t.tank.getFluidAmount();
        }
        return total.copyWithAmount(amount);
    }

    @Override
    public int getTankCapacity(int index) {
        int capacity = 0;
        for (TileTank t : getConnectedTanks()) {
            capacity += t.tank.getCapacity();
        }
        return capacity;
    }

    @Override
    public boolean isFluidValid(int index, FluidStack stack) {
        return true;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        int filled = 0;
        List<TileTank> tanks = getConnectedTanks();
        for (TileTank t : tanks) {
            FluidStack current = t.tank.getFluid();
            if (!current.isEmpty() && !FluidStack.isSameFluidSameComponents(current, resource)) {
                return 0;
            }
        }
        boolean gas = isGaseous(resource);
        if (gas) {
            Collections.reverse(tanks);
        }
        resource = resource.copy();
        for (TileTank t : tanks) {
            int tankFilled = t.tank.fill(resource, action);
            if (tankFilled > 0) {
                if (isPlayerInteracting & action.execute()) {
                    t.sendNetworkUpdate(NET_RENDER_DATA);
                }
                resource.shrink(tankFilled);
                filled += tankFilled;
                if (resource.isEmpty()) {
                    break;
                }
            }
        }
        return filled;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return drain((IFluidFilter) fluid -> true, maxDrain, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        return drain((IFluidFilter) fluid -> FluidStack.isSameFluidSameComponents(resource, fluid),
            resource.getAmount(), action);
    }

    // IFluidHandlerAdv

    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, FluidAction action) {
        if (maxDrain <= 0) {
            return FluidStack.EMPTY;
        }
        List<TileTank> tanks = getConnectedTanks();
        boolean gas = false;
        for (TileTank tile : tanks) {
            FluidStack fluid = tile.tank.getFluid();
            if (!fluid.isEmpty()) {
                gas = isGaseous(fluid);
                break;
            }
        }
        if (!gas) {
            Collections.reverse(tanks);
        }
        FluidStack total = FluidStack.EMPTY;
        for (TileTank t : tanks) {
            int realMax = maxDrain - total.getAmount();
            if (realMax <= 0) {
                break;
            }
            FluidStack drained = t.tank.drain(filter, realMax, action);
            if (drained.isEmpty()) continue;
            if (isPlayerInteracting & action.execute()) {
                t.sendNetworkUpdate(NET_RENDER_DATA);
            }
            if (total.isEmpty()) {
                total = drained.copy();
            } else {
                total.grow(drained.getAmount());
            }
        }
        return total;
    }
}
