/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import buildcraftreborn.api.transport.pipe.IPipeBehaviourRenderer;
import buildcraftreborn.api.transport.pipe.IPipeFlowRenderer;
import buildcraftreborn.api.transport.pipe.PipeApiClient.IClientRegistry;
import buildcraftreborn.api.transport.pipe.PipeBehaviour;
import buildcraftreborn.api.transport.pipe.PipeFlow;
import buildcraftreborn.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraftreborn.api.transport.pluggable.IPluggableStaticBaker;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;

/** The renderers for pipe flows, behaviours and pluggables, and the bakers of pluggable models. The bakers are read
 * from chunk building threads, so the maps are concurrent. */
public enum PipeRegistryClient implements IClientRegistry {
    INSTANCE;

    private final Map<Class<?>, IPipeFlowRenderer<?>> flowRenderMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, IPipeBehaviourRenderer<?>> behaviourRenderMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, IPlugDynamicRenderer<?>> plugRenderMap = new ConcurrentHashMap<>();
    private final Map<Class<?>, IPluggableStaticBaker<?>> plugBakerMap = new ConcurrentHashMap<>();

    @Override
    public <F extends PipeFlow> void registerRenderer(Class<? extends F> flowClass, IPipeFlowRenderer<F> renderer) {
        flowRenderMap.put(flowClass, renderer);
    }

    @Override
    public <B extends PipeBehaviour> void registerRenderer(Class<? extends B> behaviourClass,
        IPipeBehaviourRenderer<B> renderer) {
        behaviourRenderMap.put(behaviourClass, renderer);
    }

    @Override
    public <P extends PipePluggable> void registerRenderer(Class<? extends P> plugClass,
        IPlugDynamicRenderer<P> renderer) {
        plugRenderMap.put(plugClass, renderer);
    }

    @Override
    public <P extends PluggableModelKey> void registerBaker(Class<? extends P> keyClass,
        IPluggableStaticBaker<P> renderer) {
        plugBakerMap.put(keyClass, renderer);
    }

    @SuppressWarnings("unchecked")
    public static <F extends PipeFlow> IPipeFlowRenderer<F> getFlowRenderer(F flow) {
        return (IPipeFlowRenderer<F>) INSTANCE.flowRenderMap.get(flow.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <B extends PipeBehaviour> IPipeBehaviourRenderer<B> getBehaviourRenderer(B behaviour) {
        return (IPipeBehaviourRenderer<B>) INSTANCE.behaviourRenderMap.get(behaviour.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <P extends PipePluggable> IPlugDynamicRenderer<P> getPlugRenderer(P plug) {
        return (IPlugDynamicRenderer<P>) INSTANCE.plugRenderMap.get(plug.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <P extends PluggableModelKey> IPluggableStaticBaker<P> getPlugBaker(P key) {
        return (IPluggableStaticBaker<P>) INSTANCE.plugBakerMap.get(key.getClass());
    }
}
