/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.plug;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.api.core.render.EnumRenderLayer;
import buildcraftreborn.api.facades.FacadeType;
import buildcraftreborn.api.facades.IFacade;
import buildcraftreborn.api.facades.IFacadePhasedState;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.client.FacadeClientUtil;
import buildcraftreborn.silicon.client.model.key.KeyPlugFacade;
import buildcraftreborn.transport.client.model.key.KeyPlugBlocker;

/** A facade on a pipe side: a 2 pixel thick plate that looks like a block (or a frame, if hollow). Solid facades
 * block the pipe's connection on that side. */
public class PluggableFacade extends PipePluggable implements IFacade {

    private static final AABB[] BOXES = new AABB[6];

    static {
        double ll = 0 / 16.0;
        double lu = 2 / 16.0;
        double ul = 14 / 16.0;
        double uu = 16 / 16.0;

        double min = 0 / 16.0;
        double max = 16 / 16.0;

        BOXES[Direction.DOWN.get3DDataValue()] = new AABB(min, ll, min, max, lu, max);
        BOXES[Direction.UP.get3DDataValue()] = new AABB(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.get3DDataValue()] = new AABB(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.get3DDataValue()] = new AABB(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.get3DDataValue()] = new AABB(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.get3DDataValue()] = new AABB(ul, min, min, uu, max, max);
    }

    public static final int SIZE = 2;
    public final FacadeInstance states;
    public final boolean isSideSolid;
    public int activeState;

    public PluggableFacade(PluggableDefinition definition, IPipeHolder holder, Direction side, FacadeInstance states) {
        super(definition, holder, side);
        this.states = states;
        isSideSolid = states.areAllStatesSolid(side);
    }

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt,
        HolderLookup.Provider registries) {
        super(def, holder, side);
        this.states = FacadeInstance.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE),
            nbt.getCompound("facade")).result()
            .orElseGet(() -> FacadeInstance.createSingle(FacadeStateManager.defaultState(), false));
        activeState = Mth.clamp(nbt.getInt("activeState"), 0, states.phasedStates.length - 1);
        isSideSolid = states.areAllStatesSolid(side);
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = super.writeToNbt(registries);
        FacadeInstance.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), states).result()
            .ifPresent(tag -> nbt.put("facade", tag));
        nbt.putInt("activeState", activeState);
        return nbt;
    }

    // Networking

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, Direction side, PacketBufferBC buffer) {
        super(def, holder, side);
        states = FacadeInstance.readFromBuffer(buffer);
        activeState = Mth.clamp(buffer.readVarInt(), 0, states.phasedStates.length - 1);
        isSideSolid = buffer.readBoolean();
    }

    @Override
    public void writeCreationPayload(PacketBufferBC buffer) {
        states.writeToBuffer(buffer);
        buffer.writeVarInt(activeState);
        buffer.writeBoolean(isSideSolid);
    }

    // Pluggable methods

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.get3DDataValue()];
    }

    @Override
    public boolean isBlocking() {
        return !isHollow();
    }

    @Override
    public boolean canBeConnected() {
        return !isHollow();
    }

    @Override
    public boolean isSideSolid() {
        return isSideSolid;
    }

    @Override
    public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion) {
        return states.phasedStates[activeState].stateInfo.state.getBlock().getExplosionResistance();
    }

    @Override
    public ItemStack getPickStack() {
        return BCSiliconItems.PLUG_FACADE.get().createItemStack(states);
    }

    /** Client only. A basic facade draws the block's model, in the translucent layer if the block is translucent;
     * phased facades draw a plug. */
    @Override
    public PluggableModelKey getModelRenderKey(EnumRenderLayer layer) {
        if (states.type == FacadeType.Basic) {
            BlockState blockState = states.phasedStates[activeState].stateInfo.state;
            boolean translucent = FacadeClientUtil.isTranslucent(blockState);
            if (translucent != (layer == EnumRenderLayer.TRANSLUCENT)) {
                return null;
            }
            return new KeyPlugFacade(layer, side, blockState, isHollow());
        } else if (layer == EnumRenderLayer.CUTOUT) {
            return KeyPlugBlocker.create(side);
        }
        return null;
    }

    /** Client only. */
    @Override
    public int getBlockColor(int tintIndex) {
        FacadePhasedState state = states.phasedStates[activeState];
        return FacadeClientUtil.getBlockColour(state.stateInfo.state, holder.getPipeWorld(), holder.getPipePos(),
            tintIndex);
    }

    // IFacade

    @Override
    public FacadeType getType() {
        return states.getType();
    }

    @Override
    public boolean isHollow() {
        return states.isHollow();
    }

    @Override
    public IFacadePhasedState[] getPhasedStates() {
        return states.getPhasedStates();
    }
}
