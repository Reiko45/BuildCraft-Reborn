/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;

import net.neoforged.neoforge.common.NeoForge;

import buildcraftreborn.api.core.EnumHandlerPriority;
import buildcraftreborn.api.transport.pipe.EnumPipeColourType;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeConnectionAPI;
import buildcraftreborn.api.transport.pipe.PipeFlowType;
import buildcraftreborn.transport.pipe.PipeRegistry;
import buildcraftreborn.transport.pipe.StripesRegistry;
import buildcraftreborn.transport.pipe.flow.PipeFlowFluids;
import buildcraftreborn.transport.pipe.flow.PipeFlowItems;
import buildcraftreborn.transport.pipe.flow.PipeFlowPower;
import buildcraftreborn.transport.pipe.flow.PipeFlowRedstoneFlux;
import buildcraftreborn.transport.pipe.flow.PipeFlowStructure;
import buildcraftreborn.transport.stripes.PipeExtensionManager;
import buildcraftreborn.transport.stripes.StripesHandlerDispenser;
import buildcraftreborn.transport.stripes.StripesHandlerEntityInteract;
import buildcraftreborn.transport.stripes.StripesHandlerHoe;
import buildcraftreborn.transport.stripes.StripesHandlerMinecartDestroy;
import buildcraftreborn.transport.stripes.StripesHandlerPipes;
import buildcraftreborn.transport.stripes.StripesHandlerPlaceBlock;
import buildcraftreborn.transport.stripes.StripesHandlerPlant;
import buildcraftreborn.transport.stripes.StripesHandlerShears;
import buildcraftreborn.transport.stripes.StripesHandlerUse;

/** Sets up the pipe API's registries and flow types, and the stripes pipe's handlers. */
public final class BCTransportRegistries {
    private BCTransportRegistries() {}

    public static void init() {
        PipeApi.pipeRegistry = PipeRegistry.INSTANCE;
        PipeApi.stripeRegistry = StripesRegistry.INSTANCE;
        PipeApi.extensionManager = PipeExtensionManager.INSTANCE;
        NeoForge.EVENT_BUS.register(PipeExtensionManager.INSTANCE);

        PipeApi.flowItems = new PipeFlowType(PipeFlowItems::new, PipeFlowItems::new);
        PipeApi.flowFluids = new PipeFlowType(PipeFlowFluids::new, PipeFlowFluids::new);
        PipeApi.flowPower = new PipeFlowType(PipeFlowPower::new, PipeFlowPower::new);
        PipeApi.flowRf = new PipeFlowType(PipeFlowRedstoneFlux::new, PipeFlowRedstoneFlux::new);
        PipeApi.flowStructure = new PipeFlowType(PipeFlowStructure::new, PipeFlowStructure::new);
        PipeApi.flowStructure.fallbackColourType = EnumPipeColourType.BORDER_OUTER;
    }

    /** Called in common setup, once blocks are registered. */
    public static void commonSetup() {
        PipeConnectionAPI.registerConnection(Blocks.BREWING_STAND,
            (level, pos, face, state) -> face.getAxis().isHorizontal() ? 4 / 16F : 0);

        // Item use stripes handlers
        PipeApi.stripeRegistry.addHandler(StripesHandlerPlant.INSTANCE);
        PipeApi.stripeRegistry.addHandler(StripesHandlerShears.INSTANCE);
        PipeApi.stripeRegistry.addHandler(new StripesHandlerPipes());
        PipeApi.stripeRegistry.addHandler(StripesHandlerEntityInteract.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerHoe.INSTANCE);
        PipeApi.stripeRegistry.addHandler(StripesHandlerDispenser.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerPlaceBlock.INSTANCE, EnumHandlerPriority.LOW);
        PipeApi.stripeRegistry.addHandler(StripesHandlerUse.INSTANCE, EnumHandlerPriority.LOW);

        // Block breaking stripes handlers
        PipeApi.stripeRegistry.addHandler(StripesHandlerMinecartDestroy.INSTANCE);

        PipeApi.extensionManager.registerRetractionPipe(BCTransportPipes.voidItem);
    }
}
