/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.mj;

import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public interface IMjEffectManager {
    void createPowerLossEffect(Level world, Vec3 center, long microJoulesLost);

    void createPowerLossEffect(Level world, Vec3 center, Direction direction, long microJoulesLost);

    void createPowerLossEffect(Level world, Vec3 center, Vec3 direction, long microJoulesLost);
}
