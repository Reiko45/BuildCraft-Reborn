/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraftreborn.api.lists.ListMatchHandler;
import buildcraftreborn.api.lists.ListMatchHandler.Type;
import buildcraftreborn.api.lists.ListRegistry;
import buildcraftreborn.lib.item.BCItemComponents;

/** The contents of a list item: two lines of nine stacks, each with three options (precise, same type, same material).
 * A line with "same type" or "same material" on matches by its first stack only.
 * <p>
 * 1.21: the lines and the label are the {@code buildcraftreborn:list} data component ({@link Data}) instead of NBT;
 * "precise" compares all of a stack's components (1.12: damage and NBT). */
public final class ListHandler {
    public static final int WIDTH = 9;
    public static final int HEIGHT = 2;

    /** What a list item holds. */
    public record Data(String label, List<LineData> lines) {
        public static final Data EMPTY = new Data("", List.of());

        public static final Codec<Data> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("label", "").forGetter(Data::label),
            LineData.CODEC.listOf().optionalFieldOf("lines", List.of()).forGetter(Data::lines))
            .apply(instance, Data::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, Data::label,
            LineData.STREAM_CODEC.apply(ByteBufCodecs.list(HEIGHT)), Data::lines,
            Data::new);

        public Data withLabel(String newLabel) {
            return new Data(newLabel, lines);
        }
    }

    /** One saved line. */
    public record LineData(List<ItemStack> stacks, boolean precise, boolean byType, boolean byMaterial) {
        public static final Codec<LineData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.OPTIONAL_CODEC.listOf().fieldOf("stacks").forGetter(LineData::stacks),
            Codec.BOOL.optionalFieldOf("precise", false).forGetter(LineData::precise),
            Codec.BOOL.optionalFieldOf("by_type", false).forGetter(LineData::byType),
            Codec.BOOL.optionalFieldOf("by_material", false).forGetter(LineData::byMaterial))
            .apply(instance, LineData::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, LineData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC, LineData::stacks,
            ByteBufCodecs.BOOL, LineData::precise,
            ByteBufCodecs.BOOL, LineData::byType,
            ByteBufCodecs.BOOL, LineData::byMaterial,
            LineData::new);

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof LineData other)) return false;
            if (precise != other.precise || byType != other.byType || byMaterial != other.byMaterial) return false;
            if (stacks.size() != other.stacks.size()) return false;
            for (int i = 0; i < stacks.size(); i++) {
                if (!ItemStack.matches(stacks.get(i), other.stacks.get(i))) return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = Boolean.hashCode(precise) * 31 + Boolean.hashCode(byType) * 7 + Boolean.hashCode(byMaterial);
            for (ItemStack stack : stacks) {
                hash = hash * 31 + ItemStack.hashItemAndComponents(stack);
            }
            return hash;
        }
    }

    public static class Line {
        public final NonNullList<ItemStack> stacks;
        public boolean precise, byType, byMaterial;

        public Line() {
            stacks = NonNullList.withSize(WIDTH, ItemStack.EMPTY);
        }

        /** Checks to see if this line is completely blank, and no data would be lost if this line was not saved. */
        public boolean isDefault() {
            if (precise || byType || byMaterial) return false;
            return !hasItems();
        }

        /** Checks to see if this line has any items */
        public boolean hasItems() {
            for (ItemStack stack : stacks) {
                if (!stack.isEmpty()) return true;
            }
            return false;
        }

        public boolean isOneStackMode() {
            return byType || byMaterial;
        }

        public boolean getOption(int id) {
            return id == 0 ? precise : (id == 1 ? byType : byMaterial);
        }

        public void toggleOption(int id) {
            if (!byType && !byMaterial && (id == 1 || id == 2)) {
                for (int i = 1; i < stacks.size(); i++) {
                    stacks.set(i, ItemStack.EMPTY);
                }
            }
            switch (id) {
                case 0 -> precise = !precise;
                case 1 -> byType = !byType;
                case 2 -> byMaterial = !byMaterial;
                default -> {}
            }
        }

        public boolean matches(ItemStack target) {
            if (target.isEmpty()) {
                return false;
            }
            if (byType || byMaterial) {
                ItemStack compare = stacks.get(0);
                if (compare.isEmpty()) {
                    return false;
                }

                Type type = getSortingType();
                boolean anyHandled = false;
                for (ListMatchHandler h : ListRegistry.getHandlers()) {
                    if (h.matches(type, compare, target, precise)) {
                        return true;
                    } else if (h.isValidSource(type, target)) {
                        anyHandled = true;
                    }
                }
                if (!anyHandled && type == Type.TYPE) {
                    // 1.12 compared item ids here (ignoring damage) for items with subtypes
                    return compare.is(target.getItem());
                }
            } else {
                for (ItemStack s : stacks) {
                    if (!s.isEmpty() && (precise ? ItemStack.isSameItemSameComponents(s, target)
                        : s.is(target.getItem()))) {
                        return true;
                    }
                }
            }
            return false;
        }

        public Type getSortingType() {
            return byType ? (byMaterial ? Type.CLASS : Type.TYPE) : Type.MATERIAL;
        }

        public static Line fromData(LineData data) {
            Line line = new Line();
            for (int i = 0; i < Math.min(WIDTH, data.stacks.size()); i++) {
                line.stacks.set(i, data.stacks.get(i).copy());
            }
            line.precise = data.precise;
            line.byType = data.byType;
            line.byMaterial = data.byMaterial;
            return line;
        }

        public LineData toData() {
            List<ItemStack> copy = new ArrayList<>(WIDTH);
            for (ItemStack stack : stacks) {
                copy.add(stack.copy());
            }
            return new LineData(List.copyOf(copy), precise, byType, byMaterial);
        }

        public void setStack(int slotIndex, ItemStack stack) {
            if (slotIndex == 0 || (!byType && !byMaterial)) {
                if (stack.isEmpty()) {
                    stacks.set(slotIndex, ItemStack.EMPTY);
                } else {
                    stack = stack.copy();
                    stack.setCount(1);
                    stacks.set(slotIndex, stack);
                }
            }
        }

        public ItemStack getStack(int i) {
            if (i < 0 || i >= stacks.size()) {
                return ItemStack.EMPTY;
            } else {
                return stacks.get(i);
            }
        }

        /** The stacks shown in the (otherwise empty) slots of a line in "same type" or "same material" mode. */
        @OnlyIn(Dist.CLIENT)
        public NonNullList<ItemStack> getExamples() {
            ItemStack firstStack = stacks.get(0);
            if (firstStack.isEmpty()) {
                return NonNullList.withSize(0, ItemStack.EMPTY);
            }
            NonNullList<ItemStack> stackList = NonNullList.create();
            List<ListMatchHandler> handlersCustom = new ArrayList<>();
            Type type = getSortingType();
            for (ListMatchHandler h : ListRegistry.getHandlers()) {
                if (h.isValidSource(type, firstStack)) {
                    NonNullList<ItemStack> examples = h.getClientExamples(type, firstStack);
                    if (examples != null) {
                        stackList.addAll(examples);
                    } else {
                        handlersCustom.add(h);
                    }
                }
            }
            if (!handlersCustom.isEmpty()) {
                for (Item item : BuiltInRegistries.ITEM) {
                    ItemStack s = new ItemStack(item);
                    for (ListMatchHandler mh : handlersCustom) {
                        if (mh.matches(type, firstStack, s, false)) {
                            stackList.add(s);
                            break;
                        }
                    }
                }
            }
            Collections.shuffle(stackList);
            return stackList;
        }
    }

    private ListHandler() {}

    public static Data getData(ItemStack stack) {
        return stack.getOrDefault(BCItemComponents.LIST.get(), Data.EMPTY);
    }

    public static boolean hasItems(ItemStack stack) {
        for (Line l : getLines(stack)) {
            if (l.hasItems()) return true;
        }
        return false;
    }

    public static boolean isDefault(ItemStack stack) {
        for (Line l : getLines(stack)) {
            if (!l.isDefault()) return false;
        }
        return true;
    }

    public static Line[] getLines(ItemStack item) {
        Data data = getData(item);
        Line[] lines = new Line[HEIGHT];
        for (int i = 0; i < lines.length; i++) {
            lines[i] = i < data.lines.size() ? Line.fromData(data.lines.get(i)) : new Line();
        }
        return lines;
    }

    public static void saveLines(ItemStack stackList, Line[] lines) {
        boolean hasLine = false;
        for (Line l : lines) {
            if (!l.isDefault()) {
                hasLine = true;
                break;
            }
        }
        String label = getData(stackList).label;
        List<LineData> saved = new ArrayList<>();
        if (hasLine) {
            for (Line line : lines) {
                saved.add(line.toData());
            }
        }
        setData(stackList, new Data(label, List.copyOf(saved)));
    }

    /** Stores the data, removing the component altogether when there is nothing in it. */
    public static void setData(ItemStack stack, Data data) {
        if (data.label.isEmpty() && data.lines.isEmpty()) {
            stack.remove(BCItemComponents.LIST.get());
        } else {
            stack.set(BCItemComponents.LIST.get(), data);
        }
    }

    public static boolean matches(ItemStack stackList, ItemStack item) {
        for (LineData lineData : getData(stackList).lines) {
            if (Line.fromData(lineData).matches(item)) {
                return true;
            }
        }
        return false;
    }
}
