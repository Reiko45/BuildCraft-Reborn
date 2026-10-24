/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;


import buildcraftreborn.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraftreborn.api.transport.pluggable.IPluggableStaticBaker;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;

public enum PipeApiClient {
    INSTANCE;

    public static IClientRegistry registry;

    public interface IClientRegistry {

        /** Registers a dynamic renderer for the given pipe flow. Most {@link PipeFlow} types will have no use for
         * this. */
        <F extends PipeFlow> void registerRenderer(Class<? extends F> flowClass, IPipeFlowRenderer<F> renderer);

        /** Registers a dynamic renderer for the given pipe behaviour. Most {@link PipeBehaviour} types will have no use
         * for this. */
        <B extends PipeBehaviour> void registerRenderer(Class<? extends B> behaviourClass,
            IPipeBehaviourRenderer<B> renderer);

        <P extends PipePluggable> void registerRenderer(Class<? extends P> plugClass, IPlugDynamicRenderer<P> renderer);

        <P extends PluggableModelKey> void registerBaker(Class<? extends P> keyClass,
            IPluggableStaticBaker<P> renderer);
    }
}
