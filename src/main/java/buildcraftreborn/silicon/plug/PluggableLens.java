/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.plug;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import buildcraftreborn.api.core.render.EnumRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pipe.PipeEventHandler;
import buildcraftreborn.api.transport.pipe.PipeEventItem;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;

import buildcraftreborn.lib.misc.MessageUtil;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.lib.net.PacketBufferBC;

import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.client.model.key.KeyPlugLens;

public class PluggableLens extends PipePluggable {
    private static final AABB[] BOXES = new AABB[6];

    static {
        double ll = 0 / 16.0;
        double lu = 2 / 16.0;
        double ul = 14 / 16.0;
        double uu = 16 / 16.0;

        double min = 3 / 16.0;
        double max = 13 / 16.0;

        BOXES[Direction.DOWN.get3DDataValue()] = new AABB(min, ll, min, max, lu, max);
        BOXES[Direction.UP.get3DDataValue()] = new AABB(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.get3DDataValue()] = new AABB(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.get3DDataValue()] = new AABB(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.get3DDataValue()] = new AABB(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.get3DDataValue()] = new AABB(ul, min, min, uu, max, max);
    }

    public final DyeColor colour;
    public final boolean isFilter;

    // Manual constructor (called by the specific item pluggable code)

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, DyeColor colour,
        boolean isFilter) {
        super(def, holder, side);
        this.colour = colour;
        this.isFilter = isFilter;
    }

    // Saving + Loading

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt,
        HolderLookup.Provider registries) {
        super(def, holder, side);
        if (nbt.contains("colour")) {
            colour = NBTUtilBC.readEnum(nbt.get("colour"), DyeColor.class);
        } else {
            colour = DyeColor.byId(nbt.getByte("c"));
        }
        isFilter = nbt.getBoolean("f");
    }

    @Override
    public CompoundTag writeToNbt(HolderLookup.Provider registries) {
        CompoundTag nbt = super.writeToNbt(registries);
        nbt.put("colour", NBTUtilBC.writeEnum(colour));
        nbt.putBoolean("f", isFilter);
        return nbt;
    }

    // Networking

    public PluggableLens(PluggableDefinition def, IPipeHolder holder, Direction side, PacketBufferBC buffer) {
        super(def, holder, side);
        PacketBufferBC buf = buffer;
        colour = MessageUtil.readEnumOrNull(buf, DyeColor.class);
        isFilter = buf.readBoolean();
    }

    @Override
    public void writeCreationPayload(PacketBufferBC buffer) {
        PacketBufferBC buf = buffer;
        MessageUtil.writeEnumOrNull(buf, colour);
        buf.writeBoolean(isFilter);
    }

    // Pluggable methods

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.get3DDataValue()];
    }

    @Override
    public ItemStack getPickStack() {
        return BCSiliconItems.PLUG_LENS.get().getStack(colour, isFilter);
    }

    @Override
    public boolean isBlocking() {
        return false;
    }

    @Override
    public PluggableModelKey getModelRenderKey(EnumRenderLayer layer) {
        switch (layer) {
            case CUTOUT:
            case TRANSLUCENT:
                return new KeyPlugLens(layer, side, colour, isFilter);
            default:
                return null;
        }
    }

    @PipeEventHandler
    public void tryInsert(PipeEventItem.TryInsert tryInsert) {
        if (isFilter && tryInsert.from == side) {
            DyeColor itemColour = tryInsert.colour;
            if (itemColour != null && itemColour != colour) {
                tryInsert.cancel();
            }
        }
    }

    @PipeEventHandler
    public void sideCheck(PipeEventItem.SideCheck event) {
        if (isFilter) {
            if (event.colour == colour) {
                event.increasePriority(side);
            } else if (event.colour != null) {
                event.disallow(side);
            } else {
                event.decreasePriority(side);
            }
        }
    }

    /** Called from either *this* pipe, or the neighbouring pipe as given in compareSide. */
    void sideCheckAnyPos(PipeEventItem.SideCheck event, Direction compareSide) {
        // Note that this should *never* use "this.side" as it may be wrong!
        if (isFilter) {
            if (event.colour == colour) {
                event.increasePriority(compareSide);
            } else if (event.colour != null) {
                if (compareSide == side) {
                    event.disallow(compareSide);
                }
            } else {
                event.decreasePriority(compareSide);
            }
        }
    }

    @PipeEventHandler
    public void beforeInsert(PipeEventItem.OnInsert event) {
        if (!isFilter) {
            if (event.from == side) {
                event.colour = colour;
            }
        }
    }

    @PipeEventHandler
    public void reachEnd(PipeEventItem.ReachEnd event) {
        if (!isFilter) {
            if (event.to == side) {
                event.colour = colour;
            }
        }
    }
}
