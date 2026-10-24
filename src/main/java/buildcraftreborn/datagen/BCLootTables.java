/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;

import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.block.BlockSpring;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.factory.BCFactoryItems;
import buildcraftreborn.factory.block.BlockWaterGel;
import buildcraftreborn.factory.block.BlockWaterGel.GelStage;
import buildcraftreborn.lib.BCRegistries;

/** Block loot tables. 1.12 BuildCraft coded its drops in Java; in 1.21 every block needs a loot table. */
public final class BCLootTables {
    private BCLootTables() {}

    public static LootTableProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        return new LootTableProvider(output, Set.of(),
            List.of(new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)), lookup);
    }

    private static final class BlockLoot extends BlockLootSubProvider {
        BlockLoot(HolderLookup.Provider registries) {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
        }

        @Override
        protected void generate() {
            dropSelf(BCCoreBlocks.ENGINE_WOOD.get());
            dropSelf(BCCoreBlocks.ENGINE_CREATIVE.get());
            dropSelf(BCEnergyBlocks.ENGINE_STONE.get());
            dropSelf(BCEnergyBlocks.ENGINE_IRON.get());
            dropSelf(BCEnergyBlocks.ENGINE_RF.get());
            dropSelf(BCEnergyBlocks.MJ_DYNAMO.get());
            dropSelf(BCCoreBlocks.MARKER_VOLUME.get());
            dropSelf(BCCoreBlocks.MARKER_PATH.get());
            BCCoreBlocks.DECORATED.values().forEach(block -> dropSelf(block.get()));
            dropSelf(BCFactoryBlocks.AUTO_WORKBENCH_ITEMS.get());
            dropSelf(BCSiliconBlocks.LASER.get());
            dropSelf(BCSiliconBlocks.ASSEMBLY_TABLE.get());
            dropSelf(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE.get());
            dropSelf(BCSiliconBlocks.INTEGRATION_TABLE.get());
            dropSelf(BCFactoryBlocks.MINING_WELL.get());
            dropSelf(BCFactoryBlocks.PUMP.get());
            dropSelf(BCFactoryBlocks.FLOOD_GATE.get());
            dropSelf(BCFactoryBlocks.TANK.get());
            dropSelf(BCFactoryBlocks.CHUTE.get());
            dropSelf(BCFactoryBlocks.DISTILLER.get());
            dropSelf(BCFactoryBlocks.HEAT_EXCHANGE.get());
            dropSelf(BCTransportBlocks.FILTERED_BUFFER.get());
            dropSelf(BCBuildersBlocks.FILLER.get());
            dropSelf(BCBuildersBlocks.BUILDER.get());
            dropSelf(BCBuildersBlocks.ARCHITECT.get());
            dropSelf(BCBuildersBlocks.LIBRARY.get());
            dropSelf(BCBuildersBlocks.REPLACER.get());
            dropSelf(BCBuildersBlocks.QUARRY.get());
            dropSelf(BCBuildersBlocks.CONSTRUCTION_MARKER.get());
            dropSelf(BCRoboticsBlocks.ZONE_PLANNER.get());
            dropSelf(BCRoboticsBlocks.REQUESTER.get());
            dropSelf(BCRoboticsBlocks.CHARGING_TABLE.get());
            dropSelf(BCRoboticsBlocks.PROGRAMMING_TABLE.get());
            waterGel();
        }

        /** Water gel drops gelled water: one or two while it's still spreading, else one (as in 1.12). */
        private void waterGel() {
            Block block = BCFactoryBlocks.WATER_GEL.get();
            LootItem.Builder<?> item = LootItem.lootTableItem(BCFactoryItems.GEL.get());
            for (GelStage stage : GelStage.values()) {
                if (stage.spreading) {
                    item.apply(SetItemCountFunction.setCount(UniformGenerator.between(1, 2))
                        .when(LootItemBlockStatePropertyCondition.hasBlockStateProperties(block)
                            .setProperties(StatePropertiesPredicate.Builder.properties()
                                .hasProperty(BlockWaterGel.PROP_STAGE, stage))));
                }
            }
            add(block, LootTable.lootTable().withPool(applyExplosionCondition(block,
                LootPool.lootPool().setRolls(ConstantValue.exactly(1)).add(item))));
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            List<Block> blocks = new ArrayList<>();
            // Fluid blocks have no loot table, and springs drop nothing (like bedrock)
            BCRegistries.BLOCKS.getEntries().forEach(holder -> {
                if (!(holder.get() instanceof LiquidBlock) && !(holder.get() instanceof BlockSpring)) {
                    blocks.add(holder.get());
                }
            });
            return blocks;
        }
    }
}
