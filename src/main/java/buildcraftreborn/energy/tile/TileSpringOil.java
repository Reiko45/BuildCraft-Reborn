/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.tile;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.core.tile.ITileOilSpring;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.lib.misc.AdvancementUtil;

/** The tile of an oil spring, which sits under the deep shaft of a large oil well and remembers how big the well was.
 * Pumps tell it when they pump oil; a player who pumps nearly all of a well's oil from the block above the spring gets
 * the "black gold" advancement (added with the pump). */
// We don't extend TileBC here because we have no need of any of its functions.
public class TileSpringOil extends BlockEntity implements IDebuggable, ITileOilSpring {

    private static final ResourceLocation ADVANCEMENT_PUMP_LARGE_OIL_WELL = BuildCraftReborn.id("black_gold");

    private final Map<UUID, PlayerPumpInfo> pumpProgress = new ConcurrentHashMap<>();

    /** An approximation of the total number of oil source blocks in the oil spring. The actual number will be less than
     * this, so this is taken as an approximation.
     * <p>
     * Note that this SHOULD NEVER be set! (Except by the generator, and loading) */
    public int totalSources;

    public TileSpringOil(BlockPos pos, BlockState state) {
        super(BCEnergyBlocks.OIL_SPRING_TILE.get(), pos, state);
    }

    @Override
    public void onPumpOil(GameProfile profile, BlockPos oilPos) {
        if (profile == null || level == null) {
            return;
        }
        PlayerPumpInfo info = pumpProgress.computeIfAbsent(profile.getId(), id -> new PlayerPumpInfo(profile));
        info.lastPumpTick = level.getGameTime();
        info.sourcesPumped++;
        setChanged();

        if (info.sourcesPumped >= totalSources * 7 / 8) {
            if (oilPos.equals(getBlockPos().above())) {
                AdvancementUtil.unlockAdvancement(profile.getId(), ADVANCEMENT_PUMP_LARGE_OIL_WELL);
            }
        }
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        // 1.12 saved totalSources but never read it back, so every well looked empty after a reload.
        totalSources = nbt.getInt("totalSources");
        pumpProgress.clear();
        ListTag list = nbt.getList("pumpProgress", Tag.TAG_COMPOUND);
        for (int i = 0; i < list.size(); i++) {
            PlayerPumpInfo info = PlayerPumpInfo.read(list.getCompound(i));
            if (info != null) {
                pumpProgress.put(info.id, info);
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putInt("totalSources", totalSources);
        ListTag list = new ListTag();
        for (PlayerPumpInfo info : pumpProgress.values()) {
            list.add(info.write());
        }
        nbt.put("pumpProgress", list);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("totalSources = " + totalSources);
        boolean added = false;
        for (PlayerPumpInfo info : pumpProgress.values()) {
            if (!added) {
                left.add("Player Progress:");
                added = true;
            }
            long ago = level == null ? 0 : (level.getGameTime() - info.lastPumpTick) / 20;
            left.add("  " + info.name + " = " + info.sourcesPumped + " ( " + ago + "s )");
        }
    }

    static class PlayerPumpInfo {
        final UUID id;
        final String name;
        long lastPumpTick = -1;
        int sourcesPumped = 0;

        PlayerPumpInfo(GameProfile profile) {
            this(profile.getId(), profile.getName());
        }

        private PlayerPumpInfo(UUID id, String name) {
            this.id = id;
            this.name = name;
        }

        @Nullable
        static PlayerPumpInfo read(CompoundTag nbt) {
            if (!nbt.hasUUID("id")) {
                return null;
            }
            PlayerPumpInfo info = new PlayerPumpInfo(nbt.getUUID("id"), nbt.getString("name"));
            info.lastPumpTick = nbt.getLong("lastPumpTick");
            info.sourcesPumped = nbt.getInt("sourcesPumped");
            return info;
        }

        CompoundTag write() {
            CompoundTag nbt = new CompoundTag();
            nbt.putUUID("id", id);
            nbt.putString("name", name);
            nbt.putLong("lastPumpTick", lastPumpTick);
            nbt.putInt("sourcesPumped", sourcesPumped);
            return nbt;
        }
    }
}
