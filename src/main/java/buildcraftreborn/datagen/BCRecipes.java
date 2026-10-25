/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.enums.EnumRedstoneChipset;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.AssemblyRecipeBasic;
import buildcraftreborn.api.recipes.IngredientStack;
import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.BCBuildersItems;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.BCCoreTags;
import buildcraftreborn.energy.BCEnergy;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.BCFactoryItems;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.recipe.ConfigCondition;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.BCRoboticsItems;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.gate.EnumGateLogic;
import buildcraftreborn.silicon.gate.EnumGateMaterial;
import buildcraftreborn.silicon.gate.EnumGateModifier;
import buildcraftreborn.silicon.gate.GateVariant;
import buildcraftreborn.silicon.item.ItemPluggableGate;
import buildcraftreborn.silicon.item.ItemPluggableLens;
import buildcraftreborn.silicon.recipe.FacadeAssemblyRecipe;
import buildcraftreborn.silicon.recipe.FacadeSwapRecipe;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.transport.BCTransportItems;
import buildcraftreborn.transport.BCTransportPipes;
import buildcraftreborn.transport.recipe.PipeUpgradeRecipe;

/**
 * Crafting recipes. Patterns match 1.12's {@code assets/buildcraftcore/recipes/gear_*.json}: a wood gear is four
 * sticks in a ring, and each later gear is four of its material around the previous tier's gear.
 */
public final class BCRecipes extends RecipeProvider {
    public BCRecipes(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCCoreItems.GEAR_WOOD.get())
            .pattern(" o ")
            .pattern("o o")
            .pattern(" o ")
            .define('o', Tags.Items.RODS_WOODEN)
            .unlockedBy("has_stick", has(Tags.Items.RODS_WOODEN))
            .save(output, BuildCraftReborn.id("gear_wood"));

        gear(output, BCCoreItems.GEAR_STONE.get(), Tags.Items.COBBLESTONES, BCCoreTags.GEARS_WOOD, "gear_stone");
        gear(output, BCCoreItems.GEAR_IRON.get(), Tags.Items.INGOTS_IRON, BCCoreTags.GEARS_STONE, "gear_iron");
        gear(output, BCCoreItems.GEAR_GOLD.get(), Tags.Items.INGOTS_GOLD, BCCoreTags.GEARS_IRON, "gear_gold");
        gear(output, BCCoreItems.GEAR_DIAMOND.get(), Tags.Items.GEMS_DIAMOND, BCCoreTags.GEARS_GOLD, "gear_diamond");

