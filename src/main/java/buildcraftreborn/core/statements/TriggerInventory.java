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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

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

public class TriggerInventory extends BCStatement implements ITriggerExternal {
    public State state;

    public TriggerInventory(State state) {
        super("buildcraft:inventory." + state.name().toLowerCase(Locale.ROOT),
            "buildcraft.inventory." + state.name().toLowerCase(Locale.ROOT));
        this.state = state;
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.TRIGGER_INVENTORY.get(state);
    }

    @Override
    public int maxParameters() {
        return state == State.CONTAINS || state == State.SPACE ? 1 : 0;
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.trigger.inventory." + state.name().toLowerCase(Locale.ROOT));
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer container,
        IStatementParameter[] parameters) {
        ItemStack searchedStack = ItemStack.EMPTY;
        if (parameters != null && parameters.length >= 1 && parameters[0] != null) {
            searchedStack = parameters[0].getItemStack();
        }

        IItemHandler handler = CapUtil.getCapability(tile, Capabilities.ItemHandler.BLOCK, side.getOpposite());
        if (handler == null) {
            return false;
        }
        boolean hasSlots = false;
        boolean foundItems = false;
        boolean foundSpace = false;

        for (int i = 0; i < handler.getSlots(); i++) {
            hasSlots = true;
            ItemStack stack = handler.getStackInSlot(i);
            boolean matches = ItemStack.isSameItemSameComponents(stack, searchedStack);
            foundItems |= !stack.isEmpty() && (searchedStack.isEmpty() || matches);
            foundSpace |= (stack.isEmpty() || (matches && stack.getCount() < stack.getMaxStackSize()))
                && (searchedStack.isEmpty() || handler.insertItem(i, searchedStack, true).isEmpty());
        }

        if (!hasSlots) {
            return false;
        }
        switch (state) {
            case EMPTY:
                return !foundItems;
            case CONTAINS:
                return foundItems;
            case SPACE:
                return foundSpace;
            default:
                return !foundSpace;
        }
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_INVENTORY_ALL;
    }

    public enum State {
        EMPTY,
        CONTAINS,
        SPACE,
        FULL;

        public static final State[] VALUES = values();
    }
}
