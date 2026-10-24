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

public class PatternShape2dCircle extends PatternShape2d {
    public PatternShape2dCircle() {
        super("2d_circle");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_2D_CIRCLE;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        if (maxA == 0 || maxB == 0) {
            list.moveTo(0, 0);
            list.lineTo(maxA, maxB);
            return;
        }
        int halfA = maxA / 2;
        int halfB = maxB / 2;
        int halfAUpper = maxA - halfA;
        int halfBUpper = maxB - halfB;
        list.setFillPoint(halfA, halfB);
        list.arc(halfA, halfB, maxA / 2.0, maxB / 2.0, halfAUpper - halfA, halfBUpper - halfB, ArcType.FULL_CIRCLE);
    }
}
