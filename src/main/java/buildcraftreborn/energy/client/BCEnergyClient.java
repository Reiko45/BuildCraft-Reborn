/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.client;

import org.joml.Vector3f;

import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.energy.client.gui.GuiDynamoMJ;
import buildcraftreborn.energy.client.gui.GuiEngineIron_BC8;
import buildcraftreborn.energy.client.gui.GuiEngineRF;
import buildcraftreborn.energy.client.gui.GuiEngineStone_BC8;
import buildcraftreborn.energy.client.render.RenderDynamoMJ;
import buildcraftreborn.lib.client.render.tile.RenderEngine_BC8;
import buildcraftreborn.lib.fluid.BCFluid;

/** Client setup for the energy module: engine screens and renderers, and fluid textures. */
public final class BCEnergyClient {
    private BCEnergyClient() {}

    public static void init(IEventBus modBus) {
        modBus.addListener(BCEnergyClient::registerScreens);
        modBus.addListener(BCEnergyClient::registerClientExtensions);
        modBus.addListener(BCEnergyClient::registerRenderers);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(BCEnergyBlocks.ENGINE_STONE_TILE.get(), RenderEngine_BC8.provider("stone", true));
        event.registerBlockEntityRenderer(BCEnergyBlocks.ENGINE_IRON_TILE.get(), RenderEngine_BC8.provider("iron", true));
        event.registerBlockEntityRenderer(BCEnergyBlocks.ENGINE_RF_TILE.get(), RenderEngine_BC8.provider("rf", true));
        event.registerBlockEntityRenderer(BCEnergyBlocks.MJ_DYNAMO_TILE.get(), RenderDynamoMJ.PROVIDER);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(BCEnergyBlocks.ENGINE_STONE_MENU.get(), GuiEngineStone_BC8::new);
        event.register(BCEnergyBlocks.ENGINE_IRON_MENU.get(), GuiEngineIron_BC8::new);
        event.register(BCEnergyBlocks.ENGINE_RF_MENU.get(), GuiEngineRF::new);
        event.register(BCEnergyBlocks.MJ_DYNAMO_MENU.get(), GuiDynamoMJ::new);
    }

    private static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        for (BCFluid fluid : BCEnergyFluids.allFluids) {
            event.registerFluidType(new FluidExtensions(fluid), fluid.type.get());
        }
    }

    /** Textures are pre-coloured copies of the heat-level textures (made by {@code BCFluidTextures} in datagen),
     * matching 1.12's runtime-recoloured sprites, so no tint is applied. Inside the fluid the fog takes its dark
     * colour. */
    private static final class FluidExtensions implements IClientFluidTypeExtensions {
        private final ResourceLocation still, flowing;
        private final Vector3f fogColour;

        FluidExtensions(BCFluid fluid) {
            this.still = BuildCraftReborn.id("block/fluids/" + fluid.name + "_still");
            this.flowing = BuildCraftReborn.id("block/fluids/" + fluid.name + "_flow");
            int dark = fluid.colourDark;
            this.fogColour = new Vector3f(((dark >> 16) & 0xFF) / 255f, ((dark >> 8) & 0xFF) / 255f,
                (dark & 0xFF) / 255f);
        }

        @Override
        public ResourceLocation getStillTexture() {
            return still;
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return flowing;
        }

        @Override
        public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance,
            float darkenWorldAmount, Vector3f fluidFogColor) {
            return new Vector3f(fogColour);
        }
    }
}
