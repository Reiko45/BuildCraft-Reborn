/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client;

import java.util.Map;

import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import buildcraftreborn.api.transport.pipe.PipeApiClient;
import buildcraftreborn.lib.client.model.ModelLazy;
import buildcraftreborn.lib.client.model.ModelPluggableItem;
import buildcraftreborn.lib.misc.ExpressionCompat;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.transport.BCTransportItems;
import buildcraftreborn.transport.client.gui.GuiDiamondPipe;
import buildcraftreborn.transport.client.gui.GuiDiamondWoodPipe;
import buildcraftreborn.transport.client.gui.GuiEmzuliPipe_BC8;
import buildcraftreborn.transport.client.gui.GuiFilteredBuffer;
import buildcraftreborn.transport.client.model.ModelPipe;
import buildcraftreborn.transport.client.model.ModelPipeItem;
import buildcraftreborn.transport.client.model.PipeModelCaches;
import buildcraftreborn.transport.client.model.key.KeyPlugBlocker;
import buildcraftreborn.transport.client.model.key.KeyPlugPowerAdaptor;
import buildcraftreborn.transport.client.render.PipeBehaviourRendererStripes;
import buildcraftreborn.transport.client.render.PipeFlowRendererFluids;
import buildcraftreborn.transport.client.render.PipeFlowRendererItems;
import buildcraftreborn.transport.client.render.PipeFlowRendererPower;
import buildcraftreborn.transport.client.render.PipeFlowRendererRf;
import buildcraftreborn.transport.client.render.PipeWireRenderer;
import buildcraftreborn.transport.client.render.RenderPipeHolder;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraftreborn.transport.pipe.flow.PipeFlowFluids;
import buildcraftreborn.transport.pipe.flow.PipeFlowItems;
import buildcraftreborn.transport.pipe.flow.PipeFlowPower;
import buildcraftreborn.transport.pipe.flow.PipeFlowRedstoneFlux;

/** Client setup for the transport module: pipe models, the pipe renderer and the transport screens.
 * <p>
 * 1.21: every pipe is one block (and every pipe item one item model) whose model is swapped for a code-built one
 * when models bake; the JSON files only exist so nothing is reported missing. */
public final class BCTransportClient {
    private BCTransportClient() {}

    public static void init(IEventBus modBus) {
        ExpressionCompat.setup();
        PipeApiClient.registry = PipeRegistryClient.INSTANCE;
        BCTransportModels.init();

        PipeApiClient.registry.registerBaker(KeyPlugBlocker.class, BCTransportModels.BAKER_PLUG_BLOCKER);
        PipeApiClient.registry.registerBaker(KeyPlugPowerAdaptor.class, BCTransportModels.BAKER_PLUG_POWER_ADAPTOR);

        PipeApiClient.registry.registerRenderer(PipeFlowItems.class, PipeFlowRendererItems.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowFluids.class, PipeFlowRendererFluids.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowPower.class, PipeFlowRendererPower.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeFlowRedstoneFlux.class, PipeFlowRendererRf.INSTANCE);
        PipeApiClient.registry.registerRenderer(PipeBehaviourStripes.class, PipeBehaviourRendererStripes.INSTANCE);

        modBus.addListener(BCTransportClient::registerScreens);
        modBus.addListener(BCTransportClient::registerRenderers);
        modBus.addListener(BCTransportClient::registerBlockColours);
        modBus.addListener(BCTransportClient::onModifyBakingResult);
        modBus.addListener(BCTransportClient::onBakingCompleted);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BCTransportBlocks.FILTERED_BUFFER_MENU.get(), GuiFilteredBuffer::new);
        event.register(BCTransportBlocks.PIPE_DIAMOND_MENU.get(), GuiDiamondPipe::new);
        event.register(BCTransportBlocks.PIPE_DIAMOND_WOOD_MENU.get(), GuiDiamondWoodPipe::new);
        event.register(BCTransportBlocks.PIPE_EMZULI_MENU.get(), GuiEmzuliPipe_BC8::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BCTransportBlocks.PIPE_HOLDER_TILE.get(), RenderPipeHolder::new);
    }

    private static void registerBlockColours(RegisterColorHandlersEvent.Block event) {
        event.register(PipeBlockColours.INSTANCE, BCTransportBlocks.PIPE_HOLDER.get());
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        for (BlockState state : BCTransportBlocks.PIPE_HOLDER.get().getStateDefinition().getPossibleStates()) {
            models.put(BlockModelShaper.stateToModelLocation(state), ModelPipe.INSTANCE);
        }
        for (var pipe : BCTransportItems.PIPES.values()) {
            models.put(ModelResourceLocation.inventory(pipe.getId()), ModelPipeItem.INSTANCE);
        }
        models.put(ModelResourceLocation.inventory(BCTransportItems.PLUG_BLOCKER.getId()),
            new ModelLazy(() -> new ModelPluggableItem(BCTransportModels.BLOCKER.getCutoutQuads())));
        models.put(ModelResourceLocation.inventory(BCTransportItems.PLUG_POWER_ADAPTOR.getId()),
            new ModelLazy(() -> new ModelPluggableItem(BCTransportModels.POWER_ADAPTER.getCutoutQuads())));
    }

    private static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        PipeModelCaches.clearAll();
        ModelPipeItem.clearCache();
        PipeWireRenderer.clearWireCache();
    }
}
