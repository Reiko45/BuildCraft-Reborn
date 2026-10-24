/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;

import buildcraftreborn.builders.item.SnapshotTooltip;
import buildcraftreborn.builders.snapshot.ClientSnapshots;
import buildcraftreborn.builders.snapshot.Snapshot;

/** Draws a {@link SnapshotTooltip}: a 100 by 100 preview at the bottom of the tooltip. */
public class ClientSnapshotTooltip implements ClientTooltipComponent {
    private static final int SIZE = 100;
    private final SnapshotTooltip tooltip;

    public ClientSnapshotTooltip(SnapshotTooltip tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public int getHeight() {
        return SIZE + 4;
    }

    @Override
    public int getWidth(Font font) {
        return SIZE;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        Snapshot snapshot = tooltip.snapshot();
        if (snapshot == null && tooltip.header() != null) {
            snapshot = ClientSnapshots.INSTANCE.getSnapshot(tooltip.header().key);
        }
        if (snapshot != null) {
            graphics.pose().pushPose();
            // Tooltips are drawn at z 400
            graphics.pose().translate(0, 0, 250);
            SnapshotPreview.render(graphics, snapshot, x, y + 2, SIZE, SIZE);
            graphics.pose().popPose();
        }
    }
}
