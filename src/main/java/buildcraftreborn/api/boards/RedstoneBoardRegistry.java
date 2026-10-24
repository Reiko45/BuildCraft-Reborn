/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.boards;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

public abstract class RedstoneBoardRegistry {

    public static RedstoneBoardRegistry instance;

    /** Register a redstone board type.
     * 
     * @param redstoneBoardNBT The RedstoneBoardNBT instance containing the board information.
     * @param microJoules MJ price of programming the board, in micro MJ. */
    public abstract void registerBoardType(RedstoneBoardNBT<?> redstoneBoardNBT, long microJoules);

    public abstract void setEmptyRobotBoard(RedstoneBoardRobotNBT redstoneBoardNBT);

    public abstract RedstoneBoardRobotNBT getEmptyRobotBoard();

    /** @return The board with the given id, or the empty robot board if there is none. */
    public abstract RedstoneBoardNBT<?> getRedstoneBoard(@Nullable String id);

    /** @return Every registered board, apart from the empty one, in the order they were registered. */
    public abstract Collection<RedstoneBoardNBT<?>> getAllBoardNBTs();

    /** @return The power (in micro MJ) needed to program a blank board into this one. */
    public abstract long getPowerCost(RedstoneBoardNBT<?> board);
}
