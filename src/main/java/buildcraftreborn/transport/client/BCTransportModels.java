/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client;

import net.minecraft.core.Direction;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.pluggable.IPluggableStaticBaker;
import buildcraftreborn.lib.client.model.ModelHolderStatic;
import buildcraftreborn.lib.client.model.ModelHolderVariable;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.client.model.plug.PlugBakerSimple;
import buildcraftreborn.lib.expression.DefaultContexts;
import buildcraftreborn.lib.expression.FunctionContext;
import buildcraftreborn.lib.expression.node.value.NodeVariableObject;
import buildcraftreborn.transport.client.model.key.KeyPlugBlocker;
import buildcraftreborn.transport.client.model.key.KeyPlugPowerAdaptor;

/** The BuildCraft JSON models of transport's pluggables and the stripes pipe head
 * ({@code assets/buildcraftreborn/bcmodels/}). */
public final class BCTransportModels {
    public static final ModelHolderStatic BLOCKER = getStaticModel("plugs/blocker");
    public static final ModelHolderStatic POWER_ADAPTER = getStaticModel("plugs/power_adapter");

    private static final NodeVariableObject<Direction> STRIPES_DIRECTION;
    private static final ModelHolderVariable STRIPES;

    public static final IPluggableStaticBaker<KeyPlugBlocker> BAKER_PLUG_BLOCKER =
        new PlugBakerSimple<>(BLOCKER::getCutoutQuads);
    public static final IPluggableStaticBaker<KeyPlugPowerAdaptor> BAKER_PLUG_POWER_ADAPTOR =
        new PlugBakerSimple<>(POWER_ADAPTER::getCutoutQuads);

    static {
        FunctionContext fnCtx = DefaultContexts.createWithAll();
        STRIPES_DIRECTION = fnCtx.putVariableObject("side", Direction.class);
        STRIPES = new ModelHolderVariable(location("pipes/stripes"), fnCtx);
    }

    private BCTransportModels() {}

    static void init() {
        // Creates the holders (in the static initialiser), so they load with the first resource reload.
    }

    private static String location(String str) {
        return BuildCraftReborn.id("bcmodels/" + str + ".json").toString();
    }

    private static ModelHolderStatic getStaticModel(String str) {
        return new ModelHolderStatic(location(str));
    }

    /** Only called on the render thread. */
    public static MutableQuad[] getStripesDynQuads(Direction side) {
        STRIPES_DIRECTION.value = side;
        return STRIPES.getCutoutQuads();
    }
}
