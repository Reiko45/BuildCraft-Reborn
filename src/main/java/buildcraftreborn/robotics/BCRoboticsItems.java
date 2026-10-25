/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;

import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import buildcraftreborn.BuildCraftReborn;

import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.item.ItemPluggableSimple;
import buildcraftreborn.robotics.item.ItemRedstoneBoard;
import buildcraftreborn.robotics.item.ItemRobot;
import buildcraftreborn.robotics.item.ItemRobotGoggles;
import buildcraftreborn.robotics.station.PluggableRobotStation;

/** Robots, redstone boards, the robot station and the redstone crystal (7.1's robotics and silicon items), and the
 * robot goggles BuildCraft started after 7.1 and never finished. */
public final class BCRoboticsItems {
    /** The board of a redstone board or a robot (1.12: the board's {@code id} NBT tag). */
    public static final Supplier<DataComponentType<String>> BOARD = BCRegistries.DATA_COMPONENTS
        .registerComponentType("board", b -> b.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));
    /** The power (in micro MJ) a robot item holds (1.12: its {@code energy} NBT tag, in RF). */
    public static final Supplier<DataComponentType<Long>> ROBOT_POWER = BCRegistries.DATA_COMPONENTS
        .registerComponentType("robot_power", b -> b.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    public static final DeferredItem<ItemRobot> ROBOT = BCRegistries.ITEMS.register("robot",
        () -> new ItemRobot(new Item.Properties()));
    public static final DeferredItem<ItemRedstoneBoard> REDSTONE_BOARD = BCRegistries.ITEMS.register("redstone_board",
        () -> new ItemRedstoneBoard(new Item.Properties()));
    public static final DeferredItem<ItemPluggableSimple> ROBOT_STATION = BCRegistries.ITEMS.register("robot_station",
        () -> new ItemPluggableSimple(new Item.Properties(), () -> BCRoboticsPlugs.robotStation,
            PluggableRobotStation::new, null));
    public static final DeferredItem<Item> REDSTONE_CRYSTAL = BCRegistries.ITEMS.registerSimpleItem("redstone_crystal");

    /** The robot goggles' material: no protection, so nothing to repair them with, and they have no durability. What
     * they look like on a head is {@code textures/models/armor/robot_goggles_layer_1.png}. */
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> GOGGLES_MATERIAL = BCRegistries.ARMOR_MATERIALS
        .register("robot_goggles", () -> new ArmorMaterial(Map.of(ArmorItem.Type.HELMET, 0), 0,
            SoundEvents.ARMOR_EQUIP_LEATHER, () -> Ingredient.EMPTY,
            List.of(new ArmorMaterial.Layer(BuildCraftReborn.id("robot_goggles"))), 0, 0));
    public static final DeferredItem<ItemRobotGoggles> ROBOT_GOGGLES = BCRegistries.ITEMS.register("robot_goggles",
        () -> new ItemRobotGoggles(new Item.Properties().stacksTo(1)));

    private BCRoboticsItems() {}

    static void init() {}
}
