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
import buildcraftreborn.api.items.IList;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.ITriggerExternal;
import buildcraftreborn.api.statements.StatementParameterItemStack;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.lib.misc.StringUtilBC;
import buildcraftreborn.lib.tile.item.IItemHandlerFiltered;

public class TriggerInventoryLevel extends BCStatement implements ITriggerExternal {
    public TriggerType type;

    public TriggerInventoryLevel(TriggerType type) {
        super("buildcraft:inventorylevel." + type.name().toLowerCase(Locale.ROOT),
            "buildcraft.inventorylevel." + type.name().toLowerCase(Locale.ROOT),
            "buildcraft.filteredBuffer." + type.name().toLowerCase(Locale.ROOT));
        this.type = type;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public ISprite getSprite() {
        return BCCoreSprites.TRIGGER_INVENTORY_LEVEL.get(type);
    }

    @Override
    public String getDescription() {
        return StringUtilBC.formatSafe(LocaleUtil.localize("gate.trigger.inventorylevel.below"),
            (int) (type.level * 100));
    }

    @Override
    public boolean isTriggerActive(BlockEntity tile, Direction side, IStatementContainer container,
        IStatementParameter[] parameters) {
        IItemHandler itemHandler = CapUtil.getCapability(tile, Capabilities.ItemHandler.BLOCK, side.getOpposite());
        if (itemHandler == null) {
            return false;
        }
        StatementParameterItemStack param = getParam(0, parameters, new StatementParameterItemStack());
        ItemStack searchStack = param.getItemStack();
        int itemSpace = 0;
        int foundItems = 0;
        for (int slot = 0; slot < itemHandler.getSlots(); slot++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(slot);
            if (stackInSlot.isEmpty()) {
                if (searchStack.isEmpty()) {
                    itemSpace += itemHandler.getSlotLimit(slot);
                } else if (searchStack.getItem() instanceof IList) {
                    // Unfortunately lists are too generic to work properly
                    // without a simple filtered inventory.
                    ItemStack filter = itemHandler instanceof IItemHandlerFiltered filtered ? filtered.getFilter(slot)
                        : ItemStack.EMPTY;
                    if (StackUtil.matchesStackOrList(searchStack, filter)) {
                        itemSpace += Math.min(filter.getMaxStackSize(), itemHandler.getSlotLimit(slot));
                    }
                } else {
                    int count = Math.min(itemHandler.getSlotLimit(slot), searchStack.getMaxStackSize());
                    ItemStack leftOver = itemHandler.insertItem(slot, searchStack.copyWithCount(count), true);
                    itemSpace += count - leftOver.getCount();
                }
            } else if (searchStack.isEmpty() || StackUtil.matchesStackOrList(searchStack, stackInSlot)) {
                itemSpace += Math.min(stackInSlot.getMaxStackSize(), itemHandler.getSlotLimit(slot));
                foundItems += stackInSlot.getCount();
            }
        }

        if (itemSpace > 0) {
            float percentage = foundItems / (float) itemSpace;
            return percentage < type.level;
        }
        return false;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    @Override
    public IStatement[] getPossible() {
        return BCCoreStatements.TRIGGER_INVENTORY_ALL;
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
