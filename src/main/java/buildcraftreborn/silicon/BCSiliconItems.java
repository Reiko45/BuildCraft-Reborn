/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.registries.DeferredItem;

import buildcraftreborn.api.enums.EnumRedstoneChipset;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.item.ItemPluggableSimple;
import buildcraftreborn.silicon.gate.GateVariant;
import buildcraftreborn.silicon.plug.PluggablePulsar;
import buildcraftreborn.silicon.plug.FacadeInstance;
import buildcraftreborn.silicon.item.ItemGateCopier;
import buildcraftreborn.silicon.item.ItemPluggableFacade;
import buildcraftreborn.silicon.item.ItemPluggableGate;
import buildcraftreborn.silicon.item.ItemPluggableLens;
import buildcraftreborn.silicon.item.ItemPluggableLens.LensData;

/** Silicon items: the chipsets, gates, lenses and filters, the pulsar, timer and light sensor plugs, and the gate
 * copier. Their variants are data components ({@link #GATE_VARIANT}, {@link #LENS}). */
public final class BCSiliconItems {
    /** Which gate a gate item is (1.12: the {@code gate} NBT tag). */
    public static final Supplier<DataComponentType<GateVariant>> GATE_VARIANT = BCRegistries.DATA_COMPONENTS
        .registerComponentType("gate", b -> b.persistent(GateVariant.CODEC)
            .networkSynchronized(GateVariant.STREAM_CODEC));
    /** The colour and kind of a lens item (1.12: its metadata). */
    public static final Supplier<DataComponentType<LensData>> LENS = BCRegistries.DATA_COMPONENTS
        .registerComponentType("lens", b -> b.persistent(LensData.CODEC).networkSynchronized(LensData.STREAM_CODEC));
    /** The gate settings a gate copier holds (1.12: its {@code gate_data} NBT tag). */
    public static final Supplier<DataComponentType<CompoundTag>> GATE_DATA = BCRegistries.DATA_COMPONENTS
        .registerComponentType("gate_data", b -> b.persistent(CompoundTag.CODEC)
            .networkSynchronized(ByteBufCodecs.COMPOUND_TAG));

    /** What a facade item looks like (1.12: its {@code facade} NBT tag). */
    public static final Supplier<DataComponentType<FacadeInstance>> FACADE = BCRegistries.DATA_COMPONENTS
        .registerComponentType("facade", b -> b.persistent(FacadeInstance.CODEC)
            .networkSynchronized(FacadeInstance.STREAM_CODEC.cast()));

    /** 1.21: one item per chipset ({@code redstone_iron_chipset}...); 1.12 had one item with metadata. */
    public static final Map<EnumRedstoneChipset, DeferredItem<Item>> CHIPSETS = new EnumMap<>(
        EnumRedstoneChipset.class);

    public static final DeferredItem<ItemGateCopier> GATE_COPIER = BCRegistries.ITEMS.register("gate_copier",
        () -> new ItemGateCopier(new Item.Properties()));
    public static final DeferredItem<ItemPluggableGate> PLUG_GATE = BCRegistries.ITEMS.register("plug_gate",
        () -> new ItemPluggableGate(new Item.Properties()));
    public static final DeferredItem<ItemPluggableLens> PLUG_LENS = BCRegistries.ITEMS.register("plug_lens",
        () -> new ItemPluggableLens(new Item.Properties()));
    public static final DeferredItem<ItemPluggableSimple> PLUG_PULSAR = BCRegistries.ITEMS.register("plug_pulsar",
        () -> new ItemPluggableSimple(new Item.Properties(), () -> BCSiliconPlugs.pulsar, PluggablePulsar::new,
            ItemPluggableSimple.PIPE_BEHAVIOUR_ACCEPTS_RS_POWER));
    public static final DeferredItem<ItemPluggableSimple> PLUG_LIGHT_SENSOR = BCRegistries.ITEMS.register(
        "plug_light_sensor", () -> new ItemPluggableSimple(new Item.Properties(), () -> BCSiliconPlugs.lightSensor));
    public static final DeferredItem<ItemPluggableFacade> PLUG_FACADE = BCRegistries.ITEMS.register("plug_facade",
        () -> new ItemPluggableFacade(new Item.Properties()));
    public static final DeferredItem<ItemPluggableSimple> PLUG_TIMER = BCRegistries.ITEMS.register("plug_timer",
        () -> new ItemPluggableSimple(new Item.Properties(), () -> BCSiliconPlugs.timer));

    static {
        for (EnumRedstoneChipset chipset : EnumRedstoneChipset.VALUES) {
            CHIPSETS.put(chipset, BCRegistries.ITEMS.registerSimpleItem("redstone_" + chipset.getSerializedName()
                + "_chipset"));
        }
    }

    private BCSiliconItems() {}

    static void init() {
        EnumRedstoneChipset.itemGetter = chipset -> CHIPSETS.get(chipset).get();
    }
}
