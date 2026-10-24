/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.lib.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

import net.neoforged.neoforge.fluids.BaseFlowingFluid;

/** BuildCraft's flowing fluids. When they meet a different fluid, the denser one goes underneath: oil (density 900)
 * floats on water (1000) instead of flowing down through it, and residue (1200) sinks. Without this, any mod fluid
 * replaces water below it, which turned every ocean oil slick into curtains of oil reaching the sea floor. */
public final class BCFlowingFluid {
    private BCFlowingFluid() {}

    /** @return true if {@code moving} may take the place of {@code still}, which is a different fluid. */
    static boolean canDisplace(Fluid moving, Fluid still) {
        return moving.getFluidType().getDensity() > still.getFluidType().getDensity();
    }

    static boolean canSpreadInto(Fluid self, FluidState into) {
        return into.isEmpty() || into.getType().isSame(self) || canDisplace(self, into.getType());
    }

    public static class Source extends BaseFlowingFluid.Source {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        protected boolean canSpreadTo(BlockGetter level, BlockPos fromPos, BlockState fromBlockState,
            Direction direction, BlockPos toPos, BlockState toBlockState, FluidState toFluidState, Fluid fluid) {
            return canSpreadInto(this, toFluidState)
                && super.canSpreadTo(level, fromPos, fromBlockState, direction, toPos, toBlockState, toFluidState, fluid);
        }

        @Override
        public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn,
            Direction direction) {
            return super.canBeReplacedWith(state, level, pos, fluidIn, direction) && canDisplace(fluidIn, this);
        }
    }

    public static class Flowing extends BaseFlowingFluid.Flowing {
        public Flowing(Properties properties) {
            super(properties);
        }

        @Override
        protected boolean canSpreadTo(BlockGetter level, BlockPos fromPos, BlockState fromBlockState,
            Direction direction, BlockPos toPos, BlockState toBlockState, FluidState toFluidState, Fluid fluid) {
            return canSpreadInto(this, toFluidState)
                && super.canSpreadTo(level, fromPos, fromBlockState, direction, toPos, toBlockState, toFluidState, fluid);
        }

        @Override
        public boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid fluidIn,
            Direction direction) {
            return super.canBeReplacedWith(state, level, pos, fluidIn, direction) && canDisplace(fluidIn, this);
        }
    }
}
