/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.tiles;

/** This interface should be implemented by any Tile Entity which carries out work (crafting, ore processing, mining, et
 * cetera). */
public interface IHasWork {
    /** Check if the Tile Entity is currently doing any work.
     *
     * @return True if the Tile Entity is doing work. */
    boolean hasWork();
}
