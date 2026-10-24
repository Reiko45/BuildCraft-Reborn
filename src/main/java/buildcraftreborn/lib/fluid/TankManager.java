/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.fluid;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.ForwardingList;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.core.IFluidHandlerAdv;
import buildcraftreborn.api.items.FluidItemDrops;
import buildcraftreborn.lib.misc.FluidUtilBC;
import buildcraftreborn.lib.net.PacketBufferBC;

/** Provides a simple way to save+load and send+receive data for any number of tanks. This also attempts to fill all of
 * the tanks one by one via the {@link #fill(FluidStack, FluidAction)} and {@link #drain(FluidStack, FluidAction)}
 * methods. */
public class TankManager extends ForwardingList<Tank> implements IFluidHandlerAdv, INBTSerializable<CompoundTag> {

    private final List<Tank> tanks = new ArrayList<>();

    public TankManager() {}

    public TankManager(Tank... tanks) {
        addAll(tanks);
    }

    @Override
    protected List<Tank> delegate() {
        return tanks;
    }

    public void addAll(Tank... values) {
        Collections.addAll(this, values);
    }

    /** Adds the contents of every tank as fragile fluid shards. */
    public void addDrops(NonNullList<ItemStack> toDrop) {
        FluidItemDrops.addFluidDrops(toDrop, toArray(new Tank[0]));
    }

    /** Lets the item the player is holding empty into or fill from these tanks. See
     * {@link FluidUtilBC#onTankActivated}. */
    public boolean onActivated(Player player, BlockPos pos, InteractionHand hand) {
        return FluidUtilBC.onTankActivated(player, pos, hand, this);
    }

    private List<Tank> getFillOrderTanks() {
        List<Tank> list = new ArrayList<>();
        for (Tank t : tanks) {
            if (t.canFill() && !t.canDrain()) {
                list.add(t);
            }
        }
        for (Tank t : tanks) {
            if (t.canFill() && t.canDrain()) {
                list.add(t);
            }
        }
        return list;
    }

    private List<Tank> getDrainOrderTanks() {
        List<Tank> list = new ArrayList<>();
        for (Tank t : tanks) {
            if (!t.canFill() && t.canDrain()) {
                list.add(t);
            }
        }
        for (Tank t : tanks) {
            if (t.canFill() && t.canDrain()) {
                list.add(t);
            }
        }
        return list;
    }

    // IFluidHandler

    @Override
    public int getTanks() {
        return tanks.size();
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        return tanks.get(tank).getFluid();
    }

    @Override
    public int getTankCapacity(int tank) {
        return tanks.get(tank).getCapacity();
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tanks.get(tank).isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return 0;
        }
        int filled = 0;
        for (Tank tank : getFillOrderTanks()) {
            int used = tank.fill(resource, action);
            if (used > 0) {
                resource = resource.copyWithAmount(resource.getAmount() - used);
                filled += used;
                if (resource.isEmpty()) {
                    return filled;
                }
            }
        }
        return filled;
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (resource.isEmpty()) {
            return FluidStack.EMPTY;
        }
        int drainedAmount = 0;
        int left = resource.getAmount();
        for (Tank tank : getDrainOrderTanks()) {
            if (!FluidStack.isSameFluidSameComponents(resource, tank.getFluid())) {
                continue;
            }
            FluidStack drained = tank.drain(left, action);
            if (!drained.isEmpty()) {
                drainedAmount += drained.getAmount();
                left -= drained.getAmount();
            }
        }
        return drainedAmount <= 0 ? FluidStack.EMPTY : resource.copyWithAmount(drainedAmount);
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return drain((IFluidFilter) f -> true, maxDrain, action);
    }

    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, FluidAction action) {
        if (filter == null) {
            return FluidStack.EMPTY;
        }
        FluidStack draining = FluidStack.EMPTY;
        for (Tank tank : getDrainOrderTanks()) {
            if (tank.getFluid().isEmpty() || !filter.matches(tank.getFluid())) {
                continue;
            }
            if (draining.isEmpty()) {
                FluidStack drained = tank.drain(maxDrain, action);
                if (!drained.isEmpty()) {
                    draining = drained;
                    maxDrain -= drained.getAmount();
                }
            } else if (FluidStack.isSameFluidSameComponents(draining, tank.getFluid())) {
                FluidStack drained = tank.drain(maxDrain, action);
                if (!drained.isEmpty()) {
                    draining.grow(drained.getAmount());
                    maxDrain -= drained.getAmount();
                }
            }
            if (maxDrain <= 0) {
                break;
            }
        }
        return draining;
    }

    // Saving and syncing

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        for (Tank t : tanks) {
            nbt.put(t.getTankName(), t.serializeNBT(registries));
        }
        return nbt;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        for (Tank t : tanks) {
            t.deserializeNBT(registries, nbt.getCompound(t.getTankName()));
        }
    }

    public void writeData(PacketBufferBC buffer) {
        for (Tank tank : tanks) {
            tank.writeToBuffer(buffer);
        }
    }

    public void readData(PacketBufferBC buffer) {
        for (Tank tank : tanks) {
            tank.readFromBuffer(buffer);
        }
    }
}
