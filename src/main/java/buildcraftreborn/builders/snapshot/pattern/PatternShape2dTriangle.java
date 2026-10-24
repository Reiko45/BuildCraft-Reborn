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

public class PatternShape2dTriangle extends PatternShape2d {
    public PatternShape2dTriangle() {
        super("2d_triangle");
    }

    @Override
    public ISprite getSprite() {
        return BCBuildersSprites.FILLER_2D_TRIANGLE;
    }

    @Override
    protected void genShape(int maxA, int maxB, LineList list) {
        int halfA = maxA / 2;
        list.moveTo(maxA, maxB);
        list.lineTo(0, maxB);
        list.lineTo(halfA, 0);
        list.moveTo(maxA - halfA, 0);
        list.lineFrom(maxA, maxB);
        list.setFillPoint(halfA, maxB / 2);
    }
}
