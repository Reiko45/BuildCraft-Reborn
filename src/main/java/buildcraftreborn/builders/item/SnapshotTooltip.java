/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.item;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

import buildcraftreborn.builders.snapshot.Snapshot;

/** The tooltip image of a used blueprint, template or single schematic: a preview of what it holds (drawn on the
 * client by {@code ClientSnapshotTooltip}). 1.12 drew this below the tooltip in a tooltip event.
 *
 * @param header The header of a blueprint or template (the snapshot is fetched from the server), or null.
 * @param snapshot A snapshot made on the spot (for a single schematic), or null. */
public record SnapshotTooltip(@Nullable Snapshot.Header header, @Nullable Snapshot snapshot)
    implements TooltipComponent {}
