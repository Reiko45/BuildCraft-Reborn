/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.fluid;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.core.IFluidHandlerAdv;
import buildcraftreborn.lib.gui.help.ElementHelpInfo;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** Provides a useful implementation of a fluid tank that can save + load, and has a few helper functions. Can
 * optionally specify a filter to only allow a limited types of fluids in the tank.
 * <p>
 * 1.21 notes: built on NeoForge's {@link FluidTank}. The 1.12 {@code canFill}/{@code canDrain} flags are kept here
 * (NeoForge's tank has none): they gate external {@link #fill}/{@link #drain}, while {@link #fillInternal} and
 * {@link #drainInternal} always work. Network sync writes the client copy with {@link FluidStack#OPTIONAL_STREAM_CODEC}
 * instead of 1.12's object caches. */
public class Tank extends FluidTank implements IFluidHandlerAdv {
    public static final String DEFAULT_HELP_KEY = "buildcraft.help.tank.generic";

    /** What the help overlay shows for this tank in a GUI. */
    public ElementHelpInfo helpInfo;

    private final String name;
    @Nullable
    private BlockEntity tile;
    private boolean canFill = true;
    private boolean canDrain = true;

    /** The fluid shown on the client, synced by {@link #writeToBuffer}. */
    private FluidStack clientFluid = FluidStack.EMPTY;
    private int clientAmount = 0;

    /** Creates a tank with the given name and capacity (in milli buckets) with no filter set (so any fluid can go into
     * the tank) */
    public Tank(String name, int capacity, @Nullable BlockEntity tile) {
        this(name, capacity, tile, null);
    }

    /** Creates a tank with the given name and capacity (in milli buckets) with the specified filter set. If the filter
     * returns true for a given fluidstack then it will be allowed in the tank. The given fluidstack will NEVER be
     * empty. */
    public Tank(String name, int capacity, @Nullable BlockEntity tile, @Nullable Predicate<FluidStack> filter) {
        super(capacity, filter == null ? f -> true : filter);
        this.name = name;
        this.tile = tile;
        helpInfo = new ElementHelpInfo("buildcraft.help.tank.title." + name, 0xFF_00_00_00 | name.hashCode(),
            DEFAULT_HELP_KEY);
    }

    /** Changes the block entity that's told when this tank changes (for the heat exchanger, whose sections move
     * between blocks). */
    public void setTileEntity(@Nullable BlockEntity tile) {
        this.tile = tile;
    }

    public void setFilter(Predicate<FluidStack> filter) {
        setValidator(filter);
    }

    public String getTankName() {
        return name;
    }

    public boolean canFill() {
        return canFill;
    }

    public boolean canDrain() {
        return canDrain;
    }

    public Tank setCanFill(boolean canFill) {
        this.canFill = canFill;
        return this;
    }

    public Tank setCanDrain(boolean canDrain) {
        this.canDrain = canDrain;
        return this;
    }

    public boolean isFull() {
        return !fluid.isEmpty() && fluid.getAmount() >= getCapacity();
    }

    public CompoundTag serializeNBT(HolderLookup.Provider registries) {
        CompoundTag nbt = writeToNBT(registries, new CompoundTag());
        writeTankToNBT(registries, nbt);
        return nbt;
    }

    public void deserializeNBT(HolderLookup.Provider registries, CompoundTag nbt) {
        readFromNBT(registries, nbt);
        readTankFromNBT(registries, nbt);
    }

    /** Writes some additional information to the nbt, for example a single-use tank will write out the filtering
     * fluid. */
    protected void writeTankToNBT(HolderLookup.Provider registries, CompoundTag nbt) {}

    /** Reads some additional information from the nbt. */
    protected void readTankFromNBT(HolderLookup.Provider registries, CompoundTag nbt) {}

    // ##############
    // Filling and draining
    // ##############

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return canFill ? fillInternal(resource, action) : 0;
    }

    /** Fills ignoring {@link #canFill()} (but still respecting the filter). */
    public int fillInternal(FluidStack resource, FluidAction action) {
        return super.fill(resource, action);
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        return canDrain ? super.drain(resource, action) : FluidStack.EMPTY;
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        return canDrain ? drainInternal(maxDrain, action) : FluidStack.EMPTY;
    }

    /** Drains ignoring {@link #canDrain()}. */
    public FluidStack drainInternal(int maxDrain, FluidAction action) {
        return super.drain(maxDrain, action);
    }

    /** Drains the given fluid (if this tank holds it) ignoring {@link #canDrain()}. */
    public FluidStack drainInternal(FluidStack resource, FluidAction action) {
        if (resource.isEmpty() || !FluidStack.isSameFluidSameComponents(resource, fluid)) {
            return FluidStack.EMPTY;
        }
        return drainInternal(resource.getAmount(), action);
    }

    @Override
    public FluidStack drain(IFluidFilter drainFilter, int maxDrain, FluidAction action) {
        if (drainFilter == null) {
            return FluidStack.EMPTY;
        }
        if (!fluid.isEmpty() && drainFilter.matches(fluid)) {
            return drain(maxDrain, action);
        }
        return FluidStack.EMPTY;
    }

    @Override
    protected void onContentsChanged() {
        super.onContentsChanged();
        if (tile instanceof TileBC_Neptune bc) {
            bc.markChunkDirty();
        } else if (tile != null) {
            tile.setChanged();
        }
    }

    @Override
    public String toString() {
        return "Tank [" + getDebugString() + "]";
    }

    // ##############
    // Networking (server -> client)
    // ##############

    public void writeToBuffer(PacketBufferBC buffer) {
        FluidStack.OPTIONAL_STREAM_CODEC.encode(buffer, fluid.isEmpty() ? FluidStack.EMPTY : fluid.copyWithAmount(1));
        buffer.writeInt(getFluidAmount());
    }

    public void readFromBuffer(PacketBufferBC buffer) {
        clientFluid = FluidStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        clientAmount = buffer.readInt();
    }

    /** @return The fluid the client should draw, with the synced amount. Empty when nothing has been synced. */
    public FluidStack getFluidForRender() {
        if (clientFluid.isEmpty() || clientAmount <= 0) {
            return FluidStack.EMPTY;
        }
        return clientFluid.copyWithAmount(clientAmount);
    }

    public int getClientAmount() {
        return clientAmount;
    }

    /** Adds the GUI tooltip lines for this tank (client side). */
    public void addTooltip(List<Component> lines) {
        FluidStack shown = getFluidForRender();
        if (!shown.isEmpty()) {
            lines.add(shown.getHoverName());
        }
        lines.add(LocaleUtil.localizeFluidStaticAmount(clientAmount, getCapacity()).copy()
            .withStyle(ChatFormatting.GRAY));
    }

    public String getDebugString() {
        FluidStack f = getFluidForRender();
        if (f.isEmpty()) f = getFluid();
        return f.getAmount() + " / " + capacity + " mB of " + (f.isEmpty() ? "n/a" : f.getHoverName().getString());
    }

    // ##############
    // GUI and bucket interaction
    // ##############

    /** Attempts to transfer the given stack to (or from) this tank, playing the bucket sound. Server side only.
     *
     * @return The left over item after attempting to add the stack to this tank. */
    public ItemStack transferStackToTank(Player player, ItemStack stack) {
        if (player.level().isClientSide || stack.isEmpty()) {
            return stack;
        }
        boolean isCreative = player.getAbilities().instabuild;
        ItemStack single = stack.copyWithCount(1);

        // First try to empty the item into the tank
        int space = capacity - getFluidAmount();
        FluidGetResult result = space > 0 ? map(single, space) : null;
        if (result != null && !result.fluidStack().isEmpty()) {
            FluidStack drained = result.fluidStack();
            int accepted = fill(drained, FluidAction.SIMULATE);
            if (isCreative ? accepted > 0 : accepted == drained.getAmount()) {
                fill(drained, FluidAction.EXECUTE);
                playSound(player, drained, SoundActions.BUCKET_EMPTY);
                if (isCreative) {
                    return stack;
                }
                return replaceOne(player, stack, result.itemStack());
            }
        }

        // Then try to fill the item from the tank
        IFluidHandlerItem emptyHandler = FluidUtil.getFluidHandler(single.copy()).orElse(null);
        if (emptyHandler == null) return stack;
        FluidStack available = drainInternal(capacity, FluidAction.SIMULATE);
        if (available.isEmpty()) return stack;
        int filled = emptyHandler.fill(available, FluidAction.EXECUTE);
        if (filled > 0) {
            FluidStack reallyDrained = drainInternal(filled, FluidAction.EXECUTE);
            playSound(player, reallyDrained, SoundActions.BUCKET_FILL);
            if (isCreative) {
                return stack;
            }
            return replaceOne(player, stack, emptyHandler.getContainer());
        }
        return stack;
    }

    /** Works out what fluid a single item would put into this tank. Override to accept items that aren't fluid
     * containers (the combustion engine turns ice into water coolant this way).
     *
     * @param stack A single item (count 1), which may be modified.
     * @param space The free space in this tank.
     * @return The item left behind and the fluid it gives, or null if it gives nothing. */
    @Nullable
    protected FluidGetResult map(ItemStack stack, int space) {
        IFluidHandlerItem itemHandler = FluidUtil.getFluidHandler(stack.copy()).orElse(null);
        if (itemHandler == null) {
            return null;
        }
        FluidStack drained = itemHandler.drain(space, FluidAction.EXECUTE);
        if (drained.isEmpty()) {
            return null;
        }
        return new FluidGetResult(itemHandler.getContainer(), drained);
    }

    /** @param itemStack The item left after the fluid was taken out (may be empty).
     * @param fluidStack The fluid taken out. */
    public record FluidGetResult(ItemStack itemStack, FluidStack fluidStack) {}

    /** Replaces one item of {@code stack} with {@code result}, giving the result to the player if the stack had more. */
    private static ItemStack replaceOne(Player player, ItemStack stack, ItemStack result) {
        if (stack.getCount() == 1) {
            return result;
        }
        ItemStack rest = stack.copy();
        rest.shrink(1);
        if (!result.isEmpty() && !player.getInventory().add(result)) {
            player.drop(result, false);
        }
        return rest;
    }

    private static void playSound(Player player, FluidStack fluid, net.neoforged.neoforge.common.SoundAction action) {
        SoundEvent sound = fluid.getFluidType().getSound(fluid, action);
        if (sound != null) {
            player.level().playSound(null, player.blockPosition(), sound, SoundSource.BLOCKS, 1.0f, 1.0f);
        }
    }
}
