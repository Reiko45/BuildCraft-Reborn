/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.plug;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import buildcraftreborn.api.core.render.EnumRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;

import buildcraftreborn.lib.misc.AdvancementUtil;

import buildcraftreborn.transport.BCTransportItems;
import buildcraftreborn.transport.client.model.key.KeyPlugBlocker;

public class PluggableBlocker extends PipePluggable {
    private static final AABB[] BOXES = new AABB[6];

    private static final ResourceLocation ADVANCEMENT_PLACE_PLUG = BuildCraftReborn.id("plugging_the_gap");

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 4 / 16.0;
        double max = 12 / 16.0;

        BOXES[Direction.DOWN.get3DDataValue()] = new AABB(min, ll, min, max, lu, max);
        BOXES[Direction.UP.get3DDataValue()] = new AABB(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.get3DDataValue()] = new AABB(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.get3DDataValue()] = new AABB(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.get3DDataValue()] = new AABB(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.get3DDataValue()] = new AABB(ul, min, min, uu, max, max);
    }

    public PluggableBlocker(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.get3DDataValue()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCTransportItems.PLUG_BLOCKER.get());
    }

    @Override
    public void onPlacedBy(Player player) {
        super.onPlacedBy(player);
        if (!holder.getPipeWorld().isClientSide && holder.getPipe().isConnected(side)) {
            AdvancementUtil.unlockAdvancement(player, ADVANCEMENT_PLACE_PLUG);
        }
    }

    @Override
    public PluggableModelKey getModelRenderKey(EnumRenderLayer layer) {
        if (layer == EnumRenderLayer.CUTOUT) return new KeyPlugBlocker(side);
        return null;
    }
}
