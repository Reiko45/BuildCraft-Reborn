/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot.pattern.parameter;

import java.util.Locale;


import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementMouseClick;

import buildcraftreborn.lib.misc.LocaleUtil;

import buildcraftreborn.builders.BCBuildersSprites;

public enum PatternParameterAxis implements IStatementParameter {
    X(Axis.X),
    Y(Axis.Y),
    Z(Axis.Z);

    public final Direction.Axis axis;

    PatternParameterAxis(Axis axis) {
        this.axis = axis;
    }

    public static PatternParameterAxis readFromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        byte ord = nbt.getByte("a");
        if (ord <= 0) {
            return X;
        }
        if (ord >= 2) {
            return Z;
        }
        return Y;
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:filler_parameter_axis";
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("buildcraft.param.axis." + name().toLowerCase(Locale.ROOT));
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.PARAM_AXIS.get(axis);
    }

    @Override
    public ItemStack getItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public IStatementParameter onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public void writeToNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putByte("a", (byte) ordinal());
    }

    @SuppressWarnings("SuspiciousNameCombination")
    @Override
    public IStatementParameter rotateLeft() {
        switch (this) {
            case X:
                return Z;
            case Y:
                return Y;
            case Z:
                return X;
            default:
                throw new IllegalStateException("Unknown axis " + this);
        }
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return values();
    }
}
