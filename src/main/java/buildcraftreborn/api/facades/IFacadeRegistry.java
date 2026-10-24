/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.facades;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.DyeColor;

public interface IFacadeRegistry {

    Collection<? extends IFacadeState> getValidFacades();

    IFacadePhasedState createPhasedState(IFacadeState state, @Nullable DyeColor activeColor);

    IFacade createPhasedFacade(IFacadePhasedState[] states, boolean isHollow);

    default IFacade createBasicFacade(IFacadeState state, boolean isHollow) {
        return createPhasedFacade(new IFacadePhasedState[] { createPhasedState(state, null) }, isHollow);
    }
}
