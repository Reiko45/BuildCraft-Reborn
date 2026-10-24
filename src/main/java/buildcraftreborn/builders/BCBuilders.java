/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.filler.FillerManager;
import buildcraftreborn.api.template.TemplateApi;
import buildcraftreborn.builders.addon.AddonFillerPlanner;
import buildcraftreborn.builders.registry.FillerRegistry;
import buildcraftreborn.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraftreborn.builders.snapshot.RulesLoader;
import buildcraftreborn.builders.snapshot.TemplateHandlerDefault;
import buildcraftreborn.builders.snapshot.TemplateRegistry;
import buildcraftreborn.core.marker.volume.AddonsRegistry;

/** Builders module (1.12's {@code buildcraftbuilders} mod): the quarry, filler, builder, architect table, electronic
 * library and replacer, with blueprints, templates and the filler planner. Initialised from {@code BuildCraftReborn}'s
 * constructor. */
public final class BCBuilders {
    private BCBuilders() {}

    public static void init(IEventBus modBus, ModContainer container) {
        // The registries must be set before the patterns (in BCBuildersStatements) are created.
        TemplateApi.templateRegistry = TemplateRegistry.INSTANCE;
        FillerManager.registry = FillerRegistry.INSTANCE;
        AddonsRegistry.INSTANCE.register(BuildCraftReborn.id("filler_planner"), AddonFillerPlanner.class,
            AddonFillerPlanner::new);

        BCBuildersBlocks.init();
        BCBuildersItems.init();
        BCBuildersStatements.init();
        BCBuildersSchematics.init();
        TemplateApi.templateRegistry.addHandler(TemplateHandlerDefault.INSTANCE);

        container.registerConfig(ModConfig.Type.COMMON, BCBuildersConfig.SPEC,
            BuildCraftReborn.MODID + "-builders.toml");
        modBus.addListener(BCBuildersConfig::onConfigLoad);

        NeoForge.EVENT_BUS.addListener(BCBuilders::onServerStarting);
    }

    /** Called in common setup, once every block, item and mod is known. */
    public static void commonSetup() {
        RulesLoader.loadAll();
    }

    private static void onServerStarting(ServerStartingEvent event) {
        GlobalSavedDataSnapshots.reInit(LogicalSide.SERVER);
    }
}
