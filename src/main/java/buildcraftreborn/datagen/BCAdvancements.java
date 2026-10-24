/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.critereon.EnterBlockTrigger;
import net.minecraft.advancements.critereon.ImpossibleTrigger;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import net.neoforged.neoforge.common.data.AdvancementProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.gate.EnumGateLogic;
import buildcraftreborn.silicon.gate.EnumGateMaterial;
import buildcraftreborn.silicon.gate.EnumGateModifier;
import buildcraftreborn.silicon.gate.GateVariant;
import buildcraftreborn.transport.BCTransportItems;
import buildcraftreborn.transport.BCTransportPipes;

/** Advancements, following 1.12's {@code buildcraftcore} and {@code buildcraftenergy} advancement JSONs. Ones granted
 * from code use a single {@code code_trigger} criterion with the impossible trigger (see {@code AdvancementUtil}).
 * 1.12's energy advancements that its code never granted (powering up, lava power, and so on) are left out. */
public final class BCAdvancements {
    private BCAdvancements() {}

    public static AdvancementProvider create(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
        ExistingFileHelper files) {
        return new AdvancementProvider(output, lookup, files, List.of(BCAdvancements::generate));
    }

    private static void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> saver,
        ExistingFileHelper files) {
        AdvancementHolder root = Advancement.Builder.advancement()
            .display(BCCoreItems.GEAR_WOOD.get(), title("root"), desc("root"),
                ResourceLocation.withDefaultNamespace("textures/block/bricks.png"), AdvancementType.TASK, false,
                false, false)
            .addCriterion("has_stick", InventoryChangeTrigger.TriggerInstance.hasItems(Items.STICK))
            .save(saver, BuildCraftReborn.id("root"), files);

        Advancement.Builder gears = Advancement.Builder.advancement()
            .parent(root)
            .display(BCCoreItems.GEAR_DIAMOND.get(), title("gears"), desc("gears"), null, AdvancementType.GOAL, true,
                true, false);
        for (var gear : BCCoreItems.GEARS) {
            gears.addCriterion(gear.getId().getPath(), InventoryChangeTrigger.TriggerInstance.hasItems(gear.get()));
        }
        AdvancementHolder gearsHolder = gears.save(saver, BuildCraftReborn.id("gears"), files);

        // Opening the guide (ItemGuide), and using a list (ItemList_BC8)
        AdvancementHolder guide = codeTriggered(saver, files, root, "guide", BCCoreItems.GUIDE.get());
        codeTriggered(saver, files, guide, "list", BCCoreItems.LIST.get());

        AdvancementHolder wrenched = codeTriggered(saver, files, root, "wrenched", BCCoreItems.WRENCH.get());
        codeTriggered(saver, files, wrenched, "free_power", BCCoreBlocks.ENGINE_WOOD.get());

        // Factory (1.12's buildcraftfactory advancements)
        AdvancementHolder fluidStorage = codeTriggered(saver, files, root, "fluid_storage",
            BCFactoryBlocks.TANK.get());
        codeTriggered(saver, files, wrenched, "lazy_crafting", BCFactoryBlocks.AUTO_WORKBENCH_ITEMS.get());
        codeTriggered(saver, files, wrenched, "retired_hopper", BCFactoryBlocks.CHUTE.get());
        codeTriggered(saver, files, fluidStorage, "flooding_the_world", BCFactoryBlocks.FLOOD_GATE.get());
        AdvancementHolder draining = codeTriggered(saver, files, fluidStorage, "draining_the_world",
            BCFactoryBlocks.PUMP.get());
        AdvancementHolder oilPlatform = codeTriggered(saver, files, draining, "oil_platform",
            BCFactoryBlocks.PUMP.get());
        codeTriggered(saver, files, oilPlatform, "black_gold", BCFactoryBlocks.PUMP.get(),
            AdvancementType.CHALLENGE);
        // Nothing in 1.12 awarded this one either; kept so the advancement tree matches.
        codeTriggered(saver, files, oilPlatform, "heating_and_distilling", BCFactoryBlocks.DISTILLER.get());

        AdvancementHolder markers = codeTriggered(saver, files, guide, "markers", BCCoreBlocks.MARKER_VOLUME.get());
        codeTriggered(saver, files, markers, "path_markers", BCCoreBlocks.MARKER_PATH.get());

        // Builders (1.12's buildcraftbuilders advancements). "Start of something big", "building for the future"
        // and "destroying the world" were never awarded by 1.12, so they are left out.
        AdvancementHolder shaping = codeTriggered(saver, files, gearsHolder, "shaping_the_world",
            BCBuildersBlocks.QUARRY.get());
        codeTriggered(saver, files, shaping, "diggy_diggy_hole", BCBuildersBlocks.QUARRY.get(), AdvancementType.GOAL);
        AdvancementHolder architect = codeTriggered(saver, files, markers, "architect",
            BCBuildersBlocks.ARCHITECT.get());
        codeTriggered(saver, files, architect, "paving_the_way", BCCoreBlocks.MARKER_PATH.get());

        Advancement.Builder.advancement()
            .parent(guide)
            .display(BCEnergyBlocks.ENGINE_STONE.get(), title("engine"), desc("engine"), null, AdvancementType.TASK,
                true, true, false)
            .addCriterion("redstone_engine", InventoryChangeTrigger.TriggerInstance.hasItems(BCCoreBlocks.ENGINE_WOOD.get()))
            .addCriterion("stirling_engine", InventoryChangeTrigger.TriggerInstance.hasItems(BCEnergyBlocks.ENGINE_STONE.get()))
            .addCriterion("combustion_engine", InventoryChangeTrigger.TriggerInstance.hasItems(BCEnergyBlocks.ENGINE_IRON.get()))
            .requirements(AdvancementRequirements.Strategy.OR)
            .save(saver, BuildCraftReborn.id("engine"), files);

        // Energy: swimming in oil. 1.12's parent, "Fine riches" (find an oil biome), is left out as this port has no
        // oil biomes (oil wells only), so this hangs off the root.
        Advancement.Builder.advancement()
            .parent(root)
            .display(BCEnergyFluids.crudeOil[0].bucket.get(), title("sticky_dipping"), desc("sticky_dipping"), null,
                AdvancementType.TASK, true, true, false)
            .addCriterion("oil", EnterBlockTrigger.TriggerInstance.entersBlock(
                BCEnergyFluids.crudeOil[0].block.get()))
            .save(saver, BuildCraftReborn.id("sticky_dipping"), files);

        // Transport (1.12's buildcrafttransport advancements). The ones 1.12 never granted (pipe diversification,
        // pipe fanatic, all plugged up, categorizing with colours, colourful electrician) are left out, so sealing
        // fluids hangs off pipe dream.
        AdvancementHolder pipeDream = codeTriggered(saver, files, root, "pipe_dream",
            BCTransportItems.PIPES.get(BCTransportPipes.structure).get());
        AdvancementHolder plugging = codeTriggered(saver, files, pipeDream, "plugging_the_gap",
            BCTransportItems.PLUG_BLOCKER.get());

        // Silicon (1.12's pipe_logic and extended_logic were transport advancements)
        AdvancementHolder pipeLogic = codeTriggered(saver, files, plugging, "pipe_logic",
            BCSiliconItems.PLUG_GATE.get());
        Advancement.Builder.advancement()
            .parent(pipeLogic)
            .display(BCSiliconItems.PLUG_GATE.get().getStack(new GateVariant(EnumGateLogic.OR, EnumGateMaterial.GOLD,
                EnumGateModifier.DIAMOND)), title("extended_logic"), desc("extended_logic"), null,
                AdvancementType.TASK, true, true, false)
            .addCriterion("code_trigger", net.minecraft.advancements.CriteriaTriggers.IMPOSSIBLE
                .createCriterion(new ImpossibleTrigger.TriggerInstance()))
            .save(saver, BuildCraftReborn.id("extended_logic"), files);
        AdvancementHolder laserPower = Advancement.Builder.advancement()
            .parent(root)
            .display(BCSiliconBlocks.LASER.get(), title("laser_power"), desc("laser_power"), null,
                AdvancementType.TASK, true, true, false)
            .addCriterion("get_laser", InventoryChangeTrigger.TriggerInstance.hasItems(BCSiliconBlocks.LASER.get()))
            .save(saver, BuildCraftReborn.id("laser_power"), files);
        codeTriggered(saver, files, laserPower, "precision_crafting", BCSiliconBlocks.ASSEMBLY_TABLE.get());
        codeTriggered(saver, files, pipeDream, "logic_transportation",
            BCTransportItems.WIRES.get(net.minecraft.world.item.DyeColor.RED).get());
        Advancement.Builder.advancement()
            .parent(pipeDream)
            .display(BCTransportItems.WATERPROOF.get(), title("sealing_fluids"), desc("sealing_fluids"), null,
                AdvancementType.TASK, true, true, false)
            .addCriterion("has_sealant", InventoryChangeTrigger.TriggerInstance.hasItems(
                BCTransportItems.WATERPROOF.get()))
            .save(saver, BuildCraftReborn.id("sealing_fluids"), files);

        // Hidden: filling most of a diamond pipe side's filters (PipeBehaviourDiamond) unlocks the list's recipe
        Advancement.Builder.recipeAdvancement()
            .rewards(AdvancementRewards.Builder.recipe(BuildCraftReborn.id("list")))
            .addCriterion("code_trigger", net.minecraft.advancements.CriteriaTriggers.IMPOSSIBLE
                .createCriterion(new ImpossibleTrigger.TriggerInstance()))
            .save(saver, BuildCraftReborn.id("too_many_pipe_filters"), files);
    }

    private static AdvancementHolder codeTriggered(Consumer<AdvancementHolder> saver, ExistingFileHelper files,
        AdvancementHolder parent, String name, ItemLike icon) {
        return codeTriggered(saver, files, parent, name, icon, AdvancementType.TASK);
    }

    private static AdvancementHolder codeTriggered(Consumer<AdvancementHolder> saver, ExistingFileHelper files,
        AdvancementHolder parent, String name, ItemLike icon, AdvancementType type) {
        return Advancement.Builder.advancement()
            .parent(parent)
            .display(icon, title(name), desc(name), null, type, true, true, false)
            .addCriterion("code_trigger", net.minecraft.advancements.CriteriaTriggers.IMPOSSIBLE
                .createCriterion(new ImpossibleTrigger.TriggerInstance()))
            .save(saver, BuildCraftReborn.id(name), files);
    }

    static Component title(String name) {
        return Component.translatable("advancements.buildcraftreborn." + name + ".title");
    }

    static Component desc(String name) {
        return Component.translatable("advancements.buildcraftreborn." + name + ".description");
    }

}
