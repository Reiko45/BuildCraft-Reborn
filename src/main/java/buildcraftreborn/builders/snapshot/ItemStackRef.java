/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import buildcraftreborn.lib.misc.RegistryAccessUtil;

/** An item stack in a rule: an item id and an amount, each written in the rule or read from NBT.
 * <p>
 * 1.21: there is no metadata any more, so 1.12's {@code meta} is gone; {@code components} (a compound of data
 * components, as in an item stack's NBT) replaces {@code tagCompound}. */
public class ItemStackRef {
    private NbtRef item;
    private NbtRef amount;
    private NbtRef components;

    public ItemStack get(Tag nbt) {
        String id = item.getString(nbt).orElseThrow(() -> new NullPointerException("No item in " + this));
        ResourceLocation location = ResourceLocation.tryParse(id);
        Item found = location == null ? Items.AIR : BuiltInRegistries.ITEM.get(location);
        if (found == Items.AIR) {
            return ItemStack.EMPTY;
        }
        int count = Optional.ofNullable(amount).flatMap(ref -> ref.getInt(nbt)).orElse(1);
        Optional<Tag> componentTag = Optional.ofNullable(components).flatMap(ref -> ref.get(nbt));
        if (componentTag.isPresent() && componentTag.get() instanceof CompoundTag compound) {
            CompoundTag stackTag = new CompoundTag();
            stackTag.putString("id", id);
            stackTag.putInt("count", Math.max(1, count));
            stackTag.put("components", compound);
            ItemStack stack = ItemStack.parseOptional(RegistryAccessUtil.current(), stackTag);
            stack.setCount(count);
            return stack;
        }
        return new ItemStack(found, count);
    }

    @Override
    public String toString() {
        return "ItemStackRef{item=" + item + ", amount=" + amount + "}";
    }
}
