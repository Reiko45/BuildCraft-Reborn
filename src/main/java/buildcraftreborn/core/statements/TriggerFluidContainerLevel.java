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
import buildcraftreborn.lib.misc.StringUtilBC;

public class TriggerFluidContainerLevel extends BCStatement implements ITriggerExternal {
    public final TriggerType type;

    public TriggerFluidContainerLevel(TriggerType type) {
        super("buildcraft:fluid." + type.name().toLowerCase(Locale.ROOT),
            "buildcraft.fluid." + type.name().toLowerCase(Locale.ROOT));
        this.type = type;
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.TRIGGER_FLUID_LEVEL.get(type);
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public String getDescription() {
        return StringUtilBC.formatSafe(LocaleUtil.localize("gate.trigger.fluidlevel.below"), (int) (type.level * 100));
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer statementContainer,
        IStatementParameter[] parameters) {
        IFluidHandler handler = CapUtil.getCapability(tile, Capabilities.FluidHandler.BLOCK, side.getOpposite());
        if (handler == null || handler.getTanks() == 0) {
            return false;
        }
        FluidStack searchedFluid = TriggerFluidContainer.getSearchedFluid(parameters);

        for (int t = 0; t < handler.getTanks(); t++) {
            FluidStack fluid = handler.getFluidInTank(t);
            if (fluid.isEmpty()) {
                return searchedFluid.isEmpty() || handler.fill(searchedFluid, FluidAction.SIMULATE) > 0;
            }
            if (searchedFluid.isEmpty() || FluidStack.isSameFluidSameComponents(searchedFluid, fluid)) {
                float percentage = fluid.getAmount() / (float) handler.getTankCapacity(t);
                return percentage < type.level;
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

    public enum TriggerType {
        BELOW25(0.25F),
        BELOW50(0.5F),
        BELOW75(0.75F);

        public static final TriggerType[] VALUES = values();
        public final float level;

        TriggerType(float level) {
            this.level = level;
        }
    }
}
