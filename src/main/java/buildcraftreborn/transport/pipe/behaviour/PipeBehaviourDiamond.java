/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.behaviour;

import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.items.IItemHandlerModifiable;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;

import buildcraftreborn.lib.misc.AdvancementUtil;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

import buildcraftreborn.transport.BCTransportGuis;

public abstract class PipeBehaviourDiamond extends PipeBehaviour {

    public static final int FILTERS_PER_SIDE = 9;
    public static final ResourceLocation ADVANCEMENT_NEED_LIST =
        BuildCraftReborn.id("too_many_pipe_filters");

    public final ItemHandlerSimple filters = new ItemHandlerSimple(FILTERS_PER_SIDE * 6, this::onFilterSlotChange);

    public PipeBehaviourDiamond(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDiamond(IPipe pipe, CompoundTag nbt, HolderLookup.Provider registries) {
        super(pipe, nbt, registries);
        filters.deserializeNBT(registries, nbt.getCompound("filters"));
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = super.writeToNbt(registries);
        nbt.put("filters", filters.serializeNBT(registries));
        return nbt;
    }

    protected void onFilterSlotChange(IItemHandlerModifiable itemHandler, int slot, ItemStack before, ItemStack after) {
        if (pipe.getHolder().getPipeWorld().isClientSide) {
            return;
        }
        int baseIndex = FILTERS_PER_SIDE * (slot / FILTERS_PER_SIDE);
        int count = 0;
        for (int i = 0; i < FILTERS_PER_SIDE; i++) {
            int idx = i + baseIndex;
            if (!filters.getStackInSlot(idx).isEmpty()) {
                count++;
            }
        }
        if (count >= FILTERS_PER_SIDE - 2) {
            AdvancementUtil.unlockAdvancement(pipe.getHolder().getOwner().getId(), ADVANCEMENT_NEED_LIST);
        }
    }

    @Override
    public int getTextureIndex(Direction face) {
        return face == null ? 0 : face.ordinal() + 1;
    }

    @Override
    public boolean onPipeActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ,
        EnumPipePart part) {
        if (!player.level().isClientSide) {
            BCTransportGuis.PIPE_DIAMOND.openGui(player, pipe.getHolder().getPipePos());
        }
        return true;
    }
}
