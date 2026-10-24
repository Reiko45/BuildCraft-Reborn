/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.registries.DeferredItem;

import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.item.ItemPluggableSimple;
import buildcraftreborn.transport.item.ItemPipeHolder;
import buildcraftreborn.transport.item.ItemWire;

/** Transport items: one item per pipe type, the plugs, the 16 wires and waterproof.
 * <p>
 * Registry names are 1.12's ({@code pipe_cobble_item}, {@code plug_blocker}...), except the wires: 1.12 had one
 * {@code wire} item with a colour per metadata value, here there is one item per colour ({@code wire_red}...). */
public final class BCTransportItems {
    public static final DeferredItem<Item> WATERPROOF = BCRegistries.ITEMS.registerSimpleItem("waterproof");

    public static final DeferredItem<ItemPluggableSimple> PLUG_BLOCKER = BCRegistries.ITEMS.register("plug_blocker",
        () -> new ItemPluggableSimple(new Item.Properties(), () -> BCTransportPlugs.blocker));
    public static final DeferredItem<ItemPluggableSimple> PLUG_POWER_ADAPTOR = BCRegistries.ITEMS.register(
        "plug_power_adaptor", () -> new ItemPluggableSimple(new Item.Properties(), () -> BCTransportPlugs.powerAdaptor,
            ItemPluggableSimple.PIPE_BEHAVIOUR_ACCEPTS_RS_POWER));

    public static final Map<DyeColor, DeferredItem<ItemWire>> WIRES = new EnumMap<>(DyeColor.class);

    /** Every pipe item, in creative tab order. Filled by {@link #init()}. */
    public static final Map<PipeDefinition, DeferredItem<ItemPipeHolder>> PIPES = new LinkedHashMap<>();
    private static final List<DeferredItem<? extends Item>> PLUG_TAB = new ArrayList<>();

    static {
        for (DyeColor colour : DyeColor.values()) {
            WIRES.put(colour, BCRegistries.ITEMS.register("wire_" + colour.getSerializedName(),
                () -> new ItemWire(colour, new Item.Properties())));
        }
    }

    private BCTransportItems() {}

    /** Registers the pipe items. Called once every pipe definition exists ({@link BCTransportPipes#init()}). */
    static void init() {
        // In order of type -- item, fluid, power, then FE (1.12's RF) -- as in 1.12
        makePipeItem(BCTransportPipes.structure);

        makePipeItem(BCTransportPipes.woodItem);
        makePipeItem(BCTransportPipes.cobbleItem);
        makePipeItem(BCTransportPipes.stoneItem);
        makePipeItem(BCTransportPipes.quartzItem);
        makePipeItem(BCTransportPipes.ironItem);
        makePipeItem(BCTransportPipes.goldItem);
        makePipeItem(BCTransportPipes.clayItem);
        makePipeItem(BCTransportPipes.sandstoneItem);
        makePipeItem(BCTransportPipes.voidItem);
        makePipeItem(BCTransportPipes.obsidianItem);
        makePipeItem(BCTransportPipes.diamondItem);
        makePipeItem(BCTransportPipes.diaWoodItem);
        makePipeItem(BCTransportPipes.lapisItem);
        makePipeItem(BCTransportPipes.daizuliItem);
        makePipeItem(BCTransportPipes.emzuliItem);
        makePipeItem(BCTransportPipes.stripesItem);

        makePipeItem(BCTransportPipes.woodFluid);
        makePipeItem(BCTransportPipes.cobbleFluid);
        makePipeItem(BCTransportPipes.stoneFluid);
        makePipeItem(BCTransportPipes.quartzFluid);
        makePipeItem(BCTransportPipes.goldFluid);
        makePipeItem(BCTransportPipes.ironFluid);
        makePipeItem(BCTransportPipes.clayFluid);
        makePipeItem(BCTransportPipes.sandstoneFluid);
        makePipeItem(BCTransportPipes.voidFluid);
        makePipeItem(BCTransportPipes.diamondFluid);
        makePipeItem(BCTransportPipes.diaWoodFluid);
        // 1.12 didn't register an item for the obsidian fluid pipe either

        makePipeItem(BCTransportPipes.woodPower);
        makePipeItem(BCTransportPipes.cobblePower);
        makePipeItem(BCTransportPipes.stonePower);
        makePipeItem(BCTransportPipes.quartzPower);
        makePipeItem(BCTransportPipes.ironPower);
        makePipeItem(BCTransportPipes.goldPower);
        makePipeItem(BCTransportPipes.sandstonePower);
        makePipeItem(BCTransportPipes.diamondPower);
        makePipeItem(BCTransportPipes.diaWoodPower);

        makePipeItem(BCTransportPipes.woodRf);
        makePipeItem(BCTransportPipes.cobbleRf);
        makePipeItem(BCTransportPipes.stoneRf);
        makePipeItem(BCTransportPipes.quartzRf);
        makePipeItem(BCTransportPipes.ironRf);
        makePipeItem(BCTransportPipes.goldRf);
        makePipeItem(BCTransportPipes.sandstoneRf);
        makePipeItem(BCTransportPipes.diamondRf);
        makePipeItem(BCTransportPipes.diaWoodRf);

        addToPlugTab(PLUG_BLOCKER);
        addToPlugTab(PLUG_POWER_ADAPTOR);
        for (DeferredItem<ItemWire> wire : WIRES.values()) {
            addToPlugTab(wire);
        }
    }

    /** The registry name of a pipe's item: 1.12's names shorten "cobblestone" to "cobble". */
    public static String pipeItemName(PipeDefinition definition) {
        return "pipe_" + definition.identifier.getPath().replace("cobblestone_", "cobble_");
    }

    private static void makePipeItem(PipeDefinition def) {
        DeferredItem<ItemPipeHolder> item = BCRegistries.ITEMS.register(pipeItemName(def),
            () -> new ItemPipeHolder(BCTransportBlocks.PIPE_HOLDER.get(), def, new Item.Properties())
                .registerWithPipeApi());
        BCRegistries.NOT_IN_MAIN_TAB.add(item.getId());
        PIPES.put(def, item);
    }

    /** Adds an item to the plugs creative tab (and takes it out of the main one). */
    public static void addToPlugTab(DeferredItem<? extends Item> item) {
        BCRegistries.NOT_IN_MAIN_TAB.add(item.getId());
        PLUG_TAB.add(item);
    }

    public static List<DeferredItem<? extends Item>> getPlugTabItems() {
        return Collections.unmodifiableList(PLUG_TAB);
    }
}
