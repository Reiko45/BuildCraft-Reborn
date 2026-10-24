/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.core.BCCoreSprites;
import buildcraftreborn.lib.client.sprite.SpriteGui;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

/** The transport module's GUI icons: gate statements and the filtered buffer's slot backgrounds.
 * <p>
 * 1.21: these are in the GUI sprite atlas ({@code textures/gui/sprites/}). The sprites drawn in the world (pipe
 * colour overlays, power flow) are block atlas textures, looked up by the client renderers. */
public final class BCTransportSprites {
    public static final ISprite EMPTY_FILTERED_BUFFER_SLOT = gui("transport/empty_filtered_buffer_slot");
    public static final ISprite NOTHING_FILTERED_BUFFER_SLOT = gui("transport/nothing_filtered_buffer_slot");

    public static final ISprite TRIGGER_POWER_REQUESTED = BCCoreSprites.get("trigger_pipecontents_requestsenergy");
    public static final ISprite TRIGGER_ITEMS_TRAVERSING = BCCoreSprites.get("trigger_pipecontents_containsitems");
    public static final ISprite TRIGGER_FLUIDS_TRAVERSING = BCCoreSprites.get("trigger_pipecontents_containsfluids");

    public static final ISprite[] ACTION_PIPE_COLOUR = new ISprite[16];
    public static final Map<SlotIndex, ISprite> ACTION_EXTRACTION_PRESET = new EnumMap<>(SlotIndex.class);
    private static final Map<DyeColor, ISprite> PIPE_SIGNAL_ON = new EnumMap<>(DyeColor.class);
    private static final Map<DyeColor, ISprite> PIPE_SIGNAL_OFF = new EnumMap<>(DyeColor.class);
    private static final Map<Direction, ISprite> ACTION_PIPE_DIRECTION = new EnumMap<>(Direction.class);

    public static final ISprite[] POWER_LIMIT = { //
        BCCoreSprites.get("trigger_limiter_m256"), // 0 shift (100%)
        BCCoreSprites.get("trigger_limiter_m128"), // 1 shift (50%)
        BCCoreSprites.get("trigger_limiter_m64"), // 2 shift (25%)
        BCCoreSprites.get("trigger_limiter_m16"), // 3 shift (12.5%)
        BCCoreSprites.get("trigger_limiter_m8"), // 4 shift (6.25%)
        BCCoreSprites.get("trigger_limiter_m2"), // 5 shift (3.125%)
        BCCoreSprites.get("trigger_limiter_m0"), // 6 shift (0%)
    };

    public static final ISprite[] POWER_LIMIT_RF = { //
        BCCoreSprites.get("trigger_rf_limiter_m256"), //
        BCCoreSprites.get("trigger_rf_limiter_m128"), //
        BCCoreSprites.get("trigger_rf_limiter_m64"), //
        BCCoreSprites.get("trigger_rf_limiter_m16"), //
        BCCoreSprites.get("trigger_rf_limiter_m8"), //
        BCCoreSprites.get("trigger_rf_limiter_m2"), //
        BCCoreSprites.get("trigger_rf_limiter_m0"), //
    };

    static {
        for (DyeColor colour : DyeColor.values()) {
            ACTION_PIPE_COLOUR[colour.ordinal()] = BCCoreSprites.get("paintbrush_" + colour.getName());
            String pre = "trigger_pipesignal_" + oldName(colour) + "_";
            PIPE_SIGNAL_OFF.put(colour, BCCoreSprites.get(pre + "inactive"));
            PIPE_SIGNAL_ON.put(colour, BCCoreSprites.get(pre + "active"));
        }
        for (SlotIndex index : SlotIndex.VALUES) {
            ACTION_EXTRACTION_PRESET.put(index, BCCoreSprites.get("extraction_preset_" + index.colour.getName()));
        }
        for (Direction face : Direction.values()) {
            ACTION_PIPE_DIRECTION.put(face, BCCoreSprites.get("trigger_dir_" + face.getName().toLowerCase(Locale.ROOT)));
        }
    }

    private BCTransportSprites() {}

    /** @return 1.12's name of a colour (the trigger textures still use it: "silver" for light grey). */
    private static String oldName(DyeColor colour) {
        return colour == DyeColor.LIGHT_GRAY ? "silver" : colour.getName();
    }

    private static ISprite gui(String name) {
        return new SpriteGui(BuildCraftReborn.id(name));
    }

    public static ISprite getPipeSignal(boolean active, DyeColor colour) {
        return (active ? PIPE_SIGNAL_ON : PIPE_SIGNAL_OFF).get(colour);
    }

    public static ISprite getPipeDirection(Direction face) {
        return ACTION_PIPE_DIRECTION.get(face);
    }
}
