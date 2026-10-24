/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.client;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.world.item.DyeColor;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.items.IMapLocation.MapLocationType;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.client.gui.GuiList;
import buildcraftreborn.core.client.render.MarkerRenderer;
import buildcraftreborn.core.client.render.RenderVolumeBoxes;
import buildcraftreborn.core.marker.volume.ClientVolumeBoxes;
import buildcraftreborn.lib.client.render.DetachedRenderer;
import buildcraftreborn.lib.client.render.tile.RenderEngine_BC8;
import buildcraftreborn.lib.debug.BCAdvDebugging;
import buildcraftreborn.lib.debug.ClientDebuggables;
import buildcraftreborn.lib.debug.DebugRenderHelper;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.list.ListHandler;

/** Client setup for the core module. */
public final class BCCoreClient {
    private BCCoreClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(BCCoreClient::registerRenderers);
        modBus.addListener(BCCoreClient::onClientSetup);
        modBus.addListener(BCCoreClient::registerScreens);
        NeoForge.EVENT_BUS.addListener(ListTooltipHandler::onItemTooltip);
        NeoForge.EVENT_BUS.addListener(MarkerRenderer::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(RenderVolumeBoxes::onRenderLevel);
        NeoForge.EVENT_BUS.addListener(BCCoreClient::onClientLogout);
        // The debugger: F3 info and the advanced debug target's renderer
        NeoForge.EVENT_BUS.addListener(ClientDebuggables::onClientTick);
        NeoForge.EVENT_BUS.addListener(ClientDebuggables::onDebugText);
        NeoForge.EVENT_BUS.addListener(DetachedRenderer.INSTANCE::onRenderLevel);
        DetachedRenderer.INSTANCE.addRenderer(DebugRenderHelper.INSTANCE);
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        // The paintbrush model picks its colour from this: 0 is clean, then 1 to 16 are the dye colours
        event.enqueueWork(() -> ItemProperties.register(BCCoreItems.PAINTBRUSH.get(), BuildCraftReborn.id("colour"),
            (stack, level, entity, seed) -> {
                DyeColor colour = stack.get(BCItemComponents.COLOUR.get());
                return colour == null ? 0 : colour.getId() + 1;
            }));
        // The map location's picture shows what it remembers
        event.enqueueWork(() -> ItemProperties.register(BCCoreItems.MAP_LOCATION.get(), BuildCraftReborn.id("type"),
            (stack, level, entity, seed) -> MapLocationType.getFromStack(stack).meta));
        // A list with anything in it shows the "used" picture
        event.enqueueWork(() -> ItemProperties.register(BCCoreItems.LIST.get(), BuildCraftReborn.id("used"),
            (stack, level, entity, seed) -> ListHandler.hasItems(stack) ? 1 : 0));
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BCCoreItems.LIST_MENU.get(), GuiList::new);
    }

    /** Forgets the last level's volume boxes. */
    private static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientVolumeBoxes.INSTANCE.volumeBoxes.clear();
        BCAdvDebugging.setClientDebugTarget(null);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BCCoreBlocks.ENGINE_WOOD_TILE.get(), RenderEngine_BC8.provider("wood", true));
        event.registerBlockEntityRenderer(BCCoreBlocks.ENGINE_CREATIVE_TILE.get(),
            RenderEngine_BC8.provider("creative", false));
    }
}
