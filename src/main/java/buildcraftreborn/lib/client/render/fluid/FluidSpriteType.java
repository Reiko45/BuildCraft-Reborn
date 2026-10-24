/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.render.fluid;

/** Determines what sprite should be used for rendering fluids. */
public enum FluidSpriteType {
    /** A sprite with no animation. Useful if you need to show fluid moving around, and this stops the normal fluid
     * animation being distracting. In 1.12 this was a separately stitched copy of {@link #STILL}, double its size;
     * here it draws the still sprite and allows texture coordinates past the block edge (see
     * {@link FluidRenderer}). */
    FROZEN,
    /** The fluid's still texture. */
    STILL,
    /** The fluid's flowing texture. */
    FLOWING
}
