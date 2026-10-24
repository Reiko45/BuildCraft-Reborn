/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.energy.generation.OilGenStructure;
import buildcraftreborn.energy.generation.OilGenerator;
import buildcraftreborn.energy.generation.OilGenerator.GenType;
import buildcraftreborn.energy.tile.TileSpringOil;

/** Phase 2 tests for oil wells and springs. The GameTest world is superflat, where oil doesn't generate, so the well
 * shapes are generated into the test area directly and the well chances are checked without a world. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCOilGenTests {
    private static final long SEED = 0x5EED_B111_D0C2L;

    private BCOilGenTests() {}

    private static OilGenerator.WorldInfo info(GameTestHelper helper, ResourceKey<Biome> biome, boolean end) {
        Holder<Biome> holder = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME)
            .getHolderOrThrow(biome);
        return new OilGenerator.WorldInfo(SEED, -64, 319, end, (x, z) -> holder);
    }

    private static int countWells(OilGenerator.WorldInfo info, int size) {
        int count = 0;
        for (int cx = 0; cx < size; cx++) {
            for (int cz = 0; cz < size; cz++) {
                if (OilGenerator.getGenType(info, cx, cz) != GenType.NONE) {
                    count++;
                }
            }
        }
        return count;
    }

    private static int countOil(GameTestHelper helper, BoundingBox relative) {
        int count = 0;
        for (BlockPos pos : BlockPos.betweenClosed(relative.minX(), relative.minY(), relative.minZ(),
            relative.maxX(), relative.maxY(), relative.maxZ())) {
            if (helper.getBlockState(pos).is(BCEnergyFluids.crudeOil[0].getBlockOrNull())) {
                count++;
            }
        }
        return count;
    }

    /** 1.12's chances: large wells 0.04% and medium 0.1% of chunks, 30 times that in deserts and oceans (which stand
     * in for the Oil Desert and Oil Ocean biomes), none in the Nether or near the End's main island. */
    @GameTest(template = "empty")
    public static void oilWellsAreCommonestInDesertsAndOceans(GameTestHelper helper) {
        int size = 200; // 40,000 chunks
        int plains = countWells(info(helper, Biomes.PLAINS, false), size);
        int desert = countWells(info(helper, Biomes.DESERT, false), size);
        int ocean = countWells(info(helper, Biomes.DEEP_OCEAN, false), size);
        int nether = countWells(info(helper, Biomes.NETHER_WASTES, false), size);
        // Expected: plains 0.14% (56), desert and ocean about 4.2% (1680)
        helper.assertTrue(plains > 25 && plains < 100, "Plains wells out of range: " + plains);
        helper.assertTrue(desert > 1400 && desert < 2000, "Desert wells out of range: " + desert);
        helper.assertTrue(ocean > 1400 && ocean < 2000, "Ocean wells out of range: " + ocean);
        helper.assertTrue(nether == 0, "Oil in the Nether: " + nether);

        OilGenerator.WorldInfo end = info(helper, Biomes.END_HIGHLANDS, true);
        boolean nearIsland = false;
        for (int cx = -20; cx < 20; cx++) {
            for (int cz = -20; cz < 20; cz++) {
                nearIsland |= !OilGenerator.getStructures(end, cx, cz).isEmpty();
            }
        }
        helper.assertFalse(nearIsland, "Oil within 1200 blocks of the End's main island");

        // The same chunk always gets the same well, and large wells end in a spring at the bottom of the world.
        OilGenerator.WorldInfo info = info(helper, Biomes.DESERT, false);
        boolean foundLarge = false;
        for (int cx = 0; cx < size && !foundLarge; cx++) {
            for (int cz = 0; cz < size && !foundLarge; cz++) {
                if (OilGenerator.getGenType(info, cx, cz) == GenType.LARGE) {
                    List<OilGenStructure> a = OilGenerator.getStructures(info, cx, cz);
                    List<OilGenStructure> b = OilGenerator.getStructures(info, cx, cz);
                    helper.assertTrue(a.size() == b.size(), "Well changed between calls");
                    for (int i = 0; i < a.size(); i++) {
                        helper.assertTrue(a.get(i).box.equals(b.get(i).box), "Well changed between calls");
                    }
                    OilGenStructure last = a.get(a.size() - 1);
                    helper.assertTrue(last instanceof OilGenStructure.Spring spring && spring.pos.getY() == -64,
                        "Large well should end in a spring at the bottom of the world");
                    foundLarge = true;
                }
            }
        }
        helper.assertTrue(foundLarge, "No large well in 40,000 desert chunks");
        helper.succeed();
    }

    /** Spheres and tubes place exactly the blocks they count (the spring is told the total). Only the part inside the
     * given box is placed, which is how wells cross chunk borders. */
    @GameTest(template = "empty")
    public static void wellShapesOnlyPlaceInsideTheChunk(GameTestHelper helper) {
        BoundingBox relative = new BoundingBox(0, 0, 0, 2, 2, 2);
        BoundingBox area = BoundingBox.fromCorners(helper.absolutePos(BlockPos.ZERO),
            helper.absolutePos(new BlockPos(2, 2, 2)));

        OilGenStructure sphere = OilGenerator.createSphere(helper.absolutePos(new BlockPos(1, 1, 1)), 1);
        sphere.generate(helper.getLevel(), area);
        helper.assertTrue(sphere.countOilBlocks() == 7, "Sphere of radius 1 should count 7 blocks");
        int placed = countOil(helper, relative);
        clear(helper, relative);
        helper.assertTrue(placed == 7, "Sphere of radius 1 placed " + placed + " blocks");

        // A tube of radius 1 and length 2 is a plus shape, 3 layers high. Only its bottom layer is inside this box.
        OilGenStructure tube = OilGenerator.createTube(helper.absolutePos(new BlockPos(1, 0, 1)), 2, 1, Axis.Y);
        helper.assertTrue(tube.countOilBlocks() == 15, "Tube should count 15 blocks");
        BoundingBox bottomLayer = BoundingBox.fromCorners(helper.absolutePos(BlockPos.ZERO),
            helper.absolutePos(new BlockPos(2, 0, 2)));
        tube.generate(helper.getLevel(), bottomLayer);
        placed = countOil(helper, relative);
        clear(helper, relative);
        helper.assertTrue(placed == 5, "Tube placed " + placed + " blocks inside a one-layer box");
        helper.succeed();
    }

    /** Removes test oil straight away, before its fluid ticks let it flow out of the test area. */
    private static void clear(GameTestHelper helper, BoundingBox relative) {
        for (BlockPos pos : BlockPos.betweenClosed(relative.minX(), relative.minY(), relative.minZ(),
            relative.maxX(), relative.maxY(), relative.maxZ())) {
            helper.setBlock(pos, Blocks.AIR);
        }
    }

    /** The spring under a large well remembers the well's size, and keeps it when saved (1.12 forgot it on reload). */
    @GameTest(template = "empty")
    public static void oilSpringRemembersWellSize(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 0, 1);
        OilGenStructure.Spring spring = OilGenerator.createSpring(helper.absolutePos(pos));
        spring.generate(helper.getLevel(), 1234);
        helper.assertBlockPresent(BCEnergyBlocks.OIL_SPRING.get(), pos);
        TileSpringOil tile = helper.getBlockEntity(pos);
        helper.assertTrue(tile.totalSources == 1234, "Spring should know the well size");

        CompoundTag saved = tile.saveWithFullMetadata(helper.getLevel().registryAccess());
        BlockEntity loaded = BlockEntity.loadStatic(tile.getBlockPos(), tile.getBlockState(), saved,
            helper.getLevel().registryAccess());
        helper.assertTrue(loaded instanceof TileSpringOil reloaded && reloaded.totalSources == 1234,
            "Spring forgot the well size when reloaded");
        helper.succeed();
    }

    /** A water spring keeps making water above itself; an oil spring is unbreakable. */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void waterSpringMakesWater(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), BCCoreBlocks.WATER_SPRING.get());
        helper.assertTrue(BCEnergyBlocks.OIL_SPRING.get().defaultBlockState().getDestroySpeed(helper.getLevel(),
            BlockPos.ZERO) < 0, "Springs should be unbreakable");
        helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.WATER, new BlockPos(1, 1, 1)));
    }
}
