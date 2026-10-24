/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pluggable;

import java.io.IOException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.render.EnumRenderLayer;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.IPipeHolder.PipeMessageReceiver;
import buildcraftreborn.lib.net.PacketBufferBC;

/** Something attached to one side of a pipe: a gate, a facade, a lens, a plug, a power adaptor and so on. 1.21: NBT
 * methods take the registry lookup, payloads use {@link PacketBufferBC}, capabilities are NeoForge block capabilities
 * and the render layer is {@link EnumRenderLayer}. 1.12's {@code getBlockFaceShape} is gone with the method it
 * mirrored (use {@link #isSideSolid()}). */
public abstract class PipePluggable {
    public final PluggableDefinition definition;
    public final IPipeHolder holder;
    public final Direction side;

    public PipePluggable(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        this.definition = definition;
        this.holder = holder;
        this.side = side;
    }

    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = new CompoundTag();
        return nbt;
    }

    /** Writes the payload that will be passed into
     * {@link PluggableDefinition#loadFromBuffer(IPipeHolder, Direction, PacketBufferBC)} on the client. (This is
     * called on the server and sent to the client). Note that this will be called *instead* of write and read
     * payload. */
    public void writeCreationPayload(PacketBufferBC buffer) {}

    public void writePayload(PacketBufferBC buffer, LogicalSide side) {}

    public void readPayload(PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {}

    public final void scheduleNetworkUpdate() {
        holder.scheduleNetworkUpdate(PipeMessageReceiver.PLUGGABLES[side.ordinal()]);
    }

    public void onTick() {}

    /** @return A bounding box that will be used for collisions and raytracing. */
    public abstract AABB getBoundingBox();

    /** @return True if the pipe cannot connect outwards (it is blocked), or False if this does not block the pipe. */
    public boolean isBlocking() {
        return false;
    }

    /** Gets the value of a specified capability key, or null if the given capability is not supported at the call
     * time. This is what is offered on this side of the pipe's block. */
    @Nullable
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> cap) {
        return null;
    }

    /** Gets the capability that is accessible from the pipe that this is attached to. */
    @Nullable
    public <T> T getInternalCapability(BlockCapability<T, @Nullable Direction> cap) {
        return null;
    }

    /** Called whenever this pluggable is removed from the pipe. */
    public void onRemove() {}

    /** @param toDrop A list containing all the items to drop (so you should add your items to this list) */
    public void addDrops(NonNullList<ItemStack> toDrop) {
        ItemStack stack = getPickStack();
        if (!stack.isEmpty()) {
            toDrop.add(stack);
        }
    }

    /** Called whenever this pluggable is picked by the player (similar to Block.getCloneItemStack)
     * 
     * @return The stack that should be picked, or ItemStack.EMPTY if no stack can be picked from this pluggable. */
    public ItemStack getPickStack() {
        return ItemStack.EMPTY;
    }

    public boolean onPluggableActivate(Player player, BlockHitResult trace, float hitX, float hitY, float hitZ) {
        return false;
    }

    @Nullable
    public PluggableModelKey getModelRenderKey(EnumRenderLayer layer) {
        return null;
    }

    /** Called if the {@link IPluggableStaticBaker} returns quads with tint indexes set to
     * <code>data * 6 + key.side.ordinal()</code>. <code>"data"</code> is passed in here as <code>"tintIndex"</code>.
     * 
     * @return The tint index to render the quad with, or -1 for default. */
    public int getBlockColor(int tintIndex) {
        return -1;
    }

    /** Whether things like fences and panes connect to this side of the pipe. */
    public boolean canBeConnected() {
        return false;
    }

    /** Whether this side of the pipe counts as a full solid face (for torches, levers and the like). */
    public boolean isSideSolid() {
        return false;
    }

    /** PipePluggable version of the block's explosion resistance. */
    public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion) {
        return 0;
    }

    public boolean canConnectToRedstone(@Nullable Direction to) {
        return false;
    }

    public void onPlacedBy(Player player) {}
}
