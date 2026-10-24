/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.statements;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableList;

import net.minecraft.network.chat.Component;


import buildcraftreborn.api.core.IConvertable;
import buildcraftreborn.api.core.render.ISprite;

public interface IGuiSlot extends IConvertable {
    /** Every statement needs a unique tag, it should be in the format of "&lt;modid&gt;:&lt;name&gt;".
     *
     * @return the unique id */
    String getUniqueTag();

    /** Return the description in the UI. Note that this should NEVER be called directly, instead this acts as a bridge
     * for {@link #getTooltip()}. (As such this might return null or throw an exception) */
    String getDescription();

    /** @return The full tooltip for the UI. (1.21: components, so item tooltips keep their styles; 1.12 used
     *         strings.) */
    default List<Component> getTooltip() {
        String desc = getDescription();
        if (desc == null) {
            return ImmutableList.of();
        }
        return ImmutableList.of(Component.literal(desc));
    }

    /** @return A sprite to show in a GUI, or null if this should not render a sprite. */
    @Nullable
    ISprite getSprite();
}
