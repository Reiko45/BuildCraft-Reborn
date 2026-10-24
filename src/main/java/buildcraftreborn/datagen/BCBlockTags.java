/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;

import net.minecraft.tags.BlockTags;

import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.test.BCTestContent;

/** Block tags (mineable/pickaxe, needs_iron_tool, ...). */
public final class BCBlockTags extends BlockTagsProvider {
    public BCBlockTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, BuildCraftReborn.MODID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BCCoreBlocks.ENGINE_WOOD.get(), BCCoreBlocks.ENGINE_CREATIVE.get(),
            BCEnergyBlocks.ENGINE_STONE.get(), BCEnergyBlocks.ENGINE_IRON.get(), BCEnergyBlocks.ENGINE_RF.get(),
            BCEnergyBlocks.MJ_DYNAMO.get());
        BCCoreBlocks.DECORATED.values().forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BCFactoryBlocks.AUTO_WORKBENCH_ITEMS.get(),
            BCFactoryBlocks.MINING_WELL.get(), BCFactoryBlocks.PUMP.get(), BCFactoryBlocks.FLOOD_GATE.get(),
            BCFactoryBlocks.TANK.get(), BCFactoryBlocks.CHUTE.get(), BCFactoryBlocks.DISTILLER.get(),
            BCFactoryBlocks.HEAT_EXCHANGE.get());
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(BCFactoryBlocks.WATER_GEL.get());
        // Transport: pipes break quickly by hand, like 1.12 (no tool tag); the filtered buffer is iron
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BCTransportBlocks.FILTERED_BUFFER.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BCSiliconBlocks.LASER.get(), BCSiliconBlocks.ASSEMBLY_TABLE.get(),
            BCSiliconBlocks.ADVANCED_CRAFTING_TABLE.get(), BCSiliconBlocks.INTEGRATION_TABLE.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BCBuildersBlocks.FILLER.get(), BCBuildersBlocks.BUILDER.get(),
            BCBuildersBlocks.ARCHITECT.get(), BCBuildersBlocks.LIBRARY.get(), BCBuildersBlocks.REPLACER.get(),
            BCBuildersBlocks.FRAME.get(), BCBuildersBlocks.QUARRY.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BCRoboticsBlocks.ZONE_PLANNER.get(),
            BCRoboticsBlocks.REQUESTER.get(), BCRoboticsBlocks.CHARGING_TABLE.get(),
            BCRoboticsBlocks.PROGRAMMING_TABLE.get());
        // The tube under a mining well or pump can't be moved or broken
        tag(BlockTags.DRAGON_IMMUNE).add(BCFactoryBlocks.TUBE.get());
        tag(BlockTags.WITHER_IMMUNE).add(BCFactoryBlocks.TUBE.get());
        // Springs act like bedrock.
        tag(BlockTags.DRAGON_IMMUNE).add(BCCoreBlocks.WATER_SPRING.get(), BCEnergyBlocks.OIL_SPRING.get());
        tag(BlockTags.WITHER_IMMUNE).add(BCCoreBlocks.WATER_SPRING.get(), BCEnergyBlocks.OIL_SPRING.get());
        tag(BlockTags.FEATURES_CANNOT_REPLACE).add(BCCoreBlocks.WATER_SPRING.get(), BCEnergyBlocks.OIL_SPRING.get());
        if (BCTestContent.ENABLED) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BCTestContent.TEST_MACHINE_BLOCK.get());
        }
    }
}
