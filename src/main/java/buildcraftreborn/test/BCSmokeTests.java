/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.BCCoreTags;

/**
 * Phase 0 smoke tests, run headless by {@code gradlew runGameTestServer}. They prove the mod loads on a dedicated
 * server and that generated data (tags, recipes) is present. Each later phase adds tests next to its exit criteria.
 */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCSmokeTests {
    private BCSmokeTests() {}

    @GameTest(template = "empty")
    public static void gearsCarryCommonTags(GameTestHelper helper) {
        for (var gear : BCCoreItems.GEARS) {
            if (!new ItemStack(gear.get()).is(BCCoreTags.GEARS)) {
                helper.fail(gear.getId() + " is missing from #" + BCCoreTags.GEARS.location());
            }
        }
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gearRecipesLoaded(GameTestHelper helper) {
        for (var gear : BCCoreItems.GEARS) {
            ResourceLocation recipe = gear.getId();
            if (helper.getLevel().getRecipeManager().byKey(recipe).isEmpty()) {
                helper.fail("Missing crafting recipe " + recipe);
            }
        }
        helper.succeed();
    }
}
