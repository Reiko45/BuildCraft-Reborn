/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import java.util.Locale;

import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.LanguageProvider;

import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.BCBuildersItems;
import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.enums.EnumDecoratedBlock;
import buildcraftreborn.core.BCCore;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.BCCoreTags;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.energy.BCEnergyTags;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.BCFactoryItems;
import buildcraftreborn.api.enums.EnumRedstoneChipset;
import buildcraftreborn.robotics.BCRobotics;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.BCRoboticsEntities;
import buildcraftreborn.robotics.BCRoboticsItems;
import buildcraftreborn.silicon.BCSilicon;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.transport.BCTransport;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.transport.BCTransportItems;
import buildcraftreborn.lib.fluid.BCFluid;
import buildcraftreborn.test.BCTestContent;

/**
 * English names. Display strings are taken from 1.12's {@code en_US.lang} (for example
 * {@code item.woodenGearItem.name=Wood Gear}); keys follow 1.21 conventions ({@code item.buildcraftreborn.gear_wood}).
 */
public final class BCLanguage extends LanguageProvider {
    public BCLanguage(PackOutput output) {
        super(output, BuildCraftReborn.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add(BCCore.TAB_MAIN_KEY, "BuildCraft Reborn");

        add(BCCoreItems.GEAR_WOOD.get(), "Wood Gear");
        add(BCCoreItems.GEAR_STONE.get(), "Stone Gear");
        add(BCCoreItems.GEAR_IRON.get(), "Iron Gear");
        add(BCCoreItems.GEAR_GOLD.get(), "Gold Gear");
        add(BCCoreItems.GEAR_DIAMOND.get(), "Diamond Gear");

        add(BCCoreItems.WRENCH.get(), "Wrench");
        add(BCCoreItems.DEBUGGER.get(), "Debugger");
        guide();
        compat();
        add(BCCoreItems.LIST.get(), "List");
        add(BCCoreItems.MAP_LOCATION.get(), "Map Location");
        add("gui.list.nbt", "Require Exact Match\n\u00A77(Enchantments, names and stored data must precisely match)");
        add("gui.list.metadata", "Accept Variations\n\u00A77(Same Type)");
        add("gui.list.oredict", "Accept Equivalents\n\u00A77(Same Material)");
        add("tip.list.matches", "Matches");
        add("item.debugger.warning", "Use only for testing! Leaks secrets.");
        add(BCCoreItems.FRAGILE_FLUID_SHARD.get(), "Fragile %s Shard");
        add(BCCoreBlocks.ENGINE_WOOD.get(), "Redstone Engine");
        add(BCCoreBlocks.ENGINE_CREATIVE.get(), "Creative Engine");
        add(BCCoreBlocks.ENGINE_CREATIVE.get().getDescriptionId() + ".tip",
            "Right click with a wrench to change power output level.");
        add(BCEnergyBlocks.ENGINE_STONE.get(), "Stirling Engine");
        add(BCEnergyBlocks.ENGINE_IRON.get(), "Combustion Engine");
        add(BCEnergyBlocks.ENGINE_RF.get(), "RF Engine");
        add(BCEnergyBlocks.ENGINE_RF.get().getDescriptionId() + ".tip", "Converts Redstone Flux to Minecraft Joules");
        add(BCEnergyBlocks.MJ_DYNAMO.get(), "MJ Dynamo");
        add(BCEnergyBlocks.MJ_DYNAMO.get().getDescriptionId() + ".tip", "Converts Minecraft Joules to Redstone Flux");
        add("buildcraft.gui.rf_engine.upgrade_types", "Compatible Upgrades:");
        add("buildcraft.help.rf_engine.upgrades.title", "Upgrades");
        add("buildcraft.help.rf_engine.upgrades.desc",
            "Add compatible gears to\nincrease conversion rate.\n\nMax 1 gear per slot.");
        add("buildcraft.help.rf_engine.rf_battery.title", "RF Battery");
        add("buildcraft.help.rf_engine.rf_battery.desc", "Converts %s\nto %s");
        add("buildcraft.help.mj_dynamo.rf_battery.title", "RF Battery");
        add("buildcraft.help.mj_dynamo.rf_battery.desc", "Converts %s\nto %s");
        add(BCCoreBlocks.WATER_SPRING.get(), "Water Spring");
        add(BCCoreBlocks.MARKER_VOLUME.get(), "Land Mark");
        add(BCCoreBlocks.MARKER_PATH.get(), "Path Mark");
        add(BCCoreItems.MARKER_CONNECTOR.get(), "Marker Connector");
        // 1.12 only named three of these
        String[][] decorations = { { "destroy", "Destroy" }, { "blueprint", "Blueprint" }, { "template", "Template" },
            { "paper", "Paper" }, { "leather", "Leather" }, { "laser_back", "Laser" } };
        for (String[] decoration : decorations) {
            add(BCCoreBlocks.DECORATED.get(EnumDecoratedBlock.valueOf(decoration[0].toUpperCase(Locale.ROOT))).get(),
                decoration[1] + " (Decoration)");
        }
        add(BCEnergyBlocks.OIL_SPRING.get(), "Oil Spring");

        // Factory
        add(BCFactoryBlocks.AUTO_WORKBENCH_ITEMS.get(), "Auto Workbench");
        add(BCFactoryBlocks.CHUTE.get(), "Chute");
        add(BCFactoryBlocks.FLOOD_GATE.get(), "Flood Gate");
        add(BCFactoryBlocks.MINING_WELL.get(), "Mining Well");
        add(BCFactoryBlocks.PUMP.get(), "Pump");
        add(BCFactoryBlocks.TANK.get(), "Tank");
        add(BCFactoryBlocks.DISTILLER.get(), "Distiller");
        add(BCFactoryBlocks.HEAT_EXCHANGE.get(), "Heat Exchanger");
        // These two have no items; named for block info mods (1.12 had no names for them)
        add(BCFactoryBlocks.TUBE.get(), "Mining Tube");
        add(BCFactoryBlocks.WATER_GEL.get(), "Water Gel");
        add(BCFactoryItems.WATER_GEL_SPAWN.get(), "Water Gelifier");

        // Transport (pipe names, gate statements and pipe GUI text are 1.12's, see BCLegacyLang)
        add(BCTransport.TAB_PIPES_KEY, "BuildCraft Pipes");
        add(BCTransport.TAB_PLUGS_KEY, "BuildCraft Pluggables");
        add(BCTransportBlocks.FILTERED_BUFFER.get(), "Filtered Buffer");
        add(BCTransportBlocks.PIPE_HOLDER.get(), "Pipe");
        add(BCTransportItems.WATERPROOF.get(), "Pipe Sealant");
        add(BCTransportItems.PLUG_BLOCKER.get(), "Pipe Plug");
        add(BCTransportItems.PLUG_POWER_ADAPTOR.get(), "Power Adapter");
        BCTransportItems.WIRES.values().forEach(wire -> add(wire.get(), "Pipe Wire"));
        add("buildcraft.fe.flow", "%s FE/t");
        for (String[] entry : BCLegacyLang.ENTRIES) {
            add(entry[0], entry[1]);
        }
        // Silicon (1.12's names)
        add(BCCoreItems.PAINTBRUSH.get(), "Paintbrush");
        add(BCSiliconBlocks.LASER.get(), "Laser");
        add(BCSiliconBlocks.ASSEMBLY_TABLE.get(), "Assembly Table");
        add(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE.get(), "Advanced Crafting Table");
        add(BCSiliconBlocks.INTEGRATION_TABLE.get(), "Integration Table");
        // (1.12's tip, "No recipes use this!", isn't true any more: robots are programmed here)
        // 7.1's crafting by package
        add(BCSiliconPackaging.PACKAGER.get(), "Recipe Packager");
        add(BCSiliconPackaging.STAMPING_TABLE.get(), "Stamping Table");
        add(BCSiliconPackaging.PACKAGE.get(), "Package");
        add(BCSiliconPackaging.PACKAGE_ENTITY.get(), "Package");
        String[][] chipsets = { { "red", "Redstone Chipset" }, { "iron", "Iron Chipset" }, { "gold", "Golden Chipset" },
            { "quartz", "Quartz Chipset" }, { "diamond", "Diamond Chipset" } };
        for (String[] chipset : chipsets) {
            add(BCSiliconItems.CHIPSETS.get(EnumRedstoneChipset.valueOf(chipset[0].toUpperCase(java.util.Locale.ROOT)))
                .get(), chipset[1]);
        }
        add(BCSiliconItems.GATE_COPIER.get(), "Gate Copier");
        // A coloured item's name (lens, filter, pipe wire, painted pipe, paintbrush): the colour, then the name.
        // Languages can reorder them ("%2$s %1$s") or bracket the colour ("%2$s (%1$s)") where it would have to agree.
        add("item.buildcraftreborn.coloured", "%s %s");
        add("buildcraft.item.nonclean.usage", "Shift-right click to clean");
        // (gates and lenses are named from their variant; these are fallbacks)
        add(BCSiliconItems.PLUG_GATE.get(), "Gate");
        add(BCSiliconItems.PLUG_LENS.get(), "Lens");
        add(BCSiliconItems.PLUG_PULSAR.get(), "Pipe Pulsar");
        add(BCSiliconItems.PLUG_LIGHT_SENSOR.get(), "Light Sensor");
        add(BCSiliconItems.PLUG_TIMER.get(), "Timer");
        add(BCSiliconItems.PLUG_FACADE.get(), "Facade");
        add("item.FacadePhased.name", "Phased Facade");
        add(BCSilicon.TAB_FACADES_KEY, "BuildCraft Facades");
        add("gate.material.clay_brick", "Brick");
        add("gate.logic.none", "");

        add(BCFactoryItems.GEL.get(), "Gelled Water");
        add("buildcraft.help.tank.title.tank", "Tank");
        add("buildcraft.help.distiller.tank_in.title", "Input Tank");
        add("buildcraft.help.distiller.tank_in.desc", "Pipes can insert this from\nany side of the distiller.");
        add("buildcraft.help.distiller.tank_gas_out.title", "Gas Output Tank");
        add("buildcraft.help.distiller.tank_gas_out.desc", "Pipes can extract this from\nthe top of the distiller.");
        add("buildcraft.help.distiller.tank_liquid_out.title", "Liquid Output Tank");
        add("buildcraft.help.distiller.tank_liquid_out.desc",
            "Pipes can extract this from\nthe bottom of the distiller.");

        // Fluids: 1.12's fluid.<name> strings, and the heat levels (buildcraft.fluid.heat_<n>)
        String[][] fluids = { { "oil", "Oil" }, { "oil_residue", "Residue" }, { "oil_heavy", "Heavy Oil" },
            { "oil_dense", "Dense Oil" }, { "oil_distilled", "Distilled Oil" }, { "fuel_dense", "Dense Fuel" },
            { "fuel_mixed_heavy", "Mixed Heavy Fuels" }, { "fuel_light", "Light Fuel" },
            { "fuel_mixed_light", "Mixed Light Fuels" }, { "fuel_gaseous", "Gaseous Fuel" } };
        String[] heatNames = { "Cool", "Hot", "Searing" };
        for (String[] fluid : fluids) {
            add("fluid." + BuildCraftReborn.MODID + "." + fluid[0], fluid[1]);
        }
        for (BCFluid fluid : BCEnergyFluids.allFluids) {
            if (fluid.block != null) {
                String bare = java.util.Arrays.stream(fluids).filter(f -> f[0].equals(fluid.baseName)).findFirst()
                    .orElseThrow()[1];
                add(fluid.block.get(), bare + " (" + heatNames[fluid.heat] + ")");
            }
        }
        add("buildcraft.fluid.heat_0", "%s (\u00A7bCool\u00A7r)");
        add("buildcraft.fluid.heat_1", "%s (\u00A76Hot\u00A7r)");
        add("buildcraft.fluid.heat_2", "%s (\u00A7cSearing\u00A7r)");
        add("item.buildcraftreborn.bucket_of", "Bucket of %s");

        // GUIs and ledgers
        add("gui.power", "Power");
        add("gui.currentOutput", "Current Output");
        add("gui.stored", "Stored");
        add("gui.heat", "Heat");
        add("gui.ledger.ownership", "Owner");
        add("gui.ledger.help", "Help");
        add("chat.pipe.power.iron.mode", "Switched to %d MJ/t limit");

        add("buildcraft.help.tank.generic", "Clicking this with an empty bucket will\nempty this tank into the bucket, or\n"
            + "using a full bucket will fill this tank\nfrom the bucket.");
        add("buildcraft.help.tank.title.tankFuel", "Fuel Tank");
        add("buildcraft.help.tank.title.tankCoolant", "Coolant Tank");
        add("buildcraft.help.tank.title.tankResidue", "Residue Tank");
        add("buildcraft.help.tank.fuel", "This tank can accept any kind of fuel.");
        add("buildcraft.help.tank.coolant", "This tank can accept any kind of fluid\ncoolant, or ice blocks.");
        add("buildcraft.help.tank.residue", "This tank will output residue if the\nfuel hasn't been processed\n"
            + "(for example oil).");
        add("buildcraft.help.stone_engine.flame.title", "Fuel left");
        add("buildcraft.help.stone_engine.fuel.title", "Fuel input");
        // 1.12's text here was a placeholder ("THIS BURNS AND THIS HELP ISN'T DONE YET.")
        add("buildcraft.help.stone_engine.flame", "How much of the current fuel item\nis left to burn.");
        add("buildcraft.help.stone_engine.fuel", "Accepts any fuel that a furnace does.");

        // Advancements (1.12's text)
        advancement("root", "BuildCraft", "For all your engineering needs");
        advancement("gears", "Gearing up", "Get one of each gear");
        advancement("wrenched", "Wrenched!", "Use the wrench");
        advancement("free_power", "Free Power!", "Generate free power");
        advancement("engine", "My first engine", "Get your first engine");
        advancement("sticky_dipping", "Sticky dipping", "Discover the sticky nature of oil");
        advancement("markers", "Marking your territory", "Use volume markers to mark out an area");
        advancement("path_markers", "Defining the trail", "Define a path");
        advancement("fluid_storage", "Fluid storage", "Store fluid in a tank");
        advancement("lazy_crafting", "Lazy crafting", "Make the autoworkbench work for you");
        advancement("retired_hopper", "Retired hopper", "Put the chute back to work");
        advancement("flooding_the_world", "Flooding the world", "Flood the area with a floodgate");
        advancement("draining_the_world", "Draining the world", "Use a pump to drain fluids from the world");
        advancement("oil_platform", "Oil platform", "Start Pumping up an oil spout");
        advancement("heating_and_distilling", "Heating and Distilling",
            "Create a complete oil refining setup with a Distiller and a Heat Exchanger");
        advancement("black_gold", "Black gold", "Completely pump out a large oil spout");
        advancement("pipe_dream", "Pipe dream", "Place a pipe");
        advancement("plugging_the_gap", "Plugging the gap", "Plug a pipe");
        advancement("logic_transportation", "Logic transportation", "Connect pipe wires");
        advancement("pipe_logic", "Pipe logic", "Place a gate on a pipe");
        advancement("extended_logic", "Extended logic", "Use a gate with extra slots");
        advancement("laser_power", "Laser power", "Make a laser");
        advancement("precision_crafting", "Precision crafting", "Start crafting with an assembly table");
        // 1.12's text said to smelt a cactus: in 1.12 that made green dye
        advancement("sealing_fluids", "Sealing fluids", "Craft pipe sealant from green dye or a slime ball");

        // Builders (1.12's names; 1.12's "Not usable in survival" tips are gone, as they all have recipes now)
        add(BCBuildersBlocks.ARCHITECT.get(), "Architect Table");
        add(BCBuildersBlocks.BUILDER.get(), "Builder");
        add(BCBuildersBlocks.FILLER.get(), "Filler");
        add(BCBuildersBlocks.LIBRARY.get(), "Electronic Library");
        add(BCBuildersBlocks.REPLACER.get(), "Replacer");
        add(BCBuildersBlocks.FRAME.get(), "Frame");
        add(BCBuildersBlocks.QUARRY.get(), "Quarry");
        add(BCBuildersBlocks.CONSTRUCTION_MARKER.get(), "Construction Mark");

        // Robotics (7.1's names and texts; 1.12's zone planner tip said robots weren't coming)
        add(BCRoboticsBlocks.ZONE_PLANNER.get(), "Zone Planner");
        add(BCRoboticsBlocks.REQUESTER.get(), "Requester");
        add(BCRoboticsBlocks.CHARGING_TABLE.get(), "Charging Table");
        add(BCRoboticsBlocks.PROGRAMMING_TABLE.get(), "Programming Table");
        add(BCRoboticsItems.ROBOT.get(), "Robot");
        add(BCRoboticsItems.REDSTONE_BOARD.get(), "Redstone Board");
        add(BCRoboticsItems.ROBOT_STATION.get(), "Docking Station");
        add(BCRoboticsItems.REDSTONE_CRYSTAL.get(), "Redstone Crystal");
        // (the unfinished robotics' name)
        add(BCRoboticsItems.ROBOT_GOGGLES.get(), "Robot Goggles");
        add("item.buildcraftreborn.robot_goggles.tip", "Wear to see robots through walls");
        add("item.buildcraftreborn.robot_goggles.tip.shift", "Red when a robot's battery is empty,\n"
            + "yellow when half full, green when full.\nAlso shows laser beams the config hides.");
        add(BCRoboticsEntities.ROBOT.get(), "Robot");
        add(BCRobotics.TAB_ROBOTS_KEY, "BuildCraft Robots");
        add("buildcraftreborn.board.clean", "Clean");
        String[][] boards = { { "picker", "Picker", "Picks up items" },
            { "lumberjack", "Lumberjack", "Breaks logs" }, { "planter", "Planter", "Plants crops" },
            { "leave_cutter", "Leaf Cutter", "Cuts leaves" },
            { "carrier", "Carrier", "Moves items between inventories" },
            { "fluid_carrier", "Tank", "Moves fluids between tanks" }, { "bomber", "Bomber", "Bombs locations" },
            { "knight", "Knight", "Fights mobs" }, { "miner", "Miner", "Mines ores" },
            { "farmer", "Farmer", "Hoes dirt" }, { "harvester", "Harvester", "Harvests mature crops" },
            { "shovelman", "Shovelman", "Shovels area" }, { "butcher", "Butcher", "Fights animals" },
            { "builder", "Builder", "Builds structures" },
            { "delivery", "Delivery", "Delivers items upon request" }, { "pump", "Pump", "Pumps out liquids" },
            { "stripes", "Stripes", "Uhh... Stripes!" } };
        for (String[] board : boards) {
            add("buildcraftreborn.board." + board[0], board[1]);
            add("buildcraftreborn.board." + board[0] + ".desc", board[2]);
        }
        add("gate.action.robot.goto_station", "Goto Station");
        add("gate.action.robot.work_in_area", "Work in Area");
        add("gate.action.robot.load_unload_area", "Load/Unload in Area");
        add("gate.action.robot.wakeup", "Wake Up");
        add("gate.action.robot.filter", "Filter");
        add("gate.action.robot.filter_tool", "Filter Tool");
        add("gate.action.station.provide_items", "Provide Items");
        add("gate.action.station.accept_items", "Accept Items");
        add("gate.action.station.request_items", "Request Items");
        add("gate.action.station.provide_machine_request", "Request Needed Items");
        add("gate.action.station.accept_fluids", "Accept Fluids");
        // (7.1's key was misspelt "povide_fluids")
        add("gate.action.station.provide_fluids", "Provide Fluids");
        add("gate.action.station.forbid_robot", "Forbid Robot");
        add("gate.action.station.force_robot", "Force Robot");
        add("gate.trigger.robot.sleep", "Sleep");
        add("gate.trigger.robot.in.station", "Robot In Station");
        add("gate.trigger.robot.linked", "Station Linked");
        add("gate.trigger.robot.reserved", "Station Reserved");
        config("boards", "Robot boards");
        config("blacklist", "Excluded robot boards");
        add(BCBuildersItems.BLUEPRINT.get(), "Blueprint");
        add(BCBuildersItems.TEMPLATE.get(), "Template");
        add(BCBuildersItems.SCHEMATIC_SINGLE.get(), "Single Schematic");
        add(BCBuildersItems.FILLER_PLANNER.get(), "Filler Planner");
        add(BCCoreItems.VOLUME_BOX.get(), "Volume Box");
        add("item.blueprint.author", "by");
        add("item.blueprint.blank", "Blank");
        String[][] patterns = { { "none", "None" }, { "clear", "Clear" }, { "fill", "Fill" }, { "box", "Box" },
            { "frame", "Frame" }, { "pyramid", "Pyramid" }, { "stairs", "Stairs" }, { "sphere", "Sphere" },
            { "sphere_half", "Hemisphere" }, { "sphere_quarter", "Quarter-sphere" },
            { "sphere_eighth", "Eighth-Sphere" }, { "2d_arc", "Arc" }, { "2d_semi_circle", "Semi-Circle" },
            { "2d_circle", "Circle" }, { "2d_triangle", "Triangle" }, { "2d_square", "Square" },
            { "2d_pentagon", "Pentagon" }, { "2d_hexagon", "Hexagon" }, { "2d_octagon", "Octagon" } };
        for (String[] pattern : patterns) {
            add("fillerpattern." + pattern[0], pattern[1]);
        }
        add("fillerpattern.parameter.hollow", "Hollow");
        add("fillerpattern.parameter.filled", "Filled");
        add("fillerpattern.parameter.filled_outer", "Surrounded");
        for (int r = 0; r < 4; r++) {
            add("buildcraft.param.rotation." + r, r == 0 ? "No rotation" : (r * 90) + " degree rotation");
        }
        add("buildcraft.param.axis.x", "X Axis");
        add("buildcraft.param.axis.y", "Y Axis");
        add("buildcraft.param.axis.z", "Z Axis");
        for (net.minecraft.core.Direction face : net.minecraft.core.Direction.values()) {
            String name = face.getName();
            add("buildcraft.param.facing." + name, Character.toUpperCase(name.charAt(0)) + name.substring(1));
        }
        add("gui.del", "Del");
        add("gui.filling.resources", "Filling Resources");
        add("gui.progress", "Progress");
        add("gui.leftToBreak", "To break");
        add("gui.leftToPlace", "To place");
        add("gui.architect.name", "Name");
        add("gui.filler.locked", "Locked");
        add("gui.filler.control_mode", "Control mode");
        add("tip.filler.excavate.on", "Excavate");
        add("tip.filler.excavate.off", "Do Not Excavate");
        add("tip.filler.invert.on", "Invert");
        add("tip.filler.invert.off", "Do Not Invert");
        // 1.12 named these help keys but never wrote their text
        add("buildcraft.help.filler.excavate.title", "Excavate");
        add("buildcraft.help.filler.excavate.desc", "If on, the filler will also break\nblocks that are in the way.");
        add("buildcraft.help.filler.invert.title", "Invert");
        add("buildcraft.help.filler.invert.desc", "Builds everywhere the pattern\nwouldn't, and nowhere it would.");
        add("buildcraft.help.filler.locked", "Shows a lock if the pattern\nis set by a nearby gate.");
        add("buildcraft.help.filler.control_mode", "Either ON, OFF, or LOOP.\nSet by gates.");
        advancement("shaping_the_world", "Shaping the world", "Craft and place a Quarry");
        advancement("diggy_diggy_hole", "Diggy diggy hole", "Have a 64x64 quarry finish");
        advancement("architect", "Architect",
            "Scan an area with an architect table and record it in a blueprint/template");
        advancement("paving_the_way", "Paving the way", "Utilize the builder with a pathmarker to create a path");

        if (BCTestContent.ENABLED) {
            add(BCTestContent.TEST_MACHINE_BLOCK.get(), "Test Machine (development only)");
        }

        // Unit strings keep 1.12's keys (buildcraft.fluid.*, buildcraft.mj.*) so its 30 translations can be reused.
        add("buildcraft.fluid.empty", "Empty");
        add("buildcraft.fluid.static.milli.short", "%s mB");
        add("buildcraft.fluid.static.bucket.short", "%s B");
        add("buildcraft.fluid.static.milli.long", "%s milli buckets");
        add("buildcraft.fluid.static.bucket.long", "%s buckets");
        add("buildcraft.fluid.empty.milli.short", "0 / %s mB");
        add("buildcraft.fluid.empty.bucket.short", "0 / %s B");
        add("buildcraft.fluid.empty.milli.long", "Empty %s milli bucket tank");
        add("buildcraft.fluid.empty.bucket.long", "Empty %s bucket tank");
        add("buildcraft.fluid.full.milli.short", "%1$s / %1$s mB");
        add("buildcraft.fluid.full.bucket.short", "%1$s / %1$s B");
        add("buildcraft.fluid.full.milli.long", "Full %s milli bucket tank");
        add("buildcraft.fluid.full.bucket.long", "Full %s bucket tank");
        add("buildcraft.fluid.static.cap.milli.short", "%s / %s mB");
        add("buildcraft.fluid.static.cap.bucket.short", "%s / %s B");
        add("buildcraft.fluid.static.cap.milli.long", "%s milli buckets in a %s milli bucket tank");
        add("buildcraft.fluid.static.cap.bucket.long", "%s buckets in a %s bucket tank");
        add("buildcraft.fluid.flow.milli.short", "%s mB/t");
        add("buildcraft.fluid.flow.bucket.short", "%s B/s");
        add("buildcraft.fluid.flow.milli.long", "%s milli buckets per tick");
        add("buildcraft.fluid.flow.bucket.long", "%s buckets per second");
        add("buildcraft.mj.static.long", "%s Minecraft Joules");
        add("buildcraft.mj.static.short", "%s MJ");
        add("buildcraft.mj.flow.long", "%s Minecraft Joules per tick");
        add("buildcraft.mj.flow.short", "%s MJ/t");
        add("buildcraft.rf.static.long", "%s Redstone Flux");
        add("buildcraft.rf.static.short", "%s RF");
        add("buildcraft.rf.flow.long", "%s Redstone Flux per tick");
        add("buildcraft.rf.flow.short", "%s RF/t");

        config("itemLifespan", "Dropped item lifespan (seconds)");
        config("chunkLoadLevel", "Chunk loading level");
        config("useColouredLabels", "Coloured item labels");
        config("useHighContrastLabelColours", "High-contrast label colours");
        config("colourBlindMode", "Colour-blind mode");
        config("useBucketsStatic", "Show fluid amounts in buckets");
        config("useBucketsFlow", "Show fluid flow in buckets per second");
        config("useLongLocalizedName", "Long unit names");
        config("itemRenderRotation", "Travelling item rotation");
        config("renderLaserBeams", "Show laser beams");
        config("general", "General");
        config("oilIsDense", "Oil is sticky");
        config("worldgen", "World generation");
        config("oil", "Oil");
        config("enable", "Enabled");
        config("can_burn", "Oil can burn");
        config("generationRate", "Oil well rate");
        config("spawn_probability", "Oil well chances (%)");
        config("small", "Small");
        config("medium", "Medium");
        config("large", "Large");
        config("spouts", "Oil spouts");
        config("small_min_height", "Small spout minimum height");
        config("small_max_height", "Small spout maximum height");
        config("large_min_height", "Large spout minimum height");
        config("large_max_height", "Large spout maximum height");
        config("markerMaxDistance", "Marker range");
        config("miningBreaksPlayerProtectedBlocks", "Machines break protected blocks");
        config("pumpsConsumeWater", "Pumps consume water");
        config("pumpMaxDistance", "Pump range");
        config("updateFactor", "Network update interval (ticks)");
        config("miningMultiplier", "Mining power multiplier");
        config("miningMaxDepth", "Maximum mining depth");
        config("excludedBiomesIsBlacklist", "Excluded biomes tag is a blacklist");
        config("excludedDimensions", "Dimensions without oil");
        config("excludedDimensionsIsBlacklist", "Excluded dimensions is a blacklist");
    }

    /** The guide book's own text (1.12's {@code en_US.lang} and the guide resource pack's). */
    /** Text for the optional integrations (JEI, Jade, The One Probe). */
    private void compat() {
        add("jei.buildcraftreborn.assembly", "Assembly Table");
        add("jei.buildcraftreborn.distillation", "Distillable Fluids");
        add("jei.buildcraftreborn.heatable", "Heatable Fluids");
        add("jei.buildcraftreborn.coolable", "Coolable Fluids");
        add("jei.buildcraftreborn.combustion_fuel", "Combustion Engine Fuels");
        add("jei.buildcraftreborn.combustion_coolant", "Combustion Engine Coolants");
        add("jei.buildcraftreborn.heat_levels", "Heat level %s \u2192 %s");
        add("jei.buildcraftreborn.used_up", "Used up");
        add("jei.buildcraftreborn.burn_time", "Burns for %ss");
        add("jei.buildcraftreborn.burn_power", "at %s");
        add("jei.buildcraftreborn.burn_total", "total %s");
        add("jei.buildcraftreborn.cooling", "Cools by %s \u00b0C/mB");

        // Names for the common tags BuildCraft adds or burns, which JEI and EMI show for tags
        add(Tags.getTagTranslationKey(BCCoreTags.GEARS), "Gears");
        add(Tags.getTagTranslationKey(BCCoreTags.GEARS_WOOD), "Wood Gears");
        add(Tags.getTagTranslationKey(BCCoreTags.GEARS_STONE), "Stone Gears");
        add(Tags.getTagTranslationKey(BCCoreTags.GEARS_IRON), "Iron Gears");
        add(Tags.getTagTranslationKey(BCCoreTags.GEARS_GOLD), "Gold Gears");
        add(Tags.getTagTranslationKey(BCCoreTags.GEARS_DIAMOND), "Diamond Gears");
        add(Tags.getTagTranslationKey(BCEnergyTags.OIL), "Oil");
        add(Tags.getTagTranslationKey(BCEnergyTags.CRUDE_OIL), "Crude Oil");
        add(Tags.getTagTranslationKey(BCEnergyTags.FUEL), "Fuel");
        add(Tags.getTagTranslationKey(BCEnergyTags.LIGHT_FUEL), "Light Fuel");
        add(Tags.getTagTranslationKey(BCEnergyTags.DIESEL), "Diesel");
        add(Tags.getTagTranslationKey(BCEnergyTags.GASOLINE), "Gasoline");
        add(Tags.getTagTranslationKey(BCEnergyTags.KEROSENE), "Kerosene");
        add(Tags.getTagTranslationKey(BCEnergyTags.LPG), "LPG");
        add(Tags.getTagTranslationKey(BCEnergyTags.BIODIESEL), "Biodiesel");
        add(Tags.getTagTranslationKey(BCEnergyTags.ETHANOL), "Ethanol");
        add(Tags.getTagTranslationKey(BCEnergyTags.BIOETHANOL), "Bioethanol");
        add(Tags.getTagTranslationKey(BCEnergyTags.HIGH_POWER_BIODIESEL), "High Power Biodiesel");
        add(Tags.getTagTranslationKey(BCEnergyTags.BOOSTED_DIESEL), "Boosted Diesel");
        // Jade and The One Probe
        add("buildcraftreborn.probe.of", "%s / %s");
        add("buildcraftreborn.probe.making", "Making:");
        add("buildcraftreborn.probe.from", "From:");
        add("buildcraftreborn.probe.laser", "Waiting from laser: %s");
        add("config.jade.plugin_buildcraftreborn.info", "BuildCraft machine info");
    }

    private void guide() {
        add(BCCoreItems.GUIDE.get(), "BuildCraft Guide Book");
        add(BCCoreItems.GUIDE_NOTE.get(), "BuildCraft Guide Book Note");
        advancement("guide", "How does this work again?", "Check out the guide");
        advancement("list", "Sorting all the things", "Discover the advanced capabilities of the list");
        String[][] guide = {
            { "book.meta.name", "BuildCraft Configuration Guide" },
            { "too_many_results", "Too many results to display: %s" },
            { "chapter_list", "Chapters" },
            { "order.type_subtype", "Sort By Type" },
            { "order.mod_type", "Sort By Mod" },
            { "order.alphabetical", "Sort Alphabetically" },
            { "recipe.create", "Recipe" },
            { "recipe.create.plural", "Recipes" },
            { "recipe.use", "Use" },
            { "recipe.use.plural", "Uses" },
            { "chapter.contents", "Contents" },
            { "contents.all_group", "Others" },
            { "contents.item_stacks", "Items and Blocks" },
            { "contents.triggers", "Triggers" },
            { "contents.actions", "Actions" },
            { "chapter.type.unknown", "Unknown Type!" },
            { "chapter.subtype.unknown", "Unknown Subtype!" },
            { "chapter.mod.unknown", "Unknown Mod!" },
            { "meta.group.linking_to", "Linked To" },
            { "meta.group.linked_from", "Linked From" },
            { "group.to.buildcraft.pipe_power_providers", "Pipe Power Providers" },
            { "group.from.buildcraft.pipe_power_providers", "Pipe Power Receivers" },
            { "group.to.buildcraft.full_power_providers", "Full Power Providers" },
            { "group.from.buildcraft.full_power_providers", "Full Power Receivers" },
            { "group.to.buildcraft.laser_power_providers", "Laser Power Providers" },
            { "group.from.buildcraft.laser_power_providers", "Laser Power Receivers" },
            { "group.to.buildcraft.area_markers", "Area Markers" },
            { "group.from.buildcraft.area_markers", "Area dependent blocks" },
            { "chapter.type.block", "Blocks" },
            { "chapter.type.item", "Items" },
            { "chapter.type.pipe", "Pipes" },
            { "chapter.type.trigger", "Triggers" },
            { "chapter.type.action", "Actions" },
            { "chapter.type.config", "Configuration" },
            { "chapter.subtype.gear", "Gears" },
            { "chapter.subtype.tool", "Tools" },
            { "chapter.subtype.fluid", "Fluid" },
            { "chapter.subtype.item", "Item" },
            { "chapter.subtype.mining", "Mining" },
            { "chapter.subtype.engine", "Engines" },
            { "chapter.subtype.automation", "Automation" },
            { "chapter.subtype.pipe_item", "Item Transport" },
            { "chapter.subtype.pipe_fluid", "Fluid Transport" },
            { "chapter.subtype.pipe_plug", "Pluggables" },
            { "chapter.subtype.refining", "Refining" },
            { "chapter.subtype.laser", "Laser Power" },
            { "chapter.subtype.robotics", "Robotics" },
            { "chapter.subtype.basic", "Basic" },
            { "chapter.subtype.resourcepack", "Resource Pack" },
            { "chapter.mod.buildcraft", "BuildCraft" },
            { "page.guide_page_format", "Guide Page File Format" },
            { "page.guide_entry_format", "Guide Entry File Format" },
            // (1.12 wrote these in English in every language)
            { "everything", "Everything" }, { "show_lore", "Show Lore %s" }, { "show_hints", "Show Hints %s" },
            { "loaded_mods", "Loaded Mods:" }, { "loaded_resource_packs", "Loaded Resource Packs:" } };
        for (String[] entry : guide) {
            add("buildcraft.guide." + entry[0], entry[1]);
        }
        add("buildcraft.guide_book.reloaded", "Guide Book Reloaded");
    }

    private void advancement(String name, String title, String description) {
        add("advancements." + BuildCraftReborn.MODID + "." + name + ".title", title);
        add("advancements." + BuildCraftReborn.MODID + "." + name + ".description", description);
    }

    /** Labels for the NeoForge config screen, which looks up {@code <modid>.configuration.<option>}. */
    private void config(String option, String label) {
        add(BuildCraftReborn.MODID + ".configuration." + option, label);
    }
}
