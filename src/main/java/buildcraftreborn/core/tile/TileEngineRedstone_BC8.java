/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.mj.IMjConnector;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.lib.engine.EngineConnector;
import buildcraftreborn.lib.engine.TileEngineBase_BC8;
import buildcraftreborn.lib.misc.AdvancementUtil;

/** The redstone engine (1.12 "wood engine"): free, tiny power, only for machines that take redstone-pulsed power. */
public class TileEngineRedstone_BC8 extends TileEngineBase_BC8 {
    private static final ResourceLocation ADVANCEMENT = BuildCraftReborn.id("free_power");

    private boolean givenAdvancement = false;

    public TileEngineRedstone_BC8(BlockPos pos, BlockState state) {
        super(BCCoreBlocks.ENGINE_WOOD_TILE.get(), pos, state);
    }

    @Override
    protected IMjConnector createConnector() {
        return new EngineConnector(true);
    }

    @Override
    protected boolean couldPowerRf() {
        return false;
    }

    @Override
    public boolean isBurning() {
        return isRedstonePowered;
    }

    @Override
    protected void engineUpdate() {
        super.engineUpdate();
        if (isRedstonePowered) {
            power = getMaxPower();
            if (level.getGameTime() % 16 == 0) {
                if (getHeatLevel() < 0.8) {
                    heat += 4;
                }
                if (isPumping && !givenAdvancement && hasOwner()) {
                    givenAdvancement = AdvancementUtil.unlockAdvancement(getOwner().getId(), ADVANCEMENT);
                }
            }
        } else {
            power = 0;
        }
    }

    @Override
    public double getPistonSpeed() {
        return super.getPistonSpeed() / 2;
    }

    @Override
    public void updateHeatLevel() {
        if (heat > MIN_HEAT) {
            heat -= 0.2f;
            if (heat < MIN_HEAT) {
                heat = MIN_HEAT;
            }
        }
    }

    @Override
    protected int getMaxChainLength() {
        return 0;
    }

    @Override
    public long getMaxPower() {
        return MjAPI.MJ * 1;
    }

    @Override
    public long minPowerReceived() {
        return MjAPI.MJ / 10;
    }

    @Override
    public long maxPowerReceived() {
        return 4 * MjAPI.MJ;
    }

    @Override
    public long maxPowerExtracted() {
        return 4 * MjAPI.MJ;
    }

    @Override
    public float explosionRange() {
        return 0;
    }

    @Override
    public long getCurrentOutput() {
        return MjAPI.MJ / 20;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.putBoolean("givenAdvancement", givenAdvancement);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        givenAdvancement = nbt.getBoolean("givenAdvancement");
    }
}
