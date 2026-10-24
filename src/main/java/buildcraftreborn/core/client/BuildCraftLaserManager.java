/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.client;

import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserRow;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserSide;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserType;

/** Every kind of laser BuildCraft draws. The textures are in {@code textures/block/lasers}. */
public final class BuildCraftLaserManager {
    public static final LaserType MARKER_VOLUME_CONNECTED;
    public static final LaserType MARKER_VOLUME_POSSIBLE;
    public static final LaserType MARKER_VOLUME_SIGNAL;
    public static final LaserType MARKER_PATH_CONNECTED;
    public static final LaserType MARKER_PATH_POSSIBLE;
    public static final LaserType MARKER_DEFAULT_POSSIBLE;
    public static final LaserType STRIPES_READ;
    public static final LaserType STRIPES_WRITE;
    public static final LaserType STRIPES_WRITE_DIRECTION;
    public static final LaserType POWER_LOW;// red
    public static final LaserType POWER_MED;// yellow
    public static final LaserType POWER_HIGH;// green
    public static final LaserType POWER_FULL;// blue
    public static final LaserType[] POWERS;

    private BuildCraftLaserManager() {}

    private static ResourceLocation sprite(String name) {
        return BuildCraftReborn.id("block/lasers/" + name);
    }

    static {
        {
            ResourceLocation sprite = sprite("marker_volume_connected");
            LaserRow capStart = new LaserRow(sprite, 0, 0, 2, 2);
            LaserRow start = new LaserRow(sprite, 0, 0, 16, 2);
            LaserRow[] middle = { //
                new LaserRow(sprite, 0, 2, 16, 4), new LaserRow(sprite, 0, 4, 16, 6), new LaserRow(sprite, 0, 6, 16, 8), //
                new LaserRow(sprite, 0, 8, 16, 10), new LaserRow(sprite, 0, 10, 16, 12), new LaserRow(sprite, 0, 12, 16, 14) //
            };
            LaserRow end = new LaserRow(sprite, 0, 14, 16, 16);
            LaserRow capEnd = new LaserRow(sprite, 14, 14, 16, 16);
            MARKER_VOLUME_CONNECTED = new LaserType(capStart, start, middle, end, capEnd);
        }
        {
            ResourceLocation sprite = sprite("marker_path_connected");
            LaserRow capStart = new LaserRow(sprite, 0, 0, 3, 3);
            LaserRow start = new LaserRow(sprite, 0, 0, 16, 3);
            LaserRow[] middle = { //
                new LaserRow(sprite, 0, 4, 16, 7, LaserSide.TOP, LaserSide.BOTTOM), //
                new LaserRow(sprite, 0, 8, 16, 11, LaserSide.LEFT, LaserSide.RIGHT) //
            };
            LaserRow end = new LaserRow(sprite, 0, 12, 16, 15);
            LaserRow capEnd = new LaserRow(sprite, 13, 12, 16, 15);
            MARKER_PATH_CONNECTED = new LaserType(capStart, start, middle, end, capEnd);
        }
        {
            ResourceLocation sprite = sprite("marker_volume_possible");
            LaserRow capStart = new LaserRow(sprite, 0, 0, 1, 1);
            LaserRow start = new LaserRow(sprite, 0, 0, 16, 1);
            LaserRow[] middle = { //
                new LaserRow(sprite, 0, 1, 16, 2), new LaserRow(sprite, 0, 2, 16, 3), //
                new LaserRow(sprite, 0, 3, 16, 4), new LaserRow(sprite, 0, 4, 16, 5), //
                new LaserRow(sprite, 0, 5, 16, 6), new LaserRow(sprite, 0, 6, 16, 7), //
                new LaserRow(sprite, 0, 7, 16, 8), new LaserRow(sprite, 0, 8, 16, 9), //
                new LaserRow(sprite, 0, 9, 16, 10), new LaserRow(sprite, 0, 10, 16, 11), //
                new LaserRow(sprite, 0, 11, 16, 12), new LaserRow(sprite, 0, 12, 16, 13), //
                new LaserRow(sprite, 0, 13, 16, 14), new LaserRow(sprite, 0, 14, 16, 15), //
            };
            LaserRow end = new LaserRow(sprite, 0, 15, 16, 16);
            LaserRow capEnd = new LaserRow(sprite, 15, 15, 16, 16);
            MARKER_VOLUME_POSSIBLE = new LaserType(capStart, start, middle, end, capEnd);
        }
        MARKER_VOLUME_SIGNAL = new LaserType(MARKER_VOLUME_CONNECTED, sprite("marker_volume_signal"));
        MARKER_PATH_POSSIBLE = new LaserType(MARKER_VOLUME_POSSIBLE, sprite("marker_path_possible"));
        MARKER_DEFAULT_POSSIBLE = new LaserType(MARKER_VOLUME_POSSIBLE, sprite("marker_default_possible"));
        STRIPES_READ = new LaserType(MARKER_VOLUME_CONNECTED, sprite("stripes_read"));
        STRIPES_WRITE = new LaserType(MARKER_VOLUME_CONNECTED, sprite("stripes_write"));
        STRIPES_WRITE_DIRECTION = new LaserType(MARKER_PATH_CONNECTED, sprite("stripes_write_direction"));
        POWER_LOW = new LaserType(MARKER_VOLUME_POSSIBLE, sprite("power_low"));
        POWER_MED = new LaserType(MARKER_VOLUME_POSSIBLE, sprite("power_med"));
        POWER_HIGH = new LaserType(MARKER_VOLUME_POSSIBLE, sprite("power_high"));
        POWER_FULL = new LaserType(MARKER_VOLUME_POSSIBLE, sprite("power_full"));
        POWERS = new LaserType[] { POWER_LOW, POWER_MED, POWER_HIGH, POWER_FULL };
    }
}
