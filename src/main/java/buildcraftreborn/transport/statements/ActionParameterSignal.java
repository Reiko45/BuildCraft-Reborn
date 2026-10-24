/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.statements;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;


import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.gates.IGate;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementMouseClick;

import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.lib.misc.StringUtilBC;

import buildcraftreborn.transport.BCTransportSprites;

public class ActionParameterSignal implements IStatementParameter {

    public static final ActionParameterSignal EMPTY = new ActionParameterSignal(null);
    private static final Map<DyeColor, ActionParameterSignal> SIGNALS;

    static {
        SIGNALS = new EnumMap<>(DyeColor.class);
        for (DyeColor colour : ColourUtil.COLOURS) {
            SIGNALS.put(colour, new ActionParameterSignal(colour));
        }
    }

    @Nullable
    public final DyeColor colour;

    private ActionParameterSignal(DyeColor colour) {
        this.colour = colour;
    }

    public static ActionParameterSignal get(DyeColor colour) {
        return colour == null ? EMPTY : SIGNALS.get(colour);
    }

    public static ActionParameterSignal readFromNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        if (nbt.contains("color", net.minecraft.nbt.Tag.TAG_ANY_NUMERIC)) {
            return get(DyeColor.byId(nbt.getByte("color")));
        }
        return EMPTY;
    }

    @Override
    public void writeToNbt(CompoundTag nbt, HolderLookup.Provider registries) {
        DyeColor c = colour;
        if (c != null) {
            nbt.putByte("color", (byte) c.getId());
        }
    }

    @Nullable
    public DyeColor getColor() {
        return colour;
    }

    @Override
    public ISprite getSprite() {
        DyeColor c = colour;
        if (c == null) {
            return null;
        } else {
            return BCTransportSprites.getPipeSignal(true, c);
        }
    }

    @Override
    public ActionParameterSignal onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        return null;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof ActionParameterSignal) {
            ActionParameterSignal param = (ActionParameterSignal) object;

            return param.getColor() == getColor();
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(getColor());
    }

    @Override
    public String getDescription() {
        DyeColor c = colour;
        if (c == null) {
            return null;
        }
        String format = LocaleUtil.localize("gate.action.pipe.wire");
        Object[] args = { ColourUtil.getTextFullTooltip(c) };
        return StringUtilBC.formatSafe(format, args);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:pipeWireAction";
    }

    @Override
    public IStatementParameter rotateLeft() {
        return this;
    }

    
    @Override
    public ItemStack getItemStack() {
        return ItemStack.EMPTY;
    }

    @Override
    public IStatementParameter[] getPossible(IStatementContainer source) {
        if (!(source instanceof IGate)) {
            return null;
        }
        IGate gate = (IGate) source;
        List<IStatementParameter> poss = new ArrayList<>(1 + ColourUtil.COLOURS.length);
        poss.add(EMPTY);
        for (DyeColor c : ColourUtil.COLOURS) {
            if (TriggerPipeSignal.doesGateHaveColour(gate, c)) {
                poss.add(get(c));
            }
        }
        return poss.toArray(new IStatementParameter[poss.size()]);
    }
}
