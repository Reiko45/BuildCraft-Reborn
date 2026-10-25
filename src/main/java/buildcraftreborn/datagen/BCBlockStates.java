/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import java.util.List;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.enums.EnumOptionalSnapshotType;
import buildcraftreborn.api.properties.BuildCraftProperties;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.block.BlockArchitectTable;
import buildcraftreborn.builders.block.BlockBuilder;
import buildcraftreborn.builders.block.BlockFrame;
import buildcraftreborn.builders.block.BlockQuarry;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.factory.block.BlockChute;
import buildcraftreborn.factory.block.BlockFloodGate;
import buildcraftreborn.factory.block.BlockHeatExchange;
import buildcraftreborn.factory.block.BlockHeatExchange.EnumExchangePart;
import buildcraftreborn.factory.block.BlockWaterGel;
import buildcraftreborn.factory.block.BlockWaterGel.GelStage;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.fluid.BCFluid;
import buildcraftreborn.test.BCTestContent;

/** Block states and block models (plus the matching block item models). Replaces 1.12's hand-written
 * {@code forge_marker} blockstates. */
public final class BCBlockStates extends BlockStateProvider {
    public BCBlockStates(PackOutput output, ExistingFileHelper files) {
        super(output, BuildCraftReborn.MODID, files);
    }

    @Override
    protected void registerStatesAndModels() {
        silicon();
        engine(BCCoreBlocks.ENGINE_WOOD.get(), "wood", "blue");
        engine(BCCoreBlocks.ENGINE_CREATIVE.get(), "creative", "overheat");
        engine(BCEnergyBlocks.ENGINE_STONE.get(), "stone", "blue");
        engine(BCEnergyBlocks.ENGINE_IRON.get(), "iron", "blue");
        engine(BCEnergyBlocks.ENGINE_RF.get(), "rf", "blue");
        // The dynamo is drawn by RenderDynamoMJ; its item uses block/mj_dynamo_static (the dynamo at rest).
        simpleBlock(BCEnergyBlocks.MJ_DYNAMO.get(), models().getBuilder("mj_dynamo")
            .texture("particle", modLoc("block/mj_dynamo/back")));
        itemModels().withExistingParent("mj_dynamo", modLoc("block/mj_dynamo_static"));

        marker(BCCoreBlocks.MARKER_VOLUME.get(), "marker_volume");
        marker(BCCoreBlocks.MARKER_PATH.get(), "marker_path");
        marker(BCBuildersBlocks.CONSTRUCTION_MARKER.get(), "construction_marker");
        BCCoreBlocks.DECORATED.forEach((type, block) -> {
            ModelFile model = models().cubeAll("decorated_" + type.getSerializedName(),
                modLoc("block/decorated/" + type.getSerializedName()));
            simpleBlock(block.get(), model);
            simpleBlockItem(block.get(), model);
        });

        // Springs look like bedrock, as in 1.12
        ModelFile bedrock = models().getExistingFile(mcLoc("block/bedrock"));
        for (Block spring : List.of(BCCoreBlocks.WATER_SPRING.get(), BCEnergyBlocks.OIL_SPRING.get())) {
            simpleBlock(spring, bedrock);
            simpleBlockItem(spring, bedrock);
        }

        for (BCFluid fluid : BCEnergyFluids.allFluids) {
            if (fluid.block != null) {
                // Fluids are drawn by the fluid renderer; the model only gives the breaking particles
                ModelFile model = models().getBuilder("block/fluid/" + fluid.name)
                    .texture("particle", modLoc("block/fluids/" + fluid.name + "_still"));
                simpleBlock(fluid.block.get(), model);
            }
        }

        factory();
        transport();
        builders();
        robotics();

        if (BCTestContent.ENABLED) {
            ModelFile model = models().cubeAll("test_machine", modLoc("block/power_tester"));
            horizontalBlock(BCTestContent.TEST_MACHINE_BLOCK.get(), model);
            simpleBlockItem(BCTestContent.TEST_MACHINE_BLOCK.get(), model);
        }
    }

