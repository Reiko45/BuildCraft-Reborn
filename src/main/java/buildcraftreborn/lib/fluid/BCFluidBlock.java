/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.energy.BCEnergyConfig;

/** The in-world block of a {@link BCFluid}. Flammable fluids catch fire (if enabled in the config) and sticky ones
 * slow entities down like cobwebs (if enabled in the config), as in 1.12. */
public class BCFluidBlock extends LiquidBlock {
    public final BCFluid definition;

    public BCFluidBlock(BCFluid definition, FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
        this.definition = definition;
    }

    private boolean canBurn() {
        return definition.flammable && BCEnergyConfig.enableOilBurn();
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return canBurn() ? 200 : 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return canBurn() ? 200 : 0;
    }

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return canBurn();
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (definition.sticky && BCEnergyConfig.oilIsSticky()) {
            // 1.12 used setInWeb()
            entity.makeStuckInBlock(state, new Vec3(0.25, 0.05F, 0.25));
        }
    }
}
