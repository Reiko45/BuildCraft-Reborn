/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.facades;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.InterModComms;

/** 1.21: inter-mod messages go through NeoForge's {@link InterModComms} (send them during
 * {@code InterModEnqueueEvent}), to the {@code buildcraftreborn} mod. A custom mapping carries the block state as
 * written by {@link NbtUtils#writeBlockState(BlockState)} and the item as its registry name (1.12 used the block's
 * metadata and the full item stack). */
public final class FacadeAPI {
    public static final String IMC_MOD_TARGET = "buildcraftreborn";
    public static final String IMC_FACADE_DISABLE = "facade_disable_block";
    public static final String IMC_FACADE_CUSTOM = "facade_custom_map_block_item";

    public static final String NBT_CUSTOM_BLOCK_STATE = "block_state";
    public static final String NBT_CUSTOM_ITEM = "item";

    public static IFacadeItem facadeItem;
    public static IFacadeRegistry registry;

    private FacadeAPI() {}

    public static void disableBlock(Block block) {
        InterModComms.sendTo(IMC_MOD_TARGET, IMC_FACADE_DISABLE, () -> BuiltInRegistries.BLOCK.getKey(block));
    }

    public static void mapStateToStack(BlockState state, ItemStack stack) {
        CompoundTag nbt = new CompoundTag();
        nbt.put(NBT_CUSTOM_BLOCK_STATE, NbtUtils.writeBlockState(state));
        nbt.putString(NBT_CUSTOM_ITEM, BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        InterModComms.sendTo(IMC_MOD_TARGET, IMC_FACADE_CUSTOM, () -> nbt);
    }

    public static boolean isFacadeMessageId(String id) {
        return IMC_FACADE_CUSTOM.equals(id) //
            || IMC_FACADE_DISABLE.equals(id);
    }
}
