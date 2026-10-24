/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.facades;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;

public interface IFacadeItem {

    @Nullable
    default FacadeType getFacadeType(ItemStack stack) {
        IFacade facade = getFacade(stack);
        if (facade == null) {
            return null;
        }
        return facade.getType();
    }

    
    ItemStack getFacadeForBlock(BlockState state);

    /** @param facade The {@link IFacade} instance. NOTE: This MUST be an object returned from
     *            {@link IFacadeRegistry#createBasicFacade(IFacadeState, boolean)} or
     *            {@link IFacadeRegistry#createPhasedFacade(IFacadePhasedState[], boolean)}, otherwise a
     *            {@link ClassCastException} will be thrown! */
    ItemStack createFacadeStack(IFacade facade);

    @Nullable
    IFacade getFacade(ItemStack facade);
}
