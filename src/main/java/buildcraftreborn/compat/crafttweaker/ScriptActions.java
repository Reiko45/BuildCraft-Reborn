/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.crafttweaker;

import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.base.IUndoableAction;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.zencode.IScriptLoadSource;

/** Script changes to BuildCraft's code registries (refinery recipes, fuels and coolants). Each change is keyed by
 * what it is for (like "a heatable recipe for oil"), and adding one again replaces the old one, so scripts can run
 * more than once (on reloads, and on clients connected to a server) without adding duplicates. */
final class ScriptActions {
    private ScriptActions() {}

    /** How to take back each change that is in place, by key. */
    private static final Map<String, Runnable> UNDO = new HashMap<>();

    static FluidStack fluid(IFluidStack stack) {
        return stack.isEmpty() ? FluidStack.EMPTY : new FluidStack(stack.getFluid(), (int) stack.getAmount());
    }

    static String name(FluidStack fluid) {
        return BuiltInRegistries.FLUID.getKey(fluid.getFluid()).toString();
    }

    static String name(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    static String key(String kind, FluidStack fluid) {
        return kind + " " + name(fluid);
    }

    static String key(String kind, ItemStack stack) {
        return kind + " " + name(stack);
    }

    /** @param add Makes the change.
     * @param remove Takes it back. */
    static IUndoableAction change(String key, String description, Runnable add, Runnable remove) {
        return new IUndoableAction() {
            @Override
            public void apply() {
                Runnable old = UNDO.remove(key);
                if (old != null) {
                    old.run();
                }
                add.run();
                UNDO.put(key, remove);
            }

            @Override
            public void undo() {
                Runnable old = UNDO.remove(key);
                if (old != null) {
                    old.run();
                }
            }

            @Override
            public String describe() {
                return "Adding " + description;
            }

            @Override
            public String describeUndo() {
                return "Removing " + description;
            }

            @Override
            public String systemName() {
                return "BuildCraft Reborn";
            }

            /** The registries live on both sides: run on the server's reload, and on a client connected to another
             * server (in single player the client shares the integrated server's registries). */
            @Override
            public boolean shouldApplyOn(IScriptLoadSource source, Logger logger) {
                if (source.id().equals(CraftTweakerConstants.RELOAD_LISTENER_SOURCE_ID)) {
                    return true;
                }
                return source.id().equals(CraftTweakerConstants.CLIENT_RECIPES_UPDATED_SOURCE_ID)
                    && ServerLifecycleHooks.getCurrentServer() == null;
            }
        };
    }
}
