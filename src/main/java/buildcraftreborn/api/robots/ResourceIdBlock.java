/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.robots;

import java.util.Objects;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;

import buildcraftreborn.api.core.EnumPipePart;

public class ResourceIdBlock extends ResourceId {

    public BlockPos pos = BlockPos.ZERO;
    public EnumPipePart side = EnumPipePart.CENTER;

    public ResourceIdBlock() {

    }

    public ResourceIdBlock(int x, int y, int z) {
        pos = new BlockPos(x, y, z);
    }

    public ResourceIdBlock(BlockPos iIndex) {
        pos = iIndex;
    }

    public ResourceIdBlock(BlockEntity tile) {
        pos = tile.getBlockPos();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        ResourceIdBlock compareId = (ResourceIdBlock) obj;

        return pos.equals(compareId.pos) && side == compareId.side;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pos, side != null ? side.ordinal() : 0);
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        super.writeToNBT(nbt);
        nbt.putIntArray("pos", new int[] { pos.getX(), pos.getY(), pos.getZ() });
        nbt.put("side", side.writeToNBT());
    }

    @Override
    protected void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
        int[] arr = nbt.getIntArray("pos");
        if (arr.length == 3) {
            pos = new BlockPos(arr[0], arr[1], arr[2]);
        }
        side = EnumPipePart.readFromNBT(nbt.get("side"));
    }
}
