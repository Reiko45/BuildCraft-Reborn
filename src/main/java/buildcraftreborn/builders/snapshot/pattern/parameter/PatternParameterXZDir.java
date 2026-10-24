/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot.pattern.parameter;

import java.util.EnumMap;
import java.util.Map;


import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;


import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementMouseClick;

import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.builders.BCBuildersSprites;

public enum PatternParameterXZDir implements IStatementParameter {
    WEST(Direction.WEST),
    EAST(Direction.EAST),
    NORTH(Direction.NORTH),
    SOUTH(Direction.SOUTH);

    private static final PatternParameterXZDir[] POSSIBLE_ORDER =
        { null, null, NORTH, null, EAST, null, SOUTH, null, WEST };

    private static final Map<Direction, PatternParameterXZDir> map;

    static {
        map = new EnumMap<>(Direction.class);
        for (PatternParameterXZDir param : values()) {
            map.put(param.dir, param);
        }
    }

    public final Direction dir;

    PatternParameterXZDir(Direction dir) {
        this.dir = dir;
    }

    public static PatternParameterXZDir get(Direction face) {
        PatternParameterXZDir param = map.get(face);
        if (param == null) {
            throw new IllegalArgumentException("Can only accept horizontal Direction's (was given " + face + ")");
        }
        return param;
    }

    public static PatternParameterXZDir readFromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        Direction dir;
        if (nbt.contains("dir", 99)) {
            // Older versions
            int d = nbt.getByte("dir") + 2;
            dir = Direction.from2DDataValue(d);
        } else {
            dir = Direction.from2DDataValue(nbt.getByte("d"));
        }
        PatternParameterXZDir param = map.get(dir);
        if (param == null) {
            throw new IllegalStateException("Map lookup failed for " + dir);
        }
        return param;
    }

    @Override
    public void writeToNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putByte("d", (byte) dir.get2DDataValue());
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:fillerParameterXZDir";
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.PARAM_XZ_DIR.get(dir);
    }

    @Override
    public ItemStack getItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("direction." + dir.getName());
    }

    @Override
    public PatternParameterXZDir onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public IStatementParameter rotateLeft() {
        return get(dir.getClockWise());
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return POSSIBLE_ORDER;
    }

    @Override
    public boolean isPossibleOrdered() {
        return true;
    }
}
