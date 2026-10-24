/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.boards;

import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.api.robots.EntityRobotBase;

public abstract class RedstoneBoardRobotNBT extends RedstoneBoardNBT<EntityRobotBase> {

    @Override
    public abstract RedstoneBoardRobot create(EntityRobotBase robot);

    /** @return The texture of a robot running this board (a full path, such as
     *         {@code buildcraftreborn:textures/entity/robot/picker.png}). */
    public abstract ResourceLocation getRobotTexture();
}
