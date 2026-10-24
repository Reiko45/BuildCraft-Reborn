/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.tile;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.mj.ILaserTarget;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.IngredientStack;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.api.tiles.IHasWork;
import buildcraftreborn.api.tiles.TilesAPI;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.data.AverageLong;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** A laser table: lasers aim at it while {@link #getTarget()} is above zero, and it stores what they send in
 * {@link #power}. */
public abstract class TileLaserTableBase extends TileBC_Neptune implements ILaserTarget, ITickable, IDebuggable {
    private static final long MJ_FLOW_ROUND = MjAPI.MJ / 10;
    private final AverageLong avgPower = new AverageLong(120);
    public long avgPowerClient;
    public long power;

    protected TileLaserTableBase(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        caps.addCapabilityInstance(TilesAPI.CAP_HAS_WORK, (IHasWork) () -> getTarget() > 0, EnumPipePart.VALUES);
    }

    /** @return The power (in micro MJ) needed to finish the current job, or 0 if there's nothing to do. */
    public abstract long getTarget();

    @Override
    public long getRequiredLaserPower() {
        return getTarget() - power;
    }

    @Override
    public long receiveLaserPower(long microJoules) {
        long received = Math.min(microJoules, getRequiredLaserPower());
        power += received;
        avgPower.push(received);
        return microJoules - received;
    }

    @Override
    public boolean isInvalidTarget() {
        return isRemoved();
    }

    @Override
    public void update() {
        avgPower.tick();
        if (level.isClientSide) {
            return;
        }
        if (getTarget() <= 0) {
            power = 0;
            avgPower.clear();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putLong("power", power);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        power = nbt.getLong("power");
    }

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER && id == NET_GUI_TICK) {
            buffer.writeLong(power);
            long pwrAvg = Math.round(avgPower.getAverage());
            long div = pwrAvg / MJ_FLOW_ROUND;
            long mod = pwrAvg % MJ_FLOW_ROUND;
            buffer.writeInt((int) div + ((mod > MJ_FLOW_ROUND / 2) ? 1 : 0));
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT && id == NET_GUI_TICK) {
            power = buffer.readLong();
            avgPowerClient = buffer.readInt() * MJ_FLOW_ROUND;
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("power - " + LocaleUtil.localizeMj(power).getString());
        left.add("target - " + LocaleUtil.localizeMj(getTarget()).getString());
    }

    /** Takes the given ingredients out of the inventory.
     *
     * @param precise If true, only succeed if nothing else is in the inventory.
     * @return true if every ingredient was there. */
    protected boolean extract(ItemHandlerSimple inv, Collection<IngredientStack> items, boolean simulate,
        boolean precise) {
        AtomicLong remainingStacks = new AtomicLong(inv.stacks.stream().filter(stack -> !stack.isEmpty()).count());
        boolean allItemsConsumed = items.stream().allMatch(definition -> {
            int remaining = definition.count;
            for (int i = 0; i < inv.getSlots() && remaining > 0; i++) {
                ItemStack slotStack = inv.getStackInSlot(i);
                if (slotStack.isEmpty()) continue;
                if (definition.ingredient.test(slotStack)) {
                    int spend = Math.min(remaining, slotStack.getCount());
                    remaining -= spend;
                    if (!simulate) {
                        ItemStack left = slotStack.copy();
                        left.shrink(spend);
                        inv.setStackInSlot(i, left);
                    }
                }
            }
            if (remaining == 0) {
                remainingStacks.decrementAndGet();
                return true;
            }
            return false;
        });
        return allItemsConsumed && (!precise || remainingStacks.get() == 0);
    }
}
