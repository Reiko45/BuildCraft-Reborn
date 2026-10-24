/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.recipe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.IRefineryRecipeManager;
import buildcraftreborn.lib.fluid.BCFluidDataMaps;
import buildcraftreborn.lib.fluid.BCFluidDataMaps.DistillationData;
import buildcraftreborn.lib.fluid.BCFluidDataMaps.HeatExchangeData;

/** Distiller and heat exchanger recipes. Each registry looks at its data map first ({@code distillation},
 * {@code heatable} and {@code coolable}, see {@link BCFluidDataMaps}), then at recipes added in code. Recipes from the
 * data maps can't be removed with {@link IRefineryRegistry#removeRecipes(Predicate)}; a data pack changes those. */
public enum RefineryRecipeRegistry implements IRefineryRecipeManager {
    INSTANCE;

    public final IRefineryRegistry<IDistillationRecipe> distillationRegistry =
        new SingleRegistry<>(BCFluidDataMaps.DISTILLATION, RefineryRecipeRegistry::fromData);
    public final IRefineryRegistry<IHeatableRecipe> heatableRegistry =
        new SingleRegistry<>(BCFluidDataMaps.HEATABLE, (fluid, data) -> new HeatableRecipe(
            new FluidStack(fluid, data.amount()), data.result().orElse(FluidStack.EMPTY), data.heatFrom(),
            data.heatTo()));
    public final IRefineryRegistry<ICoolableRecipe> coolableRegistry =
        new SingleRegistry<>(BCFluidDataMaps.COOLABLE, (fluid, data) -> new CoolableRecipe(
            new FluidStack(fluid, data.amount()), data.result().orElse(FluidStack.EMPTY), data.heatFrom(),
            data.heatTo()));

    private static IDistillationRecipe fromData(Holder<Fluid> fluid, DistillationData data) {
        return new DistillationRecipe(Math.round(data.power() * MjAPI.MJ), new FluidStack(fluid, data.amount()),
            data.gas(), data.liquid());
    }

    @Override
    public IHeatableRecipe createHeatingRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
        return new HeatableRecipe(in, out, heatFrom, heatTo);
    }

    @Override
    public ICoolableRecipe createCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
        return new CoolableRecipe(in, out, heatFrom, heatTo);
    }

    @Override
    public IDistillationRecipe createDistillationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid,
        long powerRequired) {
        return new DistillationRecipe(powerRequired, in, outGas, outLiquid);
    }

    @Override
    public IRefineryRegistry<IHeatableRecipe> getHeatableRegistry() {
        return heatableRegistry;
    }

    @Override
    public IRefineryRegistry<ICoolableRecipe> getCoolableRegistry() {
        return coolableRegistry;
    }

    @Override
    public IRefineryRegistry<IDistillationRecipe> getDistillationRegistry() {
        return distillationRegistry;
    }

    @FunctionalInterface
    interface RecipeFactory<R, D> {
        R create(Holder<Fluid> fluid, D data);
    }

    private static class SingleRegistry<R extends IRefineryRecipe, D> implements IRefineryRegistry<R> {
        private final DataMapType<Fluid, D> dataMap;
        private final RecipeFactory<R, D> factory;
        private final List<R> allRecipes = new LinkedList<>();

        SingleRegistry(DataMapType<Fluid, D> dataMap, RecipeFactory<R, D> factory) {
            this.dataMap = dataMap;
            this.factory = factory;
        }

        @Override
        public Stream<R> getRecipes(Predicate<R> filter) {
            return getAllRecipes().stream().filter(filter);
        }

        @Override
        public Collection<R> getAllRecipes() {
            List<R> all = new ArrayList<>();
            for (Map.Entry<ResourceKey<Fluid>, D> entry : BuiltInRegistries.FLUID.getDataMap(dataMap).entrySet()) {
                BuiltInRegistries.FLUID.getHolder(entry.getKey())
                    .ifPresent(fluid -> all.add(factory.create(fluid, entry.getValue())));
            }
            for (R recipe : allRecipes) {
                if (recipe.in().getFluidHolder().getData(dataMap) == null) {
                    all.add(recipe);
                }
            }
            return Collections.unmodifiableList(all);
        }

        @Override
        @Nullable
        public R getRecipeForInput(@Nullable FluidStack fluid) {
            if (fluid == null || fluid.isEmpty()) {
                return null;
            }
            D data = fluid.getFluidHolder().getData(dataMap);
            if (data != null) {
                return factory.create(fluid.getFluidHolder(), data);
            }
            for (R recipe : allRecipes) {
                if (FluidStack.isSameFluidSameComponents(recipe.in(), fluid)) {
                    return recipe;
                }
            }
            return null;
        }

        @Override
        public Collection<R> removeRecipes(Predicate<R> toRemove) {
            List<R> removed = new ArrayList<>();
            Iterator<R> iter = allRecipes.iterator();
            while (iter.hasNext()) {
                R recipe = iter.next();
                if (toRemove.test(recipe)) {
                    iter.remove();
                    removed.add(recipe);
                }
            }
            return removed;
        }

        @Override
        public R addRecipe(R recipe) {
            if (recipe == null) throw new NullPointerException("recipe");
            ListIterator<R> iter = allRecipes.listIterator();
            while (iter.hasNext()) {
                R existing = iter.next();
                if (FluidStack.isSameFluidSameComponents(existing.in(), recipe.in())) {
                    iter.set(recipe);
                    return recipe;
                }
            }
            allRecipes.add(recipe);
            return recipe;
        }
    }

    public static abstract class RefineryRecipe implements IRefineryRecipe {
        private final FluidStack in;

        public RefineryRecipe(FluidStack in) {
            this.in = in;
        }

        @Override
        public FluidStack in() {
            return in;
        }
    }

    public static class DistillationRecipe extends RefineryRecipe implements IDistillationRecipe {
        private final FluidStack outGas, outLiquid;
        private final long powerRequired;

        public DistillationRecipe(long powerRequired, FluidStack in, FluidStack outGas, FluidStack outLiquid) {
            super(in);
            this.powerRequired = powerRequired;
            this.outGas = outGas;
            this.outLiquid = outLiquid;
        }

        @Override
        public FluidStack outGas() {
            return outGas;
        }

        @Override
        public FluidStack outLiquid() {
            return outLiquid;
        }

        @Override
        public long powerRequired() {
            return powerRequired;
        }
    }

    public static abstract class HeatExchangeRecipe extends RefineryRecipe implements IHeatExchangerRecipe {
        private final FluidStack out;
        private final int heatFrom, heatTo;

        public HeatExchangeRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(in);
            this.out = out;
            this.heatFrom = heatFrom;
            this.heatTo = heatTo;
        }

        @Override
        public FluidStack out() {
            return out;
        }

        @Override
        public int heatFrom() {
            return heatFrom;
        }

        @Override
        public int heatTo() {
            return heatTo;
        }
    }

    public static class HeatableRecipe extends HeatExchangeRecipe implements IHeatableRecipe {
        public HeatableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(in, out, heatFrom, heatTo);
        }
    }

    public static class CoolableRecipe extends HeatExchangeRecipe implements ICoolableRecipe {
        public CoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo) {
            super(in, out, heatFrom, heatTo);
        }
    }
}
