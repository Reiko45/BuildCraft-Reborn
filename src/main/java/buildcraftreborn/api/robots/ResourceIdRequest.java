/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.robots;

import java.util.Objects;

import net.minecraft.nbt.CompoundTag;

import buildcraftreborn.api.core.EnumPipePart;

/** One request slot of a station's {@link IRequestProvider}. */
public class ResourceIdRequest extends ResourceIdBlock {

    private int slot;

    public ResourceIdRequest() {

    }

    public ResourceIdRequest(DockingStation station, int slot) {
        pos = station.index();
        side = EnumPipePart.fromFacing(station.side());
        this.slot = slot;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!super.equals(obj)) return false;

        ResourceIdRequest compareId = (ResourceIdRequest) obj;

        return slot == compareId.slot;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), slot);
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        super.writeToNBT(nbt);
        nbt.putInt("localId", slot);
    }

    @Override
    protected void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
        slot = nbt.getInt("localId");
    }
}
