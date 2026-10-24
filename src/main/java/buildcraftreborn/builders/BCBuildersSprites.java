/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Direction;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.builders.snapshot.pattern.PatternSpherePart;
import buildcraftreborn.builders.snapshot.pattern.parameter.PatternParameterCenter;
import buildcraftreborn.lib.client.sprite.SpriteGui;

/** The builders' statement icons, in the GUI sprite atlas ({@code textures/gui/sprites/filler/}), and the names of the
 * block atlas sprites drawn in the world. */
public final class BCBuildersSprites {
    /** Block atlas: the filler planner addon on a volume box corner. */
    public static final String FILLER_PLANNER = BuildCraftReborn.MODID + ":block/addons/filler_planner";
    /** Block atlas: the builder's robot. */
    public static final String ROBOT = BuildCraftReborn.MODID + ":block/robot";

    public static final ISprite FILLER_NONE = get("patterns/none");
    public static final ISprite FILLER_CLEAR = get("patterns/clear");
    public static final ISprite FILLER_FILL = get("patterns/fill");
    public static final ISprite FILLER_BOX = get("patterns/box");
    public static final ISprite FILLER_FRAME = get("patterns/frame");
    public static final ISprite FILLER_FLATTEN = get("patterns/flatten");
    public static final ISprite FILLER_HORIZON = get("patterns/horizon");
    public static final ISprite FILLER_CYLINDER = get("patterns/cylinder");
    public static final ISprite FILLER_PYRAMID = get("patterns/pyramid");
    public static final ISprite FILLER_STAIRS = get("patterns/stairs");
    public static final ISprite FILLER_SPHERE = get("patterns/sphere");
    public static final ISprite FILLER_2D_TRIANGLE = get("patterns/2d_triangle");
    public static final ISprite FILLER_2D_SQUARE = get("patterns/2d_square");
    public static final ISprite FILLER_2D_PENTAGON = get("patterns/2d_pentagon");
    public static final ISprite FILLER_2D_HEXAGON = get("patterns/2d_hexagon");
    public static final ISprite FILLER_2D_OCTAGON = get("patterns/2d_octagon");
    public static final ISprite FILLER_2D_CIRCLE = get("patterns/2d_circle");
    public static final ISprite FILLER_2D_SEMI_CIRCLE = get("patterns/2d_semi_circle");
    public static final ISprite FILLER_2D_ARC = get("patterns/2d_arc");

    public static final ISprite PARAM_HOLLOW = get("parameters/hollow");
    public static final ISprite PARAM_FILLED_INNER = get("parameters/filled_inner");
    public static final ISprite PARAM_FILLED_OUTER = get("parameters/filled_outer");

    public static final ISprite PARAM_STAIRS_UP = get("parameters/stairs_ascend");
    public static final ISprite PARAM_STAIRS_DOWN = get("parameters/stairs_descend");

    public static final ISprite[] PARAM_ROTATION = new ISprite[4];

    public static final Map<Direction, ISprite> PARAM_XZ_DIR = new EnumMap<>(Direction.class);
    public static final Map<PatternParameterCenter, ISprite> PARAM_CENTER = new EnumMap<>(PatternParameterCenter.class);
    public static final Map<Direction.Axis, ISprite> PARAM_AXIS = new EnumMap<>(Direction.Axis.class);
    public static final Map<Direction, ISprite> PARAM_FACE = new EnumMap<>(Direction.class);
    public static final Map<PatternSpherePart.SpherePartType, ISprite> FILLER_SPHERE_PART =
        new EnumMap<>(PatternSpherePart.SpherePartType.class);

    static {
        for (int r = 0; r < 4; r++) {
            PARAM_ROTATION[r] = get("parameters/rotation_" + r);
        }
        PARAM_XZ_DIR.put(Direction.WEST, get("parameters/arrow_left"));
        PARAM_XZ_DIR.put(Direction.EAST, get("parameters/arrow_right"));
        PARAM_XZ_DIR.put(Direction.NORTH, get("parameters/arrow_up"));
        PARAM_XZ_DIR.put(Direction.SOUTH, get("parameters/arrow_down"));
        for (PatternParameterCenter param : PatternParameterCenter.values()) {
            PARAM_CENTER.put(param, get("parameters/center_" + param.ordinal()));
        }
        for (Direction.Axis axis : Direction.Axis.values()) {
            PARAM_AXIS.put(axis, get("parameters/axis_" + axis.getName()));
        }
        for (Direction face : Direction.values()) {
            PARAM_FACE.put(face, get("parameters/face_" + face.getName()));
        }
        for (PatternSpherePart.SpherePartType type : PatternSpherePart.SpherePartType.values()) {
            FILLER_SPHERE_PART.put(type, get("patterns/sphere_" + type.lowerCaseName));
        }
    }

    private BCBuildersSprites() {}

    private static ISprite get(String name) {
        return new SpriteGui(BuildCraftReborn.id("filler/" + name));
    }
}
