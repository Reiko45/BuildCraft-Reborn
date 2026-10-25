/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;

import buildcraftreborn.api.mj.IMjToRfStatus;
import buildcraftreborn.builders.BCBuilders;
import buildcraftreborn.compat.top.BCPluginTOP;
import buildcraftreborn.core.BCCore;
import buildcraftreborn.datagen.BCDataGenerators;
import buildcraftreborn.energy.BCEnergy;
import buildcraftreborn.energy.BCEnergyConfig;
import buildcraftreborn.factory.BCFactory;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.block.VanillaRotationHandlers;
import buildcraftreborn.lib.cap.BCCapabilities;
import buildcraftreborn.lib.chunkload.ChunkLoaderManager;
import buildcraftreborn.lib.fluid.BCFluidDataMaps;
import buildcraftreborn.lib.marker.MarkerCache;
import buildcraftreborn.lib.misc.ExpressionCompat;
import buildcraftreborn.lib.misc.MjForgeEnergyStatus;
import buildcraftreborn.lib.net.BCNetwork;
import buildcraftreborn.robotics.BCRobotics;
import buildcraftreborn.silicon.BCSilicon;
import buildcraftreborn.test.BCTestContent;
import buildcraftreborn.transport.BCTransport;

/**
 * Entry point for BuildCraft Reborn. The 1.12.2 original shipped nine mod IDs (buildcraftlib, buildcraftcore, ...);
 * this port uses one ID and keeps the modules as packages: lib, core, energy, factory, transport, silicon, builders,
 * robotics and compat.
 */
@Mod(BuildCraftReborn.MODID)
public final class BuildCraftReborn {
    public static final String MODID = "buildcraftreborn";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BuildCraftReborn(IEventBus modBus, ModContainer container) {
        // The expression types (Facing, DyeColor...) that the gate and pulsar model variables use, on both sides
        ExpressionCompat.setup();
        // Modules create their DeferredHolders first, then every register attaches to the mod bus.
        BCCore.init();
        BCEnergy.init();
        BCFactory.init();
        BCTransport.init(modBus, container);
        BCSilicon.init(modBus);
        BCBuilders.init(modBus, container);
        BCRobotics.init(modBus, container);
        BCTestContent.init();
        BCRegistries.register(modBus);

        modBus.addListener(BCNetwork::registerPayloads);
        modBus.addListener(BCCapabilities::register);
        modBus.addListener(BCDataGenerators::gatherData);
        modBus.addListener(BCFluidDataMaps::register);
        modBus.addListener(BuildCraftReborn::commonSetup);
        modBus.addListener(ChunkLoaderManager::onRegisterControllers);
        MarkerCache.registerEvents();
        // Optional integrations that need registering (JEI, Jade and CraftTweaker find their own plugins)
        if (ModList.get().isLoaded(BCPluginTOP.MOD_ID)) {
            modBus.addListener(BCPluginTOP::enqueue);
        }

        container.registerConfig(ModConfig.Type.COMMON, BCLibConfig.COMMON_SPEC);
        container.registerConfig(ModConfig.Type.CLIENT, BCLibConfig.CLIENT_SPEC);
        container.registerConfig(ModConfig.Type.COMMON, BCEnergyConfig.SPEC, MODID + "-energy.toml");
        IMjToRfStatus.set(MjForgeEnergyStatus.INSTANCE);
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        // The block registry is complete by now, so handlers can be registered for every block of a class.
        event.enqueueWork(VanillaRotationHandlers::fmlInit);
        event.enqueueWork(BCCore::commonSetup);
        event.enqueueWork(BCEnergy::commonSetup);
        event.enqueueWork(BCTransport::commonSetup);
        event.enqueueWork(BCBuilders::commonSetup);
    }

    /** Shorthand for a resource location in this mod's namespace. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
