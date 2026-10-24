/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.pluggable.IPluggableStaticBaker;
import buildcraftreborn.lib.client.model.ModelHolderStatic;
import buildcraftreborn.lib.client.model.ModelHolderVariable;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.client.model.plug.PlugBakerSimple;
import buildcraftreborn.lib.expression.DefaultContexts;
import buildcraftreborn.lib.expression.FunctionContext;
import buildcraftreborn.lib.expression.node.value.NodeVariableBoolean;
import buildcraftreborn.lib.expression.node.value.NodeVariableObject;
import buildcraftreborn.lib.misc.data.ModelVariableData;
import buildcraftreborn.silicon.client.model.key.KeyPlugLightSensor;
import buildcraftreborn.silicon.client.model.key.KeyPlugPulsar;
import buildcraftreborn.silicon.client.model.key.KeyPlugTimer;
import buildcraftreborn.silicon.gate.GateVariant;
import buildcraftreborn.silicon.plug.PluggableGate;
import buildcraftreborn.silicon.plug.PluggablePulsar;

/** The BuildCraft JSON models of silicon's pluggables ({@code assets/buildcraftreborn/bcmodels/plugs/}).
 * <p>
 * The variable models share their variables, so everything that sets them and reads quads holds the lock on this
 * class: pipe models are baked on the chunk builder threads as well as the render thread. */
public final class BCSiliconModels {
    public static final ModelHolderStatic LIGHT_SENSOR = getStaticModel("plugs/light_sensor");
    public static final ModelHolderStatic TIMER = getStaticModel("plugs/timer");

    public static final ModelHolderVariable GATE_STATIC = getModel("plugs/gate", PluggableGate.MODEL_FUNC_CTX_STATIC);
    public static final ModelHolderVariable GATE_DYNAMIC = getModel("plugs/gate_dynamic",
        PluggableGate.MODEL_FUNC_CTX_DYNAMIC);
    private static final ModelVariableData GATE_VAR_DATA_STATIC = new ModelVariableData();

    private static final ModelHolderVariable LENS, FILTER;
    private static final NodeVariableBoolean LENS_HAS_COLOUR;
    private static final NodeVariableObject<DyeColor> LENS_COLOUR;
    private static final NodeVariableObject<Direction> LENS_SIDE;

    public static final ModelHolderStatic PULSAR_STATIC = getStaticModel("plugs/pulsar_static");
    public static final ModelHolderVariable PULSAR_DYNAMIC = getModel("plugs/pulsar_dynamic",
        PluggablePulsar.MODEL_FUNC_CTX);

    public static final IPluggableStaticBaker<KeyPlugPulsar> BAKER_PLUG_PULSAR =
        new PlugBakerSimple<>(PULSAR_STATIC::getCutoutQuads);
    public static final IPluggableStaticBaker<KeyPlugTimer> BAKER_PLUG_TIMER =
        new PlugBakerSimple<>(TIMER::getCutoutQuads);
    public static final IPluggableStaticBaker<KeyPlugLightSensor> BAKER_PLUG_LIGHT_SENSOR =
        new PlugBakerSimple<>(LIGHT_SENSOR::getCutoutQuads);

    static {
        FunctionContext fnCtx = DefaultContexts.createWithAll();
        LENS_COLOUR = fnCtx.putVariableObject("colour", DyeColor.class);
        LENS_SIDE = fnCtx.putVariableObject("side", Direction.class);
        LENS_HAS_COLOUR = fnCtx.putVariableBoolean("has_colour");
        LENS = getModel("plugs/lens", fnCtx);
        FILTER = getModel("plugs/filter", fnCtx);
    }

    private BCSiliconModels() {}

    static void init() {
        // Creates the holders (in the static initialiser), so they load with the first resource reload.
    }

    private static String location(String str) {
        return BuildCraftReborn.id("bcmodels/" + str + ".json").toString();
    }

    private static ModelHolderStatic getStaticModel(String str) {
        return new ModelHolderStatic(location(str));
    }

    private static ModelHolderVariable getModel(String str, FunctionContext fnCtx) {
        return new ModelHolderVariable(location(str), fnCtx);
    }

    /** @return A copy of the gate's static quads (the shared variables are changed by the next call). */
    public static synchronized MutableQuad[] getGateStaticQuads(Direction side, GateVariant variant) {
        PluggableGate.setClientModelVariables(side, variant);
        if (GATE_VAR_DATA_STATIC.hasNoNodes()) {
            GATE_VAR_DATA_STATIC.setNodes(GATE_STATIC.createTickableNodes());
        }
        GATE_VAR_DATA_STATIC.refresh();
        return copy(GATE_STATIC.getCutoutQuads());
    }

    /** Must be called with the lock on this class held. */
    private static void setupLensVariables(ModelHolderVariable model, Direction side, @Nullable DyeColor colour) {
        LENS_COLOUR.value = colour == null ? DyeColor.WHITE : colour;
        LENS_SIDE.value = side;
        LENS_HAS_COLOUR.value = colour != null;
        ModelVariableData varData = new ModelVariableData();
        varData.setNodes(model.createTickableNodes());
        varData.tick();
        varData.refresh();
    }

    public static synchronized MutableQuad[] getLensCutoutQuads(Direction side, @Nullable DyeColor colour) {
        setupLensVariables(LENS, side, colour);
        return copy(LENS.getCutoutQuads());
    }

    public static synchronized MutableQuad[] getLensTranslucentQuads(Direction side, @Nullable DyeColor colour) {
        setupLensVariables(LENS, side, colour);
        return copy(LENS.getTranslucentQuads());
    }

    public static synchronized MutableQuad[] getFilterCutoutQuads(Direction side, @Nullable DyeColor colour) {
        setupLensVariables(FILTER, side, colour);
        return copy(FILTER.getCutoutQuads());
    }

    public static synchronized MutableQuad[] getFilterTranslucentQuads(Direction side, @Nullable DyeColor colour) {
        setupLensVariables(FILTER, side, colour);
        return copy(FILTER.getTranslucentQuads());
    }

    private static MutableQuad[] copy(MutableQuad[] from) {
        MutableQuad[] to = new MutableQuad[from.length];
        for (int i = 0; i < from.length; i++) {
            to[i] = new MutableQuad(from[i]);
        }
        return to;
    }
}
