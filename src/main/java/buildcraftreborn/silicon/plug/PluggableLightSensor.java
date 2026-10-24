/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.plug;

import net.minecraft.world.item.ItemStack;
import buildcraftreborn.api.core.render.EnumRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeEventStatement;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;

import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.BCSiliconStatements;
import buildcraftreborn.silicon.client.model.key.KeyPlugLightSensor;

public class PluggableLightSensor extends PipePluggable {

    private static final AABB[] BOXES = new AABB[6];

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 5 / 16.0;
        double max = 11 / 16.0;

        BOXES[Direction.DOWN.ordinal()] = new AABB(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = new AABB(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = new AABB(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = new AABB(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = new AABB(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = new AABB(ul, min, min, uu, max, max);
    }

    public PluggableLightSensor(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    // PipePluggable

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.ordinal()];
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCSiliconItems.PLUG_LIGHT_SENSOR.get());
    }

    @Override
    public PluggableModelKey getModelRenderKey(EnumRenderLayer layer) {
        if (layer == EnumRenderLayer.CUTOUT) return new KeyPlugLightSensor(side);
        return null;
    }

    @PipeEventHandler
    public void addInternalTriggers(PipeEventStatement.AddTriggerInternalSided event) {
        if (event.side == this.side) {
            event.triggers.add(BCSiliconStatements.TRIGGER_LIGHT_LOW);
            event.triggers.add(BCSiliconStatements.TRIGGER_LIGHT_HIGH);
        }
    }
}
