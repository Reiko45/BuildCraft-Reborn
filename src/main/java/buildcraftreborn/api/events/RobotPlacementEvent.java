/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.events;

import net.minecraft.world.entity.player.Player;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/** Fired before a player places a robot on a station, with the id of the robot's board. Cancel it to stop the robot
 * being placed. */
public class RobotPlacementEvent extends Event implements ICancellableEvent {
    public final Player player;
    public final String robotProgram;

    public RobotPlacementEvent(Player player, String robotProgram) {
        this.player = player;
        this.robotProgram = robotProgram;
    }
}
