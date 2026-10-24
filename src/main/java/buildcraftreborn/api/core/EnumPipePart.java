/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.StringRepresentable;

/** The six faces of a block plus {@link #CENTER}, which stands for "no side" (null {@link Direction}). */
public enum EnumPipePart implements StringRepresentable {
    DOWN(Direction.DOWN),
    UP(Direction.UP),
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH),
    WEST(Direction.WEST),
    EAST(Direction.EAST),
    /** CENTER, UNKNOWN and ALL are all valid uses of this. */
    CENTER(null);

    public static final EnumPipePart[] VALUES = values();
    public static final EnumPipePart[] FACES;
    public static final EnumPipePart[] HORIZONTALS;
    private static final Map<Direction, EnumPipePart> facingMap = new EnumMap<>(Direction.class);
    private static final Map<String, EnumPipePart> nameMap = new HashMap<>();
    private static final int MAX_VALUES = values().length;

    @Nullable
    public final Direction face;

    static {
        for (EnumPipePart part : values()) {
            nameMap.put(part.name(), part);
            if (part.face != null) facingMap.put(part.face, part);
        }
        FACES = fromFacingArray(Direction.values());
        HORIZONTALS = fromFacingArray(Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new));
    }

    private static EnumPipePart[] fromFacingArray(Direction... faces) {
        EnumPipePart[] arr = new EnumPipePart[faces.length];
        for (int i = 0; i < faces.length; i++) {
            arr[i] = fromFacing(faces[i]);
        }
        return arr;
    }

    public static int ordinal(@Nullable Direction face) {
        return face == null ? 6 : face.ordinal();
    }

    public static EnumPipePart fromFacing(@Nullable Direction face) {
        if (face == null) {
            return EnumPipePart.CENTER;
        }
        return facingMap.get(face);
    }

    public static EnumPipePart[] validFaces() {
        return FACES;
    }

    public static EnumPipePart fromMeta(int meta) {
        if (meta < 0 || meta >= MAX_VALUES) {
            return EnumPipePart.CENTER;
        }
        return VALUES[meta];
    }

    EnumPipePart(@Nullable Direction face) {
        this.face = face;
    }

    public int getIndex() {
        if (face == null) return 6;
        return face.get3DDataValue();
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }

    public EnumPipePart next() {
        return switch (this) {
            case DOWN -> EAST;
            case EAST -> NORTH;
            case NORTH -> SOUTH;
            case SOUTH -> UP;
            case UP -> WEST;
            case WEST -> DOWN;
            default -> DOWN;
        };
    }

    public EnumPipePart opposite() {
        if (face == null) {
            return CENTER;
        }
        return fromFacing(face.getOpposite());
    }

    public static EnumPipePart readFromNBT(@Nullable Tag base) {
        if (base == null) {
            return CENTER;
        }
        if (base instanceof StringTag nbtString) {
            return nameMap.getOrDefault(nbtString.getAsString(), CENTER);
        } else if (base instanceof NumericTag numeric) {
            byte ord = numeric.getAsByte();
            if (ord < 0 || ord > 6) {
                return CENTER;
            }
            return VALUES[ord];
        }
        return CENTER;
    }

    public Tag writeToNBT() {
        return StringTag.valueOf(name());
    }
}
