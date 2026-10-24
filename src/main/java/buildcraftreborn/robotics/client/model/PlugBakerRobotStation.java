/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.client.model;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.pluggable.IPluggableStaticBaker;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.client.model.ModelHolderStatic;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.robotics.station.PluggableRobotStation.EnumRobotStationState;

/** The robot station: a plate against the pipe and a stem out to the edge of the block, with lights for its state
 * (1.12's OBJ model, as a BuildCraft JSON model per state; the colourblind lights are separate models). */
public enum PlugBakerRobotStation implements IPluggableStaticBaker<KeyPlugRobotStation> {
    INSTANCE;

    private static final Map<EnumRobotStationState, ModelHolderStatic> MODELS = new EnumMap<>(
        EnumRobotStationState.class);
    private static final Map<EnumRobotStationState, ModelHolderStatic> MODELS_CB = new EnumMap<>(
        EnumRobotStationState.class);
    private static final Map<KeyPlugRobotStation, List<BakedQuad>> CACHED = new ConcurrentHashMap<>();
    private static final Map<KeyPlugRobotStation, List<BakedQuad>> CACHED_CB = new ConcurrentHashMap<>();

    static {
        for (EnumRobotStationState state : EnumRobotStationState.VALUES) {
            String name = "bcmodels/plugs/robot_station/" + state.name().toLowerCase(java.util.Locale.ROOT);
            MODELS.put(state, new ModelHolderStatic(BuildCraftReborn.id(name + ".json")));
            MODELS_CB.put(state, new ModelHolderStatic(BuildCraftReborn.id(name + "_cb.json")));
        }
    }

    /** Creates the model holders, so they load with the first resource reload. */
    public static void init() {}

    public static void onModelBake() {
        CACHED.clear();
        CACHED_CB.clear();
    }

    /** @return The quads of the station facing west (the way pluggable models are drawn). */
    public static MutableQuad[] getQuads(EnumRobotStationState state) {
        return (BCLibConfig.colourBlindMode() ? MODELS_CB : MODELS).get(state).getCutoutQuads();
    }

    @Override
    public List<BakedQuad> bake(KeyPlugRobotStation key) {
        boolean cb = BCLibConfig.colourBlindMode();
        return (cb ? CACHED_CB : CACHED).computeIfAbsent(key, k -> {
            MutableQuad[] quads = (cb ? MODELS_CB : MODELS).get(k.state).getCutoutQuads();
            List<BakedQuad> list = new ArrayList<>(quads.length);
            MutableQuad copy = new MutableQuad();
            for (MutableQuad q : quads) {
                copy.copyFrom(q);
                copy.rotate(Direction.WEST, k.side, 0.5f, 0.5f, 0.5f);
                copy.multShade();
                list.add(copy.toBakedBlock());
            }
            return list;
        });
    }
}
