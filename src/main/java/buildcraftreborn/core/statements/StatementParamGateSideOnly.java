/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementMouseClick;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.lib.misc.LocaleUtil;

public enum StatementParamGateSideOnly implements IStatementParameter {
    ANY(false),
    SPECIFIC(true);

    public final boolean isSpecific;

    private static final StatementParamGateSideOnly[] POSSIBLE_ANY = { ANY, SPECIFIC };
    private static final StatementParamGateSideOnly[] POSSIBLE_SPECIFIC = { SPECIFIC, ANY };

    StatementParamGateSideOnly(boolean isSpecific) {
        this.isSpecific = isSpecific;
    }

    public static StatementParamGateSideOnly readFromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        return nbt.getBoolean("isOn") ? SPECIFIC : ANY;
    }

    @Override
    public void writeToNbt(CompoundTag compound, HolderLookup.Provider registries) {
        compound.putBoolean("isOn", isSpecific);
    }

    @Override
    public ItemStack getItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public ISprite getSprite() {
        return isSpecific ? BCCoreSprites.PARAM_GATE_SIDE_ONLY : null;
    }

    @Override
    public DrawType getDrawType() {
        return DrawType.SPRITE_ONLY;
    }

    @Override
    public StatementParamGateSideOnly onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public String getDescription() {
        return isSpecific ? LocaleUtil.localize("gate.parameter.redstone.gateSideOnly") : "";
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:redstoneGateSideOnly";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        return isSpecific ? POSSIBLE_SPECIFIC : POSSIBLE_ANY;
    }
}
