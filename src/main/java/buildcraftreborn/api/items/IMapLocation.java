/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.items;

import java.util.List;
import java.util.Locale;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IBox;
import buildcraftreborn.api.core.IZone;

/** An item that remembers a place: a spot (a block face), an area, a path or a zone.
 * <p>
 * 1.21: the type is part of the item's data (1.12 used its damage value); ask the item with {@link #getType}. */
public interface IMapLocation extends INamedItem {
    enum MapLocationType implements StringRepresentable {
        CLEAN,
        SPOT,
        AREA,
        PATH,
        ZONE,
        /** Like PATH but repeats around in a loop. */
        PATH_REPEATING;

        public final int meta = ordinal();
        public static final StringRepresentable.EnumCodec<MapLocationType> CODEC =
            StringRepresentable.fromEnum(MapLocationType::values);

        public static MapLocationType getFromStack(ItemStack stack) {
            if (stack.getItem() instanceof IMapLocation location) {
                return location.getType(stack);
            }
            return CLEAN;
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    MapLocationType getType(ItemStack stack);

    /** This function can be used for SPOT types.
     *
     * @return The point representing the map location. */
    @Nullable
    BlockPos getPoint(ItemStack stack);

    /** This function can be used for SPOT and AREA types.
     *
     * @return The box representing the map location. */
    @Nullable
    IBox getBox(ItemStack stack);

    /** This function can be used for SPOT, AREA and ZONE types. The PATH type needs to be handled separately.
     *
     * @return An IZone representing the map location - also an instance of IBox for SPOT and AREA types. */
    @Nullable
    IZone getZone(ItemStack stack);

    /** This function can be used for SPOT and PATH types.
     *
     * @return A list of BlockPoses representing the path the Map Location stores. */
    @Nullable
    List<BlockPos> getPath(ItemStack stack);

    /** This function can be used for SPOT types only.
     *
     * @return The side of the spot. */
    @Nullable
    Direction getPointSide(ItemStack stack);
}
