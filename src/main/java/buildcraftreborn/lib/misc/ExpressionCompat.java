/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.DyeColor;

import buildcraftreborn.api.enums.EnumPowerStage;
import buildcraftreborn.api.tiles.IControllable;
import buildcraftreborn.lib.expression.DefaultContexts;
import buildcraftreborn.lib.expression.FunctionContext;
import buildcraftreborn.lib.expression.api.NodeType;
import buildcraftreborn.lib.expression.api.NodeTypes;

/** Adds Minecraft and BuildCraft types (facings, dye colours, power stages...) to the expression language, for the
 * variable JSON models. (1.12's GUI position types were only used by JSON GUIs, which are not ported.) */
public final class ExpressionCompat {
    public static final FunctionContext RENDERING = DefaultContexts.RENDERING;

    public static final NodeType<Axis> ENUM_AXIS;
    public static final NodeType<Direction> ENUM_FACING;
    public static final NodeType<DyeColor> ENUM_DYE_COLOUR;
    public static final NodeType<EnumPowerStage> ENUM_POWER_STAGE;
    public static final NodeType<IControllable.Mode> ENUM_CONTROL_MODE;

    static {
        ENUM_AXIS = new NodeType<>("Axis", Axis.X);
        NodeTypes.addType("Axis", ENUM_AXIS);
        for (Axis a : Axis.values()) {
            ENUM_AXIS.putConstant("" + a, a);
        }

        ENUM_FACING = new NodeType<>("Facing", Direction.UP);
        NodeTypes.addType("Facing", ENUM_FACING);
        ENUM_FACING.put_t_t("getOpposite", Direction::getOpposite);
        ENUM_FACING.put_t_o("getAxis", Axis.class, Direction::getAxis);
        ENUM_FACING.put_t_o("(string)", String.class, Direction::getName);
        for (Direction f : Direction.values()) {
            ENUM_FACING.putConstant("" + f, f);
        }

        ENUM_DYE_COLOUR = new NodeType<>("Dye Colour", DyeColor.WHITE);
        NodeTypes.addType("DyeColor", ENUM_DYE_COLOUR);
        NodeTypes.addType("DyeColour", ENUM_DYE_COLOUR);
        ENUM_DYE_COLOUR.put_t_l("to_argb", c -> 0xFF_00_00_00 | ColourUtil.getLightHex(c));
        // 1.12 colour names, so model JSON keeps working ("silver" for light grey)
        ENUM_DYE_COLOUR.put_t_o("(string)", String.class, c -> c == DyeColor.LIGHT_GRAY ? "silver" : c.getName());
        for (DyeColor c : DyeColor.values()) {
            ENUM_DYE_COLOUR.putConstant("" + c, c);
        }

        ENUM_POWER_STAGE = new NodeType<>("Engine Power Stage", EnumPowerStage.BLUE);
        NodeTypes.addType("EnginePowerStage", ENUM_POWER_STAGE);
        ENUM_POWER_STAGE.put_t_o("(string)", String.class, EnumPowerStage::getModelName);
        for (EnumPowerStage stage : EnumPowerStage.VALUES) {
            ENUM_POWER_STAGE.putConstant("" + stage, stage);
        }

        ENUM_CONTROL_MODE = new NodeType<>("Controllable Mode", IControllable.Mode.class, IControllable.Mode.ON);
        NodeTypes.addType("ControlMode", ENUM_CONTROL_MODE);
        ENUM_CONTROL_MODE.put_t_o("(string)", String.class, e -> e.lowerCaseName);
        for (IControllable.Mode mode : IControllable.Mode.VALUES) {
            ENUM_CONTROL_MODE.putConstant("" + mode, mode);
        }

        RENDERING.put_s_l("convertColourToAbgr", ExpressionCompat::convertColourToAbgr);
        RENDERING.put_s_l("convertColourToArgb", ExpressionCompat::convertColourToArgb);
    }

    private ExpressionCompat() {}

    public static void setup() {
        // Just to call the above static initializer
    }

    private static long convertColourToAbgr(String c) {
        DyeColor colour = parse(c);
        if (colour == null) return 0xFF_FF_FF_FF;
        return 0xFF_00_00_00 | ColourUtil.swapArgbToAbgr(ColourUtil.getLightHex(colour));
    }

    private static long convertColourToArgb(String c) {
        DyeColor colour = parse(c);
        if (colour == null) return 0xFF_FF_FF_FF;
        return 0xFF_00_00_00 | ColourUtil.getLightHex(colour);
    }

    private static DyeColor parse(String c) {
        return "silver".equals(c) ? DyeColor.LIGHT_GRAY : ColourUtil.parseColourOrNull(c);
    }
}
