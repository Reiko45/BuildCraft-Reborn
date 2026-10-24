/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker.volume;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import buildcraftreborn.lib.misc.NBTUtilBC;

/** Something (a machine) using a volume box, and what it stops players from doing to it. */
public class Lock {
    public Cause cause;
    public List<Target> targets = new ArrayList<>();

    public Lock() {}

    public Lock(Cause cause, Target... targets) {
        this.cause = cause;
        this.targets.addAll(Arrays.asList(targets));
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        CompoundTag causeTag = new CompoundTag();
        causeTag.put("type", NBTUtilBC.writeEnum(Cause.EnumCause.getForClass(cause.getClass())));
        causeTag.put("data", cause.writeToNBT(new CompoundTag()));
        nbt.put("cause", causeTag);
        ListTag targetList = new ListTag();
        for (Target target : targets) {
            CompoundTag targetTag = new CompoundTag();
            targetTag.put("type", NBTUtilBC.writeEnum(Target.EnumTarget.getForClass(target.getClass())));
            targetTag.put("data", target.writeToNBT(new CompoundTag()));
            targetList.add(targetTag);
        }
        nbt.put("targets", targetList);
        return nbt;
    }

    /** @return false if the lock was unreadable (and should be dropped). */
    public boolean readFromNBT(CompoundTag nbt) {
        CompoundTag causeTag = nbt.getCompound("cause");
        Cause.EnumCause causeType = NBTUtilBC.readEnum(causeTag.get("type"), Cause.EnumCause.class);
        if (causeType == null) {
            return false;
        }
        cause = causeType.supplier.get();
        cause.readFromNBT(causeTag.getCompound("data"));
        ListTag targetList = nbt.getList("targets", Tag.TAG_COMPOUND);
        for (int i = 0; i < targetList.size(); i++) {
            CompoundTag targetTag = targetList.getCompound(i);
            Target.EnumTarget type = NBTUtilBC.readEnum(targetTag.get("type"), Target.EnumTarget.class);
            if (type == null) {
                continue;
            }
            Target target = type.supplier.get();
            target.readFromNBT(targetTag.getCompound("data"));
            targets.add(target);
        }
        return true;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeEnum(Cause.EnumCause.getForClass(cause.getClass()));
        cause.toBytes(buf);
        buf.writeInt(targets.size());
        targets.forEach(target -> {
            buf.writeEnum(Target.EnumTarget.getForClass(target.getClass()));
            target.toBytes(buf);
        });
    }

    public void fromBytes(FriendlyByteBuf buf) {
        cause = buf.readEnum(Cause.EnumCause.class).supplier.get();
        cause.fromBytes(buf);
        targets.clear();
        int count = buf.readInt();
        for (int i = 0; i < count; i++) {
            Target target = buf.readEnum(Target.EnumTarget.class).supplier.get();
            target.fromBytes(buf);
            targets.add(target);
        }
    }

    public static abstract class Cause {
        public abstract CompoundTag writeToNBT(CompoundTag nbt);

        public abstract void readFromNBT(CompoundTag nbt);

        public abstract void toBytes(FriendlyByteBuf buf);

        public abstract void fromBytes(FriendlyByteBuf buf);

        public abstract boolean stillWorks(Level level);

        public static class CauseBlock extends Cause {
            public BlockPos pos;
            public Block block;

            public CauseBlock() {}

            public CauseBlock(BlockPos pos, Block block) {
                this.pos = pos;
                this.block = block;
            }

            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                nbt.put("pos", NbtUtils.writeBlockPos(pos));
                nbt.putString("block", BuiltInRegistries.BLOCK.getKey(block).toString());
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
                pos = NbtUtils.readBlockPos(nbt, "pos").orElse(BlockPos.ZERO);
                ResourceLocation id = ResourceLocation.tryParse(nbt.getString("block"));
                block = id == null ? null : BuiltInRegistries.BLOCK.get(id);
            }

