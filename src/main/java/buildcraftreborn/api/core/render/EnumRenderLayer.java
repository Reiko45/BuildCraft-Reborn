/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core.render;

/** The render layers BuildCraft models are split into. 1.12 used Minecraft's {@code BlockRenderLayer}, which was a
 * common class; 1.21's {@code RenderType} is client-only, so common code uses this and the client maps it. */
public enum EnumRenderLayer {
    SOLID,
    CUTOUT_MIPPED,
    CUTOUT,
    TRANSLUCENT;

    public static final EnumRenderLayer[] VALUES = values();
}
