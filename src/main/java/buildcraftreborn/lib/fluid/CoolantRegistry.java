/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.fluid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.fuels.ICoolant;
import buildcraftreborn.api.fuels.ICoolantManager;
import buildcraftreborn.api.fuels.ISolidCoolant;
import buildcraftreborn.lib.fluid.BCFluidDataMaps.CoolantData;
import buildcraftreborn.lib.fluid.BCFluidDataMaps.SolidCoolantData;

/** Combustion engine coolants: the {@link BCFluidDataMaps#COOLANTS} and {@link BCFluidDataMaps#SOLID_COOLANTS} data
 * maps first, then coolants added in code through {@link ICoolantManager}. */
public enum CoolantRegistry implements ICoolantManager {
    INSTANCE;

    private final List<ICoolant> coolants = new ArrayList<>();
    private final List<ISolidCoolant> solidCoolants = new ArrayList<>();

    @Override
    public ICoolant addCoolant(ICoolant coolant) {
        coolants.add(coolant);
        return coolant;
    }

    @Override
    public ISolidCoolant addSolidCoolant(ISolidCoolant solidCoolant) {
        solidCoolants.add(solidCoolant);
        return solidCoolant;
    }

    /** Removes a coolant added in code (so scripts can undo what they added). Data map coolants can only be changed
     * by data packs.
     *
     * @return True if it was there. */
    public boolean removeCoolant(ICoolant coolant) {
        return coolants.remove(coolant);
    }

    /** Removes a solid coolant added in code, as {@link #removeCoolant(ICoolant)}. */
    public boolean removeSolidCoolant(ISolidCoolant solidCoolant) {
        return solidCoolants.remove(solidCoolant);
    }

    @Override
    public ICoolant addCoolant(FluidStack fluid, float degreesCoolingPerMB) {
        return addCoolant(new Coolant(fluid, degreesCoolingPerMB));
    }

    @Override
    public ISolidCoolant addSolidCoolant(ItemStack solid, FluidStack fluid, float multiplier) {
        return addSolidCoolant(new SolidCoolant(solid, fluid, multiplier));
    }

    @Override
    public Collection<ICoolant> getCoolants() {
        List<ICoolant> all = new ArrayList<>();
        for (Map.Entry<ResourceKey<Fluid>, CoolantData> entry : BuiltInRegistries.FLUID
            .getDataMap(BCFluidDataMaps.COOLANTS).entrySet()) {
            BuiltInRegistries.FLUID.getHolder(entry.getKey()).ifPresent(
                fluid -> all.add(new Coolant(new FluidStack(fluid, 1), entry.getValue().degreesPerMb())));
        }
        all.addAll(coolants);
        return all;
    }

    @Override
    public Collection<ISolidCoolant> getSolidCoolants() {
        List<ISolidCoolant> all = new ArrayList<>();
        for (Map.Entry<ResourceKey<Item>, SolidCoolantData> entry : BuiltInRegistries.ITEM
            .getDataMap(BCFluidDataMaps.SOLID_COOLANTS).entrySet()) {
            BuiltInRegistries.ITEM.getHolder(entry.getKey()).ifPresent(item -> all
                .add(new SolidCoolant(new ItemStack(item), entry.getValue().fluid(), 1)));
        }
        all.addAll(solidCoolants);
        return all;
    }

    @Override
    @Nullable
    public ICoolant getCoolant(FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return null;
        }
        CoolantData data = fluid.getFluidHolder().getData(BCFluidDataMaps.COOLANTS);
        if (data != null) {
            return new Coolant(fluid.copyWithAmount(1), data.degreesPerMb());
        }
        for (ICoolant coolant : coolants) {
            if (coolant.matchesFluid(fluid)) {
                return coolant;
            }
        }
        return null;
    }

    @Override
    public float getDegreesPerMb(FluidStack fluid, float heat) {
        if (fluid == null || fluid.isEmpty()) {
            return 0;
        }
        CoolantData data = fluid.getFluidHolder().getData(BCFluidDataMaps.COOLANTS);
        if (data != null && data.degreesPerMb() > 0) {
            return data.degreesPerMb();
        }
        for (ICoolant coolant : coolants) {
            float degrees = coolant.getDegreesCoolingPerMB(fluid, heat);
            if (degrees > 0) {
                return degrees;
            }
        }
        return 0;
    }

    @Override
    @Nullable
    public ISolidCoolant getSolidCoolant(ItemStack solid) {
        if (solid.isEmpty()) {
            return null;
        }
        SolidCoolantData data = solid.getItemHolder().getData(BCFluidDataMaps.SOLID_COOLANTS);
        if (data != null) {
            return new SolidCoolant(solid.copyWithCount(1), data.fluid(), 1);
        }
        for (ISolidCoolant coolant : solidCoolants) {
            if (coolant.getFluidFromSolidCoolant(solid) != null) {
                return coolant;
            }
        }
        return null;
    }

    public static class Coolant implements ICoolant {
        private final FluidStack fluid;
        private final float degreesCoolingPerMB;

        public Coolant(FluidStack fluid, float degreesCoolingPerMB) {
            this.fluid = fluid;
            this.degreesCoolingPerMB = degreesCoolingPerMB;
        }

        /** The coolant fluid (for recipe viewers). */
        public FluidStack getFluid() {
            return fluid;
        }

        /** How many degrees each mB cools an engine by (for recipe viewers). */
        public float getDegreesCoolingPerMB() {
            return degreesCoolingPerMB;
        }

        @Override
        public boolean matchesFluid(FluidStack stack) {
            return FluidStack.isSameFluidSameComponents(fluid, stack);
        }

        @Override
        public float getDegreesCoolingPerMB(FluidStack stack, float heat) {
            return matchesFluid(stack) ? degreesCoolingPerMB : 0;
        }
    }

    public static class SolidCoolant implements ISolidCoolant {
        private final ItemStack solid;
        private final FluidStack fluid;
        private final float multiplier;

        public SolidCoolant(ItemStack solid, FluidStack fluid, float multiplier) {
            this.solid = solid;
            this.fluid = fluid;
            this.multiplier = multiplier;
        }

        /** The item (for recipe viewers). */
        public ItemStack getSolid() {
            return solid;
        }

        /** What {@link #getSolid()} melts into (for recipe viewers). */
        public FluidStack getMeltedFluid() {
            return fluid.copyWithAmount((int) (fluid.getAmount() * multiplier));
        }

        @Override
        @Nullable
        public FluidStack getFluidFromSolidCoolant(ItemStack stack) {
            if (stack == null || !ItemStack.isSameItem(stack, solid)) {
                return null;
            }
            int liquidAmount = (int) (stack.getCount() * fluid.getAmount() * multiplier / solid.getCount());
            return fluid.copyWithAmount(liquidAmount);
        }
    }
}
