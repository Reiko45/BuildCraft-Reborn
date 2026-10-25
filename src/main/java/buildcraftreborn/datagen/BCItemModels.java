/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.loaders.DynamicFluidContainerModelBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.enums.EnumRedstoneChipset;
import buildcraftreborn.api.items.IMapLocation.MapLocationType;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.factory.BCFactoryItems;
import buildcraftreborn.lib.fluid.BCFluid;
import buildcraftreborn.robotics.BCRoboticsItems;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.transport.BCTransportItems;

/** Flat item models (textures in {@code assets/buildcraftreborn/textures/item/<name>.png}) and the fluid buckets. */
public final class BCItemModels extends ItemModelProvider {
    public BCItemModels(PackOutput output, ExistingFileHelper files) {
        super(output, BuildCraftReborn.MODID, files);
    }

    @Override
    protected void registerModels() {
        BCCoreItems.GEARS.forEach(gear -> basicItem(gear.get()));
        handheldItem(BCCoreItems.WRENCH.get());
        basicItem(BCCoreItems.DEBUGGER.get());
        // (1.12's guide.json used the guide_book texture)
        withExistingParent("guide", mcLoc("item/generated")).texture("layer0", modLoc("item/guide_book"));
        basicItem(BCCoreItems.GUIDE_NOTE.get());
        handheldItem(BCCoreItems.MARKER_CONNECTOR.get());
        // 1.12: one model per metadata; here the "colour" item property picks the model
        ItemModelBuilder brush = withExistingParent("paintbrush", mcLoc("item/handheld"))
            .texture("layer0", modLoc("item/paintbrush/clean"));
        for (net.minecraft.world.item.DyeColor colour : net.minecraft.world.item.DyeColor.values()) {
            ItemModelBuilder coloured = withExistingParent("paintbrush_" + colour.getName(), mcLoc("item/handheld"))
                .texture("layer0", modLoc("item/paintbrush/" + colour.getName()));
            brush.override().predicate(BuildCraftReborn.id("colour"), colour.getId() + 1).model(coloured).end();
        }
        basicItem(BCCoreBlocks.MARKER_VOLUME.get().asItem());
        basicItem(BCCoreBlocks.MARKER_PATH.get().asItem());
        basicItem(BCBuildersBlocks.CONSTRUCTION_MARKER.get().asItem());
        basicItem(BCFactoryItems.GEL.get());
        withExistingParent("water_gel_spawn", mcLoc("item/generated")).texture("layer0", modLoc("item/water_gel"));

        // Transport. Pipe and plug item models are swapped for code-built ones when models bake (see
        // BCTransportClient); these files are what shows if that ever fails.
        basicItem(BCTransportItems.WATERPROOF.get());
        BCTransportItems.WIRES.values().forEach(wire -> handheldItem(wire.get()));
        BCTransportItems.PIPES.forEach((def, item) -> withExistingParent(item.getId().getPath(),
            mcLoc("item/generated")).texture("layer0", def.textures[def.itemTextureCenter].replace(":", ":")));
        withExistingParent("plug_blocker", mcLoc("item/generated")).texture("layer0", modLoc("block/pipes/plug"));
        withExistingParent("plug_power_adaptor", mcLoc("item/generated"))
            .texture("layer0", modLoc("block/pipes/power_adapter"));

        // Silicon. The gate, lens and pluggable item models are swapped for code-built ones (see BCSiliconClient).
        for (EnumRedstoneChipset chipset : EnumRedstoneChipset.VALUES) {
            withExistingParent(BCSiliconItems.CHIPSETS.get(chipset).getId().getPath(), mcLoc("item/generated"))
                .texture("layer0", modLoc("item/redstone_chipset/" + chipset.getSerializedName()));
        }
        ItemModelBuilder copierFull = withExistingParent("gate_copier_full", mcLoc("item/handheld"))
            .texture("layer0", modLoc("item/gate_copier/full"));
        withExistingParent("gate_copier", mcLoc("item/handheld")).texture("layer0", modLoc("item/gate_copier/empty"))
            .override().predicate(BuildCraftReborn.id("full"), 1).model(copierFull).end();
        withExistingParent("plug_gate", mcLoc("item/generated")).texture("layer0", modLoc("item/gates/gate_logic_and"));
        withExistingParent("plug_lens", mcLoc("item/generated")).texture("layer0", modLoc("item/lens/clear"));
        withExistingParent("plug_pulsar", mcLoc("item/generated"))
            .texture("layer0", modLoc("block/plugs/pulsar_static"));
        withExistingParent("plug_light_sensor", mcLoc("item/generated"))
            .texture("layer0", modLoc("block/plugs/daylight_sensor"));
        withExistingParent("plug_timer", mcLoc("item/generated")).texture("layer0", modLoc("block/plugs/timer"));
        withExistingParent("plug_facade", mcLoc("item/generated")).texture("layer0", mcLoc("block/bricks"));

        // Builders. Blueprints, templates and single schematics show a different picture once used.
        for (String name : new String[] { "blueprint", "template", "schematic_single", "list" }) {
            ItemModelBuilder used = withExistingParent(name + "_used", mcLoc("item/generated"))
                .texture("layer0", modLoc("item/" + name + "/used"));
            withExistingParent(name, mcLoc("item/generated")).texture("layer0", modLoc("item/" + name + "/clean"))
                .override().predicate(BuildCraftReborn.id("used"), 1).model(used).end();
        }
        // The map location shows what it remembers (1.12 used one model per damage value)
        ItemModelBuilder mapLocation = withExistingParent("map_location", mcLoc("item/generated"))
            .texture("layer0", modLoc("item/map_location/clean"));
        for (MapLocationType type : MapLocationType.values()) {
            if (type == MapLocationType.CLEAN) {
                continue;
            }
            ItemModelBuilder typed = withExistingParent("map_location_" + type.getSerializedName(),
                mcLoc("item/generated")).texture("layer0", modLoc("item/map_location/" + type.getSerializedName()));
            mapLocation.override().predicate(BuildCraftReborn.id("type"), type.meta).model(typed).end();
        }
        withExistingParent("filler_planner", mcLoc("block/cube_all")).texture("all", modLoc("block/addons/filler_planner"));

        // Robotics. The robot is drawn by RobotItemRenderer (its body with its board's texture); the board shows
        // its colour (BCBoardNBT's model, picked by ModelRedstoneBoardItem); the docking station's model is built
        // from its pluggable model (see BCRoboticsClient).
        robot();
        for (String colour : new String[] { "clean", "green", "blue", "red", "yellow" }) {
            // (a name with a slash is taken as already having its folder)
            withExistingParent("item/redstone_board/" + colour, mcLoc("item/generated"))
                .texture("layer0", modLoc("item/redstone_board/" + colour));
        }
        getBuilder("redstone_board").parent(new ModelFile.UncheckedModelFile(modLoc("item/redstone_board/clean")));
        withExistingParent("robot_station", mcLoc("item/generated")).texture("layer0",
            modLoc("block/robot_station/base"));
        basicItem(BCRoboticsItems.REDSTONE_CRYSTAL.get());
        basicItem(BCRoboticsItems.ROBOT_GOGGLES.get());
        basicItem(BCSiliconPackaging.PACKAGE.get());

        // 1.12's forgebucket model with the shard's own base and fluid mask; the fluid comes from the stack
        withExistingParent("fragile_fluid_shard", ResourceLocationHelper.neoforge("item/default"))
            .texture("base", modLoc("item/fragile_fluid_shard_base"))
            .texture("fluid", modLoc("item/fragile_fluid_shard_fluid"))
            .customLoader(DynamicFluidContainerModelBuilder::begin)
            .fluid(Fluids.EMPTY)
            .flipGas(true)
            .end();

        // 1.12 used Forge's universal bucket; here each fluid has a bucket drawn with NeoForge's fluid container model
        for (BCFluid fluid : BCEnergyFluids.allFluids) {
            withExistingParent(fluid.name + "_bucket", ResourceLocationHelper.neoforge("item/bucket"))
                .customLoader(DynamicFluidContainerModelBuilder::begin)
                .fluid(fluid.source.get())
                .flipGas(true)
                .end();
        }
    }

    /** The robot's body is half a block, so it's shown twice as big as a block would be. */
    private void robot() {
        getBuilder("robot").parent(new ModelFile.UncheckedModelFile("builtin/entity"))
            .transforms()
            .transform(ItemDisplayContext.GUI).rotation(30, 225, 0).scale(1.25F).end()
            .transform(ItemDisplayContext.GROUND).translation(0, 3, 0).scale(0.5F).end()
            .transform(ItemDisplayContext.FIXED).scale(1.0F).end()
            .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND).rotation(75, 45, 0).translation(0, 2.5F, 0)
            .scale(0.75F).end()
            .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND).rotation(75, 45, 0).translation(0, 2.5F, 0)
            .scale(0.75F).end()
            .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND).rotation(0, 45, 0).scale(0.8F).end()
            .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND).rotation(0, 225, 0).scale(0.8F).end()
            .end();
    }

    private static final class ResourceLocationHelper {
        static net.minecraft.resources.ResourceLocation neoforge(String path) {
            return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoforge", path);
        }
    }
}
