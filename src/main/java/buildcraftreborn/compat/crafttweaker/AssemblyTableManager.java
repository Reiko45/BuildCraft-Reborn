/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.crafttweaker;

import java.util.ArrayList;
import java.util.List;

import org.openzen.zencode.java.ZenCodeType;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.AssemblyRecipe;
import buildcraftreborn.api.recipes.AssemblyRecipeBasic;
import buildcraftreborn.api.recipes.IngredientStack;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.ingredient.IIngredientWithAmount;
import com.blamejared.crafttweaker.api.item.IItemStack;
import com.blamejared.crafttweaker.api.recipe.manager.base.IRecipeManager;

/** CraftTweaker's {@code <recipetype:buildcraftreborn:assembly>}: adds assembly table recipes, and removes them with
 * the usual {@code remove}, {@code removeByName}... (1.12: {@code mods.buildcraft.AssemblyTable}).
 *
 * <pre>
 * &lt;recipetype:buildcraftreborn:assembly&gt;.addRecipe("shiny_dirt", &lt;item:minecraft:diamond&gt;, 400,
 *     [&lt;item:minecraft:dirt&gt; * 8, &lt;tag:item:c:dusts/redstone&gt;]);
 * &lt;recipetype:buildcraftreborn:assembly&gt;.remove(&lt;item:buildcraftreborn:plug_pulsar&gt;);
 * </pre>
 * CraftTweaker finds this class by its annotation, and only loads it when CraftTweaker is installed. */
@ZenRegister
@ZenCodeType.Name("mods.buildcraftreborn.AssemblyTable")
public enum AssemblyTableManager implements IRecipeManager<AssemblyRecipe> {
    INSTANCE;

    /** @param name The recipe's name (it gets the {@code crafttweaker} namespace).
     * @param output What the recipe makes.
     * @param powerMj The power it takes, in MJ.
     * @param inputs Up to 12 inputs, each with an amount. */
    @ZenCodeType.Method
    public void addRecipe(String name, IItemStack output, double powerMj, IIngredientWithAmount[] inputs) {
        if (inputs.length == 0 || inputs.length > 12) {
            throw new IllegalArgumentException("An assembly recipe needs 1 to 12 inputs, not " + inputs.length);
        }
        List<IngredientStack> stacks = new ArrayList<>();
        for (IIngredientWithAmount input : inputs) {
            stacks.add(new IngredientStack(input.ingredient().asVanillaIngredient(), input.amount()));
        }
        ResourceLocation id = fixRecipeId(name);
        AssemblyRecipeBasic recipe = new AssemblyRecipeBasic(id, Math.max(1, Math.round(powerMj * MjAPI.MJ)), stacks,
            output.getInternal().copy());
        CraftTweakerAPI.apply(new ActionAddRecipe<>(this, new RecipeHolder<>(id, recipe)));
    }

    @Override
    public RecipeType<AssemblyRecipe> getRecipeType() {
        return AssemblyRecipe.type.get();
    }
}
