/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.enums.EnumPowerStage;
import buildcraftreborn.api.tiles.IControllable;
import buildcraftreborn.core.statements.TriggerFluidContainer;
import buildcraftreborn.core.statements.TriggerFluidContainerLevel;
import buildcraftreborn.core.statements.TriggerInventory;
import buildcraftreborn.core.statements.TriggerInventoryLevel;
import buildcraftreborn.lib.client.sprite.SpriteGui;

/** The gate statement icons of the core module.
 * <p>
 * 1.21: 1.12 stitched these into the block atlas; they're only ever drawn in GUIs, so here they live in the GUI sprite
 * atlas ({@code textures/gui/sprites/triggers/}). Creating the holders loads nothing client-only, so statements can
 * return them on either side. */
public final class BCCoreSprites {
    public static final ISprite TRIGGER_TRUE = get("trigger_true");
    public static final ISprite PARAM_GATE_SIDE_ONLY = get("redstone_gate_side_only");

    public static final ISprite TRIGGER_MACHINE_ACTIVE = get("trigger_machine_active");
    public static final ISprite TRIGGER_MACHINE_INACTIVE = get("trigger_machine_inactive");

    public static final ISprite TRIGGER_REDSTONE_ACTIVE = get("trigger_redstoneinput_active");
    public static final ISprite TRIGGER_REDSTONE_INACTIVE = get("trigger_redstoneinput_inactive");
    public static final ISprite ACTION_REDSTONE = get("action_redstoneoutput");

    public static final ISprite TRIGGER_POWER_HIGH = get("trigger_energy_storage_high");
    public static final ISprite TRIGGER_POWER_LOW = get("trigger_energy_storage_low");

    public static final ISprite[] PARAM_REDSTONE_LEVEL = new ISprite[16];
    public static final ISprite[] TRIGGER_DIRECTION = new ISprite[6];

    public static final Map<IControllable.Mode, ISprite> ACTION_MACHINE_CONTROL =
        new EnumMap<>(IControllable.Mode.class);
    public static final Map<TriggerInventory.State, ISprite> TRIGGER_INVENTORY =
        new EnumMap<>(TriggerInventory.State.class);
    public static final Map<TriggerInventoryLevel.TriggerType, ISprite> TRIGGER_INVENTORY_LEVEL =
        new EnumMap<>(TriggerInventoryLevel.TriggerType.class);
    public static final Map<TriggerFluidContainer.State, ISprite> TRIGGER_FLUID =
        new EnumMap<>(TriggerFluidContainer.State.class);
    public static final Map<TriggerFluidContainerLevel.TriggerType, ISprite> TRIGGER_FLUID_LEVEL =
        new EnumMap<>(TriggerFluidContainerLevel.TriggerType.class);
    public static final Map<EnumPowerStage, ISprite> TRIGGER_POWER_STAGE = new EnumMap<>(EnumPowerStage.class);

    static {
        for (int i = 0; i < PARAM_REDSTONE_LEVEL.length; i++) {
            PARAM_REDSTONE_LEVEL[i] = get("parameter_redstone_" + i);
        }
        String[] dirs = { "down", "up", "north", "south", "west", "east" };
        for (int i = 0; i < 6; i++) {
            TRIGGER_DIRECTION[i] = get("trigger_dir_" + dirs[i]);
        }
        for (IControllable.Mode mode : IControllable.Mode.values()) {
            ACTION_MACHINE_CONTROL.put(mode, get("action_machinecontrol_" + mode.name().toLowerCase(Locale.ROOT)));
        }
        for (TriggerInventory.State state : TriggerInventory.State.VALUES) {
            TRIGGER_INVENTORY.put(state, get("trigger_inventory_" + state.name().toLowerCase(Locale.ROOT)));
        }
        for (TriggerInventoryLevel.TriggerType type : TriggerInventoryLevel.TriggerType.VALUES) {
            TRIGGER_INVENTORY_LEVEL.put(type, get("trigger_inventory_" + type.name().toLowerCase(Locale.ROOT)));
        }
        for (TriggerFluidContainer.State state : TriggerFluidContainer.State.VALUES) {
            TRIGGER_FLUID.put(state, get("trigger_liquidcontainer_" + state.name().toLowerCase(Locale.ROOT)));
        }
        for (TriggerFluidContainerLevel.TriggerType type : TriggerFluidContainerLevel.TriggerType.VALUES) {
            TRIGGER_FLUID_LEVEL.put(type, get("trigger_liquidcontainer_" + type.name().toLowerCase(Locale.ROOT)));
        }
        for (EnumPowerStage stage : EnumPowerStage.values()) {
            if (stage != EnumPowerStage.BLACK) {
                TRIGGER_POWER_STAGE.put(stage, get("trigger_engineheat_" + stage.getModelName()));
            }
        }
    }

    private BCCoreSprites() {}

    /** @return A statement icon from {@code textures/gui/sprites/triggers/<name>.png}. */
    public static ISprite get(String name) {
        return new SpriteGui(BuildCraftReborn.id("triggers/" + name));
    }
}
