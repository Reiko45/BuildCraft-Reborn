/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.properties;

import java.util.EnumMap;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import buildcraftreborn.api.enums.EnumOptionalSnapshotType;

/** Block state properties shared between BuildCraft blocks. Only the ones used so far are ported; the rest arrive
 * with the blocks that use them. (1.12's horizontal {@code BLOCK_FACING} is
 * {@code BlockBCBase_Neptune.PROP_FACING}.) */
public final class BuildCraftProperties {
    /** Set on tanks that have another tank below them. */
    public static final BooleanProperty JOINED_BELOW = BooleanProperty.create("joined_below");

    public static final BooleanProperty CONNECTED_UP = BooleanProperty.create("connected_up");
    public static final BooleanProperty CONNECTED_DOWN = BooleanProperty.create("connected_down");
    public static final BooleanProperty CONNECTED_EAST = BooleanProperty.create("connected_east");
    public static final BooleanProperty CONNECTED_WEST = BooleanProperty.create("connected_west");
    public static final BooleanProperty CONNECTED_NORTH = BooleanProperty.create("connected_north");
    public static final BooleanProperty CONNECTED_SOUTH = BooleanProperty.create("connected_south");

    public static final Map<Direction, BooleanProperty> CONNECTED_MAP;

    /** What kind of snapshot is in a builder (shown on its front). */
    public static final EnumProperty<EnumOptionalSnapshotType> SNAPSHOT_TYPE =
        EnumProperty.create("snapshot_type", EnumOptionalSnapshotType.class);
    /** Set on architect tables that have an area to scan. */
    public static final BooleanProperty VALID = BooleanProperty.create("valid");

    static {
        Map<Direction, BooleanProperty> map = new EnumMap<>(Direction.class);
        map.put(Direction.DOWN, CONNECTED_DOWN);
        map.put(Direction.UP, CONNECTED_UP);
        map.put(Direction.EAST, CONNECTED_EAST);
        map.put(Direction.WEST, CONNECTED_WEST);
        map.put(Direction.NORTH, CONNECTED_NORTH);
        map.put(Direction.SOUTH, CONNECTED_SOUTH);
        CONNECTED_MAP = Maps.immutableEnumMap(map);
    }

    private BuildCraftProperties() {}
}
