/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.crafttweaker;

import org.openzen.zencode.java.ZenCodeType;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IRefineryRecipe;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IRefineryRegistry;
import buildcraftreborn.lib.recipe.RefineryRecipeRegistry;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;

/** CraftTweaker's {@code mods.buildcraftreborn.Refinery} (1.12: {@code mods.buildcraft.Refinery}): adds heat
 * exchanger and distiller recipes. The built-in recipes are data maps, which win over these; change those with a data
 * pack.
 *
 * <pre>
 * mods.buildcraftreborn.Refinery.addHeatable(&lt;fluid:minecraft:water&gt; * 10, &lt;fluid:minecraft:lava&gt; * 1, 0, 3);
 * mods.buildcraftreborn.Refinery.addDistillation(&lt;fluid:minecraft:lava&gt; * 10, &lt;fluid:minecraft:water&gt; * 5,
 *     &lt;fluid:minecraft:milk&gt; * 5, 20);
 * </pre> */
@ZenRegister
@ZenCodeType.Name("mods.buildcraftreborn.Refinery")
public final class Refinery {
    private Refinery() {}

    /** A fluid the heat exchanger heats up (its cold side), from heat level {@code heatFrom} to {@code heatTo}. An
     * empty output means the fluid is used up. */
    @ZenCodeType.Method
    public static void addHeatable(IFluidStack in, IFluidStack out, int heatFrom, int heatTo) {
        RefineryRecipeRegistry registry = RefineryRecipeRegistry.INSTANCE;
        FluidStack input = ScriptActions.fluid(in);
        add(registry.getHeatableRegistry(), "heatable", input,
            registry.createHeatingRecipe(input, ScriptActions.fluid(out), heatFrom, heatTo));
    }

    /** A fluid the heat exchanger cools down (its hot side). */
    @ZenCodeType.Method
    public static void addCoolable(IFluidStack in, IFluidStack out, int heatFrom, int heatTo) {
        RefineryRecipeRegistry registry = RefineryRecipeRegistry.INSTANCE;
        FluidStack input = ScriptActions.fluid(in);
        add(registry.getCoolableRegistry(), "coolable", input,
            registry.createCoolableRecipe(input, ScriptActions.fluid(out), heatFrom, heatTo));
    }

    /** A fluid the distiller splits into a gas and a liquid, using {@code powerMj} MJ each time (new in BuildCraft
     * Reborn). */
    @ZenCodeType.Method
    public static void addDistillation(IFluidStack in, IFluidStack gas, IFluidStack liquid, double powerMj) {
        RefineryRecipeRegistry registry = RefineryRecipeRegistry.INSTANCE;
        FluidStack input = ScriptActions.fluid(in);
        add(registry.getDistillationRegistry(), "distillation", input, registry.createDistillationRecipe(input,
            ScriptActions.fluid(gas), ScriptActions.fluid(liquid), Math.max(1, Math.round(powerMj * MjAPI.MJ))));
    }

    private static <R extends IRefineryRecipe> void add(IRefineryRegistry<R> registry, String kind, FluidStack input,
        R recipe) {
        if (input.isEmpty()) {
            throw new IllegalArgumentException("The input of a " + kind + " recipe can't be empty");
        }
        CraftTweakerAPI.apply(ScriptActions.change(ScriptActions.key(kind, input),
            kind + " recipe for " + ScriptActions.name(input),
            () -> registry.addRecipe(recipe), () -> registry.removeRecipes(r -> r == recipe)));
    }
}
