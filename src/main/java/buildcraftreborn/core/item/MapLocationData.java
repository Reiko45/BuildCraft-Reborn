/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.item;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import buildcraftreborn.api.items.IMapLocation.MapLocationType;

/** What a map location remembers (1.12 kept these in item NBT, with the type as the damage value). Only the fields of
 * its type are used: {@code pos} and {@code side} for a spot, {@code min} and {@code max} for an area, {@code path}
 * for a path, and {@code zone} (a saved {@code ZonePlan}) for a zone. */
public record MapLocationData(MapLocationType type, String label, BlockPos pos, Direction side, BlockPos min,
    BlockPos max, List<BlockPos> path, CompoundTag zone) {

    public static final MapLocationData CLEAN = new MapLocationData(MapLocationType.CLEAN, "", BlockPos.ZERO,
        Direction.UP, BlockPos.ZERO, BlockPos.ZERO, List.of(), new CompoundTag());

    public static final Codec<MapLocationData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        MapLocationType.CODEC.optionalFieldOf("type", MapLocationType.CLEAN).forGetter(MapLocationData::type),
        Codec.STRING.optionalFieldOf("label", "").forGetter(MapLocationData::label),
        BlockPos.CODEC.optionalFieldOf("pos", BlockPos.ZERO).forGetter(MapLocationData::pos),
        Direction.CODEC.optionalFieldOf("side", Direction.UP).forGetter(MapLocationData::side),
        BlockPos.CODEC.optionalFieldOf("min", BlockPos.ZERO).forGetter(MapLocationData::min),
        BlockPos.CODEC.optionalFieldOf("max", BlockPos.ZERO).forGetter(MapLocationData::max),
        BlockPos.CODEC.listOf().optionalFieldOf("path", List.of()).forGetter(MapLocationData::path),
        CompoundTag.CODEC.optionalFieldOf("zone", new CompoundTag()).forGetter(MapLocationData::zone))
        .apply(instance, MapLocationData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, MapLocationData> STREAM_CODEC =
        ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public static MapLocationData spot(String label, BlockPos pos, Direction side) {
        return new MapLocationData(MapLocationType.SPOT, label, pos, side, BlockPos.ZERO, BlockPos.ZERO, List.of(),
            new CompoundTag());
    }

    public static MapLocationData area(String label, BlockPos min, BlockPos max) {
        return new MapLocationData(MapLocationType.AREA, label, BlockPos.ZERO, Direction.UP, min, max, List.of(),
            new CompoundTag());
    }

    public static MapLocationData path(String label, List<BlockPos> path, boolean repeating) {
        return new MapLocationData(repeating ? MapLocationType.PATH_REPEATING : MapLocationType.PATH, label,
            BlockPos.ZERO, Direction.UP, BlockPos.ZERO, BlockPos.ZERO, List.copyOf(path), new CompoundTag());
    }

    public static MapLocationData zone(String label, CompoundTag zone) {
        return new MapLocationData(MapLocationType.ZONE, label, BlockPos.ZERO, Direction.UP, BlockPos.ZERO,
            BlockPos.ZERO, List.of(), zone.copy());
    }

    public MapLocationData withLabel(String newLabel) {
        return new MapLocationData(type, newLabel, pos, side, min, max, path, zone);
    }
}
