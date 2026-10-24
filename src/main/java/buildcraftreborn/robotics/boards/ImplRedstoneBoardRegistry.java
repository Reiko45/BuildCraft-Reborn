/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.robotics.BCRoboticsConfig;

/** 1.21: boards are kept in the order they were registered. Boards in the {@code boards.blacklist} config are left
 * out of {@link #getAllBoardNBTs()} (so they can't be programmed) rather than not registered, as the config loads
 * after the boards register. */
public class ImplRedstoneBoardRegistry extends RedstoneBoardRegistry {
    private record BoardFactory(RedstoneBoardNBT<?> boardNBT, long powerCost) {}

    private final Map<String, BoardFactory> boards = new LinkedHashMap<>();
    private RedstoneBoardRobotNBT emptyRobotBoardNBT;

    @Override
    public void registerBoardType(RedstoneBoardNBT<?> redstoneBoardNBT, long microJoules) {
        boards.put(redstoneBoardNBT.getID(), new BoardFactory(redstoneBoardNBT, microJoules));
    }

    @Override
    public void setEmptyRobotBoard(RedstoneBoardRobotNBT redstoneBoardNBT) {
        emptyRobotBoardNBT = redstoneBoardNBT;
    }

    @Override
    public RedstoneBoardRobotNBT getEmptyRobotBoard() {
        return emptyRobotBoardNBT;
    }

    @Override
    public RedstoneBoardNBT<?> getRedstoneBoard(@Nullable String id) {
        BoardFactory factory = id == null ? null : boards.get(id);

        if (factory != null) {
            return factory.boardNBT;
        } else {
            return emptyRobotBoardNBT;
        }
    }

    @Override
    public Collection<RedstoneBoardNBT<?>> getAllBoardNBTs() {
        List<RedstoneBoardNBT<?>> result = new ArrayList<>();

        for (BoardFactory f : boards.values()) {
            if (!BCRoboticsConfig.isBoardBlacklisted(f.boardNBT.getID())) {
                result.add(f.boardNBT);
            }
        }

        return result;
    }

    /** @return Every registered board, including blacklisted ones (for models and textures). */
    public Collection<RedstoneBoardNBT<?>> getEveryBoardNBT() {
        List<RedstoneBoardNBT<?>> result = new ArrayList<>();
        for (BoardFactory f : boards.values()) {
            result.add(f.boardNBT);
        }
        return result;
    }

    @Override
    public long getPowerCost(RedstoneBoardNBT<?> board) {
        BoardFactory factory = boards.get(board.getID());
        return factory == null ? 0 : factory.powerCost;
    }
}
