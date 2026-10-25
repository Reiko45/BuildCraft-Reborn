/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.emi;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.fluids.FluidStack;

import dev.emi.emi.api.neoforge.NeoForgeEmiStack;
import dev.emi.emi.api.stack.EmiStack;

final class BCEmiUtil {
    private BCEmiUtil() {}

    static EmiStack fluid(@Nullable FluidStack fluid) {
        return fluid == null || fluid.isEmpty() ? EmiStack.EMPTY : NeoForgeEmiStack.of(fluid);
    }

    /** The stacks that aren't empty. */
    static List<EmiStack> present(EmiStack... stacks) {
        List<EmiStack> list = new ArrayList<>();
        for (EmiStack stack : stacks) {
            if (!stack.isEmpty()) {
                list.add(stack);
            }
        }
        return list;
    }

    /** EMI's id for a recipe that isn't in the recipe manager: the path starts with a slash. */
    static ResourceLocation synthetic(ResourceLocation id) {
        return id.getPath().startsWith("/") ? id
            : ResourceLocation.fromNamespaceAndPath(id.getNamespace(), "/" + id.getPath());
    }
}
