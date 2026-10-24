/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.boards;

import java.util.List;
import java.util.Random;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** A kind of redstone board: what a programmed board item is, and what it makes when put into a robot.
 * <p>
 * 1.21: boards are told apart by {@link #getID()} alone (the item keeps it in a data component), so 1.12's NBT
 * methods are gone. Names and tooltips are {@link Component}s. */
public abstract class RedstoneBoardNBT<T> {
    private static final Random rand = new Random();

    /** @return A unique id, such as {@code buildcraftreborn:picker}. */
    public abstract String getID();

    public abstract void addInformation(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
        TooltipFlag flag);

    public abstract Component getDisplayName();

    public abstract IRedstoneBoard<T> create(T object);

    /** @return The item model used for a board item of this type (registered as a standalone model). */
    public abstract ResourceLocation getItemModelLocation();

    public float nextFloat(int difficulty) {
        return 1F - (float) Math.pow(rand.nextFloat(), 1F / difficulty);
    }
}
