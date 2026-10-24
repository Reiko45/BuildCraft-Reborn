/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.list;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.lists.ListMatchHandler;

/** 1.12's ore dictionary matching, with 1.21's common item tags. A tag {@code c:<type>/<material>} (such as
 * {@code c:ingots/iron}) gives a stack's type ({@code ingots}) and material ({@code iron}), like 1.12 split
 * {@code ingotIron}:
 * <ul>
 * <li>"Same type" matches anything in a {@code c:<type>/...} tag (every ingot).
 * <li>"Same material" matches anything in a {@code c:.../<material>} tag (iron ingots, nuggets, blocks, ores...).
 * <li>Both matches anything sharing one of those tags.
 * </ul>
 * As in 1.12, when a stack has several such tags the most specific one is used for type and material. The
 * {@code c:tools/...} tags are left to {@link ListMatchHandlerTools}. */
public class ListMatchHandlerTags extends ListMatchHandler {
    private static final String NAMESPACE = "c";

    /** The {@code c:<type>/<material>} tags of a stack. */
    private static List<TagKey<Item>> getCommonTags(ItemStack stack) {
        return stack.getTags().filter(ListMatchHandlerTags::isTypeMaterialTag).toList();
    }

    private static boolean isTypeMaterialTag(TagKey<Item> tag) {
        String path = tag.location().getPath();
        // Tools have their own handler (c:tools/mining_tool would make a pickaxe "the same type" as an axe)
        return NAMESPACE.equals(tag.location().getNamespace()) && path.indexOf('/') > 0 && !path.startsWith("tools/");
    }

    private static String getType(TagKey<Item> tag) {
        String path = tag.location().getPath();
        return path.substring(0, path.indexOf('/'));
    }

    private static String getMaterial(TagKey<Item> tag) {
        String path = tag.location().getPath();
        return path.substring(path.lastIndexOf('/') + 1);
    }

    /** The most specific tag (the most path segments), like 1.12's "longest ore name". */
    @Nullable
    private static TagKey<Item> getBestTag(List<TagKey<Item>> tags) {
        return tags.stream().max(Comparator.<TagKey<Item>>comparingInt(t -> t.location().getPath().split("/").length)
            .thenComparing(t -> t.location().toString(), Comparator.reverseOrder())).orElse(null);
    }

    private static boolean matchesPart(Type type, TagKey<Item> best, TagKey<Item> other) {
        return type == Type.MATERIAL ? getMaterial(best).equals(getMaterial(other))
            : getType(best).equals(getType(other));
    }

    @Override
    public boolean matches(Type type, ItemStack stack, ItemStack target, boolean precise) {
        List<TagKey<Item>> tags = getCommonTags(stack);
        if (tags.isEmpty()) {
            // Unfortunately we cannot compare the items.
            return false;
        }
        List<TagKey<Item>> targetTags = getCommonTags(target);
        if (type == Type.CLASS) {
            for (TagKey<Item> tag : tags) {
                if (targetTags.contains(tag)) {
                    return true;
                }
            }
        } else {
            TagKey<Item> best = getBestTag(tags);
            if (best != null) {
                for (TagKey<Item> other : targetTags) {
                    if (matchesPart(type, best, other)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isValidSource(Type type, ItemStack stack) {
        return !getCommonTags(stack).isEmpty();
    }

    @Override
    public NonNullList<ItemStack> getClientExamples(Type type, ItemStack stack) {
        List<TagKey<Item>> tags = getCommonTags(stack);
        NonNullList<ItemStack> stacks = NonNullList.create();
        if (tags.isEmpty()) {
            if (type == Type.TYPE) {
                stacks.add(new ItemStack(stack.getItem()));
            }
            return stacks;
        }
        TagKey<Item> best = getBestTag(tags);
        BuiltInRegistries.ITEM.getTags().forEach(pair -> {
            TagKey<Item> tag = pair.getFirst();
            if (!isTypeMaterialTag(tag)) {
                return;
            }
            boolean wanted = type == Type.CLASS ? tags.contains(tag) : best != null && matchesPart(type, best, tag);
            if (wanted) {
                for (Holder<Item> item : pair.getSecond()) {
                    ItemStack example = new ItemStack(item);
                    if (stacks.stream().noneMatch(s -> s.is(item))) {
                        stacks.add(example);
                    }
                }
            }
        });
        return stacks;
    }

    /** @return the tag used for "same type" and "same material" of a stack, for tooltips and tests. */
    public static Optional<TagKey<Item>> getMainTag(ItemStack stack) {
        return Optional.ofNullable(getBestTag(getCommonTags(stack)));
    }
}
