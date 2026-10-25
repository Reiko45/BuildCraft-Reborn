/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client;

import java.util.Map;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.pipe.PipeApiClient;
import buildcraftreborn.lib.client.model.ModelLazy;
import buildcraftreborn.lib.client.model.ModelPluggableItem;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.client.gui.GuiAdvancedCraftingTable;
import buildcraftreborn.silicon.client.gui.GuiAssemblyTable;
import buildcraftreborn.silicon.client.gui.GuiGate;
import buildcraftreborn.silicon.client.gui.GuiIntegrationTable;
import buildcraftreborn.silicon.client.gui.GuiPackager;
import buildcraftreborn.silicon.client.gui.GuiStampingTable;
import buildcraftreborn.silicon.client.model.ModelGateItem;
import buildcraftreborn.silicon.client.model.ModelLensItem;
import buildcraftreborn.silicon.client.model.key.KeyPlugFacade;
import buildcraftreborn.silicon.client.model.key.KeyPlugGate;
import buildcraftreborn.silicon.client.model.key.KeyPlugLens;
import buildcraftreborn.silicon.client.model.key.KeyPlugLightSensor;
import buildcraftreborn.silicon.client.model.key.KeyPlugPulsar;
import buildcraftreborn.silicon.client.model.key.KeyPlugTimer;
import buildcraftreborn.silicon.client.model.plug.ModelFacadeItem;
import buildcraftreborn.silicon.client.model.plug.PlugBakerFacade;
import buildcraftreborn.silicon.client.model.plug.PlugBakerLens;
import buildcraftreborn.silicon.client.model.plug.PlugGateBaker;
import buildcraftreborn.silicon.client.render.PlugGateRenderer;
import buildcraftreborn.silicon.client.render.PlugPulsarRenderer;
import buildcraftreborn.silicon.client.render.RenderLaser;
import buildcraftreborn.silicon.plug.PluggableGate;
import buildcraftreborn.silicon.item.ItemPackage.PackageTooltip;
import buildcraftreborn.silicon.plug.PluggablePulsar;

/** Client setup for silicon: the table and gate screens, the laser beam, and the pluggable models. Runs after
 * transport's client setup (which sets up the pipe model registry). */
public final class BCSiliconClient {
    private BCSiliconClient() {}

    public static void init(IEventBus modBus) {
        BCSiliconModels.init();

        PipeApiClient.registry.registerBaker(KeyPlugGate.class, PlugGateBaker.INSTANCE);
        PipeApiClient.registry.registerBaker(KeyPlugPulsar.class, BCSiliconModels.BAKER_PLUG_PULSAR);
        PipeApiClient.registry.registerBaker(KeyPlugLightSensor.class, BCSiliconModels.BAKER_PLUG_LIGHT_SENSOR);
        PipeApiClient.registry.registerBaker(KeyPlugTimer.class, BCSiliconModels.BAKER_PLUG_TIMER);
        PipeApiClient.registry.registerBaker(KeyPlugLens.class, PlugBakerLens.INSTANCE);
        PipeApiClient.registry.registerBaker(KeyPlugFacade.class, PlugBakerFacade.INSTANCE);

        PipeApiClient.registry.registerRenderer(PluggableGate.class, PlugGateRenderer.INSTANCE);
        PipeApiClient.registry.registerRenderer(PluggablePulsar.class, PlugPulsarRenderer.INSTANCE);

        modBus.addListener(BCSiliconClient::registerScreens);
        modBus.addListener(BCSiliconClient::registerTooltips);
        modBus.addListener(BCSiliconClient::registerRenderers);
        modBus.addListener(BCSiliconClient::onClientSetup);
        modBus.addListener(BCSiliconClient::onModifyBakingResult);
        modBus.addListener(BCSiliconClient::onBakingCompleted);
        modBus.addListener(BCSiliconClient::registerItemColours);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BCSiliconBlocks.ASSEMBLY_TABLE_MENU.get(), GuiAssemblyTable::new);
        event.register(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE_MENU.get(), GuiAdvancedCraftingTable::new);
        event.register(BCSiliconBlocks.INTEGRATION_TABLE_MENU.get(), GuiIntegrationTable::new);
        event.register(BCSiliconBlocks.GATE_MENU.get(), GuiGate::new);
        event.register(BCSiliconPackaging.PACKAGER_MENU.get(), GuiPackager::new);
        event.register(BCSiliconPackaging.STAMPING_TABLE_MENU.get(), GuiStampingTable::new);
    }

    private static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(PackageTooltip.class, ClientPackageTooltip::new);
    }

    private static void registerItemColours(RegisterColorHandlersEvent.Item event) {
        event.register(FacadeItemColours.INSTANCE, BCSiliconItems.PLUG_FACADE.get());
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BCSiliconBlocks.LASER_TILE.get(), RenderLaser::new);
        event.registerEntityRenderer(BCSiliconPackaging.PACKAGE_ENTITY.get(), ThrownItemRenderer::new);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        // 1.12: metadata 0 (empty) and 1 (holding a gate's settings)
        event.enqueueWork(() -> ItemProperties.register(BCSiliconItems.GATE_COPIER.get(), BuildCraftReborn.id("full"),
            (stack, level, entity, seed) -> stack.has(BCSiliconItems.GATE_DATA.get()) ? 1 : 0));
    }

    private static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        Map<ModelResourceLocation, BakedModel> models = event.getModels();
        models.put(ModelResourceLocation.inventory(BCSiliconItems.PLUG_GATE.getId()), ModelGateItem.INSTANCE);
        models.put(ModelResourceLocation.inventory(BCSiliconItems.PLUG_LENS.getId()), ModelLensItem.INSTANCE);
        models.put(ModelResourceLocation.inventory(BCSiliconItems.PLUG_FACADE.getId()), ModelFacadeItem.INSTANCE);
        models.put(ModelResourceLocation.inventory(BCSiliconItems.PLUG_PULSAR.getId()), new ModelLazy(() -> {
            synchronized (BCSiliconModels.class) {
                PluggablePulsar.setModelVariablesForItem();
                return new ModelPluggableItem(BCSiliconModels.PULSAR_STATIC.getCutoutQuads(),
                    BCSiliconModels.PULSAR_DYNAMIC.getCutoutQuads());
            }
        }));
        models.put(ModelResourceLocation.inventory(BCSiliconItems.PLUG_LIGHT_SENSOR.getId()),
            new ModelLazy(() -> new ModelPluggableItem(BCSiliconModels.LIGHT_SENSOR.getCutoutQuads())));
        models.put(ModelResourceLocation.inventory(BCSiliconItems.PLUG_TIMER.getId()),
            new ModelLazy(() -> new ModelPluggableItem(BCSiliconModels.TIMER.getCutoutQuads())));
    }

    private static void onBakingCompleted(ModelEvent.BakingCompleted event) {
        PlugGateBaker.onModelBake();
        PlugBakerLens.onModelBake();
        ModelFacadeItem.onModelBake();
        ModelGateItem.onModelBake();
        ModelLensItem.onModelBake();
        PlugPulsarRenderer.onModelBake();
        PlugGateRenderer.onModelBake();
    }
}
