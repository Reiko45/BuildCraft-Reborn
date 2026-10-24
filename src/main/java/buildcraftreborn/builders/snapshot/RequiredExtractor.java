/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonDeserializer;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import net.neoforged.neoforge.fluids.FluidStack;

@SuppressWarnings("WeakerAccess")
public abstract class RequiredExtractor {

    public List<ItemStack> extractItemsFromBlock(BlockState blockState, @Nullable CompoundTag tileNbt) {
        return Collections.emptyList();
    }


    public List<FluidStack> extractFluidsFromBlock(BlockState blockState, @Nullable CompoundTag tileNbt) {
        return Collections.emptyList();
    }


    public List<ItemStack> extractItemsFromEntity(CompoundTag entityNbt) {
        return Collections.emptyList();
    }


    public List<FluidStack> extractFluidsFromEntity(CompoundTag entityNbt) {
        return Collections.emptyList();
    }

    public enum EnumType {
        CONSTANT(RequiredExtractorConstant.class),
        ITEM_FROM_BLOCK(RequiredExtractorItemFromBlock.class),
        ITEM(RequiredExtractorItem.class),
        ITEMS_LIST(RequiredExtractorItemsList.class),
        TANK(RequiredExtractorTank.class);

        public final Class<? extends RequiredExtractor> clazz;

        EnumType(Class<? extends RequiredExtractor> clazz) {
            this.clazz = clazz;
        }

        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static EnumType byName(String name) {
            return Arrays.stream(values())
                .filter(type -> type.getName().equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Required extractor type not found"));
        }
    }

    public static final JsonDeserializer<RequiredExtractor> DESERIALIZER = (json, typeOfT, context) -> {
        EnumType type = EnumType.byName(json.getAsJsonObject().get("type").getAsString());
        json.getAsJsonObject().remove("type");
        return context.deserialize(json, type.clazz);
    };
}