            @Override
            public void toBytes(FriendlyByteBuf buf) {
                buf.writeBlockPos(pos);
                buf.writeUtf(BuiltInRegistries.BLOCK.getKey(block).toString());
            }

            @Override
            public void fromBytes(FriendlyByteBuf buf) {
                pos = buf.readBlockPos();
                block = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(buf.readUtf(1024)));
            }

            @Override
            public boolean stillWorks(Level level) {
                return block != null && level.getBlockState(pos).getBlock() == block;
            }
        }

        enum EnumCause {
            BLOCK(CauseBlock::new);

            public final Supplier<? extends Cause> supplier;

            EnumCause(Supplier<? extends Cause> supplier) {
                this.supplier = supplier;
            }

            public static EnumCause getForClass(Class<? extends Cause> clazz) {
                return Arrays.stream(values())
                    .filter(enumCause -> enumCause.supplier.get().getClass() == clazz)
                    .findFirst()
                    .orElse(null);
            }
        }
    }

    public static abstract class Target {
        public abstract CompoundTag writeToNBT(CompoundTag nbt);

        public abstract void readFromNBT(CompoundTag nbt);

        public abstract void toBytes(FriendlyByteBuf buf);

        public abstract void fromBytes(FriendlyByteBuf buf);

        public static class TargetRemove extends Target {
            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {}

            @Override
            public void toBytes(FriendlyByteBuf buf) {}

            @Override
            public void fromBytes(FriendlyByteBuf buf) {}
        }

        public static class TargetResize extends Target {
            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {}

            @Override
            public void toBytes(FriendlyByteBuf buf) {}

            @Override
            public void fromBytes(FriendlyByteBuf buf) {}
        }

        public static class TargetAddon extends Target {
            public EnumAddonSlot slot;

            public TargetAddon() {}

            public TargetAddon(EnumAddonSlot slot) {
                this.slot = slot;
            }

            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                nbt.put("slot", NBTUtilBC.writeEnum(slot));
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
                slot = NBTUtilBC.readEnum(nbt.get("slot"), EnumAddonSlot.class);
            }

            @Override
            public void toBytes(FriendlyByteBuf buf) {
                buf.writeEnum(slot);
            }

            @Override
            public void fromBytes(FriendlyByteBuf buf) {
                slot = buf.readEnum(EnumAddonSlot.class);
            }
        }

        public static class TargetUsedByMachine extends Target {
            public EnumType type;

            public TargetUsedByMachine() {}

            public TargetUsedByMachine(EnumType type) {
                this.type = type;
            }

            @Override
            public CompoundTag writeToNBT(CompoundTag nbt) {
                nbt.put("type", NBTUtilBC.writeEnum(type));
                return nbt;
            }

            @Override
            public void readFromNBT(CompoundTag nbt) {
                type = NBTUtilBC.readEnum(nbt.get("type"), EnumType.class);
            }

            @Override
            public void toBytes(FriendlyByteBuf buf) {
                buf.writeEnum(type);
            }

            @Override
            public void fromBytes(FriendlyByteBuf buf) {
                type = buf.readEnum(EnumType.class);
            }

            /** 1.21: the laser type for each is picked by the client renderer. */
            public enum EnumType {
                STRIPES_WRITE,
                STRIPES_READ
            }
        }

        enum EnumTarget {
            REMOVE(TargetRemove::new),
            RESIZE(TargetResize::new),
            ADDON(TargetAddon::new),
            USED_BY_MACHINE(TargetUsedByMachine::new);

            public final Supplier<? extends Target> supplier;

            EnumTarget(Supplier<? extends Target> supplier) {
                this.supplier = supplier;
            }

            public static EnumTarget getForClass(Class<? extends Target> clazz) {
                return Arrays.stream(values())
                    .filter(enumTarget -> enumTarget.supplier.get().getClass() == clazz)
                    .findFirst()
                    .orElse(null);
            }
        }
    }
}
