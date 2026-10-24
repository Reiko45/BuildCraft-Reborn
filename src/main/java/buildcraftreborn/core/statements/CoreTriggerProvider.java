/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.statements;

import java.util.Collection;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.ITriggerExternal;
import buildcraftreborn.api.statements.ITriggerInternal;
import buildcraftreborn.api.statements.ITriggerInternalSided;
import buildcraftreborn.api.statements.ITriggerProvider;
import buildcraftreborn.api.statements.containers.IRedstoneStatementContainer;
import buildcraftreborn.api.tiles.TilesAPI;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.misc.CapUtil;

public enum CoreTriggerProvider implements ITriggerProvider {
    INSTANCE;

    @Override
    public void addInternalTriggers(Collection<ITriggerInternal> res, IStatementContainer container) {
        res.add(BCCoreStatements.TRIGGER_TRUE);
        if (container instanceof IRedstoneStatementContainer) {
            res.add(BCCoreStatements.TRIGGER_REDSTONE_ACTIVE);
            res.add(BCCoreStatements.TRIGGER_REDSTONE_INACTIVE);
        }

        if (TriggerPower.isTriggeringTile(container.getTile())) {
            res.add(BCCoreStatements.TRIGGER_POWER_HIGH);
            res.add(BCCoreStatements.TRIGGER_POWER_LOW);
        }
    }

    @Override
    public void addInternalSidedTriggers(Collection<ITriggerInternalSided> res, IStatementContainer container,
        Direction side) {}

    @Override
    public void addExternalTriggers(Collection<ITriggerExternal> res, Direction side, BlockEntity tile) {
        if (TriggerPower.isTriggeringTile(tile, side.getOpposite())) {
            res.add(BCCoreStatements.TRIGGER_POWER_HIGH);
            res.add(BCCoreStatements.TRIGGER_POWER_LOW);
        }

        boolean blockInventoryTriggers = false;
        boolean blockFluidHandlerTriggers = false;

        if (tile instanceof IBlockDefaultTriggers defaults) {
            blockInventoryTriggers = defaults.blockInventoryTriggers(side);
            blockFluidHandlerTriggers = defaults.blockFluidHandlerTriggers(side);
        }

        if (!blockInventoryTriggers) {
            IItemHandler itemHandler = CapUtil.getCapability(tile, Capabilities.ItemHandler.BLOCK, side.getOpposite());
            if (itemHandler != null) {
                res.add(BCCoreStatements.TRIGGER_INVENTORY_EMPTY);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_SPACE);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_CONTAINS);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_FULL);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_BELOW_25);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_BELOW_50);
                res.add(BCCoreStatements.TRIGGER_INVENTORY_BELOW_75);
            }
        }

        if (!blockFluidHandlerTriggers) {
            IFluidHandler fluidHandler =
                CapUtil.getCapability(tile, Capabilities.FluidHandler.BLOCK, side.getOpposite());
            if (fluidHandler != null && fluidHandler.getTanks() > 0) {
                res.add(BCCoreStatements.TRIGGER_FLUID_EMPTY);
                res.add(BCCoreStatements.TRIGGER_FLUID_SPACE);
                res.add(BCCoreStatements.TRIGGER_FLUID_CONTAINS);
                res.add(BCCoreStatements.TRIGGER_FLUID_FULL);
                res.add(BCCoreStatements.TRIGGER_FLUID_BELOW_25);
                res.add(BCCoreStatements.TRIGGER_FLUID_BELOW_50);
                res.add(BCCoreStatements.TRIGGER_FLUID_BELOW_75);
            }
        }

        if (CapUtil.hasCapability(tile, TilesAPI.CAP_HAS_WORK, null)) {
            res.add(BCCoreStatements.TRIGGER_MACHINE_ACTIVE);
            res.add(BCCoreStatements.TRIGGER_MACHINE_INACTIVE);
        }

        if (TriggerEnginePowerStage.isTriggeringTile(tile)) {
            res.add(BCCoreStatements.TRIGGER_POWER_BLUE);
            res.add(BCCoreStatements.TRIGGER_POWER_GREEN);
            res.add(BCCoreStatements.TRIGGER_POWER_YELLOW);
            res.add(BCCoreStatements.TRIGGER_POWER_RED);
            res.add(BCCoreStatements.TRIGGER_POWER_OVERHEAT);
        }
    }
}
