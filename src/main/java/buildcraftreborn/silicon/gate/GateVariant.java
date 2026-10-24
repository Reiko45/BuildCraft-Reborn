/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.gate;

import java.util.Objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.lib.misc.StringUtilBC;

/** What kind of gate something is: its logic (and / or), material (how many slots) and modifier (how many
 * parameters). 1.21: also stored on gate items, as the {@code gate} data component ({@link #CODEC}). */
public class GateVariant {
    public static final Codec<GateVariant> CODEC = RecordCodecBuilder.create(i -> i.group(
        Codec.STRING.xmap(s -> EnumGateLogic.byTag(s), l -> l.tag).fieldOf("logic").forGetter(v -> v.logic),
        Codec.STRING.xmap(s -> EnumGateMaterial.byTag(s), m -> m.tag).fieldOf("material").forGetter(v -> v.material),
        Codec.STRING.xmap(s -> EnumGateModifier.byTag(s), m -> m.tag).fieldOf("modifier")
            .forGetter(v -> v.modifier)).apply(i, GateVariant::new));

    public static final StreamCodec<ByteBuf, GateVariant> STREAM_CODEC = StreamCodec.composite(
        ByteBufCodecs.BYTE, v -> (byte) v.logic.ordinal(),
        ByteBufCodecs.BYTE, v -> (byte) v.material.ordinal(),
        ByteBufCodecs.BYTE, v -> (byte) v.modifier.ordinal(),
        (l, m, mod) -> new GateVariant(EnumGateLogic.getByOrdinal(l), EnumGateMaterial.getByOrdinal(m),
            EnumGateModifier.getByOrdinal(mod)));

    public final EnumGateLogic logic;
    public final EnumGateMaterial material;
    public final EnumGateModifier modifier;
    public final int numSlots;
    public final int numTriggerArgs, numActionArgs;
    private final int hash;

    public GateVariant(EnumGateLogic logic, EnumGateMaterial material, EnumGateModifier modifier) {
        this.logic = logic;
        this.material = material;
        this.modifier = modifier;
        this.numSlots = material.numSlots / modifier.slotDivisor;
        this.numTriggerArgs = modifier.triggerParams;
        this.numActionArgs = modifier.actionParams;
        this.hash = Objects.hash(logic, material, modifier);
    }

    public GateVariant(CompoundTag nbt) {
        this.logic = EnumGateLogic.getByOrdinal(nbt.getByte("logic"));
        this.material = EnumGateMaterial.getByOrdinal(nbt.getByte("material"));
        this.modifier = EnumGateModifier.getByOrdinal(nbt.getByte("modifier"));
        this.numSlots = material.numSlots / modifier.slotDivisor;
        this.numTriggerArgs = modifier.triggerParams;
        this.numActionArgs = modifier.actionParams;
        this.hash = Objects.hash(logic, material, modifier);
    }

    public CompoundTag writeToNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putByte("logic", (byte) logic.ordinal());
        nbt.putByte("material", (byte) material.ordinal());
        nbt.putByte("modifier", (byte) modifier.ordinal());
        return nbt;
    }

    public GateVariant(FriendlyByteBuf buffer) {
        this.logic = EnumGateLogic.getByOrdinal(buffer.readUnsignedByte());
        this.material = EnumGateMaterial.getByOrdinal(buffer.readUnsignedByte());
        this.modifier = EnumGateModifier.getByOrdinal(buffer.readUnsignedByte());
        this.numSlots = material.numSlots / modifier.slotDivisor;
        this.numTriggerArgs = modifier.triggerParams;
        this.numActionArgs = modifier.actionParams;
        this.hash = Objects.hash(logic, material, modifier);
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeByte(logic.ordinal());
        buffer.writeByte(material.ordinal());
        buffer.writeByte(modifier.ordinal());
    }

    public String getVariantName() {
        if (material.canBeModified) {
            return material.tag + "_" + logic.tag + "_" + modifier.tag;
        } else {
            return material.tag;
        }
    }

    public String getLocalizedName() {
        if (material == EnumGateMaterial.CLAY_BRICK) {
            return LocaleUtil.localize("gate.name.basic");
        } else {
            String gateName = LocaleUtil.localize("gate.name");
            String materialName = LocaleUtil.localize("gate.material." + material.tag);
            Object logicName = LocaleUtil.localize("gate.logic." + logic.tag);
            return StringUtilBC.formatSafe(gateName, materialName, logicName);
        }
    }

    /** The gate's name, translated where it's shown (the server has no language). */
    public Component getDisplayName() {
        if (material == EnumGateMaterial.CLAY_BRICK) {
            return Component.translatable("gate.name.basic");
        }
        return Component.translatable("gate.name", Component.translatable("gate.material." + material.tag),
            Component.translatable("gate.logic." + logic.tag));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        GateVariant other = (GateVariant) obj;
        return other.logic == logic//
            && other.material == material//
            && other.modifier == modifier;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
