/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.client;

import net.minecraft.world.inventory.InventoryMenu;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.TextureAtlasStitchedEvent;

import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.client.gui.GuiAutoCraftItems;
import buildcraftreborn.factory.client.gui.GuiChute;
import buildcraftreborn.factory.client.gui.GuiDistiller;
import buildcraftreborn.factory.client.gui.GuiTank;
import buildcraftreborn.factory.client.render.RenderDistiller;
import buildcraftreborn.factory.client.render.RenderHeatExchange;
import buildcraftreborn.factory.client.render.RenderMiningWell;
import buildcraftreborn.factory.client.render.RenderPump;
import buildcraftreborn.factory.client.render.RenderTank;
import buildcraftreborn.lib.client.render.fluid.FluidRenderer;

/** Client setup for the factory module: machine screens and block entity renderers. */
public final class BCFactoryClient {
    private BCFactoryClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(BCFactoryClient::registerScreens);
        modBus.addListener(BCFactoryClient::registerRenderers);
        modBus.addListener(BCFactoryClient::onTextureStitch);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BCFactoryBlocks.TANK_TILE.get(), RenderTank::new);
        event.registerBlockEntityRenderer(BCFactoryBlocks.MINING_WELL_TILE.get(), RenderMiningWell::new);
        event.registerBlockEntityRenderer(BCFactoryBlocks.PUMP_TILE.get(), RenderPump::new);
        event.registerBlockEntityRenderer(BCFactoryBlocks.DISTILLER_TILE.get(), RenderDistiller::new);
        event.registerBlockEntityRenderer(BCFactoryBlocks.HEAT_EXCHANGE_TILE.get(), RenderHeatExchange::new);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BCFactoryBlocks.TANK_MENU.get(), GuiTank::new);
        event.register(BCFactoryBlocks.CHUTE_MENU.get(), GuiChute::new);
        event.register(BCFactoryBlocks.AUTO_WORKBENCH_ITEMS_MENU.get(), GuiAutoCraftItems::new);
        event.register(BCFactoryBlocks.DISTILLER_MENU.get(), GuiDistiller::new);
    }

    private static void onTextureStitch(TextureAtlasStitchedEvent event) {
        if (event.getAtlas().location().equals(InventoryMenu.BLOCK_ATLAS)) {
            FluidRenderer.clearCaches();
        }
    }
}
