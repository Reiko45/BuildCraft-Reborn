/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.util;

/** A search that is done a little at a time. */
public interface IIterableAlgorithm {

    /** Does one small step of the search. 1.21: the steps run on the server thread, so they are a tenth of 7.1's. */
    void iterate();

    boolean isDone();
}
