/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.client.model;

import java.util.Objects;

import net.minecraft.core.Direction;

import buildcraftreborn.api.core.render.EnumRenderLayer;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;
import buildcraftreborn.robotics.station.PluggableRobotStation.EnumRobotStationState;

/** The model of a robot station: its side and state (the coloured lights). */
public final class KeyPlugRobotStation extends PluggableModelKey {
    public final EnumRobotStationState state;
    private final int hash;

    public KeyPlugRobotStation(Direction side, EnumRobotStationState state) {
        super(EnumRenderLayer.CUTOUT, side);
        this.state = state;
        this.hash = Objects.hash(super.hashCode(), state);
    }

    /** Factory for {@link #KeyPlugRobotStation(Direction, EnumRobotStationState)} that avoids class verification
     * errors. */
    public static PluggableModelKey create(Direction side, EnumRobotStationState state) {
        return new KeyPlugRobotStation(side, state);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj) && ((KeyPlugRobotStation) obj).state == state;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
