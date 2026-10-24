/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.BCBuildersItems;
import buildcraftreborn.builders.addon.AddonFillerPlanner;
import buildcraftreborn.builders.client.gui.GuiArchitectTable;
import buildcraftreborn.builders.client.gui.GuiBuilder;
import buildcraftreborn.builders.client.gui.GuiElectronicLibrary;
import buildcraftreborn.builders.client.gui.GuiFiller;
import buildcraftreborn.builders.client.gui.GuiFillerPlanner;
import buildcraftreborn.builders.client.gui.GuiReplacer;
import buildcraftreborn.builders.client.render.AddonRendererFillerPlanner;
import buildcraftreborn.builders.client.render.RenderArchitectTable;
import buildcraftreborn.builders.client.render.RenderArchitectTables;
import buildcraftreborn.builders.client.render.RenderBuilder;
import buildcraftreborn.builders.client.render.RenderConstructionMarker;
import buildcraftreborn.builders.client.render.RenderFiller;
import buildcraftreborn.builders.client.render.RenderQuarry;
import buildcraftreborn.builders.item.ItemSchematicSingle;
import buildcraftreborn.builders.item.ItemSnapshot;
import buildcraftreborn.builders.item.SnapshotTooltip;
import buildcraftreborn.builders.snapshot.ClientSnapshots;
import buildcraftreborn.core.client.render.AddonRenderers;

/** Client side of the builders module: GUIs, renderers, item models and tooltips. */
public final class BCBuildersClient {
    private BCBuildersClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(BCBuildersClient::registerScreens);
        modBus.addListener(BCBuildersClient::registerRenderers);
        modBus.addListener(BCBuildersClient::onClientSetup);
        modBus.addListener(BCBuildersClient::registerTooltips);
        NeoForge.EVENT_BUS.addListener(RenderArchitectTables::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(BCBuildersClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(BCBuildersClient::onClientLogout);
        AddonRenderers.register(AddonFillerPlanner.class, new AddonRendererFillerPlanner());
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BCBuildersBlocks.FILLER_MENU.get(), GuiFiller::new);
        event.register(BCBuildersBlocks.BUILDER_MENU.get(), GuiBuilder::new);
        event.register(BCBuildersBlocks.ARCHITECT_MENU.get(), GuiArchitectTable::new);
        event.register(BCBuildersBlocks.LIBRARY_MENU.get(), GuiElectronicLibrary::new);
        event.register(BCBuildersBlocks.REPLACER_MENU.get(), GuiReplacer::new);
        event.register(BCBuildersBlocks.FILLER_PLANNER_MENU.get(), GuiFillerPlanner::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BCBuildersBlocks.QUARRY_TILE.get(), RenderQuarry::new);
        event.registerBlockEntityRenderer(BCBuildersBlocks.BUILDER_TILE.get(), RenderBuilder::new);
        event.registerBlockEntityRenderer(BCBuildersBlocks.CONSTRUCTION_MARKER_TILE.get(),
            RenderConstructionMarker::new);
        event.registerBlockEntityRenderer(BCBuildersBlocks.FILLER_TILE.get(), RenderFiller::new);
        event.registerBlockEntityRenderer(BCBuildersBlocks.ARCHITECT_TILE.get(), RenderArchitectTable::new);
    }

    private static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(SnapshotTooltip.class, ClientSnapshotTooltip::new);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        // Used blueprints, templates and single schematics have their own models
        ResourceLocation used = BuildCraftReborn.id("used");
        event.enqueueWork(() -> {
            ItemProperties.register(BCBuildersItems.BLUEPRINT.get(), used,
                (stack, level, entity, seed) -> ItemSnapshot.isUsed(stack) ? 1 : 0);
            ItemProperties.register(BCBuildersItems.TEMPLATE.get(), used,
                (stack, level, entity, seed) -> ItemSnapshot.isUsed(stack) ? 1 : 0);
            ItemProperties.register(BCBuildersItems.SCHEMATIC_SINGLE.get(), used,
                (stack, level, entity, seed) -> ItemSchematicSingle.isUsed(stack) ? 1 : 0);
        });
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (!Minecraft.getInstance().isPaused()) {
            ClientArchitectTables.tick();
        }
    }

    private static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSnapshots.INSTANCE.clear();
        ClientArchitectTables.clear();
    }
}
