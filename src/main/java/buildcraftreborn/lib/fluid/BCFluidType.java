/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.fluid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/** The {@link FluidType} of a {@link BCFluid}. Adds the heat level to the name ("Oil (Hot)") like 1.12's
 * {@code BCFluid.getLocalizedName}, and makes gaseous fluids vaporise when placed. */
public class BCFluidType extends FluidType {
    public final BCFluid fluid;

    public BCFluidType(BCFluid fluid, Properties properties) {
        super(properties);
        this.fluid = fluid;
    }

    @Override
    public Component getDescription() {
        Component bare = Component.translatable(getDescriptionId());
        if (fluid.heat <= 0 && !fluid.heatable) {
            return bare;
        }
        return Component.translatable("buildcraft.fluid.heat_" + fluid.heat, bare);
    }

    @Override
    public Component getDescription(FluidStack stack) {
        return getDescription();
    }

    @Override
    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        return isLighterThanAir();
    }

    @Override
    public void onVaporize(@org.jetbrains.annotations.Nullable net.minecraft.world.entity.player.Player player,
        Level level, BlockPos pos, FluidStack stack) {
        level.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
            2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);
        for (int i = 0; i < 8; i++) {
            level.addParticle(ParticleTypes.CLOUD, pos.getX() + Math.random(), pos.getY() + Math.random(),
                pos.getZ() + Math.random(), 0.0, 0.05, 0.0);
        }
    }
}
