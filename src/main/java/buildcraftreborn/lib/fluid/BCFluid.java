/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.fluid;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.BCRegistries;

/** A BuildCraft fluid: its {@link FluidType}, source and flowing fluids, liquid block and bucket, registered together.
 * <p>
 * 1.12's {@code BCFluid} extended Forge's {@code Fluid}, which was both the type and the definition. In 1.21 those are
 * split, so this class is the definition (colours, heat, flow) and holds the registered objects.
 * <p>
 * Gaseous fluids (negative density) have no liquid block: in 1.12 they flowed upwards, which vanilla fluids can't
 * do, so here they vaporise when a bucket of them is emptied in the world. They still work in tanks and pipes. */
public final class BCFluid {
    /** Registry name, for example {@code oil} or {@code oil_heat_1}. */
    public final String name;
    /** The name without the heat suffix, used for the translation key ({@code fluid.buildcraftreborn.oil}). */
    public final String baseName;
    public final int heat;
    public final boolean heatable;
    public final int colourLight, colourDark;
    public final boolean flammable, sticky;
    public final int density, viscosity, temperature;
    /** 1.12's quanta per block: how far the fluid flows. */
    public final int quanta;
    public final MapColor mapColour;

    public final DeferredHolder<FluidType, BCFluidType> type;
    public final DeferredHolder<Fluid, BaseFlowingFluid.Source> source;
    public final DeferredHolder<Fluid, BaseFlowingFluid.Flowing> flowing;
    @Nullable
    public final DeferredBlock<BCFluidBlock> block;
    public final DeferredItem<BucketItem> bucket;

    private BCFluid(Builder b) {
        this.name = b.name;
        this.baseName = b.baseName;
        this.heat = b.heat;
        this.heatable = b.heatable;
        this.colourLight = b.colourLight;
        this.colourDark = b.colourDark;
        this.flammable = b.flammable;
        this.sticky = b.sticky;
        this.density = b.density;
        this.viscosity = b.viscosity;
        this.temperature = b.temperature;
        this.quanta = b.quanta;
        this.mapColour = b.mapColour;

        BCFluid def = this;
        type = BCRegistries.FLUID_TYPES.register(name, () -> new BCFluidType(def, FluidType.Properties.create()
            .descriptionId("fluid." + BuildCraftReborn.MODID + "." + baseName)
            .density(density)
            .viscosity(viscosity)
            .temperature(temperature)
            .canConvertToSource(false)
            .supportsBoating(false)
            .motionScale(0.007)
            .sound(net.neoforged.neoforge.common.SoundActions.BUCKET_FILL,
                net.minecraft.sounds.SoundEvents.BUCKET_FILL)
            .sound(net.neoforged.neoforge.common.SoundActions.BUCKET_EMPTY,
                net.minecraft.sounds.SoundEvents.BUCKET_EMPTY)));

        BaseFlowingFluid.Properties[] props = new BaseFlowingFluid.Properties[1];
        source = BCRegistries.FLUIDS.register(name, () -> new BCFlowingFluid.Source(props[0]));
        flowing = BCRegistries.FLUIDS.register(name + "_flowing", () -> new BCFlowingFluid.Flowing(props[0]));

        if (isGaseous()) {
            block = null;
        } else {
            block = BCRegistries.BLOCKS.register(name, () -> new BCFluidBlock(def, source.get(),
                BlockBehaviour.Properties.of()
                    .mapColor(mapColour)
                    .replaceable()
                    .noCollission()
                    .strength(100.0F)
                    .pushReaction(PushReaction.DESTROY)
                    .noLootTable()
                    .liquid()
                    .sound(SoundType.EMPTY)));
        }
        bucket = BCRegistries.ITEMS.register(name + "_bucket",
            () -> new BCBucketItem(def, new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));

        BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(type, source, flowing)
            .bucket(bucket)
            .slopeFindDistance(4)
            .levelDecreasePerBlock(quanta <= 5 ? 2 : 1)
            .tickRate(Math.max(1, viscosity / 200))
            .explosionResistance(100.0F);
        if (block != null) {
            properties.block(block);
        }
        props[0] = properties;
    }

    /** @return true if this fluid is lighter than air (BuildCraft's gaseous fuels). */
    public boolean isGaseous() {
        return density < 0;
    }

    public Block getBlockOrNull() {
        return block == null ? null : block.get();
    }

    public BaseFlowingFluid.Source getSource() {
        return source.get();
    }

    public static Builder builder(String baseName, int heat) {
        return new Builder(baseName, heat);
    }

    public static final class Builder {
        private final String name, baseName;
        private final int heat;
        private boolean heatable;
        private int colourLight = 0xFF_FF_FF, colourDark = 0xFF_FF_FF;
        private boolean flammable, sticky;
        private int density = 1000, viscosity = 1000, temperature = 300, quanta = 8;
        private MapColor mapColour = MapColor.COLOR_BLACK;

        private Builder(String baseName, int heat) {
            this.baseName = baseName;
            this.heat = heat;
            this.name = baseName + (heat == 0 ? "" : "_heat_" + heat);
        }

        public Builder heatable(boolean heatable) {
            this.heatable = heatable;
            return this;
        }

        public Builder colour(int light, int dark) {
            this.colourLight = light;
            this.colourDark = dark;
            return this;
        }

        public Builder flammable(boolean flammable) {
            this.flammable = flammable;
            return this;
        }

        public Builder sticky(boolean sticky) {
            this.sticky = sticky;
            return this;
        }

        public Builder density(int density) {
            this.density = density;
            return this;
        }

        public Builder viscosity(int viscosity) {
            this.viscosity = viscosity;
            return this;
        }

        public Builder temperature(int temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder quanta(int quanta) {
            this.quanta = quanta;
            return this;
        }

        public Builder mapColour(MapColor mapColour) {
            this.mapColour = mapColour;
            return this;
        }

        /** Registers the fluid's type, fluids, block and bucket. Call during mod construction. */
        public BCFluid register() {
            return new BCFluid(this);
        }
    }
}
