/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core;

/** An engine variant. In 1.12 this held the item model location for the metadata-based engine block; in 1.21 each
 * engine is its own block, so only the name is left. */
public interface IEngineType {
    /** @return The lower-case name, used for registry names ({@code engine_<name>}) and model variables. */
    String getEngineName();
}
