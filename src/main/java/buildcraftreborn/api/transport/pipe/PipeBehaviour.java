/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pipe;

import java.io.IOException;


import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.lib.cap.ICapabilityProviderBC;
import buildcraftreborn.lib.net.PacketBufferBC;

/** The logic of a pipe: what it connects to and what it does to the items, fluid or power going through it (the flow
 * moves them). 1.21: NBT methods take the registry lookup (item stacks need it), payloads use {@link PacketBufferBC}
 * and capabilities are NeoForge block capabilities. */
public abstract class PipeBehaviour implements ICapabilityProviderBC {
    public final IPipe pipe;

    public PipeBehaviour(IPipe pipe) {
        this.pipe = pipe;
    }

    public PipeBehaviour(IPipe pipe, CompoundTag nbt, HolderLookup.Provider registries) {
        this.pipe = pipe;
    }

    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();

        return nbt;
    }

    public void writePayload(PacketBufferBC buffer, LogicalSide side) {}

    public void readPayload(PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx) throws IOException {}

    /** @deprecated Replaced by {@link #getTextureData(Direction)}. */
    @Deprecated
    public int getTextureIndex(Direction face) {
        return 0;
    }

    /** Gets the texture data to use for the specified face. This may return null for the center, which indicates that
     * the center of the pipe will use the face texture instead.
     * 
     * @param face Null indicates the center of the pipe.
     * @return The texture data for the given face. This may be null, but only for the center! */
    public PipeFaceTex getTextureData(Direction face) {
        return PipeFaceTex.get(getTextureIndex(face));
    }

    // Event handling

    public boolean canConnect(Direction face, PipeBehaviour other) {
        return true;
    }

    public boolean canConnect(Direction face, BlockEntity oTile) {
        return true;
    }

    /** Used to force a connection to a given tile, even if the {@link PipeFlow} wouldn't normally connect to it. */
    public boolean shouldForceConnection(Direction face, BlockEntity oTile) {
        return false;
    }

    public boolean onPipeActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ,
        EnumPipePart part) {
        return false;
    }

    public void onEntityCollide(Entity entity) {}

    public void onTick() {}

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction facing) {
        return null;
    }

    public void addDrops(NonNullList<ItemStack> toDrop) {}
}