    /** A marker on the face it was placed on (1.12's facing variants). The item is a flat sprite (see
     * {@link BCItemModels}). */
    private void marker(Block block, String name) {
        ModelFile model = models().withExistingParent(name, modLoc("block/marker_torch"))
            .texture("all", modLoc("block/marker/" + name));
        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(BlockBCBase_Neptune.BLOCK_FACING_6);
            int x = switch (facing) {
                case UP -> 0;
                case DOWN -> 180;
                default -> 90;
            };
            int y = switch (facing) {
                case EAST -> 90;
                case SOUTH -> 180;
                case WEST -> 270;
                default -> 0;
            };
            return ConfiguredModel.builder().modelFile(model).rotationX(x).rotationY(y).build();
        });
    }

    /** Engines are drawn by {@code RenderEngine_BC8}; the block model only gives breaking particles. The item uses
     * {@code block/engine_static} (the engine at rest) with this engine's textures. */
    private void engine(Block block, String type, String trunk) {
        String name = "engine_" + type;
        ModelFile model = models().getBuilder(name).texture("particle", modLoc("block/engine/" + type + "/back"));
        simpleBlock(block, model);
        itemModels().withExistingParent(name, modLoc("block/engine_static"))
            .texture("back", modLoc("block/engine/" + type + "/back"))
            .texture("side", modLoc("block/engine/" + type + "/side"))
            .texture("trunk", modLoc("block/engine/trunk_" + trunk));
    }

    /** Silicon: the laser (pointing along its facing) and the laser tables, from 1.12's model files (in
     * {@code src/main/resources}). */
    private void silicon() {
        ModelFile laser = models().getExistingFile(modLoc("block/laser"));
        getVariantBuilder(BCSiliconBlocks.LASER.get()).forAllStates(state -> {
            int[] rot = switch (state.getValue(BlockBCBase_Neptune.BLOCK_FACING_6)) {
                case UP -> new int[] { 0, 0 };
                case DOWN -> new int[] { 180, 0 };
                case NORTH -> new int[] { 90, 0 };
                case EAST -> new int[] { 90, 90 };
                case SOUTH -> new int[] { 90, 180 };
                case WEST -> new int[] { 90, 270 };
            };
            return ConfiguredModel.builder().modelFile(laser).rotationX(rot[0]).rotationY(rot[1]).build();
        });
        simpleBlockItem(BCSiliconBlocks.LASER.get(), laser);
        table(BCSiliconBlocks.ASSEMBLY_TABLE.get(), "assembly");
        table(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE.get(), "advanced_crafting");
        table(BCSiliconBlocks.INTEGRATION_TABLE.get(), "integration");
        // 7.1's packager, and its stamping table (stamping.json is 7.1's model of it, with 7.1's texture sheet)
        String p = "block/packager/";
        ModelFile packager = models().cubeBottomTop("packager", modLoc(p + "side"), modLoc(p + "bottom"),
            modLoc(p + "top"));
        simpleBlock(BCSiliconPackaging.PACKAGER.get(), packager);
        simpleBlockItem(BCSiliconPackaging.PACKAGER.get(), packager);
        table(BCSiliconPackaging.STAMPING_TABLE.get(), "stamping");
    }

    private void table(Block block, String name) {
        ModelFile model = models().getExistingFile(modLoc("block/table/" + name));
        simpleBlock(block, model);
        simpleBlockItem(block, model);
    }

    /** Transport: the filtered buffer, and the pipe holder (drawn by {@code ModelPipe}; its model file only gives a
     * particle texture so nothing is reported missing). */
    private void transport() {
        ModelFile filteredBuffer = models().cubeAll("filtered_buffer", modLoc("block/filtered_buffer"));
        simpleBlock(BCTransportBlocks.FILTERED_BUFFER.get(), filteredBuffer);
        simpleBlockItem(BCTransportBlocks.FILTERED_BUFFER.get(), filteredBuffer);
        simpleBlock(BCTransportBlocks.PIPE_HOLDER.get(),
            models().getBuilder("pipe_holder").texture("particle", modLoc("block/pipes/structure")));
    }

    /** The factory machines. Block models that 1.12 shipped as plain JSON are kept as files (in
     * {@code src/main/resources}); the rest are made here. */
    private void factory() {
        String fac = "block/";
        // Item models with 1.12's display settings (tank, chute, distiller) are files in src/main/resources
        // Tank: the joined_below texture continues the glass into the tank below
        ModelFile tank = models().getExistingFile(modLoc("block/tank"));
        ModelFile tankJoined = models().getExistingFile(modLoc("block/tank_joined_below"));
        getVariantBuilder(BCFactoryBlocks.TANK.get()).forAllStates(state -> ConfiguredModel.builder()
            .modelFile(state.getValue(BuildCraftProperties.JOINED_BELOW) ? tankJoined : tank).build());

        ModelFile autoWorkbench = models().cubeBottomTop("autoworkbench_item",
            modLoc(fac + "auto_workbench_item/side"), modLoc(fac + "auto_workbench_item/top"),
            modLoc(fac + "auto_workbench_item/top"));
        simpleBlock(BCFactoryBlocks.AUTO_WORKBENCH_ITEMS.get(), autoWorkbench);
        simpleBlockItem(BCFactoryBlocks.AUTO_WORKBENCH_ITEMS.get(), autoWorkbench);

        ModelFile miningWell = models().cube("mining_well", modLoc(fac + "mining_well/bottom"),
            modLoc(fac + "mining_well/top"), modLoc(fac + "mining_well/front"), modLoc(fac + "mining_well/back"),
            modLoc(fac + "mining_well/side"), modLoc(fac + "mining_well/side"))
            .texture("particle", modLoc(fac + "mining_well/side"));
        horizontalBlock(BCFactoryBlocks.MINING_WELL.get(), miningWell);
        simpleBlockItem(BCFactoryBlocks.MINING_WELL.get(), miningWell);

        ModelFile pump = models().cubeBottomTop("pump", modLoc(fac + "pump/side"), modLoc(fac + "pump/bottom"),
            modLoc(fac + "pump/top"));
        simpleBlock(BCFactoryBlocks.PUMP.get(), pump);
        simpleBlockItem(BCFactoryBlocks.PUMP.get(), pump);

        // The tube is drawn by the mining well and pump renderers
        simpleBlock(BCFactoryBlocks.TUBE.get(),
            models().getBuilder("tube").texture("particle", modLoc(fac + "mining_well/tube")));

        floodGate();
        chute();

        // Distiller: the model faces west; the moving power blocks are drawn by RenderDistiller
        ModelFile distiller = models().getExistingFile(modLoc("block/distiller"));
        getVariantBuilder(BCFactoryBlocks.DISTILLER.get()).forAllStates(state -> ConfiguredModel.builder()
            .modelFile(distiller).rotationY(westModelRotation(state.getValue(BlockBCBase_Neptune.PROP_FACING)))
            .build());

        heatExchange();

        getVariantBuilder(BCFactoryBlocks.WATER_GEL.get()).forAllStates(state -> {
            GelStage stage = state.getValue(BlockWaterGel.PROP_STAGE);
            String name = stage.getSerializedName();
            return ConfiguredModel.builder()
                .modelFile(models().cubeAll("water_gel_" + name, modLoc(fac + "gel/" + name))).build();
        });
    }

    /** y rotation for a model that faces west (1.12's distiller and heat exchanger models). */
    private static int westModelRotation(Direction facing) {
        return switch (facing) {
            case NORTH -> 90;
            case EAST -> 180;
            case SOUTH -> 270;
            default -> 0;
        };
    }

    /** 1.12 swapped each side's texture between "open" and "closed"; here each side is its own model part. */
    private void floodGate() {
        ResourceLocation top = modLoc("block/flood_gate/top");
        ModelFile base = models().getBuilder("block/flood_gate/top").texture("particle", top).texture("top", top)
            .element().from(0, 0, 0).to(16, 16, 16).face(Direction.UP).texture("#top").cullface(Direction.UP).end()
            .end();
        MultiPartBlockStateBuilder builder = getMultipartBuilder(BCFactoryBlocks.FLOOD_GATE.get());
        builder.part().modelFile(base).addModel().end();
        for (Direction side : BlockFloodGate.CONNECTED_MAP.keySet()) {
            for (boolean open : new boolean[] { true, false }) {
                String state = open ? "open" : "closed";
                ResourceLocation tex = modLoc("block/flood_gate/" + state);
                ModelFile face = models().getBuilder("block/flood_gate/" + side.getSerializedName() + "_" + state)
                    .texture("particle", tex).texture("face", tex)
                    .element().from(0, 0, 0).to(16, 16, 16).face(side).texture("#face").cullface(side).end().end();
                builder.part().modelFile(face).addModel().condition(BlockFloodGate.CONNECTED_MAP.get(side), open)
                    .end();
            }
        }
        // The item shows every side open
        ResourceLocation open = modLoc("block/flood_gate/open");
        itemModels().withExistingParent("flood_gate", mcLoc("block/cube")).texture("particle", open)
            .texture("up", top).texture("down", open).texture("north", open).texture("south", open)
            .texture("east", open).texture("west", open);
    }

    /** The chute points its hopper end along its facing, with a small spout drawn on every side that has an
     * inventory next to it. The item model (with 1.12's display settings) is a file. */
    private void chute() {
        ModelFile main = models().getExistingFile(modLoc("block/chute"));
        ModelFile connected = models().getExistingFile(modLoc("block/chute_connected"));
        MultiPartBlockStateBuilder builder = getMultipartBuilder(BCFactoryBlocks.CHUTE.get());
        for (Direction facing : Direction.values()) {
            int[] rot = switch (facing) {
                case UP -> new int[] { 0, 0 };
                case DOWN -> new int[] { 180, 0 };
                case EAST -> new int[] { 90, 90 };
                case SOUTH -> new int[] { 90, 180 };
                case WEST -> new int[] { 90, 270 };
                case NORTH -> new int[] { 90, 0 };
            };
            builder.part().modelFile(main).rotationX(rot[0]).rotationY(rot[1]).addModel()
                .condition(BlockBCBase_Neptune.BLOCK_FACING_6, facing).end();
        }
        for (Direction side : Direction.values()) {
            int[] rot = switch (side) {
                case UP -> new int[] { 180, 0 };
                case DOWN -> new int[] { 0, 0 };
                case EAST -> new int[] { 90, 270 };
                case SOUTH -> new int[] { 90, 0 };
                case WEST -> new int[] { 90, 90 };
                case NORTH -> new int[] { 90, 180 };
            };
            builder.part().modelFile(connected).rotationX(rot[0]).rotationY(rot[1]).addModel()
                .condition(BlockChute.CONNECTED_MAP.get(side), true).end();
        }
    }

    /** 1.12 drew the heat exchanger from one variable model ({@code heat_exchange_static.json}); its parts are
     * converted to plain model files ({@code block/heat_exchange/*}), picked by the block state here. */
    private void heatExchange() {
        ModelFile inner = models().getExistingFile(modLoc("block/heat_exchange/inner"));
        ModelFile outer = models().getExistingFile(modLoc("block/heat_exchange/outer"));
        ModelFile capLeft = models().getExistingFile(modLoc("block/heat_exchange/cap_left"));
        ModelFile capRight = models().getExistingFile(modLoc("block/heat_exchange/cap_right"));
        ModelFile start = models().getExistingFile(modLoc("block/heat_exchange/start"));
        ModelFile end = models().getExistingFile(modLoc("block/heat_exchange/end"));
        MultiPartBlockStateBuilder builder = getMultipartBuilder(BCFactoryBlocks.HEAT_EXCHANGE.get());
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int y = westModelRotation(facing);
            builder.part().modelFile(inner).rotationY(y).addModel()
                .condition(BlockBCBase_Neptune.PROP_FACING, facing).end();
            builder.part().modelFile(outer).rotationY(y).addModel()
                .condition(BlockBCBase_Neptune.PROP_FACING, facing)
                .condition(BlockHeatExchange.PROP_PART, EnumExchangePart.MIDDLE).end();
            builder.part().modelFile(capLeft).rotationY(y).addModel()
                .condition(BlockBCBase_Neptune.PROP_FACING, facing)
                .condition(BlockHeatExchange.PROP_PART, EnumExchangePart.MIDDLE)
                .condition(BlockHeatExchange.PROP_CONNECTED_LEFT, false).end();
            builder.part().modelFile(capRight).rotationY(y).addModel()
                .condition(BlockBCBase_Neptune.PROP_FACING, facing)
                .condition(BlockHeatExchange.PROP_PART, EnumExchangePart.MIDDLE)
                .condition(BlockHeatExchange.PROP_CONNECTED_RIGHT, false).end();
            builder.part().modelFile(start).rotationY(y).addModel()
                .condition(BlockBCBase_Neptune.PROP_FACING, facing)
                .condition(BlockHeatExchange.PROP_PART, EnumExchangePart.START).end();
            builder.part().modelFile(end).rotationY(y).addModel()
                .condition(BlockBCBase_Neptune.PROP_FACING, facing)
                .condition(BlockHeatExchange.PROP_PART, EnumExchangePart.END).end();
        }
        itemModels().withExistingParent("heat_exchange", modLoc("block/heat_exchange/item"));
    }

    /** The builders blocks. The frame's parts and the builder's snapshot slots are 1.12's model files (in
     * {@code src/main/resources}). */
    /** Robotics: the zone planner (1.12's {@code zone_planner.json}), the requester and the 7.1 tables. */
    private void robotics() {
        String b = "block/zone_planner/";
        ModelFile planner = models().cube("zone_planner", modLoc(b + "default"), modLoc(b + "top"),
            modLoc(b + "front"), modLoc(b + "back"), modLoc(b + "right"), modLoc(b + "left"))
            .texture("particle", modLoc(b + "default"));
        horizontalBlock(BCRoboticsBlocks.ZONE_PLANNER.get(), planner);
        simpleBlockItem(BCRoboticsBlocks.ZONE_PLANNER.get(), planner);

        // 1.12's requester.json: the front faces the player that placed it
        String r = "block/requester/";
        ModelFile requester = models().cube("requester", modLoc(r + "bottom"), modLoc(r + "top"), modLoc(r + "front"),
            modLoc(r + "back"), modLoc(r + "side"), modLoc(r + "side")).texture("particle", modLoc(r + "side"));
        horizontalBlock(BCRoboticsBlocks.REQUESTER.get(), requester);
        simpleBlockItem(BCRoboticsBlocks.REQUESTER.get(), requester);

        // 8.0's table models (it had them, but not the tables)
        table(BCRoboticsBlocks.CHARGING_TABLE.get(), "charging");
        table(BCRoboticsBlocks.PROGRAMMING_TABLE.get(), "programming");
    }

    private void builders() {
        String b = "block/";
        ModelFile filler = models().cube("filler", modLoc(b + "filler/bottom"), modLoc(b + "filler/top"),
            modLoc(b + "filler/front"), modLoc(b + "filler/side"), modLoc(b + "filler/side"),
            modLoc(b + "filler/side")).texture("particle", modLoc(b + "filler/side"));
        horizontalBlock(BCBuildersBlocks.FILLER.get(), filler);
        simpleBlockItem(BCBuildersBlocks.FILLER.get(), filler);

        ModelFile library = models().cube("library", modLoc(b + "library/bottom"), modLoc(b + "library/top"),
            modLoc(b + "library/front"), modLoc(b + "library/back"), modLoc(b + "library/left"),
            modLoc(b + "library/right")).texture("particle", modLoc(b + "library/back"));
        horizontalBlock(BCBuildersBlocks.LIBRARY.get(), library);
        simpleBlockItem(BCBuildersBlocks.LIBRARY.get(), library);

        ModelFile replacer = models().cube("replacer", modLoc(b + "replacer/bottom"), modLoc(b + "replacer/top"),
            modLoc(b + "replacer/front"), modLoc(b + "replacer/side"), modLoc(b + "replacer/side"),
            modLoc(b + "replacer/side")).texture("particle", modLoc(b + "replacer/side"));
        horizontalBlock(BCBuildersBlocks.REPLACER.get(), replacer);
        simpleBlockItem(BCBuildersBlocks.REPLACER.get(), replacer);

        // Architect table: the front lights up when it has an area to scan
        ModelFile[] architect = new ModelFile[2];
        for (int on = 0; on < 2; on++) {
            architect[on] = models().cube(on == 1 ? "architect_on" : "architect_off", modLoc(b + "architect/bottom"),
                modLoc(b + "architect/top"), modLoc(b + "architect/front_" + (on == 1 ? "on" : "off")),
                modLoc(b + "architect/back"), modLoc(b + "architect/left"), modLoc(b + "architect/right"))
                .texture("particle", modLoc(b + "architect/back"));
        }
        horizontalBlock(BCBuildersBlocks.ARCHITECT.get(),
            state -> architect[state.getValue(BlockArchitectTable.PROP_VALID) ? 1 : 0], 180);
        simpleBlockItem(BCBuildersBlocks.ARCHITECT.get(), architect[0]);

        // Builder: the main block, plus the blueprint or template sticking out of its slot
        ModelFile builder = models().cube("builder", modLoc(b + "builder/bottom"), modLoc(b + "builder/top"),
            modLoc(b + "builder/front"), modLoc(b + "builder/back"), modLoc(b + "builder/side"),
            modLoc(b + "builder/side")).texture("particle", modLoc(b + "builder/side"));
        ModelFile slotBlueprint = models().getExistingFile(modLoc("block/builder/slot_blueprint"));
        ModelFile slotTemplate = models().getExistingFile(modLoc("block/builder/slot_template"));
        MultiPartBlockStateBuilder builderState = getMultipartBuilder(BCBuildersBlocks.BUILDER.get());
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            int y = ((int) facing.toYRot() + 180) % 360;
            builderState.part().modelFile(builder).rotationY(y).addModel()
                .condition(BlockBCBase_Neptune.PROP_FACING, facing).end();
            builderState.part().modelFile(slotBlueprint).rotationY(y).addModel()
                .condition(BlockBCBase_Neptune.PROP_FACING, facing)
                .condition(BlockBuilder.SNAPSHOT_TYPE, EnumOptionalSnapshotType.BLUEPRINT).end();
            builderState.part().modelFile(slotTemplate).rotationY(y).addModel()
                .condition(BlockBCBase_Neptune.PROP_FACING, facing)
                .condition(BlockBuilder.SNAPSHOT_TYPE, EnumOptionalSnapshotType.TEMPLATE).end();
        }
        simpleBlockItem(BCBuildersBlocks.BUILDER.get(), builder);

        quarry();

        // Frame: a centre piece, and an arm to every frame or quarry next to it
        ModelFile frameBase = models().getExistingFile(modLoc("block/frame/base"));
        ModelFile frameConnection = models().getExistingFile(modLoc("block/frame/connection"));
        MultiPartBlockStateBuilder frame = getMultipartBuilder(BCBuildersBlocks.FRAME.get());
        frame.part().modelFile(frameBase).addModel().end();
        for (Direction side : Direction.values()) {
            int[] rot = switch (side) {
                case UP -> new int[] { 270, 0 };
                case DOWN -> new int[] { 90, 0 };
                case EAST -> new int[] { 0, 90 };
                case WEST -> new int[] { 0, 270 };
                case SOUTH -> new int[] { 0, 180 };
                case NORTH -> new int[] { 0, 0 };
            };
            frame.part().modelFile(frameConnection).rotationX(rot[0]).rotationY(rot[1]).addModel()
                .condition(BlockFrame.CONNECTED_MAP.get(side), true).end();
        }
    }

    /** 1.12 swapped each face's texture between "normal" and "connected" (an item hatch, shown when an inventory is
     * next to that side); here each face is its own model part. The sides are relative to the quarry's front. */
    private void quarry() {
        MultiPartBlockStateBuilder builder = getMultipartBuilder(BCBuildersBlocks.QUARRY.get());
        for (Direction side : Direction.values()) {
            String texName = switch (side) {
                case DOWN -> "bottom";
                case UP -> "top";
                case NORTH -> "front";
                case SOUTH -> "back";
                default -> "side";
            };
            for (boolean connected : new boolean[] { false, true }) {
                ResourceLocation tex = modLoc("block/quarry/" + (connected ? "connected/" : "normal/") + texName);
                ModelFile face = models().getBuilder("block/quarry/" + side.getSerializedName() + "_"
                    + (connected ? "connected" : "normal"))
                    .texture("particle", modLoc("block/quarry/normal/side")).texture("face", tex)
                    .element().from(0, 0, 0).to(16, 16, 16).face(side).texture("#face").cullface(side).end().end();
                for (Direction facing : Direction.Plane.HORIZONTAL) {
                    builder.part().modelFile(face).rotationY(((int) facing.toYRot() + 180) % 360).addModel()
                        .condition(BlockBCBase_Neptune.PROP_FACING, facing)
                        .condition(BlockQuarry.CONNECTED_MAP.get(side), connected).end();
                }
            }
        }
        itemModels().withExistingParent("quarry", mcLoc("block/cube"))
            .texture("particle", modLoc("block/quarry/normal/side"))
            .texture("down", modLoc("block/quarry/normal/bottom")).texture("up", modLoc("block/quarry/normal/top"))
            .texture("north", modLoc("block/quarry/normal/front"))
            .texture("south", modLoc("block/quarry/normal/back"))
            .texture("east", modLoc("block/quarry/normal/side")).texture("west", modLoc("block/quarry/normal/side"));
    }
}
