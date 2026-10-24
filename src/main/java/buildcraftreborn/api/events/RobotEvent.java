/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import buildcraftreborn.api.robots.EntityRobotBase;

/** Fired on {@link net.neoforged.neoforge.common.NeoForge#EVENT_BUS} when a player places, uses or dismantles a
 * robot. Cancel it to stop that happening. */
public abstract class RobotEvent extends Event {
    public final EntityRobotBase robot;

    public RobotEvent(EntityRobotBase robot) {
        this.robot = robot;
    }

    public static class Place extends RobotEvent implements ICancellableEvent {
        public final Player player;

        public Place(EntityRobotBase robot, Player player) {
            super(robot);
            this.player = player;
        }
    }

    public static class Interact extends RobotEvent implements ICancellableEvent {
        public final Player player;
        public final ItemStack item;

        public Interact(EntityRobotBase robot, Player player, ItemStack item) {
            super(robot);
            this.player = player;
            this.item = item;
        }
    }

    public static class Dismantle extends RobotEvent implements ICancellableEvent {
        public final Player player;

        public Dismantle(EntityRobotBase robot, Player player) {
            super(robot);
            this.player = player;
        }
    }
}
