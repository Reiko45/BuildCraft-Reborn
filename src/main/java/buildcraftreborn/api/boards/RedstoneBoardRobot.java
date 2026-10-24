/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.boards;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;

public abstract class RedstoneBoardRobot extends AIRobot implements IRedstoneBoard<EntityRobotBase> {

    public RedstoneBoardRobot(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public abstract RedstoneBoardRobotNBT getNBTHandler();

    @Override
    public final void updateBoard(EntityRobotBase container) {

    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }
}