        // 1.12: buildcraftcore/recipes/wrench.json
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BCCoreItems.WRENCH.get())
            .pattern("I I")
            .pattern(" G ")
            .pattern(" I ")
            .define('I', Tags.Items.INGOTS_IRON)
            .define('G', BCCoreTags.GEARS_STONE)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_STONE))
            .save(output, BuildCraftReborn.id("wrench"));

        // 1.12: marker_volume.json (lapis lazuli was dye:4), marker_path.json (cactus green was dye:2) and
        // marker_connector.json
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BCCoreBlocks.MARKER_VOLUME.get())
            .pattern("l")
            .pattern("t")
            .define('l', Tags.Items.GEMS_LAPIS)
            .define('t', Items.REDSTONE_TORCH)
            .unlockedBy("has_torch", has(Items.REDSTONE_TORCH))
            .save(output, BuildCraftReborn.id("marker_volume"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BCCoreBlocks.MARKER_PATH.get())
            .pattern("g")
            .pattern("t")
            .define('g', Tags.Items.DYES_GREEN)
            .define('t', Items.REDSTONE_TORCH)
            .unlockedBy("has_torch", has(Items.REDSTONE_TORCH))
            .save(output, BuildCraftReborn.id("marker_path"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BCCoreItems.MARKER_CONNECTOR.get())
            .pattern("r")
            .pattern("g")
            .pattern("w")
            .define('r', Items.REDSTONE_TORCH)
            .define('g', BCCoreTags.GEARS_WOOD)
            .define('w', BCCoreItems.WRENCH.get())
            .unlockedBy("has_wrench", has(BCCoreItems.WRENCH.get()))
            .save(output, BuildCraftReborn.id("marker_connector"));

        // 1.12: BCCoreRecipes (the paintbrush, and dipping it in dye)
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BCCoreItems.PAINTBRUSH.get())
            .pattern(" iw")
            .pattern(" gi")
            .pattern("s  ")
            .define('i', Tags.Items.STRINGS)
            .define('s', Tags.Items.RODS_WOODEN)
            .define('g', BCCoreTags.GEARS_WOOD)
            .define('w', Items.WHITE_WOOL)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_WOOD))
            .save(output, BuildCraftReborn.id("paintbrush"));
        for (DyeColor colour : DyeColor.values()) {
            ItemStack brush = new ItemStack(BCCoreItems.PAINTBRUSH.get());
            brush.set(BCItemComponents.COLOUR.get(), colour);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.TOOLS, brush)
                .requires(BCCoreItems.PAINTBRUSH.get())
                .requires(colour.getTag())
                .group(BuildCraftReborn.id("paintbrush_colouring").toString())
                .unlockedBy("has_brush", has(BCCoreItems.PAINTBRUSH.get()))
                .save(output, BuildCraftReborn.id("paintbrush_" + colour.getName()));
        }

        // 1.12: redstone_engine.json, stirling_engine.json and combustion_engine.json
        engine(output, BCCoreBlocks.ENGINE_WOOD.get(), Ingredient.of(ItemTags.PLANKS), BCCoreTags.GEARS_WOOD,
            "engine_wood");
        engine(output, BCEnergyBlocks.ENGINE_STONE.get(), Ingredient.of(Tags.Items.COBBLESTONES), BCCoreTags.GEARS_STONE,
            "engine_stone");
        engine(output, BCEnergyBlocks.ENGINE_IRON.get(), Ingredient.of(Tags.Items.INGOTS_IRON), BCCoreTags.GEARS_IRON,
            "engine_iron");
        // 1.12: guide_book.json (a wooden gear and three paper)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BCCoreItems.GUIDE.get())
            .requires(BCCoreTags.GEARS_WOOD).requires(Items.PAPER, 3)
            .group("buildcraft")
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_WOOD))
            .save(output, BuildCraftReborn.id("guide"));
        // 1.12: list.json
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCCoreItems.LIST.get())
            .pattern("pRp").pattern("pGp").pattern("ppp")
            .define('p', Items.PAPER).define('R', Tags.Items.DUSTS_REDSTONE).define('G', Tags.Items.DYES_GREEN)
            .unlockedBy("has_paper", has(Items.PAPER))
            .save(output, BuildCraftReborn.id("list"));
        // 1.12: rf_engine.json and mj_dynamo.json, only loaded while their config switches are on
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, BCEnergyBlocks.ENGINE_RF.get())
            .pattern("www").pattern(" g ").pattern("GpG")
            .define('w', Tags.Items.DUSTS_REDSTONE).define('g', Tags.Items.GLASS_BLOCKS_COLORLESS)
            .define('G', BCCoreTags.GEARS_IRON).define('p', Items.PISTON)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_IRON))
            .save(output.withConditions(new ConfigCondition(BCEnergy.FLAG_RF_ENGINE)), BuildCraftReborn.id("engine_rf"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, BCEnergyBlocks.MJ_DYNAMO.get())
            .pattern("wgw").pattern(" p ").pattern("GwG")
            .define('w', Tags.Items.DUSTS_REDSTONE).define('g', Tags.Items.GLASS_BLOCKS_COLORLESS)
            .define('G', BCCoreTags.GEARS_IRON).define('p', Items.PISTON)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_IRON))
            .save(output.withConditions(new ConfigCondition(BCEnergy.FLAG_MJ_DYNAMO)), BuildCraftReborn.id("mj_dynamo"));

        factory(output);
        transport(output);
        silicon(output);
        builders(output);
        robotics(output);
        packaging(output);
    }

    /** 7.1's recipes for the recipe packager and the stamping table (BuildCraftSilicon). */
    private static void packaging(RecipeOutput output) {
        Item redChipset = chipset(EnumRedstoneChipset.RED);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCSiliconPackaging.PACKAGER.get())
            .pattern(" I ").pattern("ICI").pattern(" P ")
            .define('I', Tags.Items.INGOTS_IRON).define('C', Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
            .define('P', Items.PISTON)
            .unlockedBy("has_piston", has(Items.PISTON))
            .save(output, BuildCraftReborn.id("packager"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCSiliconPackaging.STAMPING_TABLE.get())
            .pattern("OWO").pattern("ORO").pattern("OGO")
            .define('O', Tags.Items.OBSIDIANS_NORMAL).define('W', Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
            .define('R', redChipset).define('G', BCCoreTags.GEARS_GOLD)
            .unlockedBy("has_chipset", has(redChipset))
            .save(output, BuildCraftReborn.id("stamping_table"));
    }

    /** Robotics: 7.1's recipes (BuildCraftRobotics and BuildCraftSilicon). 1.12 had none, not even for the zone
     * planner. */
    private static void robotics(RecipeOutput output) {
        Item redChipset = chipset(EnumRedstoneChipset.RED);
        Item goldChipset = chipset(EnumRedstoneChipset.GOLD);
        Item diamondChipset = chipset(EnumRedstoneChipset.DIAMOND);
        Item crystal = BCRoboticsItems.REDSTONE_CRYSTAL.get();
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCRoboticsItems.ROBOT.get())
            .pattern("PPP").pattern("PRP").pattern("C C")
            .define('P', Tags.Items.INGOTS_IRON).define('R', crystal).define('C', diamondChipset)
            .unlockedBy("has_crystal", has(crystal))
            .save(output, BuildCraftReborn.id("robot"));
        // (the robot goggles had no recipe: BuildCraft never finished them)
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, BCRoboticsItems.ROBOT_GOGGLES.get())
            .pattern("LCL").pattern("G G")
            .define('L', Tags.Items.LEATHERS).define('C', redChipset).define('G', Tags.Items.GLASS_PANES)
            .unlockedBy("has_chipset", has(redChipset))
            .save(output, BuildCraftReborn.id("robot_goggles"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCRoboticsItems.REDSTONE_BOARD.get())
            .pattern("PPP").pattern("PRP").pattern("PPP")
            .define('P', Items.PAPER).define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_redstone", has(Tags.Items.DUSTS_REDSTONE))
            .save(output, BuildCraftReborn.id("redstone_board"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCRoboticsItems.ROBOT_STATION.get())
            .pattern(" I ").pattern("ICI")
            .define('I', Tags.Items.INGOTS_IRON).define('C', goldChipset)
            .unlockedBy("has_chipset", has(goldChipset))
            .save(output, BuildCraftReborn.id("robot_station"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCRoboticsBlocks.ZONE_PLANNER.get())
            .pattern("IRI").pattern("GMG").pattern("IDI")
            .define('I', Tags.Items.INGOTS_IRON).define('R', Tags.Items.DUSTS_REDSTONE)
            .define('G', BCCoreTags.GEARS_GOLD).define('D', BCCoreTags.GEARS_DIAMOND).define('M', Items.MAP)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_DIAMOND))
            .save(output, BuildCraftReborn.id("zone_planner"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCRoboticsBlocks.REQUESTER.get())
            .pattern("IPI").pattern("GCG").pattern("IRI")
            .define('I', Tags.Items.INGOTS_IRON).define('P', Items.PISTON).define('G', BCCoreTags.GEARS_IRON)
            .define('C', Tags.Items.CHESTS_WOODEN).define('R', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_IRON))
            .save(output, BuildCraftReborn.id("requester"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCRoboticsBlocks.CHARGING_TABLE.get())
            .pattern("OrO").pattern("OcO").pattern("OgO")
            .define('r', Tags.Items.DUSTS_REDSTONE).define('c', redChipset).define('g', BCCoreTags.GEARS_GOLD)
            .define('O', Tags.Items.OBSIDIANS_NORMAL)
            .unlockedBy("has_chipset", has(redChipset))
            .save(output, BuildCraftReborn.id("charging_table"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCRoboticsBlocks.PROGRAMMING_TABLE.get())
            .pattern("OeO").pattern("OcO").pattern("OgO")
            .define('e', Tags.Items.GEMS_EMERALD).define('c', redChipset).define('g', BCCoreTags.GEARS_DIAMOND)
            .define('O', Tags.Items.OBSIDIANS_NORMAL)
            .unlockedBy("has_chipset", has(redChipset))
            .save(output, BuildCraftReborn.id("programming_table"));
        // (a builders block, but only builder robots use it)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCBuildersBlocks.CONSTRUCTION_MARKER.get())
            .pattern("g").pattern("t")
            .define('g', BCCoreTags.GEARS_GOLD).define('t', Items.REDSTONE_TORCH)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_GOLD))
            .save(output, BuildCraftReborn.id("construction_marker"));
        // 7.1: 10,000,000 RF
        assembly(output, "redstone_crystal", 1_000_000, new ItemStack(crystal),
            IngredientStack.of(Ingredient.of(Tags.Items.STORAGE_BLOCKS_REDSTONE)));
    }

    /** Builders: 1.12's quarry, architect table, blueprint and template recipes
     * ({@code buildcraftbuilders/recipes/*.json}). 1.12 had none for the filler, builder and electronic library;
     * these use BuildCraft 7's recipes, so the whole module can be used in survival. */
    private static void builders(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCBuildersBlocks.QUARRY.get())
            .pattern("iri").pattern("gig").pattern("dpd")
            .define('i', BCCoreTags.GEARS_IRON).define('r', Tags.Items.DUSTS_REDSTONE)
            .define('g', BCCoreTags.GEARS_GOLD).define('d', BCCoreTags.GEARS_DIAMOND)
            .define('p', Items.DIAMOND_PICKAXE)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_DIAMOND))
            .save(output, BuildCraftReborn.id("quarry"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCBuildersItems.BLUEPRINT.get())
            .pattern("ppp").pattern("pbp").pattern("ppp")
            .define('p', Items.PAPER).define('b', Tags.Items.GEMS_LAPIS)
            .unlockedBy("has_paper", has(Items.PAPER))
            .save(output, BuildCraftReborn.id("blueprint"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCBuildersItems.TEMPLATE.get())
            .pattern("ppp").pattern("pbp").pattern("ppp")
            .define('p', Items.PAPER).define('b', Tags.Items.DYES_BLACK)
            .unlockedBy("has_paper", has(Items.PAPER))
            .save(output, BuildCraftReborn.id("template"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCBuildersBlocks.ARCHITECT.get())
            .pattern("bmb").pattern("ycy").pattern("dsd")
            .define('b', Tags.Items.DYES_BLACK).define('m', BCCoreBlocks.MARKER_VOLUME.get())
            .define('y', Tags.Items.DYES_YELLOW).define('c', Items.CRAFTING_TABLE)
            .define('d', BCCoreTags.GEARS_DIAMOND).define('s', BCBuildersItems.BLUEPRINT.get())
            .unlockedBy("has_blueprint", has(BCBuildersItems.BLUEPRINT.get()))
            .save(output, BuildCraftReborn.id("architect_table"));
        // BuildCraft 7's recipes (not in 1.12)
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCBuildersBlocks.FILLER.get())
            .pattern("bmb").pattern("ycy").pattern("gCg")
            .define('b', Tags.Items.DYES_BLACK).define('m', BCCoreBlocks.MARKER_VOLUME.get())
            .define('y', Tags.Items.DYES_YELLOW).define('c', Items.CRAFTING_TABLE)
            .define('g', BCCoreTags.GEARS_GOLD).define('C', Tags.Items.CHESTS_WOODEN)
            .unlockedBy("has_marker", has(BCCoreBlocks.MARKER_VOLUME.get()))
            .save(output, BuildCraftReborn.id("filler"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCBuildersBlocks.BUILDER.get())
            .pattern("bmb").pattern("ycy").pattern("gCg")
            .define('b', Tags.Items.DYES_BLACK).define('m', BCCoreBlocks.MARKER_VOLUME.get())
            .define('y', Tags.Items.DYES_YELLOW).define('c', Items.CRAFTING_TABLE)
            .define('g', BCCoreTags.GEARS_DIAMOND).define('C', Tags.Items.CHESTS_WOODEN)
            .unlockedBy("has_blueprint", has(BCBuildersItems.BLUEPRINT.get()))
            .save(output, BuildCraftReborn.id("builder"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCBuildersBlocks.LIBRARY.get())
            .pattern("igi").pattern("bBb").pattern("iri")
            .define('i', Tags.Items.INGOTS_IRON).define('g', BCCoreTags.GEARS_IRON)
            .define('b', Items.BOOKSHELF).define('B', BCBuildersItems.BLUEPRINT.get())
            .define('r', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_blueprint", has(BCBuildersItems.BLUEPRINT.get()))
            .save(output, BuildCraftReborn.id("library"));
    }

    /** Silicon: 1.12's laser and table recipes ({@code buildcraftsilicon/recipes/*.json}), the gate crafting recipes
     * and every assembly recipe (1.12 added those from code, in {@code BCSiliconRecipes} and, for wires,
     * {@code BCTransportRecipes}). */
    private static void silicon(RecipeOutput output) {
        Item redChipset = chipset(EnumRedstoneChipset.RED);
        Item ironChipset = chipset(EnumRedstoneChipset.IRON);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCSiliconBlocks.LASER.get())
            .pattern("rro").pattern("rdd").pattern("rro")
            .define('r', Tags.Items.DUSTS_REDSTONE).define('d', Tags.Items.GEMS_DIAMOND)
            .define('o', Tags.Items.OBSIDIANS_NORMAL)
            .unlockedBy("has_diamond", has(Tags.Items.GEMS_DIAMOND))
            .save(output, BuildCraftReborn.id("laser"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCSiliconBlocks.ASSEMBLY_TABLE.get())
            .pattern("OdO").pattern("OrO").pattern("OgO")
            .define('r', Tags.Items.DUSTS_REDSTONE).define('d', Tags.Items.GEMS_DIAMOND)
            .define('g', BCCoreTags.GEARS_DIAMOND).define('O', Tags.Items.OBSIDIANS_NORMAL)
            .unlockedBy("has_laser", has(BCSiliconBlocks.LASER.get()))
            .save(output, BuildCraftReborn.id("assembly_table"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCSiliconBlocks.ADVANCED_CRAFTING_TABLE.get())
            .pattern("OtO").pattern("OcO").pattern("OrO")
            .define('r', redChipset).define('c', Tags.Items.CHESTS_WOODEN).define('t', Items.CRAFTING_TABLE)
            .define('O', Tags.Items.OBSIDIANS_NORMAL)
            .unlockedBy("has_chipset", has(redChipset))
            .save(output, BuildCraftReborn.id("advanced_crafting_table"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCSiliconBlocks.INTEGRATION_TABLE.get())
            .pattern("OiO").pattern("OrO").pattern("OgO")
            .define('r', ironChipset).define('g', BCCoreTags.GEARS_DIAMOND).define('i', Tags.Items.INGOTS_GOLD)
            .define('O', Tags.Items.OBSIDIANS_NORMAL)
            .unlockedBy("has_chipset", has(ironChipset))
            .save(output, BuildCraftReborn.id("integration_table"));

        // Gates that can be made in a crafting table
        ItemPluggableGate gateItem = BCSiliconItems.PLUG_GATE.get();
        Ingredient blocker = Ingredient.of(BCTransportItems.PLUG_BLOCKER.get());
        craftGate(output, Ingredient.of(Items.BRICK), blocker, EnumGateMaterial.CLAY_BRICK);
        craftGate(output, Ingredient.of(Tags.Items.INGOTS_IRON), blocker, EnumGateMaterial.IRON);
        craftGate(output, Ingredient.of(Items.NETHER_BRICK), blocker, EnumGateMaterial.NETHER_BRICK);
        ItemStack ironGate = gateItem.getStack(new GateVariant(EnumGateLogic.AND, EnumGateMaterial.IRON,
            EnumGateModifier.NO_MODIFIER));
        for (Object[] mod : new Object[][] { { Tags.Items.GEMS_LAPIS, EnumGateModifier.LAPIS },
            { Tags.Items.GEMS_QUARTZ, EnumGateModifier.QUARTZ } }) {
            @SuppressWarnings("unchecked")
            TagKey<Item> m = (TagKey<Item>) mod[0];
            EnumGateModifier modifier = (EnumGateModifier) mod[1];
            ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, gateItem.getStack(new GateVariant(EnumGateLogic.AND,
                EnumGateMaterial.IRON, modifier)))
                .pattern(" m ").pattern("mgm").pattern(" m ")
                .define('m', m).define('g', gateIngredient(ironGate))
                .unlockedBy("has_gate", has(gateItem))
                .save(output, BuildCraftReborn.id("plug_gate_create_iron_" + modifier.tag));
        }
        // And gate <-> or gate
        for (EnumGateMaterial material : EnumGateMaterial.VALUES) {
            if (material == EnumGateMaterial.CLAY_BRICK) {
                continue;
            }
            for (EnumGateModifier modifier : EnumGateModifier.VALUES) {
                ItemStack and = gateItem.getStack(new GateVariant(EnumGateLogic.AND, material, modifier));
                ItemStack or = gateItem.getStack(new GateVariant(EnumGateLogic.OR, material, modifier));
                String name = "plug_gate_" + material.tag + "_" + modifier.tag;
                ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, and).pattern("i")
                    .define('i', gateIngredient(or)).group("plug_gate")
                    .unlockedBy("has_gate", has(gateItem)).save(output, BuildCraftReborn.id(name + "_and"));
                ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, or).pattern("i")
                    .define('i', gateIngredient(and)).group("plug_gate")
                    .unlockedBy("has_gate", has(gateItem)).save(output, BuildCraftReborn.id(name + "_or"));
            }
        }

        // Assembly table
        IngredientStack redstone = IngredientStack.of(Ingredient.of(Tags.Items.DUSTS_REDSTONE));
        assembly(output, "redstone_chipset", 10_000, new ItemStack(redChipset), redstone);
        assembly(output, "iron_chipset", 20_000, new ItemStack(ironChipset), redstone,
            IngredientStack.of(Ingredient.of(Tags.Items.INGOTS_IRON)));
        assembly(output, "gold_chipset", 40_000, new ItemStack(chipset(EnumRedstoneChipset.GOLD)), redstone,
            IngredientStack.of(Ingredient.of(Tags.Items.INGOTS_GOLD)));
        assembly(output, "quartz_chipset", 60_000, new ItemStack(chipset(EnumRedstoneChipset.QUARTZ)), redstone,
            IngredientStack.of(Ingredient.of(Tags.Items.GEMS_QUARTZ)));
        assembly(output, "diamond_chipset", 80_000, new ItemStack(chipset(EnumRedstoneChipset.DIAMOND)), redstone,
            IngredientStack.of(Ingredient.of(Tags.Items.GEMS_DIAMOND)));

        assembly(output, "plug_pulsar", 1000, new ItemStack(BCSiliconItems.PLUG_PULSAR.get()),
            IngredientStack.of(BCCoreBlocks.ENGINE_WOOD.get()),
            new IngredientStack(Ingredient.of(Tags.Items.INGOTS_IRON), 2));
        assembly(output, "light_sensor", 500, new ItemStack(BCSiliconItems.PLUG_LIGHT_SENSOR.get()),
            IngredientStack.of(Items.DAYLIGHT_DETECTOR));
        assembly(output, "timer", 500, new ItemStack(BCSiliconItems.PLUG_TIMER.get()),
            IngredientStack.of(Items.CLOCK));
        assembly(output, "gate_copier", 500, new ItemStack(BCSiliconItems.GATE_COPIER.get()),
            IngredientStack.of(BCCoreItems.WRENCH.get()), IngredientStack.of(ironChipset));

        assemblyGate(output, 20_000, EnumGateMaterial.IRON, IngredientStack.of(ironChipset));
        assemblyGate(output, 40_000, EnumGateMaterial.NETHER_BRICK, IngredientStack.of(ironChipset),
            IngredientStack.of(Items.NETHER_BRICKS));
        assemblyGate(output, 80_000, EnumGateMaterial.GOLD, IngredientStack.of(chipset(EnumRedstoneChipset.GOLD)));
        IngredientStack lapis = IngredientStack.of(Ingredient.of(Tags.Items.GEMS_LAPIS));
        IngredientStack quartz = IngredientStack.of(chipset(EnumRedstoneChipset.QUARTZ));
        IngredientStack diamond = IngredientStack.of(chipset(EnumRedstoneChipset.DIAMOND));
        assemblyModifier(output, 40_000, EnumGateMaterial.IRON, EnumGateModifier.LAPIS, lapis);
        assemblyModifier(output, 60_000, EnumGateMaterial.IRON, EnumGateModifier.QUARTZ, quartz);
        assemblyModifier(output, 80_000, EnumGateMaterial.IRON, EnumGateModifier.DIAMOND, diamond);
        assemblyModifier(output, 80_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.LAPIS, lapis);
        assemblyModifier(output, 100_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.QUARTZ, quartz);
        assemblyModifier(output, 120_000, EnumGateMaterial.NETHER_BRICK, EnumGateModifier.DIAMOND, diamond);
        assemblyModifier(output, 100_000, EnumGateMaterial.GOLD, EnumGateModifier.LAPIS, lapis);
        assemblyModifier(output, 140_000, EnumGateMaterial.GOLD, EnumGateModifier.QUARTZ, quartz);
        assemblyModifier(output, 180_000, EnumGateMaterial.GOLD, EnumGateModifier.DIAMOND, diamond);

        // Lenses and filters: stained glass (any glass for the clear ones), and iron bars for a filter
        ItemPluggableLens lens = BCSiliconItems.PLUG_LENS.get();
        IngredientStack bars = IngredientStack.of(Items.IRON_BARS);
        for (DyeColor colour : DyeColor.values()) {
            Item glass = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(colour.getName()
                + "_stained_glass"));
            IngredientStack stained = IngredientStack.of(glass);
            assembly(output, "lens_regular_" + colour.getName(), 500, lens.getStack(colour, false), stained);
            assembly(output, "lens_filter_" + colour.getName(), 500, lens.getStack(colour, true), stained, bars);
        }
        IngredientStack clear = IngredientStack.of(Ingredient.of(Tags.Items.GLASS_BLOCKS_COLORLESS));
        assembly(output, "lens_regular", 500, lens.getStack(null, false), clear);
        assembly(output, "lens_filter", 500, lens.getStack(null, true), clear, bars);

        // Facades: 3 structure pipes and any block make 6 (worked out in code), and a facade swaps to hollow and back
        output.accept(BuildCraftReborn.id("assembly/facades"), FacadeAssemblyRecipe.INSTANCE, null);
        SpecialRecipeBuilder.special(FacadeSwapRecipe::new).save(output, BuildCraftReborn.id("facade_swap"));

        // Pipe wires (1.12: BCTransportRecipes): redstone and a dye make 8
        for (DyeColor colour : DyeColor.values()) {
            assembly(output, "wire_" + colour.getName(), 10_000,
                new ItemStack(BCTransportItems.WIRES.get(colour).get(), 8), redstone,
                IngredientStack.of(Ingredient.of(colour.getTag())));
        }
    }

    /** A gate of the stack's variant (1.12: IngredientNBTBC). Only the {@code gate} component is checked. */
    private static Ingredient gateIngredient(ItemStack gate) {
        return DataComponentIngredient.of(false, DataComponentPredicate.builder()
            .expect(BCSiliconItems.GATE_VARIANT.get(), ItemPluggableGate.getVariant(gate)).build(), gate.getItem());
    }

    private static Item chipset(EnumRedstoneChipset chipset) {
        return BCSiliconItems.CHIPSETS.get(chipset).get();
    }

    private static void craftGate(RecipeOutput output, Ingredient material, Ingredient blocker,
        EnumGateMaterial gate) {
        ItemStack result = BCSiliconItems.PLUG_GATE.get().getStack(new GateVariant(EnumGateLogic.AND, gate,
            EnumGateModifier.NO_MODIFIER));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, result)
            .pattern(" m ").pattern("mrm").pattern(" b ")
            .define('m', material).define('r', Tags.Items.DUSTS_REDSTONE).define('b', blocker)
            .unlockedBy("has_plug", has(BCTransportItems.PLUG_BLOCKER.get()))
            .save(output, BuildCraftReborn.id("plug_gate_create_" + gate.tag + "_no_modifier"));
    }

    /** An and gate and an or gate made from the same things. */
    private static void assemblyGate(RecipeOutput output, long mj, EnumGateMaterial material,
        IngredientStack... inputs) {
        for (EnumGateLogic logic : EnumGateLogic.VALUES) {
            ItemStack gate = BCSiliconItems.PLUG_GATE.get().getStack(new GateVariant(logic, material,
                EnumGateModifier.NO_MODIFIER));
            assembly(output, "gate_" + logic.tag + "_" + material.tag + "_no_modifier", mj, gate, inputs);
        }
    }

    /** Adds a modifier to a gate. */
    private static void assemblyModifier(RecipeOutput output, long mj, EnumGateMaterial material,
        EnumGateModifier modifier, IngredientStack mod) {
        for (EnumGateLogic logic : EnumGateLogic.VALUES) {
            ItemStack from = BCSiliconItems.PLUG_GATE.get().getStack(new GateVariant(logic, material,
                EnumGateModifier.NO_MODIFIER));
            ItemStack to = BCSiliconItems.PLUG_GATE.get().getStack(new GateVariant(logic, material, modifier));
            assembly(output, "gate_modifier_" + logic.tag + "_" + material.tag + "_" + modifier.tag, mj, to,
                new IngredientStack(gateIngredient(from)), mod);
        }
    }

    private static void assembly(RecipeOutput output, String name, long mj, ItemStack result,
        IngredientStack... inputs) {
        output.accept(BuildCraftReborn.id("assembly/" + name),
            new AssemblyRecipeBasic(mj * MjAPI.MJ, List.of(inputs), result), null);
    }

    /** 1.12: BCTransportRecipes (pipes, made 8 at a time with glass: stained glass makes painted pipes) and
     * buildcrafttransport/recipes/*.json. Upgrades between pipe types keep the paint (see PipeUpgradeRecipe). */
    private static void transport(RecipeOutput output) {
        pipe(output, BCTransportPipes.woodItem, Ingredient.of(ItemTags.PLANKS));
        pipe(output, BCTransportPipes.cobbleItem, Ingredient.of(Tags.Items.COBBLESTONES));
        pipe(output, BCTransportPipes.stoneItem, Ingredient.of(Tags.Items.STONES));
        pipe(output, BCTransportPipes.quartzItem, Ingredient.of(Items.QUARTZ_BLOCK));
        pipe(output, BCTransportPipes.ironItem, Ingredient.of(Tags.Items.INGOTS_IRON));
        pipe(output, BCTransportPipes.goldItem, Ingredient.of(Tags.Items.INGOTS_GOLD));
        pipe(output, BCTransportPipes.clayItem, Ingredient.of(Items.CLAY));
        pipe(output, BCTransportPipes.sandstoneItem, Ingredient.of(Tags.Items.SANDSTONE_UNCOLORED_BLOCKS));
        pipe(output, BCTransportPipes.voidItem, Ingredient.of(Tags.Items.DYES_BLACK),
            Ingredient.of(Tags.Items.DUSTS_REDSTONE));
        pipe(output, BCTransportPipes.obsidianItem, Ingredient.of(Tags.Items.OBSIDIANS_NORMAL));
        pipe(output, BCTransportPipes.diamondItem, Ingredient.of(Tags.Items.GEMS_DIAMOND));
        pipe(output, BCTransportPipes.lapisItem, Ingredient.of(Tags.Items.STORAGE_BLOCKS_LAPIS));
        pipe(output, BCTransportPipes.daizuliItem, Ingredient.of(Tags.Items.STORAGE_BLOCKS_LAPIS),
            Ingredient.of(Tags.Items.GEMS_DIAMOND));
        pipe(output, BCTransportPipes.diaWoodItem, Ingredient.of(ItemTags.PLANKS),
            Ingredient.of(Tags.Items.GEMS_DIAMOND));
        pipe(output, BCTransportPipes.stripesItem, Ingredient.of(BCCoreTags.GEARS_GOLD));
        upgrade(output, BCTransportPipes.diaWoodItem, BCTransportPipes.emzuliItem,
            Ingredient.of(Tags.Items.STORAGE_BLOCKS_LAPIS));

        Ingredient sealant = Ingredient.of(BCTransportItems.WATERPROOF.get());
        upgrade(output, BCTransportPipes.woodItem, BCTransportPipes.woodFluid, sealant);
        upgrade(output, BCTransportPipes.cobbleItem, BCTransportPipes.cobbleFluid, sealant);
        upgrade(output, BCTransportPipes.stoneItem, BCTransportPipes.stoneFluid, sealant);
        upgrade(output, BCTransportPipes.quartzItem, BCTransportPipes.quartzFluid, sealant);
        upgrade(output, BCTransportPipes.ironItem, BCTransportPipes.ironFluid, sealant);
        upgrade(output, BCTransportPipes.goldItem, BCTransportPipes.goldFluid, sealant);
        upgrade(output, BCTransportPipes.clayItem, BCTransportPipes.clayFluid, sealant);
        upgrade(output, BCTransportPipes.sandstoneItem, BCTransportPipes.sandstoneFluid, sealant);
        upgrade(output, BCTransportPipes.voidItem, BCTransportPipes.voidFluid, sealant);
        upgrade(output, BCTransportPipes.diamondItem, BCTransportPipes.diamondFluid, sealant);
        upgrade(output, BCTransportPipes.diaWoodItem, BCTransportPipes.diaWoodFluid, sealant);
        // (1.12 had no obsidian fluid pipe item, so no recipe either)

        Ingredient redstone = Ingredient.of(Tags.Items.DUSTS_REDSTONE);
        upgrade(output, BCTransportPipes.woodItem, BCTransportPipes.woodPower, redstone);
        upgrade(output, BCTransportPipes.cobbleItem, BCTransportPipes.cobblePower, redstone);
        upgrade(output, BCTransportPipes.stoneItem, BCTransportPipes.stonePower, redstone);
        upgrade(output, BCTransportPipes.quartzItem, BCTransportPipes.quartzPower, redstone);
        upgrade(output, BCTransportPipes.ironItem, BCTransportPipes.ironPower, redstone);
        upgrade(output, BCTransportPipes.goldItem, BCTransportPipes.goldPower, redstone);
        upgrade(output, BCTransportPipes.sandstoneItem, BCTransportPipes.sandstonePower, redstone);
        upgrade(output, BCTransportPipes.diamondItem, BCTransportPipes.diamondPower, redstone);
        upgrade(output, BCTransportPipes.diaWoodItem, BCTransportPipes.diaWoodPower, redstone);

        // FE pipes (1.12's RF pipes): a kinesis pipe and redstone. 1.12 only had recipes for the wood, cobblestone
        // and stone ones; the other six were uncraftable.
        upgrade(output, BCTransportPipes.woodPower, BCTransportPipes.woodRf, redstone);
        upgrade(output, BCTransportPipes.cobblePower, BCTransportPipes.cobbleRf, redstone);
        upgrade(output, BCTransportPipes.stonePower, BCTransportPipes.stoneRf, redstone);
        upgrade(output, BCTransportPipes.quartzPower, BCTransportPipes.quartzRf, redstone);
        upgrade(output, BCTransportPipes.ironPower, BCTransportPipes.ironRf, redstone);
        upgrade(output, BCTransportPipes.goldPower, BCTransportPipes.goldRf, redstone);
        upgrade(output, BCTransportPipes.sandstonePower, BCTransportPipes.sandstoneRf, redstone);
        upgrade(output, BCTransportPipes.diamondPower, BCTransportPipes.diamondRf, redstone);
        upgrade(output, BCTransportPipes.diaWoodPower, BCTransportPipes.diaWoodRf, redstone);

        Item structure = pipeItem(BCTransportPipes.structure);
        // 1.12: pipe_structure.json
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, structure, 8)
            .pattern("cgc")
            .define('c', Tags.Items.COBBLESTONES)
            .define('g', Items.GRAVEL)
            .unlockedBy("has_gravel", has(Items.GRAVEL))
            .save(output, BuildCraftReborn.id("pipe_structure"));
        // 1.12: pipe_sealant.json ("sealant" was cactus green or a slime ball)
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BCTransportItems.WATERPROOF.get())
            .requires(Ingredient.fromValues(java.util.stream.Stream.of(
                new Ingredient.TagValue(Tags.Items.DYES_GREEN), new Ingredient.TagValue(Tags.Items.SLIME_BALLS))))
            .unlockedBy("has_slime", has(Tags.Items.SLIME_BALLS))
            .unlockedBy("has_green_dye", has(Tags.Items.DYES_GREEN))
            .save(output, BuildCraftReborn.id("pipe_sealant"));
        // 1.12: plug_blocker.json and plug_power_adaptor.json
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, BCTransportItems.PLUG_BLOCKER.get(), 4)
            .pattern("s")
            .define('s', structure)
            .unlockedBy("has_structure_pipe", has(structure))
            .save(output, BuildCraftReborn.id("plug_blocker"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, BCTransportItems.PLUG_POWER_ADAPTOR.get(), 4)
            .pattern("sis")
            .pattern("sgs")
            .pattern("srs")
            .define('s', structure)
            .define('i', Tags.Items.INGOTS_GOLD)
            .define('g', BCCoreTags.GEARS_STONE)
            .define('r', Tags.Items.DUSTS_REDSTONE)
            .unlockedBy("has_structure_pipe", has(structure))
            .save(output, BuildCraftReborn.id("plug_power_adaptor"));
        // 1.12: filtered_buffer.json (only when the quarry existed; here always)
        Item diamondPipe = pipeItem(BCTransportPipes.diamondItem);
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, BCTransportBlocks.FILTERED_BUFFER.get())
            .pattern("wdw")
            .pattern("wcw")
            .pattern("wpw")
            .define('w', ItemTags.PLANKS)
            .define('d', diamondPipe)
            .define('c', Items.CHEST)
            .define('p', Items.PISTON)
            .unlockedBy("has_diamond_pipe", has(diamondPipe))
            .save(output, BuildCraftReborn.id("filtered_buffer"));
    }

    private static Item pipeItem(PipeDefinition def) {
        return BCTransportItems.PIPES.get(def).get();
    }

    private static void pipe(RecipeOutput output, PipeDefinition def, Ingredient material) {
        pipe(output, def, material, material);
    }

    /** A pipe made with glass between two materials, 8 at a time. Each colour of stained glass makes pipes painted
     * that colour. */
    private static void pipe(RecipeOutput output, PipeDefinition def, Ingredient left, Ingredient right) {
        Item pipe = pipeItem(def);
        String name = BCTransportItems.pipeItemName(def);
        ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, pipe, 8)
            .pattern("lgr")
            .define('l', left)
            .define('g', Tags.Items.GLASS_BLOCKS_COLORLESS)
            .define('r', right)
            .unlockedBy("has_glass", has(Tags.Items.GLASS_BLOCKS_COLORLESS))
            .save(output, BuildCraftReborn.id(name + "_colorless"));
        for (DyeColor colour : DyeColor.values()) {
            ItemStack result = new ItemStack(pipe, 8);
            result.set(BCItemComponents.COLOUR.get(), colour);
            Item glass = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(colour.getName()
                + "_stained_glass"));
            ShapedRecipeBuilder.shaped(RecipeCategory.TRANSPORTATION, result)
                .pattern("lgr")
                .define('l', left)
                .define('g', glass)
                .define('r', right)
                .unlockedBy("has_glass", has(glass))
                .save(output, BuildCraftReborn.id(name + "_" + colour.getName()));
        }
    }

    /** {@code from} and the additive make {@code to}; {@code to} alone turns back into {@code from}. Both keep the
     * pipe's paint. */
    private static void upgrade(RecipeOutput output, PipeDefinition from, PipeDefinition to, Ingredient additive) {
        Item fromItem = pipeItem(from);
        Item toItem = pipeItem(to);
        String name = BCTransportItems.pipeItemName(to);
        output.accept(BuildCraftReborn.id(name), new PipeUpgradeRecipe("", fromItem, Optional.of(additive), toItem),
            null);
        output.accept(BuildCraftReborn.id(name + "_undo"), new PipeUpgradeRecipe("", toItem, Optional.empty(),
            fromItem), null);
    }

    /** 1.12: buildcraftfactory/recipes/*.json */
    private static void factory(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BCFactoryBlocks.TANK.get())
            .pattern("ggg")
            .pattern("g g")
            .pattern("ggg")
            .define('g', Tags.Items.GLASS_BLOCKS_COLORLESS)
            .unlockedBy("has_glass", has(Tags.Items.GLASS_BLOCKS_COLORLESS))
            .save(output, BuildCraftReborn.id("tank"));
        ShapedRecipeBuilder.shaped(RecipeCategory.DECORATIONS, BCFactoryBlocks.AUTO_WORKBENCH_ITEMS.get())
            .pattern("gwg")
            .define('g', BCCoreTags.GEARS_STONE)
            .define('w', Tags.Items.PLAYER_WORKSTATIONS_CRAFTING_TABLES)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_STONE))
            .save(output, BuildCraftReborn.id("autoworkbench_item"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, BCFactoryBlocks.CHUTE.get())
            .pattern("ici")
            .pattern("igi")
            .pattern(" i ")
            .define('c', Items.CHEST)
            .define('g', BCCoreTags.GEARS_STONE)
            .define('i', Tags.Items.INGOTS_IRON)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_STONE))
            .save(output, BuildCraftReborn.id("chute"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, BCFactoryBlocks.DISTILLER.get())
            .pattern("rtr")
            .pattern("tgt")
            .define('r', Items.REDSTONE_TORCH)
            .define('t', BCFactoryBlocks.TANK.get())
            .define('g', BCCoreTags.GEARS_DIAMOND)
            .unlockedBy("has_tank", has(BCFactoryBlocks.TANK.get()))
            .save(output, BuildCraftReborn.id("distiller"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, BCFactoryBlocks.FLOOD_GATE.get())
            .pattern("igi")
            .pattern("btb")
            .pattern("ibi")
            .define('b', Items.IRON_BARS)
            .define('t', BCFactoryBlocks.TANK.get())
            .define('g', BCCoreTags.GEARS_IRON)
            .define('i', Tags.Items.INGOTS_IRON)
            .unlockedBy("has_tank", has(BCFactoryBlocks.TANK.get()))
            .save(output, BuildCraftReborn.id("flood_gate"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, BCFactoryBlocks.HEAT_EXCHANGE.get())
            .pattern("igi")
            .pattern("###")
            .pattern("igi")
            .define('i', Tags.Items.INGOTS_IRON)
            .define('g', BCCoreTags.GEARS_IRON)
            .define('#', Tags.Items.GLASS_BLOCKS_COLORLESS)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_IRON))
            .save(output, BuildCraftReborn.id("heat_exchange"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, BCFactoryBlocks.MINING_WELL.get())
            .pattern("iri")
            .pattern("igi")
            .pattern("ipi")
            .define('p', Items.IRON_PICKAXE)
            .define('r', Tags.Items.DUSTS_REDSTONE)
            .define('g', BCCoreTags.GEARS_IRON)
            .define('i', Tags.Items.INGOTS_IRON)
            .unlockedBy("has_gear", has(BCCoreTags.GEARS_IRON))
            .save(output, BuildCraftReborn.id("mining_well"));
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, BCFactoryBlocks.PUMP.get())
            .pattern("iri")
            .pattern("igi")
            .pattern("tbt")
            .define('r', Tags.Items.DUSTS_REDSTONE)
            .define('b', Items.BUCKET)
            .define('t', BCFactoryBlocks.TANK.get())
            .define('g', BCCoreTags.GEARS_IRON)
            .define('i', Tags.Items.INGOTS_IRON)
            .unlockedBy("has_tank", has(BCFactoryBlocks.TANK.get()))
            .save(output, BuildCraftReborn.id("pump"));
        // 1.12 asked for a universal bucket of (cool) residue
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BCFactoryItems.WATER_GEL_SPAWN.get())
            .pattern(" s ")
            .pattern("srs")
            .pattern(" s ")
            .define('s', Items.SAND)
            .define('r', BCEnergyFluids.oilResidue[0].bucket.get())
            .unlockedBy("has_residue", has(BCEnergyFluids.oilResidue[0].bucket.get()))
            .save(output, BuildCraftReborn.id("water_gel_spawn"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.WATER_BUCKET)
            .pattern("g")
            .pattern("b")
            .define('g', BCFactoryItems.GEL.get())
            .define('b', Items.BUCKET)
            .unlockedBy("has_gel", has(BCFactoryItems.GEL.get()))
            .save(output, BuildCraftReborn.id("water_gel_to_bucket"));
    }

    /** Three of {@code top}, glass, then gear-piston-gear. */
    private static void engine(RecipeOutput output, ItemLike result, Ingredient top, TagKey<Item> gear, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, result)
            .pattern("www")
            .pattern(" g ")
            .pattern("GpG")
            .define('w', top)
            .define('g', Tags.Items.GLASS_BLOCKS_COLORLESS)
            .define('G', gear)
            .define('p', Items.PISTON)
            .unlockedBy("has_gear", has(gear))
            .save(output, BuildCraftReborn.id(name));
    }

    /** Four of {@code material} around a gear of the previous tier. */
    private static void gear(RecipeOutput output, ItemLike result, TagKey<Item> material, TagKey<Item> previousGear,
        String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result)
            .pattern(" o ")
            .pattern("oio")
            .pattern(" o ")
            .define('o', material)
            .define('i', previousGear)
            .unlockedBy("has_previous_gear", has(previousGear))
            .save(output, BuildCraftReborn.id(name));
    }
}
