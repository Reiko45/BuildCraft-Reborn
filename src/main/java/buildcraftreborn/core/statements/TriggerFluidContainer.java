/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import java.util.Locale;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerExternal;
import buildcraftreborn.api.statements.StatementParameterItemStack;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.LocaleUtil;

public class TriggerFluidContainer extends BCStatement implements ITriggerExternal {
    public State state;

    public TriggerFluidContainer(State state) {
        super("buildcraft:fluid." + state.name().toLowerCase(Locale.ROOT),
            "buildcraft.fluid." + state.name().toLowerCase(Locale.ROOT));
        this.state = state;
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.TRIGGER_FLUID.get(state);
    }

    @Override
    public int maxParameters() {
        return state == State.CONTAINS || state == State.SPACE ? 1 : 0;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.fluid." + state.name().toLowerCase(Locale.ROOT));
    }

    /** @return The fluid the first parameter's item holds (amount 1), or empty. */
    static FluidStack getSearchedFluid(IStatementParameter[] parameters) {
        if (parameters != null && parameters.length >= 1 && parameters[0] != null
            && !parameters[0].getItemStack().isEmpty()) {
            return FluidUtil.getFluidContained(parameters[0].getItemStack()).map(f -> f.copyWithAmount(1))
                .orElse(FluidStack.EMPTY);
        }
        return FluidStack.EMPTY;
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer statementContainer,
        IStatementParameter[] parameters) {
        IFluidHandler handler = CapUtil.getCapability(tile, Capabilities.FluidHandler.BLOCK, side.getOpposite());
        if (handler == null || handler.getTanks() == 0) {
            return false;
        }
        FluidStack searchedFluid = getSearchedFluid(parameters);

        switch (state) {
            case EMPTY: {
                return handler.drain(1, FluidAction.SIMULATE).isEmpty();
            }
            case CONTAINS: {
                for (int t = 0; t < handler.getTanks(); t++) {
                    FluidStack fluid = handler.getFluidInTank(t);
                    if (!fluid.isEmpty()
                        && (searchedFluid.isEmpty() || FluidStack.isSameFluidSameComponents(searchedFluid, fluid))) {
                        return true;
                    }
                }
                return false;
            }
            case SPACE: {
                if (searchedFluid.isEmpty()) {
                    for (int t = 0; t < handler.getTanks(); t++) {
                        if (handler.getFluidInTank(t).getAmount() < handler.getTankCapacity(t)) {
                            return true;
                        }
                    }
                    return false;
                }
                return handler.fill(searchedFluid, FluidAction.SIMULATE) > 0;
            }
            case FULL: {
                if (searchedFluid.isEmpty()) {
                    for (int t = 0; t < handler.getTanks(); t++) {
                        if (handler.getFluidInTank(t).getAmount() < handler.getTankCapacity(t)) {
                            return false;
                        }
                    }
                    return true;
                }
                return handler.fill(searchedFluid, FluidAction.SIMULATE) <= 0;
            }
        }
        return false;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_FLUID_ALL;
    }

    public enum State {
        EMPTY,
        CONTAINS,
        SPACE,
        FULL;

        public static final State[] VALUES = values();
    }
}
