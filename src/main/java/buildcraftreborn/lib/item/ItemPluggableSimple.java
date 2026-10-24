/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.item;

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.transport.IItemPluggable;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition.IPluggableCreator;
import buildcraftreborn.lib.misc.SoundUtil;

/** An item that places a simple pluggable (one that needs nothing from the item) on the clicked side of a pipe. */
public class ItemPluggableSimple extends Item implements IItemPluggable {
    private static final IPlacementPredicate ALWAYS_CAN = (item, h, s) -> true;

    /** Allows placing only on pipes whose behaviour accepts redstone engine power on that side (wooden kinesis
     * pipes). */
    public static final IPlacementPredicate PIPE_BEHAVIOUR_ACCEPTS_RS_POWER = (item, pipeHolder, side) -> {
        IPipe pipe = pipeHolder.getPipe();
        if (pipe != null) {
            return pipe.getBehaviour().getCapability(MjAPI.CAP_REDSTONE_RECEIVER, side) != null;
        }
        return false;
    };

    private final Supplier<PluggableDefinition> definition;
    @Nullable
    private final IPluggableCreator creator;
    private final IPlacementPredicate canPlace;

    /** @param definition The pluggable definition (a supplier, as items are created before pluggables register).
     * @param creator What makes the pluggable, for definitions that read their pluggables from NBT (and so have no
     *            creator of their own). Null to use the definition's. */
    public ItemPluggableSimple(Properties properties, Supplier<PluggableDefinition> definition,
        @Nullable IPluggableCreator creator, @Nullable IPlacementPredicate canPlace) {
        super(properties);
        this.definition = definition;
        this.creator = creator;
        this.canPlace = canPlace == null ? ALWAYS_CAN : canPlace;
    }

    public ItemPluggableSimple(Properties properties, Supplier<PluggableDefinition> definition,
        @Nullable IPlacementPredicate canPlace) {
        this(properties, definition, null, canPlace);
    }

    public ItemPluggableSimple(Properties properties, Supplier<PluggableDefinition> definition) {
        this(properties, definition, null);
    }

    @Override
    public PipePluggable onPlace(ItemStack stack, IPipeHolder holder, Direction side, Player player,
        InteractionHand hand) {
        if (!canPlace.canPlace(stack, holder, side)) {
            return null;
        }
        PluggableDefinition def = definition.get();
        IPluggableCreator creator = this.creator != null ? this.creator : def.creator;
        if (creator == null) {
            return null;
        }
        SoundUtil.playBlockPlace(holder.getPipeWorld(), holder.getPipePos());
        return creator.createSimplePluggable(def, holder, side);
    }

    @FunctionalInterface
    public interface IPlacementPredicate {
        boolean canPlace(ItemStack stack, IPipeHolder holder, Direction side);
    }
}
