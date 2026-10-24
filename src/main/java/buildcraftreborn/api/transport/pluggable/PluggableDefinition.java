/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pluggable;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.api.core.InvalidInputDataException;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.lib.net.PacketBufferBC;

public final class PluggableDefinition {
    public final ResourceLocation identifier;

    public final IPluggableNetLoader loader;
    public final IPluggableNbtReader reader;

    @Nullable
    public final IPluggableCreator creator;

    public PluggableDefinition(ResourceLocation identifier, IPluggableNbtReader reader, IPluggableNetLoader loader) {
        this.identifier = identifier;
        this.reader = reader;
        this.loader = loader;
        this.creator = null;
    }

    public PluggableDefinition(ResourceLocation identifier, @Nullable IPluggableCreator creator) {
        this.identifier = identifier;
        this.reader = creator;
        this.loader = creator;
        this.creator = creator;
    }

    public PipePluggable readFromNbt(IPipeHolder holder, Direction side, CompoundTag nbt,
        HolderLookup.Provider registries) {
        return reader.readFromNbt(this, holder, side, nbt, registries);
    }

    public PipePluggable loadFromBuffer(IPipeHolder holder, Direction side, PacketBufferBC buffer)
        throws InvalidInputDataException {
        return loader.loadFromBuffer(this, holder, side, buffer);
    }

    @FunctionalInterface
    public interface IPluggableNbtReader {
        /** Reads the pipe pluggable from NBT. Unlike {@link IPluggableNetLoader} (which is allowed to fail and throw an
         * exception if the wrong data is given) this should make a best effort to read the pluggable from nbt, or fall
         * back to sensible defaults. */
        PipePluggable readFromNbt(PluggableDefinition definition, IPipeHolder holder, Direction side,
            CompoundTag nbt, HolderLookup.Provider registries);
    }

    @FunctionalInterface
    public interface IPluggableNetLoader {
        PipePluggable loadFromBuffer(PluggableDefinition definition, IPipeHolder holder, Direction side,
            PacketBufferBC buffer) throws InvalidInputDataException;
    }

    @FunctionalInterface
    public interface IPluggableCreator extends IPluggableNbtReader, IPluggableNetLoader {
        @Override
        default PipePluggable loadFromBuffer(PluggableDefinition definition, IPipeHolder holder, Direction side,
            PacketBufferBC buffer) {
            return createSimplePluggable(definition, holder, side);
        }

        @Override
        default PipePluggable readFromNbt(PluggableDefinition definition, IPipeHolder holder, Direction side,
            CompoundTag nbt, HolderLookup.Provider registries) {
            return createSimplePluggable(definition, holder, side);
        }

        PipePluggable createSimplePluggable(PluggableDefinition definition, IPipeHolder holder, Direction side);
    }
}
