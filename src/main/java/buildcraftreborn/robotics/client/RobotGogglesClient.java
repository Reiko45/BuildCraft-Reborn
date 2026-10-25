/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import buildcraftreborn.robotics.item.ItemRobotGoggles;

/** Whether the player on this client wears robot goggles (the unfinished robotics' {@code isWearingGlasses}). Only
 * call this on the client. */
public final class RobotGogglesClient {
    private RobotGogglesClient() {}

    public static boolean isWorn() {
        Player player = Minecraft.getInstance().player;
        return player != null && ItemRobotGoggles.isWornBy(player);
    }
}
