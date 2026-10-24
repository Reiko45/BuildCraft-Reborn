/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.list;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.common.Tags;

import buildcraftreborn.api.lists.ListMatchHandler;

/** "Same type" for tools: a tool matches others that are the same kinds of tool (precise: exactly the same kinds).
 * 1.21 has no tool classes, so the kinds come from item tags ({@code #minecraft:pickaxes}, {@code #c:tools/shear}...).
 */
public class ListMatchHandlerTools extends ListMatchHandler {
    private enum ToolKind {
        PICKAXE(ItemTags.PICKAXES),
        AXE(ItemTags.AXES),
        SHOVEL(ItemTags.SHOVELS),
        HOE(ItemTags.HOES),
        SWORD(ItemTags.SWORDS),
        SHEARS(Tags.Items.TOOLS_SHEAR),
        BOW(Tags.Items.TOOLS_BOW),
        CROSSBOW(Tags.Items.TOOLS_CROSSBOW),
        FISHING_ROD(Tags.Items.TOOLS_FISHING_ROD),
        SPEAR(Tags.Items.TOOLS_SPEAR),
        SHIELD(Tags.Items.TOOLS_SHIELD),
        BRUSH(Tags.Items.TOOLS_BRUSH),
        MACE(Tags.Items.TOOLS_MACE);

        final TagKey<Item> tag;

        ToolKind(TagKey<Item> tag) {
            this.tag = tag;
        }
    }

    private static Set<ToolKind> getToolKinds(ItemStack stack) {
        EnumSet<ToolKind> kinds = EnumSet.noneOf(ToolKind.class);
        for (ToolKind kind : ToolKind.values()) {
            if (stack.is(kind.tag)) {
                kinds.add(kind);
            }
        }
        return kinds;
    }

    @Override
    public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
        if (type == Type.TYPE) {
            Set<ToolKind> source = getToolKinds(stack);
            Set<ToolKind> other = getToolKinds(target);
            if (!source.isEmpty() && !other.isEmpty()) {
                if (precise && source.size() != other.size()) {
                    return false;
                }
                return other.containsAll(source);
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, ItemStack stack) {
        return !getToolKinds(stack).isEmpty();
    }
}
