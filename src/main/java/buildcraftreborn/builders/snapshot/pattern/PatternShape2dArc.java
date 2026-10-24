/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot.pattern;

import buildcraftreborn.api.core.render.ISprite;

import buildcraftreborn.builders.BCBuildersSprites;

public class PatternShape2dArc extends PatternShape2d {
    public PatternShape2dArc() {
        super("2d_arc");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_2D_ARC;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        if (maxA == 0 || maxB == 0) {
            list.moveTo(0, 0);
            list.lineTo(maxA, maxB);
            return;
        }
        list.setFillPoint(maxA, maxB);
        list.arc(maxA, maxB, maxA, maxB);
    }
}
