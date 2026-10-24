/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.list;

import java.util.EnumSet;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.lists.ListMatchHandler;

/** "Same type" for armour: a piece matches others worn in the same slot. */
public class ListMatchHandlerArmor extends ListMatchHandler {
    private static EnumSet<EquipmentSlot> getArmorTypes(ItemStack stack) {
        EnumSet<EquipmentSlot> types = EnumSet.noneOf(EquipmentSlot.class);
        // 1.12 asked each armour slot whether the item fits (with a player); 1.21 items name their slot
        EquipmentSlot slot = stack.getEquipmentSlot();
        if (slot == null) {
            Equipable equipable = Equipable.get(stack);
            slot = equipable == null ? null : equipable.getEquipmentSlot();
        }
        if (slot != null && slot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR) {
            types.add(slot);
        }
        return types;
    }

    @Override
    public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            EnumSet<EquipmentSlot> source = getArmorTypes(stack);
            if (!source.isEmpty()) {
                EnumSet<EquipmentSlot> other = getArmorTypes(target);
                if (precise) {
                    return source.equals(other);
                } else {
                    source.retainAll(other);
                    return !source.isEmpty();
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, ItemStack stack) {
        return !getArmorTypes(stack).isEmpty();
    }
}
