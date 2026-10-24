/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;

public final class PipeFlowType {
    public final IFlowCreator creator;
    public final IFlowLoader loader;

    /** The default colour type, if none is given in {@link PipeDefinition}. if this is also null then the final
     * fallback type is {@link EnumPipeColourType#TRANSLUCENT}. */
    public EnumPipeColourType fallbackColourType;

    public PipeFlowType(IFlowCreator creator, IFlowLoader loader) {
        this(creator, loader, null);
    }

    public PipeFlowType(IFlowCreator creator, IFlowLoader loader, EnumPipeColourType colourType) {
        this.creator = creator;
        this.loader = loader;
        this.fallbackColourType = colourType;
    }

    @FunctionalInterface
    public interface IFlowCreator {
        PipeFlow createFlow(IPipe t);
    }

    @FunctionalInterface
    public interface IFlowLoader {
        PipeFlow loadFlow(IPipe t, CompoundTag u, HolderLookup.Provider registries);
    }
}
